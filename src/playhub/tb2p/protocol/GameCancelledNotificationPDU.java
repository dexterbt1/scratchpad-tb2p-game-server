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
public class GameCancelledNotificationPDU extends PDU {

    public static final String COMMAND = "GAME_CANCELLED";

    public GameCancelledNotificationPDU(long id) {
        this.setId(id);
        this.setType(Type.NOTIFICATION);
        this.setCommand(COMMAND);
    }

    public GameCancelledNotificationPDU(PDU pdu) throws MalformedPDUException {
        this(pdu.getId());
        if (     pdu.getCommand().equals(COMMAND)
             && (pdu.getType() == PDU.Type.NOTIFICATION))
        {
             // nop
        }
        else {
            throw new MalformedPDUException("Invalid GameCancelledNotification");
        }
    }


}
