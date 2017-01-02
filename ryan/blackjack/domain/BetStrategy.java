//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;

/**
 * BetStrategy class
 */
public abstract class BetStrategy
{
    //----------------------------------------- Constants ------------------------------------------
    public static final int USER_SPECIFIED  = 0;
    public static final int FLAT            = 1;
    public static final int RAMPED          = 2;
    public static final int DISCRETE_BINARY = 3;
    public static final int CONSTANT_RISK   = 4;

    //------------------------------------------ Defaults ------------------------------------------
    public static final double DEFAULT_MIN_BET =    1.0;
    public static final double DEFAULT_MAX_BET = 1000.0;


    //-------------------------------------- Class Attributes --------------------------------------
    private static double minBet = DEFAULT_MIN_BET;
    private static double maxBet = DEFAULT_MAX_BET;

    //-------------------------------------- Class Behaviour ---------------------------------------
    /**
     * Sets the table's min bet
     * @param minBet minimum bet that can be laid at the table
     */
    public static void setMinBet(double newMinBet)
    {
        minBet = newMinBet;
    }

    /**
     * Gets the minimum bet that can be laid at the table
     * @return minimum bet that can be laid at the table
     */
    public static double minBet()
    {
        return minBet;
    }

    /**
     * Sets the table's max bet
     * @param minBet maximum bet that can be laid at the table
     */
    public static void setMaxBet(double newMaxBet)
    {
        maxBet = newMaxBet;
    }

    /**
     * Gets the maximum bet that can be laid at the table\
     * @return maximum bet that can be laid at the table
     */
    public static double maxBet()
    {
        return maxBet;
    }

    /**
     * Gets an initial bet for a round. Different Bet Strategies will do this different ways.
     * @param trueCount this player's CountStrategy's current trueCount
     * @return initial bet for the coming round
     */
    public abstract double getBet(double trueCount);
}




