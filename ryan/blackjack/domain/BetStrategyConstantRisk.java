//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;

/**
 * BetStrategyConstantRisk class
 */
public final class BetStrategyConstantRisk extends BetStrategy
{
    double trueCountThreshold;      //trueCount point past which constant risk bets are placed
    Player player;                  //player who owns this BetStrategy. So we can get current Bank
    double bankToBetRatio;          //proportion of current bank to bet

    /**
     * Constructor
     * 
     * @param ui UserInterface to get bet from
     * @param pIdx the specific player for whom we are getting a bet
     */
    public BetStrategyConstantRisk(double trueCountThreshold, double bankToBetRatio)
    {
        if(bankToBetRatio < 1.0)
            throw new IllegalArgumentException("bankToBetRatio must be > 1.0: " + bankToBetRatio);
        this.trueCountThreshold = trueCountThreshold;
        this.bankToBetRatio = bankToBetRatio;
    }

    /**
     * Assigns a Player to this BetStrategy.
     * BetStrategy is constructed, then Player, so only then Player can be assigned
     * @param player the specified player that owns this BetStrategy
     */
    public void setPlayer(Player player)
    {
        this.player = player;
    }

    /**
     * Gets an initial bet for a round. 
     * If trueCount is below threshold, or player is in debt, returns table's minimum bet. 
     * Else returns a fixed proportion of the players bank.
     * @param trueCount this player's CountStrategy's current trueCount
     * @return initial bet for the coming round
     */
    public double getBet(double trueCount)
    {
        if(trueCount < trueCountThreshold || player.bank() < minBet())
            return minBet();
        else
            return Math.min( maxBet(), player.bank() / bankToBetRatio);
    }

    /**
     * Returns the multiple by which bank exceeds bet
     * @return the multiple by which bank exceeds bet
     */
    public double bankToBetRatio()
    {
        return bankToBetRatio;
    }

    /**
     * Returns the True Count threshold, beyond which we ramp up the bet size
     * @return True Count threshold, beyond which we ramp up the bet size
     */
    public double trueCountThreshold()
    {
        return trueCountThreshold;
    }
}




