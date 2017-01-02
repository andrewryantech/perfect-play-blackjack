//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;

import ryan.blackjack.view.UserInterface;

/**
 * BetStrategyUserSpecified class
 */
public final class BetStrategyUserSpecified extends BetStrategy
{
    private UserInterface ui;                     //the user interface to query to get bet size
    private int pIdx;                             //player index this BetStrategy belongs to

    /**
     * Constructor
     * 
     * @param ui UserInterface to get bet from
     * @param pIdx the specific player for whom we are getting a bet
     */
    public BetStrategyUserSpecified(UserInterface ui, int pIdx)
    {
        this.ui = ui;
        this.pIdx = pIdx;
    }

    /**
     * Gets an initial bet for a round. Gets the value from the user
     * 
     * @param trueCount this player's CountStrategy's current trueCount
     * @return initial bet for the coming round
     */
    public double getBet(double trueCount)
    {
        return ui.getBet(pIdx, minBet(), maxBet());
    }
}




