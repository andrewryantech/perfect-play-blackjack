//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;

/**
 * Recommendations class.
 * Encapsulates the best Option and whether to take insurance for each possible intitial deal
 */
public final class Recommendations
{
    //----------------------------------------- Constants ------------------------------------------

    //-------------------------------------- Class Attributes --------------------------------------

    //-------------------------------------- Class Behaviour ---------------------------------------


    //------------------------------------------ Defaults ------------------------------------------

    //------------------------------------ Instance Attributes -------------------------------------
    private Option[][][]  bestOptions;                 //size = 13^3 although only max 1183 are used 
    private boolean[][][] insuranceChoices;            //[dCard1][pCard1][pCard2]

    //------------------------------------- Instance Behaviour -------------------------------------
    /**
     * Constructor
     */
    public Recommendations()
    {
        bestOptions = new Option[13][13][13];
        insuranceChoices = new boolean[13][13][13];
    }

    /**
     * Inserts best Option & insurance choice for specified dCard1Type, pCard1Type & pCard2Type
     * @param dC1Type Dealer's Face Card
     * @param pC1Type Player's Face Card
     * @param pC2Type Player's Hole Card
     * @param insrChc whether Player should take insurance for this initial deal
     * @param bestOption best available Player Option for this initial deal
     */
    public void insert(int dC1Type, int pC1Type, int pC2Type, boolean insrChc, Option bestOption)
    {
        insuranceChoices[dC1Type][pC1Type][pC2Type] = insrChc;
        bestOptions[dC1Type][pC1Type][pC2Type] = bestOption;
    }


    /**
     * Returns whether Player should take insurance on specified initial deal
     * @param dC1Type Dealer's Face Card
     * @param pC1Type Player's Face Card
     * @param pC2Type Player's Hole Card
     * @return whether Player should take insurance on specified initial deal
     */
    public boolean takeInsurance(int dC1Type, int pC1Type, int pC2Type)
    {
        return insuranceChoices[dC1Type][pC1Type][pC2Type];
    }

    /**
     * Returns best available Player Option for specified initial deal
     * @param dC1Type Dealer's Face Card
     * @param pC1Type Player's Face Card
     * @param pC2Type Player's Hole Card
     * @return best available Player Option for specified initial deal
     */
    public Option bestOption(int dC1Type, int pC1Type, int pC2Type)
    {
        return bestOptions[dC1Type][pC1Type][pC2Type];
    }






}