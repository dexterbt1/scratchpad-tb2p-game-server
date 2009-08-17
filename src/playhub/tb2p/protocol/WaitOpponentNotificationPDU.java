/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

import playhub.tb2p.exceptions.*;
/**
 *
 * @author dexter
 */
public class WaitOpponentNotificationPDU extends PDU {
    
    public static final String COMMAND = "WAIT_OPPONENT_PLAYER";

    public WaitOpponentNotificationPDU(long id) {
        this.setId(id);
        this.setCommand(COMMAND);
        this.setType(Type.NOTIFICATION);
    }

    public WaitOpponentNotificationPDU(PDU pdu) throws MalformedPDUException {
        this(pdu.getId());
        if (    pdu.getCommand().equals(COMMAND)
             && (pdu.getType()==PDU.Type.NOTIFICATION)) {
            // nop
        }
        else {
            throw new MalformedPDUException("Invalid WaitOpponentNotification PDU");
        }
    }

}
