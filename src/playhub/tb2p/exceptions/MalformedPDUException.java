/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.exceptions;

/**
 *
 * @author dexter
 */
public class MalformedPDUException extends Exception {

    private String diagnostic;

    public MalformedPDUException(String diag) {
        this.diagnostic = diag;
    }

    @Override
    public String toString() {
        return "Malformed PDU: diagnostic="+diagnostic;
    }
}
