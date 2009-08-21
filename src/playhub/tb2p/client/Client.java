/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.client;

import java.util.logging.*;
import java.io.*;
import naga.*;

import playhub.tb2p.protocol.*;
import playhub.tb2p.exceptions.*;

/**
 *
 * @author dexter
 */
public class Client extends SocketObserverAdapter {

    // Login -> (wait for LoginResponse)
    // WaitOpponent
    // WaitTurn
    // StartPlay
    // notifyScoreUpdate
    // EndPlay

    private static final Logger logger = Logger.getLogger(Client.class.getCanonicalName());
    private ClientHandler clientHandler;
    private ClientConfig config;
    private NIOService nioService;
    private NIOSocket nioSocket;

    private long pduCounter = 0;
    private long getNextPduCounter() { return pduCounter++; }

    private boolean connected                   = false;
    private boolean loggedIn                    = false;
    private boolean opponentBeenWaited          = false;
    private boolean gameCancelled               = false;
    private boolean gameDone                    = false;
    private boolean opponentAvailable           = false;
    private boolean playStarted                 = false;
    private boolean playEnded                   = false;
    private boolean opponentPlayStarted         = false;
    private boolean opponentPlayEnded           = false;

    public Client(final ClientConfig config, ClientHandler handler) {
        this.config = config;
        this.clientHandler = handler;
    }

    public void doIO() throws IOException {
        this.nioService.selectNonBlocking();
    }

    public void connect() throws IOException {
        this.connect(new NIOService());
    }

    public void connect(NIOService nioservice) throws IOException {
        this.nioService = nioservice;
        NIOSocket nios = this.nioService.openSocket(this.config.getHost(), this.config.getPort());
        nios.listen(this);
    }

    public void endTurn() {
        this.playEnded = true;
        this.writePDU(new PlayEndedNotificationPDU(this.getNextPduCounter()));
    }

    public void updateScore(long score) {
        this.writePDU(new ScoreUpdateNotificationPDU(this.getNextPduCounter(), score));
    }

  



    private void interpretPDU(PDU pdu) {
        // The game flow can be two variants:
        //   1. wait-op, start-play, wait-op-turn, game-ended
        //   2. wait-op-turn, start-play, game-ended
        while (true) {
            System.err.println("< " + pdu.toJSONString());
            boolean handled = false;
            boolean gameInProgress = this.connected && this.loggedIn && (!this.gameCancelled || !this.gameDone);
            if (this.connected && !this.loggedIn) {
                handled = this.expectLoginResponse(pdu);
                break;
            }
            if (gameInProgress) {
                if (!this.opponentBeenWaited) {
                    // we're expecting either a wait-op or a wait-op-turn
                    handled = this.expectWaitForOpponent(pdu);
                    break;
                }
                if (this.opponentBeenWaited
                        && !this.opponentAvailable
                        )
                {
                    // we're player-1 and we are expecting either a start-play or game-cancelled
                    handled = this.expectWaitStartPlayAsPlayer1(pdu);
                    break;
                }
                if (this.opponentBeenWaited
                        && this.opponentPlayStarted
                        && !this.opponentPlayEnded
                        && !this.playStarted
                        )
                {
                    handled = this.expectWaitStartPlayAsPlayer2(pdu);
                }
                if (this.opponentAvailable
                        && this.playStarted
                        && this.playEnded
                        && !this.opponentPlayStarted
                        )
                {
                    // player-1 expects wait-op-turn
                    handled = this.expectPlayer1FinishedWaitOpponentTurn(pdu);
                    break;
                }

                // try if game-done
                if (this.expectGameDone(pdu)) { break; }
                // try if score-update
                if (this.expectScoreUpdate(pdu)) { break; }
                // --- unhandled packet
                if (!handled) {
                    this.clientHandler.opponentGameEvent(pdu);
                }

            } // game in progress
            break;
        }
    }


    private boolean expectScoreUpdate(PDU pdu) {
        try {
            ScoreUpdateNotificationPDU scoreupdate = new ScoreUpdateNotificationPDU(pdu);
            this.clientHandler.opponentScoreUpdated(scoreupdate.getScore());
            return true;
        }
        catch (MalformedPDUException mal) {
            logger.finer("check if score-update: false");
        }
        return false;
    }


    private boolean expectGameDone(PDU pdu) {
        try {
            GameDoneNotificationPDU gamedone = new GameDoneNotificationPDU(pdu);
            if (this.opponentPlayStarted && !this.opponentPlayEnded) {
                // opponent was playing when the game ended
                // so we declare that the opponent ended his play
                this.clientHandler.opponentPlayEnded();
            }
            this.clientHandler.gameDone(gamedone.wasWon());
            return true;
        }
        catch (MalformedPDUException mal) {
            logger.finer("check if game-done: false");
        }
        return false;
    }
    

    private boolean expectPlayer1FinishedWaitOpponentTurn(PDU pdu) {
        try {
            WaitTurnNotificationPDU waitturn = new WaitTurnNotificationPDU(pdu);
            this.opponentPlayStarted = true;
            this.clientHandler.opponentPlayStarted();
            return true;
        }
        catch (MalformedPDUException malwaitturn) {
            logger.warning("Unable to find opponent");
        }
        return false;
    }


    private boolean expectWaitStartPlayAsPlayer2(PDU pdu) {
        try {
            StartPlayNotificationPDU play = new StartPlayNotificationPDU(pdu);
            this.playStarted = true;
            this.opponentPlayEnded = true;
            this.clientHandler.opponentPlayEnded();
            this.clientHandler.playerPlayStarted(play.getDurationSeconds());
            return true;
        }
        catch (MalformedPDUException malx) {
            logger.finer("expect wait start-play but not found");
        }
        return false;
    }


    private boolean expectWaitStartPlayAsPlayer1(PDU pdu) {
        try {
            StartPlayNotificationPDU play = new StartPlayNotificationPDU(pdu);
            this.opponentAvailable = true;
            this.playStarted = true;
            this.clientHandler.playerPlayStarted(play.getDurationSeconds());
            return true;
        }
        catch (MalformedPDUException malx) {
            try {
                GameCancelledNotificationPDU cancelled = new GameCancelledNotificationPDU(pdu);
                this.gameCancelled = true;
                this.clientHandler.gameCancelled();
            }
            catch (MalformedPDUException malxcancelled) {
                logger.finer("expect wait start-play but not found");
            }
        }
        return false;
    }


    private boolean expectWaitForOpponent(PDU pdu) {
        try {
            WaitOpponentNotificationPDU waitop = new WaitOpponentNotificationPDU(pdu);
            // then we are player-1
            this.opponentBeenWaited = true;
            this.opponentAvailable = false;
            this.clientHandler.opponentNotYetAvailable();
            return true;
        }
        catch (MalformedPDUException malx) {
            try {
                WaitTurnNotificationPDU waitturn = new WaitTurnNotificationPDU(pdu);
                // then we are player-2
                this.opponentBeenWaited = true;
                this.opponentAvailable = true;
                this.opponentPlayStarted = true;
                this.clientHandler.opponentPlayStarted();
                return true;
            }
            catch (MalformedPDUException malwaitturn) {
                logger.warning("Unable to find opponent");
            }
        }
        return false;
    }


    private boolean expectLoginResponse(PDU pdu) {
        try {
            LoginResponsePDU login = new LoginResponsePDU(pdu);
            this.loggedIn = true;
            this.clientHandler.clientLoggedIn();
            return true;
        }
        catch (Exception e) {
            logger.warning("Invalid Login Response: "+e.toString());
            this.nioSocket.close();
        }
        return false;
    }




    protected void writePDU(PDU pdu) {
        this.nioSocket.write(pdu.toJSONString().getBytes());
        System.err.println("> " + pdu.toJSONString());
    }

    // -------------------

    @Override
    public void connectionOpened(NIOSocket nios) {
        this.pduCounter = 0;
        this.connected = true;
        this.nioSocket = nios;
        this.clientHandler.clientConnected();
        Player player = new Player(this.config.getUsername(), this.config.getBetAmount());
        this.writePDU(new LoginRequestPDU(this.getNextPduCounter(), this.config.getGameId(), player));
    }

    @Override
    public void connectionBroken(NIOSocket arg0, Exception arg1) {
        this.connected = false;
    }

    @Override
    public void packetReceived(NIOSocket arg0, byte[] arg1) {
        try {
            PDU pdu = PDU.parsedFromPacket(arg1);
            this.interpretPDU(pdu);
        }
        catch (MalformedPDUException malx) {
            // TODO: disconnect?
        }
    }



}
