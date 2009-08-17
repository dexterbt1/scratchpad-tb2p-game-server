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
public class LoginResponsePDU extends PDU {
    
    public static final String COMMAND = "LOGIN";

    public LoginResponsePDU(long id) {
        this.setId(id);
        this.setType(PDU.Type.RESPONSE);        
        this.setCommand(COMMAND);
    }

    public LoginResponsePDU(PDU pdu) throws MalformedPDUException {
        this(pdu.getId());
        if (     (pdu.getCommand().equals(COMMAND))
              && (pdu.getType() == PDU.Type.RESPONSE)
                ) {
            // nop
        }
        else {
            throw new MalformedPDUException("Not a valid LoginResponse");
        }
    }

}
