//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;

/**
 * BetStrategyFlat
 */
public final class BetStrategyFlat extends BetStrategy
{
    double betSize;                      //the bet this BetStrategy always makes, regardless of count

    /**
     * Constructor
     * 
     * @param ui UserInterface to get bet from
     * @param pIdx the specific player for whom we are getting a bet
     */
    public BetStrategyFlat(double betSize)
    {
        this.betSize = betSize;
    }


    /**
     * Gets an initial bet for a round. Always returns the same value, regardless of count value
     * 
     * @param trueCount this player's CountStrategy's current trueCount. (IGNORED)
     * @return initial bet for the coming round
     */
    public double getBet(double trueCount)
    {
        return betSize;
    }
}




