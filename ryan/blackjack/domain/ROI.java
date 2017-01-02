//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;

/**
 * ROI class
 */
public final class ROI
{
    //----------------------------------------- Constants ------------------------------------------
    public static final double WIN       =  1.0;
    public static final double DRAW      =  0.0;
    public static final double SURRENDER = -0.5;
    public static final double LOSS      = -1.0;


    //------------------------------------------ Defaults ------------------------------------------
    public static final double DEFAULT_BLACKJACK_ROI       =   1.5;
    public static final double DEFAULT_SPLIT_BLACKJACK_ROI =   1.0;
    public static final double DEFAULT_FIVE_CARD_21_AMOUNT = 100.0;


    //-------------------------------------- Class Attributes --------------------------------------
    private static double blackJackROI      = DEFAULT_BLACKJACK_ROI;
    private static double splitBlackJackROI = DEFAULT_SPLIT_BLACKJACK_ROI;
    private static double fiveCard21Amount  = DEFAULT_FIVE_CARD_21_AMOUNT;

    //-------------------------------------- Class Behaviour ---------------------------------------
    /**
     * Returns the ROI for a player BlackJack win
     * @return the ROI for a player BlackJack win
     */
    public static double blackJack()
    {
        return blackJackROI;
    }

    /**
     * Returns the ROI for a draw
     * @return the ROI for a draw
     */
    public static double draw()
    {
        return DRAW;
    }

    /**
     * Returns the ROI for a player Five Card 21 Win. 
     * Assumes Five-Card-21 Amount is fixed, bet size variables, therefore ROI will vary with bet
     * @return the ROI for a player Five Card 21 Win. 
     */
    public static double fiveCard21(double bet)
    {
        return WIN + fiveCard21Amount / bet;
    }

    /**
     * Returns the ROI for a player loss
     * @return the ROI for a player loss
     */
    public static double loss()
    {
        return LOSS;
    }

    /**
     * Returns the ROI for a player surrender
     * @return the ROI for a player surrender
     */
    public static double surrender()
    {
        return SURRENDER;
    }

    /**
     * Gets the best ROI from a series or array of ROIs
     * @param rois a varialbe length series or array or ROIs
     * @return maximum ROI
     */
    public static double max(ROI... rois)
    {
        double max = rois[0].roi();                //first is standROI so never null
        for(int i = 1; i < rois.length; i++)
            if(rois[i] != null && rois[i].roi() > max)
                max = rois[i].roi();
        return max;      
    }

    /**
     * Gets the best ROI from a series or array of ROIs
     * @param rois a varialbe length series or array or ROIs
     * @return maximum ROI
     */
    public static double max(Double... rois)
    {
        double max = rois[0];                      //first is standROI, always valid
        for(int i = 1; i < rois.length; i++)
            if(rois[i] > max)
                max = rois[i];
        return max;      
    }

    /**
     * Merges an array of ROIs into a aggregated ROI
     * @param rois an array of possible rois, that will be merged into an aggregate
     * @return a merged aggregate roi
     */
    public static double merge(ROI[] rois)
    {

//TODO, remove this check after testing. Just slows things down and shouldn't be necessary
double probCheck = 0.0;
for(ROI roi : rois)
    if(roi != null)
        probCheck += roi.probability();
if(Math.abs(1.0 - probCheck) > 0.000001)
{
System.out.println("Individual probabilities:");
    for(ROI roi : rois)
        if(roi != null)
            System.out.println(roi.probability());
        else
            System.out.println(roi);
System.out.println();
    throw new IllegalArgumentException("\nProbabilties must sum to 1.0: " + probCheck);
}
        double aggregateROI = 0.0;        
        {
            for(int i = 0; i < rois.length; i++)
            {
                if(rois[i] != null)
                {
                    aggregateROI += rois[i].probability() * rois[i].roi();
                }
            }
        }
        return aggregateROI;
    }

    /**
     * Sets the table's BlackJack ROI
     * @param newBlackJackROI the new BlackJack ROI to apply to the table
     */
    public static void setBlackJackROI(double newBlackJackROI)
    {
        blackJackROI = newBlackJackROI;
    }

    /**
     * Sets the table's Five Card 21 Amount
     * @param newBlackJackROI the new Five Card 21 Amount to apply to the table
     */
    public static void setFiveCard21Amount(double newFiveCard21Amount)
    {
        fiveCard21Amount = newFiveCard21Amount;
    }


    /**
     * Sets the table's Split BlackJack ROI
     * @param newSplitBlackJackROI the new Split BlackJack ROI to apply to the table
     */
    public static void setSplitBlackJackROI(double newSplitBlackJackROI)
    {
        splitBlackJackROI = newSplitBlackJackROI;
    }


    /**
     * Gets the table's Split BlackJack ROI
     * @return the table's Split BlackJack ROI
     */
    public static double splitBlackJack()
    {
        return splitBlackJackROI;
    }

    /**
     * Returns the ROI for a player win
     * @return the ROI for a player win
     */
    public static double win()
    {
        return WIN;
    }


    //------------------------------------ Instance Attributes -------------------------------------
    private double probability;               //the probability this ROI will occur
    private double roi;                       //the ROI from a $1 bet for this result

    //------------------------------------- Instance Behaviour -------------------------------------
    /**
     * Constructor.
     */
    public ROI(double probability, double roi)
    {
        this.probability = probability;
        this.roi = roi;
    }

    /**
     * Returns this ROI's probability of occurrence
     * @return this ROI's probability of occurrence
     */
    public double probability()
    {
        return probability;
    } 

    /**
     * Returns this ROI's return-on-investment (roi)
     * @return this ROI's return-on-investment (roi)
     */
    public double roi()
    {
        return roi;
    } 




}