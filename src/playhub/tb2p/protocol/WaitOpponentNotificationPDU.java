/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

/**
 *
 * @author dexter
 */
public class WaitOpponentNotificationPDU extends PDU {
    
    public static final String COMMAND = "WAIT_OPPONENT";

    public WaitOpponentNotificationPDU(long id) {
        this.setId(id);
        this.setCommand(COMMAND);
        this.setType(Type.NOTIFICATION);
    }

}
