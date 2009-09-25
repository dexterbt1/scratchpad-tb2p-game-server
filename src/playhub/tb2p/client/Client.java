/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.client;

import java.util.logging.*;
import java.io.*;
import java.lang.reflect.*;
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
    private static ClientCipher clientcipher;
        static {
            try {
                clientcipher = new ClientCipher();
            }
            catch (Exception e) {
                e.printStackTrace();
                System.exit(255);
            }
        }
    private ClientHandler clientHandler;
    private ClientConfig config;
    private NIOService nioService;
    private NIOSocket nioSocket;

    private long pduCounter = 0;
    private long getNextPduCounter() { return pduCounter++; }

    private boolean connected                   = false;
    private boolean loggedIn                    = false;
    private boolean gameCancelled               = false;
    private boolean gameDone                    = false;
    private boolean playerPlaying               = false;
    private boolean opponentPlaying             = false;


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
        nios.setPacketReader(new naga.packetreader.RegularPacketReader(2, true));
        nios.setPacketWriter(new naga.packetwriter.RegularPacketWriter(2, true));
    }

    public void endTurn() {
        this.playerPlaying = false;
        this.writePDU(new PlayEndedNotificationPDU(this.getNextPduCounter()));
    }

    public void updateScore(long score) {
        this.writePDU(new ScoreUpdateNotificationPDU(this.getNextPduCounter(), score));
    }



    private void interpretPDU(PDU pdu) {
        // The game flow can be two variants:
        //   1. wait-op, op-avail, start-play, wait-op-turn, game-ended
        //   2. op-avail, wait-op-turn, start-play, game-ended
        while (true) {
            //System.err.println("< " + pdu.toJSONString());
            boolean handled = false;
            boolean gameInProgress = this.connected && this.loggedIn && (!this.gameCancelled || !this.gameDone);
            if (this.connected && !this.loggedIn) {
                if (this.expectLoginResponse(pdu)) {
                    break;
                }
            }
            while (gameInProgress) {
                // try wait opponent
                try {
                    WaitOpponentNotificationPDU p = new WaitOpponentNotificationPDU(pdu);
                    this.clientHandler.opponentNotYetAvailable();
                    break;
                }
                catch (Exception e) { }
                
                // try game cancelled
                try {
                    GameCancelledNotificationPDU p = new GameCancelledNotificationPDU(pdu);
                    this.gameCancelled = true;
                    this.clientHandler.gameCancelled();
                    break;
                }
                catch (Exception e) {; }

                // try opponent available
                try {
                    OpponentAvailableNotificationPDU p = new OpponentAvailableNotificationPDU(pdu);
                    this.clientHandler.opponentAvailable(p.getOpponentName());
                    break;
                }
                catch (Exception e) { }

                // try start play
                try {
                    StartPlayNotificationPDU p = new StartPlayNotificationPDU(pdu);
                    if (playerPlaying) {
                        // TODO: error state
                    }
                    this.playerPlaying = true;
                    if (opponentPlaying) {
                        this.opponentPlaying = false;
                        this.clientHandler.opponentPlayEnded();
                    }
                    this.clientHandler.playerPlayStarted(p.getDurationSeconds());
                    break;
                }
                catch (Exception e) { }

                // try wait turn
                try {
                    WaitTurnNotificationPDU p = new WaitTurnNotificationPDU(pdu);
                    if (opponentPlaying) {
                        // TODO: error state
                    }
                    this.opponentPlaying = true;
                    if (playerPlaying) {
                        this.playerPlaying = false;
                    }
                    this.clientHandler.opponentPlayStarted();
                    break;
                }
                catch (Exception e) {  }

                // try game done
                try {
                    GameDoneNotificationPDU p = new GameDoneNotificationPDU(pdu);
                    this.clientHandler.gameDone(p.wasWon());
                    break;
                }
                catch (Exception e) {  }

                // try score updated
                try {
                    ScoreUpdateNotificationPDU p = new ScoreUpdateNotificationPDU(pdu);
                    this.clientHandler.opponentScoreUpdated(p.getScore());
                    break;
                }
                catch (Exception e) {  }

                // unknown pdu
                this.clientHandler.opponentGameEvent(pdu);
                break;
            } // while game in progress
            break;
        }
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




    public void writePDU(PDU pdu) {
        try {
            byte[] pdubytes = pdu.toJSONString().getBytes("UTF8");
            byte[] encrypted = clientcipher.encrypt(pdubytes);
            this.nioSocket.write(encrypted);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(255);
        }
        //System.err.println("> " + pdu.toJSONString());
    }

    // -------------------

    @Override
    public void connectionOpened(NIOSocket nios) {
        this.pduCounter = 0;
        this.connected = true;
        this.nioSocket = nios;
        this.clientHandler.clientConnected();
        Player player = new Player(this.config.getUsername(), this.config.getBetAmount());
        this.writePDU(new LoginRequestPDU(
                this.getNextPduCounter(),
                this.config.getGameId(),
                this.config.getGameName(),
                player)
        );
    }

    @Override
    public void connectionBroken(NIOSocket arg0, Exception arg1) {
        this.connected = false;
        this.clientHandler.clientDisconnected();
    }

    @Override
    public void packetReceived(NIOSocket arg0, byte[] encrypted) {
        try {
            byte[] decrypted = clientcipher.decrypt(encrypted);
            PDU pdu = PDU.parsedFromPacket(decrypted);
            this.interpretPDU(pdu);
        }
        catch (MalformedPDUException malx) {
            // TODO: disconnect?
        }
        catch (Exception e) {

        }
    }



}
