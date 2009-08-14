/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

/**
 *
 * @author dexter
 */
public class LoginResponsePDU extends PDU {
    
    public static final String COMMAND = "LOGIN";

    public LoginResponsePDU(long id) {
        this.setId(id);
        this.setType(PDU.Type.RESPONSE);        
        this.setCommand(COMMAND);
    }

}
