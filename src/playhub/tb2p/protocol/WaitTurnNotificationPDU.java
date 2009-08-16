/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

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

}
