/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

/**
 *
 * @author dexter
 */
public class GameDoneNotificationPDU extends PDU {

    public static final String COMMAND = "GAME_DONE";

    private boolean won;

    public GameDoneNotificationPDU(long id, boolean won) {
        this.setCommand(COMMAND);
        this.setId(id);
        this.setType(Type.NOTIFICATION);
        this.setWon(won);
    }

    public boolean wasWon() { return this.won; }
    public void setWon(boolean won) {
        this.won = won;
        this.setPduField("won", Boolean.valueOf(won));
    }
    
}
