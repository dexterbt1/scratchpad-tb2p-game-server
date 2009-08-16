/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import playhub.tb2p.exceptions.*;

/**
 *
 * @author dexter
 */
public class PDUTest {

    public PDUTest() {
    }

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

    /**
     * Test of parsedFromPacket method, of class PDU.
     */
    @Test(expected=MalformedPDUException.class)
    public void test__parsedFromPacket__empty() throws Exception
        { PDU pdu = PDU.parsedFromPacket("".getBytes()); }

    @Test(expected=MalformedPDUException.class)
    public void test__parsedFromPacket__blankObject() throws Exception
        { PDU pdu = PDU.parsedFromPacket("{ }".getBytes()); }

    @Test(expected=MalformedPDUException.class)
    public void test__parsedFromPacket__nullType() throws Exception
        { PDU pdu = PDU.parsedFromPacket("{ \"type\" : null }".getBytes()); }

    @Test(expected=MalformedPDUException.class)
    public void test__parsedFromPacket__unknownType() throws Exception
        { PDU pdu = PDU.parsedFromPacket("{ \"type\" : 123 }".getBytes()); }

    @Test(expected=MalformedPDUException.class)
    public void test__parsedFromPacket__unknownType2() throws Exception
        { PDU pdu = PDU.parsedFromPacket("{ \"type\" : \"hey\" }".getBytes()); }

    @Test(expected=MalformedPDUException.class)
    public void test__parsedFromPacket__requiredPduNumber() throws Exception
        { PDU pdu = PDU.parsedFromPacket("{ \"type\" : \"REQUEST\" }".getBytes()); }

    @Test(expected=MalformedPDUException.class)
    public void test__parsedFromPacket__nullPduNumber() throws Exception
        { PDU pdu = PDU.parsedFromPacket("{ \"type\" : \"REQUEST\", \"id\" : null }".getBytes()); }

    @Test(expected=MalformedPDUException.class)
    public void test__parsedFromPacket__stringPduNumber() throws Exception
        { PDU pdu = PDU.parsedFromPacket("{ \"type\" : \"REQUEST\", \"id\" : \"string-is-invalid\" }".getBytes()); }


    @Test
    public void test__parsedFromPacket__gotRequest() throws Exception {
        PDU pdu = PDU.parsedFromPacket("{ \"type\" : \"REQUEST\", \"id\" : 0 }".getBytes());
        assertEquals(pdu.getType(), PDU.Type.REQUEST);
        assertEquals(pdu.getId(), 0);
    }

    @Test
    public void test__parsedFromPacket__gotRequest2() throws Exception {
        PDU pdu = PDU.parsedFromPacket("{ \"type\" : \"REQUEST\", \"id\" : 12345 }".getBytes());
        assertEquals(pdu.getType(), PDU.Type.REQUEST);
        assertEquals(pdu.getId(), 12345);
    }

    @Test
    public void test__parsedFromPacket__gotResponse() throws Exception {
        PDU pdu = PDU.parsedFromPacket("{ \"type\" : \"RESPONSE\", \"id\" : 12345 }".getBytes());
        assertEquals(pdu.getType(), PDU.Type.RESPONSE);
        assertEquals(pdu.getId(), 12345);
    }

    @Test
    public void test__parsedFromPacket__gotNotification() throws Exception {
        PDU pdu = PDU.parsedFromPacket("{ \"type\" : \"NOTIFICATION\", \"id\" : 12345 }".getBytes());
        assertEquals(pdu.getType(), PDU.Type.NOTIFICATION);
        assertEquals(pdu.getId(), 12345);
    }



}