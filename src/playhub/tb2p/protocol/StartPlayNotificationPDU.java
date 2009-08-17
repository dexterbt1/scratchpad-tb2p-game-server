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

    public StartPlayNotificationPDU(PDU pdu) throws MalformedPDUException {
        this(pdu.getId());
        if (     pdu.getCommand().equals(COMMAND)
             && (pdu.getType() == PDU.Type.NOTIFICATION)
             && (pdu.json.containsKey("duration_seconds")))
        {
            try {
                Number n = (Number)pdu.getPduField("duration_seconds");
                this.setDurationSeconds(n.intValue());
            }
            catch (Exception e) {
                throw new MalformedPDUException("duration_seconds expected to be an integer");
            }
        }
        else {
            throw new MalformedPDUException("Invalid StartPlayNotification");
        }
    }

    public void setDurationSeconds(int d) { this.duration_seconds = d; this.setPduField("duration_seconds", Integer.valueOf(d)); }
    public int getDurationSeconds() { return this.duration_seconds; }



}
