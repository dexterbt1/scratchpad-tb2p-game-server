/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

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

}
