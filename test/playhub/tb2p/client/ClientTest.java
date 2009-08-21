/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.client;

import java.math.BigDecimal;
import naga.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.*;
import playhub.tb2p.protocol.*;

/**
 *
 * @author dexter
 */
public class ClientTest {

    private ClientConfig config;
    private Client client;
    private ClientHandler clientHandler = mock(ClientHandler.class);
    private NIOService testService = mock(NIOService.class);
    private NIOSocket testSocket = mock(NIOSocket.class);

    public ClientTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        config = new ClientConfig();
        config.setGameId("123");
        config.setHost("localhost");
        config.setPort(17888);
        config.setUsername("A");
        config.setBetAmount(BigDecimal.valueOf(50L));
        // assume this does not go thru connect()
        client = new Client(config, clientHandler);
        testSocket = mock(NIOSocket.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test_login_failure() {
        client.connectionOpened(this.testSocket);
        verify(clientHandler).clientConnected();
        // simulate failure
        client.connectionBroken(this.testSocket, new Exception("simulated login failure"));
        verify(clientHandler, never()).clientLoggedIn();
        verify(clientHandler).clientDisconnected();
    }


    public byte[] packetLoginResponse() { return new LoginResponsePDU(1L).toJSONString().getBytes(); }
    public byte[] packetWaitTurn() { return new WaitTurnNotificationPDU(1L).toJSONString().getBytes(); }
    public byte[] packetWaitOpponent() { return new WaitOpponentNotificationPDU(1L).toJSONString().getBytes(); }
    public byte[] packetGameCancelled() { return new GameCancelledNotificationPDU(1L).toJSONString().getBytes(); }
    public byte[] packetStartPlay(int duration) { return new StartPlayNotificationPDU(1L,duration).toJSONString().getBytes(); }
    public byte[] packetGameDone(boolean won) { return new GameDoneNotificationPDU(1L, won).toJSONString().getBytes(); }
    public byte[] packetScoreUpdate(long score) { return new ScoreUpdateNotificationPDU(1L, score).toJSONString().getBytes(); }

    public static class GameEventKeyDownPDU extends PDU {
        public static final String COMMAND = "KEYPRESS";
        private int keyCode;
        public GameEventKeyDownPDU(long id, int keyCode) {
            this.setCommand(COMMAND);
            this.setId(id);
            this.setType(Type.NOTIFICATION);
            this.setPduField("keyCode", Integer.valueOf(keyCode));
            this.keyCode = keyCode;
        }
    }
    public byte[] packetFromPDU(PDU p) { return p.toJSONString().getBytes(); }


    @Test
    public void test_login_success() {
        client.connectionOpened(this.testSocket);
        verify(clientHandler).clientConnected();
        // simulate login response
        client.packetReceived(this.testSocket, this.packetLoginResponse());
        verify(clientHandler).clientLoggedIn();
    }


    @Test
    public void test_flow_as_player1_cancelled() {
        client.connectionOpened(this.testSocket);
        client.packetReceived(this.testSocket, this.packetLoginResponse());
        client.packetReceived(this.testSocket, this.packetWaitOpponent());
        // simulate that there was NO opponent after server timeout, there game is cancelled
        client.packetReceived(this.testSocket, this.packetGameCancelled());
        verify(clientHandler).gameCancelled();
    }

    
    @Test
    public void test_flow_as_player1_won_basic() {
        client.connectionOpened(this.testSocket);
        client.packetReceived(this.testSocket, this.packetLoginResponse());
        client.packetReceived(this.testSocket, this.packetWaitOpponent());
        verify(clientHandler).opponentNotYetAvailable();
        // simulate that an opponent entered, and player1 now starts playing
        client.packetReceived(this.testSocket, this.packetStartPlay(77));
        verify(clientHandler).playerPlayStarted(77);
        client.endTurn();
        client.packetReceived(this.testSocket, this.packetWaitTurn());
        verify(clientHandler).opponentPlayStarted();
        client.packetReceived(this.testSocket, this.packetGameDone(true));
        verify(clientHandler).opponentPlayEnded();
        verify(clientHandler).gameDone(true);
    }


    @Test
    public void test_flow_as_player1_won_player2_disconnected() {
        client.connectionOpened(this.testSocket);
        client.packetReceived(this.testSocket, this.packetLoginResponse());
        client.packetReceived(this.testSocket, this.packetWaitOpponent());
        client.packetReceived(this.testSocket, this.packetStartPlay(77));
        verify(clientHandler).playerPlayStarted(77);
        client.packetReceived(this.testSocket, this.packetGameDone(true));
        verify(clientHandler).gameDone(true);
    }


    @Test
    public void test_flow_as_player1_lost_invalid_client() {
        client.connectionOpened(this.testSocket);
        client.packetReceived(this.testSocket, this.packetLoginResponse());
        client.packetReceived(this.testSocket, this.packetWaitOpponent());
        client.packetReceived(this.testSocket, this.packetStartPlay(77));
        verify(clientHandler).playerPlayStarted(77);
        client.packetReceived(this.testSocket, this.packetGameDone(false));
        verify(clientHandler).gameDone(false);
    }


    @Test
    public void test_flow_as_player2_won_basic() {
        client.connectionOpened(this.testSocket);
        client.packetReceived(this.testSocket, this.packetLoginResponse());
        client.packetReceived(this.testSocket, this.packetWaitTurn());
        verify(clientHandler).opponentPlayStarted();
        client.packetReceived(this.testSocket, this.packetStartPlay(33));
        verify(clientHandler).opponentPlayEnded();
        verify(clientHandler).playerPlayStarted(33);
        client.endTurn();
        client.packetReceived(this.testSocket, this.packetGameDone(true));
        verify(clientHandler).gameDone(true);
    }


    @Test
    public void test_flow_as_player1_with_game_events() {
        client.connectionOpened(this.testSocket);
        client.packetReceived(this.testSocket, this.packetLoginResponse()); // 1st write
        client.packetReceived(this.testSocket, this.packetWaitOpponent());
        client.packetReceived(this.testSocket, this.packetStartPlay(33));
        client.updateScore(0L);            // 2nd write
        client.updateScore(2000L);         // 3rd write
        client.endTurn();                  // 4th write
        verify(this.testSocket, times(4)).write(anyString().getBytes());
        client.packetReceived(this.testSocket, this.packetWaitTurn());
        PDU p;
        p = new GameEventKeyDownPDU(1L,1234);
        client.packetReceived(this.testSocket, this.packetFromPDU(p));
        p = new GameEventKeyDownPDU(2L,4321);
        client.packetReceived(this.testSocket, this.packetFromPDU(p));
        verify(clientHandler, times(2)).opponentGameEvent(any(PDU.class));
        client.packetReceived(this.testSocket, this.packetScoreUpdate(222L));
        client.packetReceived(this.testSocket, this.packetScoreUpdate(223L));
        verify(clientHandler).opponentScoreUpdated(222L);
        verify(clientHandler).opponentScoreUpdated(223L);
        client.packetReceived(this.testSocket, this.packetGameDone(true));
    }



}