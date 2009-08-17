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
public class WaitTurnNotificationPDU extends PDU {

    public static final String COMMAND = "WAIT_OPPONENT_TURN";

    public WaitTurnNotificationPDU(long id) {
        this.setCommand(COMMAND);
        this.setId(id);
        this.setType(Type.NOTIFICATION);
    }

    public WaitTurnNotificationPDU(PDU pdu) throws MalformedPDUException {
        this.setId(pdu.getId());
        if (      pdu.getCommand().equals(COMMAND)
              && (pdu.getType()==PDU.Type.NOTIFICATION) ) {
            // nop
        }
        else {
            throw new MalformedPDUException("Invalid WaitTurnNotification");
        }

    }

}
