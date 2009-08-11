/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;

/**
 *
 * @author dexter
 */
public class GameSession {

    enum States {
        WAIT_PLAYER1,
        WAIT_PLAYER2,
        PLAY_PLAYER1,
        PLAY_PLAYER2,
    }

    private Player player1, player2;

}
