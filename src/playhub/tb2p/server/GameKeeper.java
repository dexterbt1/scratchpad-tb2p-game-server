/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import java.util.logging.*;
import java.util.concurrent.*;
import java.util.*;
import naga.*;

import playhub.tb2p.exceptions.*;
import playhub.tb2p.protocol.*;

/**
 *
 * @author dexter
 */
public class GameKeeper {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public final Logger logger = Logger.getLogger(LoginRequestPDU.class.getCanonicalName());

    private final ConcurrentMap<NIOSocket,GameSession> mapSockGame = new ConcurrentHashMap<NIOSocket,GameSession>();
    private final ConcurrentMap<String,GameSession> mapIdSession = new ConcurrentHashMap<String,GameSession>();
    private final Set<GameSession> gamesWaitingPlayers = new HashSet<GameSession>();
    private final Set<GameSession> gamesActive = new HashSet<GameSession>();
    private final Set<String> usernamesPlaying = new HashSet<String>();
    private final Map<Player,NIOSocket> mapPlayerSocket = new ConcurrentHashMap<Player,NIOSocket>();

    private ServerSettings settings;
    public ServerSettings getServerSettings() { return this.settings; }

    private long pdu_counter = 0;


    

    public GameKeeper(ServerSettings settings) {
        this.settings = settings;
    }

    public void registerSocket(NIOSocket nios) {
    }

    public void unregisterSocket(NIOSocket nios) {
        if (mapSockGame.containsKey(nios)) {
            GameSession game = mapSockGame.get(nios);
            mapIdSession.remove(game.getGameId());
            if (gamesWaitingPlayers.contains(game)) { gamesWaitingPlayers.remove(game); }
            if (gamesActive.contains(game)) { gamesActive.remove(game); }
            if (game.getPlayer1() != null) {
                String username = game.getPlayer1().getUName();
                usernamesPlaying.remove(username);
                mapPlayerSocket.remove(game.getPlayer1());
            }
            if (game.getPlayer2() != null) {
                String username = game.getPlayer2().getUName();
                usernamesPlaying.remove(username);
                mapPlayerSocket.remove(game.getPlayer2());
            }
            // TODO: notify game error+end if in play
            mapSockGame.remove(nios);
            logger.fine("game unregistered gameId="+game.getGameId());
        }
    }

    public void receivePDU(NIOSocket nios, PDU pdu) {
        GameSession game = mapSockGame.get(nios);
        if (game == null) {
            // requires login first, expect that this is the first pdu
            try {
                game = this.login(nios, pdu);
                mapSockGame.putIfAbsent(nios, game);
            }
            catch (InvalidLoginException ile) {
                logger.info("invalid login exception "+ile.toString());
                nios.close();
            }
            catch (GameStateViolation gsv) {
                // TODO: dump game state for debugging later
                logger.info("game state violation "+gsv.toString());
                nios.close();
            }
        }
    }


    protected GameSession login(NIOSocket nios, PDU pdu) throws InvalidLoginException, GameStateViolation {
        GameSession game;
        LoginRequestPDU lp = new LoginRequestPDU(pdu);
        Player player = new Player(lp.getPlayerName(), lp.getBetAmount());
        // player must not be playing already
        if (usernamesPlaying.contains(player.getUName())) {
            throw new InvalidLoginException("duplicate player "+player.getUName());
        }
        mapPlayerSocket.put(player, nios);
        usernamesPlaying.add(player.getUName());
        if (mapIdSession.containsKey(lp.getGameId())) {
            // existing game, assign player as player 2
            game = mapIdSession.get(lp.getGameId());
            game.loginPlayer2(player);
            gamesWaitingPlayers.remove(game);
            gamesActive.add(game);
            // login success
            this.writePDU(nios, new LoginResponsePDU(pdu.getId()));
            logger.fine("player="+lp.getPlayerName()+" assigned to existing gameId="+game.getGameId());
            // player-2 whould wait for his/her turn
            this.writePDU(nios, new WaitTurnNotificationPDU(this.getNextPduCounter()));
            // and we'll let the player-1 start playing (w/ duration)
            game.startPlayPlayer1();
            NIOSocket nios1 = mapPlayerSocket.get(game.getPlayer1());
            this.writePDU(nios1, new StartPlayNotificationPDU(this.getNextPduCounter(), settings.getPlayTurnDurationSeconds()));
            // expire game after turn duration
            ScheduledFuture<?> sf = scheduler.schedule(
                    GameKeeper.newTaskSwitchPlayerTurns(
                        this,
                        game,
                        mapPlayerSocket
                    ),
                    settings.getPlayTurnDurationSeconds(),
                    TimeUnit.SECONDS
            );
        }
        else {
            // new game
            game = new GameSession(lp.getGameId());
            game.loginPlayer1(player);
            mapIdSession.put(lp.getGameId(), game);
            gamesWaitingPlayers.add(game);
            // login success
            this.writePDU(nios, new LoginResponsePDU(pdu.getId()));
            // no opponent, so wait first
            this.writePDU(nios, new WaitOpponentNotificationPDU(this.getNextPduCounter()));
            logger.fine("player="+lp.getPlayerName()+" created new gameId="+game.getGameId());
        }
        
        return game;
    }



    public void writePDU(NIOSocket nios, PDU pdu) {
        nios.write(pdu.toJSONString().getBytes());
    }

    public long getNextPduCounter() { return this.pdu_counter++; }


//    public static class TaskSwitchPlayerTurns implements Runnable {
//
//        public TaskSwitchPlayerTurns()
//
//        @Override
//        public void run() {
//
//        }
//
//    }



    protected static Runnable newTaskSwitchPlayerTurns(final GameKeeper gk, final GameSession g, final Map<Player, NIOSocket> mps ) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    gk.logger.fine("Switching player turns for gameId="+g.getGameId());
                    // end player-1's turn
                    g.endPlayPlayer1();
                    Player p1 = g.getPlayer1();
                    NIOSocket nios1 = mps.get(p1);
                    gk.writePDU( nios1, new WaitTurnNotificationPDU(gk.getNextPduCounter()) );

                    // and start player-2's turn
                    g.startPlayPlayer2();
                    Player p2 = g.getPlayer2();
                    NIOSocket nios2 = mps.get(p2);
                    gk.writePDU(
                        nios2,
                        new StartPlayNotificationPDU(
                            gk.getNextPduCounter(),
                            gk.getServerSettings().getPlayTurnDurationSeconds()
                        )
                    );
                }
                catch (GameStateViolation gsv) {
                    gk.logger.warning("GameStateViolation detected at gameId="+g.getGameId());
                }
            }
        };
    }





}
