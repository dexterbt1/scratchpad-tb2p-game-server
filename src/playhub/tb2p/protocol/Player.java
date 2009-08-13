/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package playhub.tb2p.protocol;


import java.math.BigDecimal;

/**
 *
 * @author dexter
 */
public class Player {

    private String playerName;
    private BigDecimal betAmount;

    public Player(String name, BigDecimal betAmount) {
        this.playerName = name;
        this.betAmount = betAmount;
    }

    public String getName() { return this.playerName; }
    public BigDecimal getBetAmount() { return this.betAmount; }

    public String getUName() { return this.playerName.toLowerCase(); }

}
