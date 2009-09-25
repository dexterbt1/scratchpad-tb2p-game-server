/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.server;

import playhub.tb2p.client.ClientCipher;
import java.util.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dexter
 */
public class ServerEncryptionTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    @Test
    public void test_client_to_server() {
        final String msg1 = "The quick brown fox jumps over the lazy sleeping dog. The quick brown fox jumps over the lazy sleeping dog. The quick brown fox jumps over the lazy sleeping dog. The quick brown fox jumps over the lazy sleeping dog. The quick brown fox jumps over the lazy sleeping dog.";

        try {
            ClientCipher enc = new ClientCipher();
            ServerCipher dec = new ServerCipher();
            byte[] encrypted = enc.encrypt(msg1.getBytes("UTF8"));
            System.err.println("encrypted=["+encrypted.length+"]");
            byte[] decrypted = dec.decrypt(encrypted);
            System.err.println("decrypted=["+decrypted.length+"]");
            System.err.println("msg1=["+msg1.getBytes("UTF8").length+"]");
            assertTrue( Arrays.equals(msg1.getBytes("UTF8"), decrypted) );
        }
        catch (Exception e) { e.printStackTrace(); fail(); }

    }


    public void test_server_to_client() {
        final String msg1 = "The quick brown fox jumps over the lazy sleeping dog. The quick brown fox jumps over the lazy sleeping dog. The quick brown fox jumps over the lazy sleeping dog. The quick brown fox jumps over the lazy sleeping dog. The quick brown fox jumps over the lazy sleeping dog.";

        try {
            ClientCipher dec = new ClientCipher();
            ServerCipher enc = new ServerCipher();
            byte[] encrypted = enc.encrypt(msg1.getBytes("UTF8"));
            System.err.println("encrypted=["+encrypted.length+"]");
            byte[] decrypted = dec.decrypt(encrypted);
            System.err.println("decrypted=["+decrypted.length+"]");
            System.err.println("msg1=["+msg1.getBytes("UTF8").length+"]");
            assertTrue( Arrays.equals(msg1.getBytes("UTF8"), decrypted) );
        }
        catch (Exception e) { e.printStackTrace(); fail(); }

    }



}
