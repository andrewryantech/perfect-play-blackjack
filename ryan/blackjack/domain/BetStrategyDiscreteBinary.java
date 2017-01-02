//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;

/**
 * BetStrategyDiscreteBinary class
 */
public final class BetStrategyDiscreteBinary extends BetStrategy
{
    double trueCountThreshold;     //if trueCount past this point, switch from lowBet to bigBet
    double lowBet;                  //bet made if trueCount is less than trueCountThreshold
    double bigBet;                  //bet made if trueCount is more than trueCountThreshold

    /**
     * Constructor
     * 
     * @param ui UserInterface to get bet from
     * @param pIdx the specific player for whom we are getting a bet
     */
    public BetStrategyDiscreteBinary(double trueCountThreshold, double lowBet, double bigBet)
    {
        this.trueCountThreshold = trueCountThreshold;
        this.lowBet = lowBet;
        this.bigBet = bigBet;
    }


    /**
     * Gets an initial bet for a round. 
     * 
     * If trueCount is less than threshold, return low bet, otherwise return big bet
     * 
     * @param trueCount this player's CountStrategy's current trueCount
     * @return initial bet for the coming round
     */
    public double getBet(double trueCount)
    {
        if(trueCount < trueCountThreshold)
            return lowBet;
        else
            return bigBet;
    }

    /**
     * Returns the True Count threshold, beyond which we ramp up the bet size
     * 
     * @return True Count threshold, beyond which we ramp up the bet size
     */
    public double trueCountThreshold()
    {
        return trueCountThreshold;
    }

    /**
     * Returns the low bet
     * 
     * @return the low bet
     */
    public double lowBet()
    {
        return lowBet;
    }


    /**
     * Returns the big bet
     * 
     * @return the big bet
     */
    public double bigBet()
    {
        return bigBet;
    }
}




