//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;

/**
 * BetStrategyRamped class
 */
public final class BetStrategyRamped extends BetStrategy
{
    double trueCountThreshold;          //point past which bets are ramped
    double minRampedBet;                //minimum bet made if trueCount threshold is reached
    double rampFactor;                  //how much bet size increases with trueCount excess

    /**
     * Constructor
     * 
     * @param trueCountThreshold point past which bets are ramped
     * @param minRampedBet minimum bet made if trueCount threshold is reached
     * @param rampFactor how much bet size increases with trueCount excess
     */
    public BetStrategyRamped(double trueCountThreshold, double minRampedBet, double rampFactor)
    {
        this.trueCountThreshold = trueCountThreshold;
        this.minRampedBet = minRampedBet;
        this.rampFactor = rampFactor;
    }


    /**
     * Gets an initial bet for a round. 
     * 
     * If true count is below threshold, bets table minimum.
     * Else, places a bet that varies proportionally with trueCounts excess of trueCount threshold
     * 
     * @param trueCount this player's CountStrategy's current trueCount
     * @return initial bet for the coming round
     */
    public double getBet(double trueCount)
    {
        if(trueCount < trueCountThreshold)
            return minBet();
        else
            return Math.min(maxBet(),
                    minRampedBet + minRampedBet * (trueCount - trueCountThreshold) * rampFactor);
    }

    /**
     * Returns the bet size when true count reaches true count threshold
     * 
     * @return the bet size when true count reaches true count threshold
     */
    public double minRampedBet()
    {
        return minRampedBet;
    }

    /**
     * Returns bet = minRampedBet * trueCountExcess * rampFactor
     * 
     * @return multiple by which bet minRampedBet increases for increase in trueCountExcess
     */
    public double rampFactor()
    {
        return rampFactor;
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
}




