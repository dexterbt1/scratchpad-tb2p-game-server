/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.client;

import playhub.tb2p.protocol.*;

/**
 *
 * @author dexter
 */
public interface ClientHandler {

    public void clientConnected();

    public void clientDisconnected();

    public void clientLoggedIn();

    public void gameCancelled();

    public void gameDone(boolean won);

    public void opponentNotYetAvailable();

    public void opponentAvailable(String opponetName);

    public void opponentScoreUpdated(long Score);

    public void opponentGameEvent(PDU pdu);

    public void opponentPlayStarted();

    public void opponentPlayEnded();

    public void playerPlayStarted(int duration_seconds);

}
