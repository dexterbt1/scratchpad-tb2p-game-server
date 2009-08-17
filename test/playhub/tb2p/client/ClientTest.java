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
import playhub.tb2p.client.*;
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
        assertTrue(client.isConnected());
        // simulate failure
        client.connectionBroken(this.testSocket, new Exception("simulated login failure"));
        assertFalse(client.isLoggedIn());
        assertFalse(client.isConnected());
    }


    public byte[] packetLoginResponse() { return new LoginResponsePDU(1L).toJSONString().getBytes(); }
    public byte[] packetWaitTurn() { return new WaitTurnNotificationPDU(1L).toJSONString().getBytes(); }
    public byte[] packetWaitOpponent() { return new WaitOpponentNotificationPDU(1L).toJSONString().getBytes(); }
    public byte[] packetGameCancelled() { return new GameCancelledNotificationPDU(1L).toJSONString().getBytes(); }
    public byte[] packetStartPlay() { return new StartPlayNotificationPDU(1L,77).toJSONString().getBytes(); }
    

    @Test
    public void test_login_success() {
        client.connectionOpened(this.testSocket);
        assertTrue(client.isConnected());
        // simulate login response
        client.packetReceived(this.testSocket, this.packetLoginResponse());
        assertTrue(client.isLoggedIn());
    }


    @Test
    public void test_flow_player1_cancelled() {
        client.connectionOpened(this.testSocket);
        client.packetReceived(this.testSocket, this.packetLoginResponse());
        client.packetReceived(this.testSocket, this.packetWaitOpponent());
        // simulate that there was NO opponent after server timeout, there game is cancelled
        client.packetReceived(this.testSocket, this.packetGameCancelled());
        verify(clientHandler).gameCancelled();
        assertTrue(client.isGameCancelled());
    }

    
    @Test
    public void test_flow_player1() {
        client.connectionOpened(this.testSocket);
        client.packetReceived(this.testSocket, this.packetLoginResponse());
        client.packetReceived(this.testSocket, this.packetWaitOpponent());
        assertFalse(client.isPlayStarted());
        assertFalse(client.isOpponentPlayStarted());
        verify(clientHandler).opponentNotYetAvailable();
        assertFalse(client.isOpponentAvailable());
        // simulate that an opponent entered, and player1 now starts playing
        client.packetReceived(this.testSocket, this.packetStartPlay());
        verify(clientHandler).playerPlayStarted(77);
        assertTrue(client.isPlayStarted());
        assertFalse(client.isOpponentPlayStarted());
        assertTrue(client.isOpponentAvailable());
        client.endTurn();
        client.packetReceived(this.testSocket, this.packetWaitTurn());
        verify(clientHandler).opponentPlayStarted();
        assertTrue(client.isPlayStarted());
        assertTrue(client.isOpponentPlayStarted());
        // TODO: play ended
    }





}