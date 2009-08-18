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
public class PlayEndedNotificationPDU extends PDU {

    public static final String COMMAND = "PLAY_ENDED";

    public PlayEndedNotificationPDU(long id) {
        this.setCommand(COMMAND);
        this.setId(id);
        this.setType(Type.NOTIFICATION);
    }

    public PlayEndedNotificationPDU(PDU pdu) throws MalformedPDUException {
        boolean successParse = false;
        if (    pdu.getType().equals(Type.NOTIFICATION)
             && pdu.getCommand().equals(COMMAND)) {
            this.setCommand(pdu.getCommand());
            this.setId(pdu.getId());
            this.setType(pdu.getType());
            successParse = true;
        }
        else {
            throw new MalformedPDUException("Invalid PLAY_ENDED notification");
        }
    }

}
