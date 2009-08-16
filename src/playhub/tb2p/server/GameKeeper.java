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

    private final Logger logger = Logger.getLogger(LoginRequestPDU.class.getCanonicalName());

    private final ConcurrentMap<NIOSocket,GameSession> mapSockGame = new ConcurrentHashMap<NIOSocket,GameSession>();
    private final ConcurrentMap<String,GameSession> mapIdSession = new ConcurrentHashMap<String,GameSession>();
    private final Set<GameSession> gamesWaitingPlayers = new HashSet<GameSession>();
    private final Set<GameSession> gamesActive = new HashSet<GameSession>();
    private final Set<String> usernamesPlaying = new HashSet<String>();
    private final ConcurrentMap<Player,NIOSocket> mapPlayerSocket = new ConcurrentHashMap<Player,NIOSocket>();

    private ServerSettings settings;
    public ServerSettings getServerSettings() { return this.settings; }

    private long pdu_counter = 0;


    

    public GameKeeper(ServerSettings settings) {
        this.settings = settings;
    }

    public void registerSocket(NIOSocket nios) {
    }

    public void unregisterSocket(NIOSocket nios) {
        // TODO: notify clients of game error+end if in play
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
            return;
        }
        // interpret pdu based on game state
        if (game.inPlay()) {
            // try if this is a PlayEndedNotification
            try {
                PlayEndedNotificationPDU playEndedPDU = new PlayEndedNotificationPDU(pdu);
                if (mapPlayerSocket.get(game.getPlayer1()).equals(nios)) {
                    // player1 ended
                    this.switchPlayerTurns(game);
                }
                else if (mapPlayerSocket.get(game.getPlayer2()).equals(nios)) {
                    // player2 ended

                }
                else {
                    throw new GameStateViolation();
                }
            } catch (MalformedPDUException malx) {
                // TODO: relay pdu
            } catch (GameStateViolation gsv) {
                logger.info("game state violation during in-play pdu parsing: "+gsv.toString());
            }
        }
        else {
            logger.info("game state inconsistent gameId="+game.getGameId());
            // TODO: penalize the offending client
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
            // login player 2 (+ cancel any pending timeouts)
            game.loginPlayer2(player);
            gamesWaitingPlayers.remove(game);
            gamesActive.add(game);
            // login success
            this.writePDU(nios, new LoginResponsePDU(pdu.getId()));
            logger.fine("player="+lp.getPlayerName()+" assigned to existing gameId="+game.getGameId());
            // player-2 should wait for his/her turn
            this.writePDU(nios, new WaitTurnNotificationPDU(this.getNextPduCounter()));
            // and we'll let the player-1 start playing (w/ duration)
            game.startPlayPlayer1();
            NIOSocket nios1 = mapPlayerSocket.get(game.getPlayer1());
            this.writePDU(nios1, new StartPlayNotificationPDU(this.getNextPduCounter(), settings.getPlayTurnDurationSeconds()));

            // we need to timeout the turn (turn_duration+client_wait),
            // after which, the client who does not end his turn within
            // the timeout period will be penalized as the loser
            ScheduledFuture<?> futurePenalize = scheduler.schedule(
                      GameKeeper.newTaskPenalizePlayer1(this, game),
                      settings.getPlayTurnDurationSeconds() + settings.getClientWaitTimeoutSeconds(),
                      TimeUnit.SECONDS
            );
            game.setTaskPenalizePlayer1(futurePenalize);
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
            // consider timing out, so as not to keep the client waiting for opponent
            ScheduledFuture<?> f = scheduler.schedule(
                    GameKeeper.newTaskCancelGame(this, game),
                    settings.getClientWaitTimeoutSeconds(),
                    TimeUnit.SECONDS
            );
            game.setTaskCancel(f);
        }
        
        return game;
    }


    public void switchPlayerTurns(GameSession gs) {
        try {
            logger.fine("Switching player turns for gameId="+gs.getGameId());
            // end player-1's turn
            gs.endPlayPlayer1();
            Player p1 = gs.getPlayer1();
            NIOSocket nios1 = mapPlayerSocket.get(p1);
            this.writePDU( nios1, new WaitTurnNotificationPDU( this.getNextPduCounter()) );

            // and start player-2's turn
            gs.startPlayPlayer2();
            Player p2 = gs.getPlayer2();
            NIOSocket nios2 = mapPlayerSocket.get(p2);
            this.writePDU(
                nios2,
                new StartPlayNotificationPDU(
                    this.getNextPduCounter(),
                    this.settings.getPlayTurnDurationSeconds()
                )
            );

            // notify
            ScheduledFuture<?> futurePenalize = scheduler.schedule(
                      GameKeeper.newTaskPenalizePlayer2(this, gs),
                      settings.getPlayTurnDurationSeconds() + settings.getClientWaitTimeoutSeconds(),
                      TimeUnit.SECONDS
            );
            gs.setTaskPenalizePlayer2(futurePenalize);
        }
        catch (GameStateViolation gsv) {
            // TODO: offensive client will be penalize, notify game winner then...
            logger.warning("GameStateViolation detected at gameId="+gs.getGameId());
        }
    }

    public void cancelGame(GameSession gs) {
        try {
            logger.fine("Cancelling gameId="+gs.getGameId());
            gs.cancelGame();
            // try to inform player(s) that was cancelled/aborted
            Player p1 = gs.getPlayer1();
            if (p1 != null) {
                NIOSocket nios = mapPlayerSocket.get(p1);
                this.writePDU(nios, new GameCancelledNotificationPDU(this.getNextPduCounter()));
                // TODO: notify if winner/loser
                nios.closeAfterWrite();
            }
            Player p2 = gs.getPlayer2();
            if (p2 != null) {
                NIOSocket nios = mapPlayerSocket.get(p2);
                this.writePDU(nios, new GameCancelledNotificationPDU(this.getNextPduCounter()));
                // TODO: notify if winner/loser
                nios.closeAfterWrite();
            }
        }
        catch (GameStateViolation gsv) {
            logger.warning("GameStateViolation detected at gameId="+gs.getGameId());
        }
    }


    public void penalizePlayer1(GameSession gs) {
        try {
            logger.fine("Penalizing player1="+gs.getPlayer1().getName()+" gameId="+gs.getGameId());
            gs.penalizePlayer1();
            Player p1 = gs.getPlayer1();
            NIOSocket nios1 = mapPlayerSocket.get(p1);
            if (nios1 != null) {
                this.writePDU(nios1, new GameDoneNotificationPDU(this.getNextPduCounter(), false));
                nios1.closeAfterWrite();
            }
            Player p2 = gs.getPlayer2();
            NIOSocket nios2 = mapPlayerSocket.get(p2);
            if (nios2 != null) {
                this.writePDU(nios2, new GameDoneNotificationPDU(this.getNextPduCounter(), true));
                nios2.closeAfterWrite();
            }
        }
        catch (GameStateViolation gsv) {
            logger.warning("GameStateViolation detected at gameId="+gs.getGameId());
        }
    }


    public void penalizePlayer2(GameSession gs) {
        try {
            logger.fine("Penalizing player2="+gs.getPlayer2().getName()+" gameId="+gs.getGameId());
            gs.penalizePlayer2();
            Player p1 = gs.getPlayer1();
            NIOSocket nios1 = mapPlayerSocket.get(p1);
            if (nios1 != null) {
                this.writePDU(nios1, new GameDoneNotificationPDU(this.getNextPduCounter(), true));
                nios1.closeAfterWrite();
            }
            Player p2 = gs.getPlayer2();
            NIOSocket nios2 = mapPlayerSocket.get(p2);
            if (nios2 != null) {
                this.writePDU(nios2, new GameDoneNotificationPDU(this.getNextPduCounter(), false));
                nios2.closeAfterWrite();
            }
        }
        catch (GameStateViolation gsv) {
            logger.warning("GameStateViolation detected at gameId="+gs.getGameId());
        }
    }



    public void writePDU(NIOSocket nios, PDU pdu) {
        nios.write(pdu.toJSONString().getBytes());
    }

    public long getNextPduCounter() { return this.pdu_counter++; }


    protected static Runnable newTaskSwitchPlayerTurns(final GameKeeper gk, final GameSession g) {
        return new Runnable() {
            @Override
            public void run() {
                gk.switchPlayerTurns(g);
            }
        };
    }


    protected static Runnable newTaskCancelGame(final GameKeeper gk, final GameSession g) {
        return new Runnable() {
            @Override
            public void run() {
                gk.cancelGame(g);
            }
        };
    }


    protected static Runnable newTaskPenalizePlayer1(final GameKeeper gk, final GameSession g) {
        return new Runnable() {
            @Override
            public void run() {
                gk.penalizePlayer1(g);
            }
        };
    }


    protected static Runnable newTaskPenalizePlayer2(final GameKeeper gk, final GameSession g) {
        return new Runnable() {
            @Override
            public void run() {
                gk.penalizePlayer2(g);
            }
        };
    }

}
