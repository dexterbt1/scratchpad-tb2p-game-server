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
public class GameKeeper implements Runnable {

    static class GameKeeperJob {
        private PDU pdu;
        private NIOSocket nios;
        public GameKeeperJob(NIOSocket nios, PDU pdu) {
            this.nios = nios;
            this.pdu = pdu;
        }
        public PDU getPDU() { return this.pdu; }
        public NIOSocket getNIOSocket() { return this.nios; }
    }

    private Logger logger = Logger.getLogger(LoginRequestPDU.class.getCanonicalName());

    private BlockingQueue<GameKeeperJob> jobQueue = new LinkedBlockingQueue<GameKeeperJob>();
    private ConcurrentMap<NIOSocket,GameSession> mapSockGame = new ConcurrentHashMap<NIOSocket,GameSession>();
    private ConcurrentMap<String,GameSession> mapIdSession = new ConcurrentHashMap<String,GameSession>();
    private Set<GameSession> gamesWaitingPlayers = new HashSet<GameSession>();
    private Set<GameSession> gamesActive = new HashSet<GameSession>();
    private Set<String> playersPlaying = new HashSet<String>();

    public GameKeeper() {
        // spawn interpreter
        new Thread(this).start();
    }

    public void registerSocket(NIOSocket nios) {

    }

    public void unregisterSocket(NIOSocket nios) {
        if (mapSockGame.containsKey(nios)) {
            GameSession game = mapSockGame.get(nios);
            mapIdSession.remove(game.getGameId());
            if (gamesWaitingPlayers.contains(game)) { gamesWaitingPlayers.remove(game); }
            if (gamesActive.contains(game)) { gamesActive.remove(game); }
            if (game.getPlayer1().getUName() != null) { playersPlaying.remove(game.getPlayer1().getUName()); }
            if (game.getPlayer2().getUName() != null) { playersPlaying.remove(game.getPlayer2().getUName()); }
            // TODO: notify game error+end if in play
            mapSockGame.remove(nios);
            logger.fine("game unregistered gameId="+game.getGameId());
        }
    }

    public void submit(NIOSocket nios, PDU pdu) {
        jobQueue.offer(new GameKeeperJob(nios,pdu));
    }


    // -----------------
    public void run() {
        logger.info("GameKeeper worker interpreter thread started...");
        while (true) {
            GameKeeperJob job;
            try {
                job = this.jobQueue.take();
            } catch (InterruptedException ie) {
                continue;
            }
            NIOSocket nios = job.getNIOSocket();
            PDU pdu = job.getPDU();
            GameSession game = mapSockGame.get(nios);
            if (game == null) {
                // requires login first, expect that this is the first pdu
                try {
                    game = this.login(pdu);
                    mapSockGame.putIfAbsent(nios, game);
                }
                catch (InvalidLoginException ile) {
                    logger.info(ile.toString());
                    nios.close();
                }
                catch (GameStateViolation gsv) {
                    // TODO: dump game state for debugging later
                    logger.info(gsv.toString());
                    nios.close();
                }
                continue;
            }
        }
    }


    public GameSession login(PDU pdu) throws InvalidLoginException, GameStateViolation {
        GameSession game;
        LoginRequestPDU lp = new LoginRequestPDU(pdu);
        Player player = new Player(lp.getPlayerName(), lp.getBetAmount());
        // player must not be playing already
        if (playersPlaying.contains(player.getUName())) {
            throw new InvalidLoginException("duplicate player "+player.getUName());
        }
        playersPlaying.add(player.getUName());
        if (mapIdSession.containsKey(lp.getGameId())) {
            // existing game, assign player as player 2
            game = mapIdSession.get(lp.getGameId());
            game.loginPlayer2(player);
            gamesWaitingPlayers.remove(game);
            gamesActive.add(game);
            logger.fine("player assigned to existing gameId="+game.getGameId());
        }
        else {
            // new game
            game = new GameSession(lp.getGameId());
            game.loginPlayer1(player);
            mapIdSession.put(lp.getGameId(), game);
            gamesWaitingPlayers.add(game);
            logger.fine("player created new gameId="+game.getGameId());
        }        
        return game;
    }

}
