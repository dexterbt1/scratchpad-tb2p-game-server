/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

/**
 *
 * @author dexter
 */
public class StartPlayNotificationPDU extends PDU {

    public static final String COMMAND = "START_PLAY";

    private int duration_seconds = 30; // default

    public StartPlayNotificationPDU(long id) {
        this.setCommand(COMMAND);
        this.setId(id);
        this.setType(Type.NOTIFICATION);
    }

    public StartPlayNotificationPDU(long id, int duration_seconds) {
        this(id);
        this.setDurationSeconds(duration_seconds);
    }

    public void setDurationSeconds(int d) { this.duration_seconds = d; this.setPduField("duration_seconds", Integer.valueOf(d)); }
    public int getDurationSeconds() { return this.duration_seconds; }

}
