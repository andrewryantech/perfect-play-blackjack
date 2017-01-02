//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;

/**
 * CountStrategy class
 */
public final class CountStrategy
{
    //----------------------------------------- Constants ------------------------------------------
    public static final int NONE       = -1;        //so user can select they wish no count strategy
    public static final int HI_LO      =  0;
    public static final int HI_OPT1    =  1;
    public static final int HI_OPT2    =  2;
    public static final int KO         =  3;
    public static final int OMEGA2     =  4;
    public static final int RED7       =  5;
    public static final int ZEN_COUNT  =  6;
    public static final int CUSTOM     =  7;

    public static final int  COUNT_ADJUSTMENTS_TYPES = 10; // |2, 3, 4, 5, 6, 7, 8, 9, {X=J=Q=K}, A|

    //------------------------------------------ Defaults ------------------------------------------

    //Implemented this way so this class is always in a valid state. More robust
    public static final String     DEFAULT_CUSTOM_NAME             = "Custom";
    public static final double[]   DEFAULT_CUSTOM_ADJUSTMENTS      = {0,0,0,0,0,0,0,0,0,0};

    public static final String[] DEFAULT_NAMES = 
            {"Hi-Lo","Hi OptI","Hi OptII","KO","OMEGA II","Red 7","Zen Count", DEFAULT_CUSTOM_NAME};

    public static final double[][] DEFAULT_ADJUSTMENTS = {
            {1, 1, 1, 1, 1, 0  ,0, 0, -1, -1},
            {0, 1, 1, 1, 1, 0  ,0, 0, -1,  0},
            {1, 1, 2, 2, 1, 1  ,0, 0, -2,  0},
            {1, 1, 1, 1, 1, 1  ,0, 0, -1, -1},
            {1, 1, 2, 2, 2, 1  ,0,-1, -2,  0},
            {1, 1, 1, 1, 1, 0.5,0, 0, -1, -1},
            {1, 1, 2, 2, 2, 1  ,0, 0, -2, -1},
            DEFAULT_CUSTOM_ADJUSTMENTS};

    public static final boolean[]   DEFAULT_DISPLAY_RUNNING_COUNTS  = 
            {true, true, true, true, true, true, true, false};

    public static final boolean    DEFAULT_DISPLAY_ACE_GUAGE       = true;
    public static final boolean    DEFAULT_DISPLAY_PLAYER_BJ_GUAGE = true;

    //-------------------------------------- Class Attributes --------------------------------------
    private static String[] names = new String[8];              //names of each CountStrategy
    static
    {
        for(int idx = 0; idx < DEFAULT_NAMES.length; idx++)
            names[idx] = DEFAULT_NAMES[idx];
    }
    private static double[][] adjustments = new double[8][10];  //adjustments for each CountStrategy
    static
    {
        for(int idx = 0; idx < DEFAULT_ADJUSTMENTS.length; idx++)
            adjustments[idx] = DEFAULT_ADJUSTMENTS[idx];
        
    }

    //-------------------------------------- Class Behaviour ---------------------------------------
    /**
     * Sets the name of the custom CountStrategy
     * @param customName name of Custom CountStrategy
     */
    public static void setCustomName(String customName)
    {
        if(customName == null)
            throw new IllegalArgumentException("Custom Name cannot be null");
        if(customName.length() == 0)
            throw new IllegalArgumentException("Custom Name cannot be Zero-length");
    
        names[names.length - 1] = customName;
    }

    /**
     * Sets the running count adjustments for the custom CountStrategy
     * 
     * @param customAdjustments array of running count adjustments
     */
    public static void setCustomAdjustments(double[] customAdjustments)
    {
        if(customAdjustments == null)
            throw new IllegalArgumentException("Custom Adjustments cannot be null");

        if(customAdjustments.length != COUNT_ADJUSTMENTS_TYPES)
            throw new IllegalArgumentException("length must be COUNT_ADJUSTMENTS_TYPES: " + 
                    customAdjustments.length);
        
        adjustments[adjustments.length - 1] = customAdjustments.clone();
    }

    /**
     * Gets the name of a CountStrategy as a String
     * 
     * @param strategy a CountStrategy
     * @return the name of the specified CountStrategy
     */
    public static String getName(int strategy)
    {
        return names[strategy];
    }

    /**
     * Gets an adjustment for a particular Card and CountStrategy
     * 
     * @param strategy a CountStrategy
     * @param card the Card being removed from the shoe
     * @return the running count adjustment for the specified CountStrategy and Card type
     */
    public static double getAdjustment(int strategy, Card card)
    {
        return adjustments[strategy][card.value() - 2];
    }

    /**
     * Gets an adjustment for a particular Card and CountStrategy
     * 
     * @param strategy a CountStrategy
     * @param cardVal the Card being removed from the shoe's value
     * @return the running count adjustment for the specified CountStrategy and Card value
     */
    public static double getAdjustment(int strategy, int cardValue)
    {
        return adjustments[strategy][cardValue - 2];
    }

    /**
     * Constructor unavailable.
     */
    private CountStrategy()
    {
        //Cannot be instantiated
    }
}