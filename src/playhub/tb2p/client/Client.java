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

    private boolean connected                   = false;
    private boolean loggedIn                    = false;
    private boolean opponentBeenWaited          = false;
    private boolean gameCancelled               = false;
    private boolean opponentAvailable           = false;
    private boolean playStarted                 = false;
    private boolean playEnded                   = false;
    private boolean opponentPlayStarted         = false;
    private boolean opponentPlayEnded           = false;

    public Client(final ClientConfig config, ClientHandler handler) {
        this.config = config;
        this.clientHandler = handler;
    }
    
    public void connect(NIOService nioservice) throws IOException {
        this.nioService = nioservice;
        NIOSocket nios = this.nioService.openSocket(this.config.getHost(), this.config.getPort());
        nios.listen(this);
    }

    public void endTurn() {
        this.playEnded = true;
    }

    public void updateScore(long score) {
        
    }


    public boolean isConnected() { return this.connected; }
    public boolean isLoggedIn() { return this.loggedIn; }
    public boolean isOpponentAvailable() { return this.opponentAvailable; }
    public boolean isPlayStarted() { return this.playStarted; }
    public boolean isPlayEnded() { return this.playEnded; }
    public boolean isOpponentPlayEnded() { return opponentPlayEnded; }
    public boolean isOpponentPlayStarted() { return opponentPlayStarted; }
    public boolean isGameCancelled() { return gameCancelled; }
    



    private void interpretPDU(PDU pdu) {
        // The game flow can be two variants:
        //   1. wait-op, start-play, wait-op-turn, game-ended
        //   2. wait-op-turn, start-play, game-ended
        while (true) {
            System.err.println(pdu.toJSONString());
            boolean handled = false;
            if (this.isConnected() && !this.isLoggedIn()) {
                handled = this.expectLoginResponse(pdu);
                break;
            }
            if (this.isConnected() && this.isLoggedIn() && !this.opponentBeenWaited) {
                // we're expecting either a wait-op or a wait-op-turn
                handled = this.expectWaitForOpponent(pdu);
                break;
            }
            if (this.isConnected() && this.isLoggedIn() && this.opponentBeenWaited && !this.isOpponentAvailable()) {
                // we're player-1 and we are expecting either a start-play or game-cancelled
                handled = this.expectWaitStartPlayAsPlayer1(pdu);
                break;
            }
            if (this.isConnected() && this.isLoggedIn() && this.isOpponentAvailable()
                    && this.isPlayStarted() && this.isPlayEnded()
                    && !this.isOpponentPlayStarted()) {
                // player-1 expects wait-op-turn
                handled = this.expectPlayer1FinishedWaitOpponentTurn(pdu);
                break;
            }
            // --- unhandled packet
//            if (!handled) {
//                this.clientHandler.opponentGameEvent(pdu);
//            }
            break;
        }
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
                GameCancelledNotificationPDU gcn = new GameCancelledNotificationPDU(pdu);
                this.gameCancelled = true;
                this.clientHandler.gameCancelled();
                return true;
            }
            catch (Exception x) {
                logger.warning("expecting a start-play as player-1 but got malformed pdu instead");
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
    }

    // -------------------

    @Override
    public void connectionOpened(NIOSocket nios) {
        this.connected = true;
        this.nioSocket = nios;
        Player player = new Player(this.config.getUsername(), this.config.getBetAmount());
        this.writePDU(new LoginRequestPDU(this.config.getGameId(), player));
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
