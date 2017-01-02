//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;
import java.io.Serializable;
import java.util.TreeSet;

/**
 * Encapsulates all settings for this system.
 */
public final class Settings implements Serializable
{
    //----------------------------------------- Constants ------------------------------------------
    //------------------------------------------ Defaults ------------------------------------------

    //-------------------------------------- Class Attributes --------------------------------------
	private static final long serialVersionUID = 1L;	//used for serialising
	
    //-------------------------------------- Class Behaviour ---------------------------------------


	
	
	//------------------------------------ Instance Attributes -------------------------------------
    private double           shoeMaxPenetration           = Shoe.DEFAULT_SHOE_MAX_PENETRATION;
    private int              shoeCapacity                 = Shoe.DEFAULT_SHOE_CAPACITY;
    private int              shoeShuffleType              = Shoe.DEFAULT_SHUFFLE_TYPE;
    private int              shoeCountShuffleCuts         = Shoe.DEFAULT_COUNT_SHUFFLE_CUTS;
    private int              shoeCountShuffleLoops        = Shoe.DEFAULT_COUNT_SHUFFLE_LOOPS;
    private boolean[]        displayableCountStrategies   = CountStrategy.DEFAULT_DISPLAY_RUNNING_COUNTS;
    private String           customCountName              = CountStrategy.DEFAULT_CUSTOM_NAME;
    private double[]         customCountAdjustments       = CountStrategy.DEFAULT_ADJUSTMENTS[7];
    private boolean          displayAceGuage              = CountStrategy.DEFAULT_DISPLAY_ACE_GUAGE;
    private boolean          displayPlayerBJGuage         = CountStrategy.DEFAULT_DISPLAY_PLAYER_BJ_GUAGE;
    private int              autoPlayDisplayDelay         = GameLogic.DEFAULT_AUTOPLAY_DISPLAY_DELAY;
    private double           minBet                       = BetStrategy.DEFAULT_MIN_BET;
    private double           maxBet                       = BetStrategy.DEFAULT_MAX_BET;
    private boolean          dealerDealtHoleCard          = DealerHand.DEFAULT_DEALER_DEALT_HOLE_CARD;
    private int              surrenderType                = GameLogic.DEFAULT_SURRENDER_TYPE;
    private boolean          playerHandDealtFaceUp        = PlayerHand.DEFAULT_DEALT_FACE_UP;
    private boolean          dealerOffersInsurance        = GameLogic.DEFAULT_DEALER_OFFERS_INSURANCE;
    private boolean          dealerOffersEvenMoney        = GameLogic.DEFAULT_DEALER_OFFERS_EVEN_MONEY;
    private boolean          canHitAfterAcesSplit         = PlayerHand.DEFAULT_CAN_HIT_AFTER_ACES_SPLIT;
    private int              splitCardEqualityType        = PlayerHand.DEFAULT_SPLIT_CARD_EQUALITY_TYPE;
    private int              times2toK_Splittable         = PlayerHand.DEFAULT_TIMES_2_K_SPLITTABLE;
    private int              timesAcesSplittable          = PlayerHand.DEFAULT_TIMES_ACE_SPLITTABLE;
    private boolean          canDoubleAfter2toK_Split     = PlayerHand.DEFAULT_CAN_DOUBLE_AFTER_2toK_SPLIT;
    private boolean          canDoubleAfterAcesSplit      = PlayerHand.DEFAULT_CAN_DOUBLE_AFTER_ACE_SPLIT;
    private TreeSet<Integer> doublableCardCounts          = PlayerHand.DEFAULT_DOUBLABLE_CARD_COUNTS;
    private TreeSet<Integer> doublableScores              = PlayerHand.DEFAULT_DOUBLABLE_SCORES;
    private boolean          dealerHitsSoft17             = DealerHand.DEFAULT_DEALER_HITS_SOFT_17S;
    private boolean          dealerWinsTies               = GameLogic.DEFAULT_DEALER_WINS_TIES;
    private boolean          originalBetsOnly             = GameLogic.DEFAULT_ORIGINAL_BETS_ONLY;
    private double           blackJackROI                 = ROI.DEFAULT_BLACKJACK_ROI;
    private double           splitBlackJackROI            = ROI.DEFAULT_SPLIT_BLACKJACK_ROI;
    private boolean          fiveCardCharlieWins          = GameLogic.DEFAULT_FIVE_CARD_CHARLIE_WINS;
    private boolean          fiveCard21Wins               = GameLogic.DEFAULT_FIVE_CARD_21_WINS;
    private double           fiveCard21Amount             = ROI.DEFAULT_FIVE_CARD_21_AMOUNT;
    private boolean   	     showBankHistory              = true;

    //------------------------------------- Instance Behaviour -------------------------------------
    
    /**
     * Constructor. Loads defaults
     */
    public Settings()
    {
    }


    /**
     * Returns shoeMaxPenetration
     * 
     * @return shoeMaxPenetration
     */
    public double shoeMaxPenetration()
    {
        return shoeMaxPenetration;
    }

    /**
     * Sets shoeMaxPenetration. Caller must also update Shoe instance
     * 
     * @param shoeMaxPenetration
     */
    public void setShoeMaxPenetration(double shoeMaxPenetration)
    {
        this.shoeMaxPenetration = shoeMaxPenetration;
    }

    /**
     * Returns shoeCapcity
     * 
     * @return shoeCapcity
     */
    public int shoeCapacity()
    {
        return shoeCapacity;
    }

    /**
     * Sets shoeCapacity. Caller must also update Shoe instance
     * 
     * @param shoeCapacity
     */
    public void setShoeCapacity(int shoeCapacity)
    {
        this.shoeCapacity = shoeCapacity;
    }

    /**
     * Returns shoeShuffleType
     * 
     * @return shoeShuffleType
     */
    public int shoeShuffleType()
    {
        return shoeShuffleType;
    }

    /**
     * Sets shoeShuffleType. Caller must also update Shoe instance
     * 
     * @param shoeShuffleType
     */
    public void setShoeShuffleType(int shoeShuffleType)
    {
        this.shoeShuffleType = shoeShuffleType;
    }

    /**
     * Returns shoeCountShuffleCuts
     * 
     * @return shoeCountShuffleCuts
     */
    public int shoeCountShuffleCuts()
    {
        return shoeCountShuffleCuts;
    }

    /**
     * Sets shoeCountShuffleCuts. Caller must also update Shoe instance
     * @param shoeCountShuffleCuts
     */
    public void setShoeCountShuffleCuts(int shoeCountShuffleCuts)
    {
        this.shoeCountShuffleCuts = shoeCountShuffleCuts;
    }

    /**
     * Returns shoeCountShuffleLoops
     * @return shoeCountShuffleLoops
     */
    public int shoeCountShuffleLoops()
    {
        return shoeCountShuffleLoops;
    }

    /**
     * Sets shoeCountShuffleLoops. Caller must also update Shoe instance
     * @param shoeCountShuffleLoops
     */
    public void setShoeCountShuffleLoops(int shoeCountShuffleLoops)
    {
        this.shoeCountShuffleLoops = shoeCountShuffleLoops;
    }

    /**
     * Returns displayableCountStrategies
     * @return displayableCountStrategies
     */
    public boolean[] displayableCountStrategies()
    {
        return displayableCountStrategies.clone();
    }

    /**
     * Sets displayableCountStrategies
     * @param displayableCountStrategies
     */
    public void setDisplayableCountStrategies(int countStrategy, boolean isDisplayable)
    {
        displayableCountStrategies[countStrategy] = isDisplayable;
    }

    /**
     * Returns customCountName
     * @return customCountName
     */
    public String customCountName()
    {
        return customCountName;
    }

    /**
     * Sets customCountName
     * @param customCountName
     */
    public void setCustomCountName(String customCountName)
    {
        this.customCountName = customCountName;
        CountStrategy.setCustomName(customCountName);
    }

    /**
     * Returns customCountAdjustments
     * @return customCountAdjustments
     */
    public double[] customCountAdjustments()
    {
        return customCountAdjustments.clone();
    }

    /**
     * Sets customCountAdjustments
     * @param customCountAdjustments
     */
    public void setCustomCountAdjustments(double[] customCountAdjustments)
    {
        this.customCountAdjustments = customCountAdjustments;
        CountStrategy.setCustomAdjustments(customCountAdjustments);
    }

    /**
     * Returns displayAceGuage
     * @return displayAceGuage
     */
    public boolean displayAceGuage()
    {
        return displayAceGuage;
    }

    /**
     * Sets displayAceGuage
     * @param displayAceGuage
     */
    public void setDisplayAceGuage(boolean displayAceGuage)
    {
        this.displayAceGuage = displayAceGuage;
    }

    /**
     * Returns displayPlayerBJGuage
     * @return displayPlayerBJGuage
     */
    public boolean displayPlayerBJGuage()
    {
        return displayPlayerBJGuage;
    }

    /**
     * Sets displayPlayerBJGuage
     * @param displayPlayerBJGuage
     */
    public void setDisplayPlayerBJGuage(boolean displayPlayerBJGuage)
    {
        this.displayPlayerBJGuage = displayPlayerBJGuage;
    }


    /**
     * Returns autoPlayDisplayDelay
     * @return autoPlayDisplayDelay
     */
    public int autoPlayDisplayDelay()
    {
        return autoPlayDisplayDelay;
    }

    /**
     * Sets autoPlayDisplayDelay
     * @param autoPlayDisplayDelay
     */
    public void setAutoPlayDisplayDelay(int autoPlayDisplayDelay)
    {
        this.autoPlayDisplayDelay = autoPlayDisplayDelay;
    }



    /**
     * Returns minBet
     * @return minBet
     */
    public double minBet()
    {
        return minBet;
    }

    /**
     * Sets minBet
     * @param minBet
     */
    public void setMinBet(double minBet)
    {
        this.minBet = minBet;
        BetStrategy.setMinBet(minBet);
    }

    /**
     * Returns maxBet
     * @return maxBet
     */
    public double maxBet()
    {
        return maxBet;
    }

    /**
     * Sets maxBet
     * @param maxBet
     */
    public void setMaxBet(double maxBet)
    {
        this.maxBet = maxBet;
        BetStrategy.setMaxBet(maxBet);
    }

    /**
     * Returns dealerDealtHoleCard
     * @return dealerDealtHoleCard
     */
    public boolean dealerDealtHoleCard()
    {
        return dealerDealtHoleCard;
    }

    /**
     * Sets dealerDealtHoleCard
     * @param dealerDealtHoleCard
     */
    public void setDealerDealtHoleCard(boolean dealerDealtHoleCard)
    {
        this.dealerDealtHoleCard = dealerDealtHoleCard;
    }

    /**
     * Returns surrenderType
     * @return surrenderType
     */
    public int surrenderType()
    {
        return surrenderType;
    }

    /**
     * Sets surrenderType
     * @param surrenderType
     */
    public void setSurrenderType(int surrenderType)
    {
        this.surrenderType = surrenderType;
    }

    /**
     * Returns playerHandDealtFaceUp
     * @return playerHandDealtFaceUp
     */
    public boolean playerHandDealtFaceUp()
    {
        return playerHandDealtFaceUp;
    }

    /**
     * Sets playerHandDealtFaceUp
     * @param playerHandDealtFaceUp
     */
    public void setPlayerHandDealtFaceUp(boolean playerHandDealtFaceUp)
    {
        this.playerHandDealtFaceUp = playerHandDealtFaceUp;
    }

    /**
     * Returns dealerOffersInsurance
     * @return dealerOffersInsurance
     */
    public boolean dealerOffersInsurance()
    {
        return dealerOffersInsurance;
    }

    /**
     * Sets dealerOffersInsurance
     * @param dealerOffersInsurance
     */
    public void setDealerOffersInsurance(boolean dealerOffersInsurance)
    {
        this.dealerOffersInsurance = dealerOffersInsurance;
    }

    /**
     * Returns dealerOffersEvenMoney
     * @return dealerOffersEvenMoney
     */
    public boolean dealerOffersEvenMoney()
    {
        return dealerOffersEvenMoney;
    }

    /**
     * Sets dealerOffersEvenMoney
     * @param dealerOffersEvenMoney
     */
    public void setDealerOffersEvenMoney(boolean dealerOffersEvenMoney)
    {
        this.dealerOffersEvenMoney = dealerOffersEvenMoney;
    }

    /**
     * Returns canHitAfterAcesSplit
     * @return canHitAfterAcesSplit
     */
    public boolean canHitAfterAcesSplit()
    {
        return canHitAfterAcesSplit;
    }

    /**
     * Sets canHitAfterAcesSplit
     * @param canHitAfterAcesSplit
     */
    public void setCanHitAfterAcesSplit(boolean canHitAfterAcesSplit)
    {
        this.canHitAfterAcesSplit = canHitAfterAcesSplit;
        PlayerHand.setCanHitAfterAcesSplit(canHitAfterAcesSplit);
    }

    /**
     * Returns splitCardEqualityType
     * @return splitCardEqualityType
     */
    public int splitCardEqualityType()
    {
        return splitCardEqualityType;
    }

    /**
     * Sets splitCardEqualityType
     * @param splitCardEqualityType
     */
    public void setSplitCardEqualityType(int splitCardEqualityType)
    {
        this.splitCardEqualityType = splitCardEqualityType;
        PlayerHand.setSplitCardEqualityType(splitCardEqualityType);
    }

    /**
     * Returns times2toK_Splittable
     * @return times2toK_Splittable
     */
    public int times2toK_Splittable()
    {
        return times2toK_Splittable;
    }

    /**
     * Sets times2toK_Splittable
     * @param times2toK_Splittable
     */
    public void setTimes2toK_Splittable(int times2toK_Splittable)
    {
        this.times2toK_Splittable = times2toK_Splittable;
        PlayerHand.setTimes2toK_Splittable(times2toK_Splittable);
    }

    /**
     * Returns timesAcesSplittable
     * @return timesAcesSplittable
     */
    public int timesAcesSplittable()
    {
        return timesAcesSplittable;
    }

    /**
     * Sets timesAcesSplittable
     * @param timesAcesSplittable
     */
    public void setTimesAcesSplittable(int timesAcesSplittable)
    {
        this.timesAcesSplittable = timesAcesSplittable;
        PlayerHand.setTimesAcesSplittable(timesAcesSplittable);
    }

    /**
     * Returns canDoubleAfter2toK_Split
     * @return canDoubleAfter2toK_Split
     */
    public boolean canDoubleAfter2toK_Split()
    {
        return canDoubleAfter2toK_Split;
    }

    /**
     * Sets canDoubleAfter2toK_Split
     * @param canDoubleAfter2toK_Split
     */
    public void setCanDoubleAfter2toK_Split(boolean canDoubleAfter2toK_Split)
    {
        this.canDoubleAfter2toK_Split = canDoubleAfter2toK_Split;
        PlayerHand.setDoublableStates(doublableCardCounts, doublableScores,
                canDoubleAfter2toK_Split, canDoubleAfterAcesSplit);
    }

    /**
     * Returns canDoubleAfterAcesSplit
     * @return canDoubleAfterAcesSplit
     */
    public boolean canDoubleAfterAcesSplit()
    {
        return canDoubleAfterAcesSplit;
    }

    /**
     * Sets canDoubleAfterAcesSplit
     * @param canDoubleAfterAcesSplit
     */
    public void setCanDoubleAfterAcesSplit(boolean canDoubleAfterAcesSplit)
    {
        this.canDoubleAfterAcesSplit = canDoubleAfterAcesSplit;
        PlayerHand.setDoublableStates(doublableCardCounts, doublableScores,
                canDoubleAfter2toK_Split, canDoubleAfterAcesSplit);
    }

    /**
     * Returns doublableCardCounts
     * @return doublableCardCounts
     */
    @SuppressWarnings("unchecked")
	public TreeSet<Integer> doublableCardCounts()
    {
        return (TreeSet<Integer>)doublableCardCounts.clone();
    }

    /**
     * Sets doublableCardCounts
     * @param doublableCardCounts
     */
    @SuppressWarnings("unchecked")
	public void setDoublableCardCounts(TreeSet<Integer> doublableCardCounts)
    {
        this.doublableCardCounts = (TreeSet<Integer>) doublableCardCounts.clone();
        
        PlayerHand.setDoublableStates(
        		doublableCardCounts,
        		doublableScores,
                canDoubleAfter2toK_Split,
                canDoubleAfterAcesSplit
        );
    }

    /**
     * Returns doublableScores
     * @return doublableScores
     */
    @SuppressWarnings("unchecked")
	public TreeSet<Integer> doublableScores()
    {
        return (TreeSet<Integer>)doublableScores.clone();
    }

    /**
     * Sets doublableScores
     * @param doublableScores
     */
    @SuppressWarnings("unchecked")
	public void setDoublableScores(TreeSet<Integer> doublableScores)
    {
        this.doublableScores = (TreeSet<Integer>)doublableScores.clone();
        PlayerHand.setDoublableStates(doublableCardCounts, doublableScores,
                canDoubleAfter2toK_Split, canDoubleAfterAcesSplit);
    }

    /**
     * Returns dealerHitsSoft17
     * @return dealerHitsSoft17
     */
    public boolean dealerHitsSoft17()
    {
        return dealerHitsSoft17;
    }

    /**
     * Sets dealerHitsSoft17
     * @param dealerHitsSoft17
     */
    public void setDealerHitsSoft17(boolean dealerHitsSoft17)
    {
        this.dealerHitsSoft17 = dealerHitsSoft17;
    }

    /**
     * Returns dealerWinsTies
     * @return dealerWinsTies
     */
    public boolean dealerWinsTies()
    {
        return dealerWinsTies;
    }

    /**
     * Sets dealerWinsTies
     * @param dealerWinsTies
     */
    public void setDealerWinsTies(boolean dealerWinsTies)
    {
        this.dealerWinsTies = dealerWinsTies;
    }

    /**
     * Returns originalBetsOnly
     * @return originalBetsOnly
     */
    public boolean originalBetsOnly()
    {
        return originalBetsOnly;
    }

    /**
     * Sets originalBetsOnly
     * @param originalBetsOnly
     */
    public void setOriginalBetsOnly(boolean originalBetsOnly)
    {
        this.originalBetsOnly = originalBetsOnly;
    }

    /**
     * Returns blackJackROI
     * @return blackJackROI
     */
    public double blackJackROI()
    {
        return blackJackROI;
    }

    /**
     * Sets blackJackROI
     * @param blackJackROI
     */
    public void setBlackJackROI(double blackJackROI)
    {
        this.blackJackROI = blackJackROI;
        ROI.setBlackJackROI(blackJackROI);
    }

    /**
     * Returns splitBlackJackROI
     * @return splitBlackJackROI
     */
    public double splitBlackJackROI()
    {
        return splitBlackJackROI;
    }

    /**
     * Sets splitBlackJackROI
     * @param splitBlackJackROI
     */
    public void setSplitBlackJackROI(double splitBlackJackROI)
    {
        this.splitBlackJackROI = splitBlackJackROI;
        ROI.setSplitBlackJackROI(splitBlackJackROI);
    }

    /**
     * Returns fiveCardCharlieWins
     * @return fiveCardCharlieWins
     */
    public boolean fiveCardCharlieWins()
    {
        return fiveCardCharlieWins;
    }

    /**
     * Sets fiveCardCharlieWins
     * @param fiveCardCharlieWins
     */
    public void setFiveCardCharlieWins(boolean fiveCardCharlieWins)
    {
        this.fiveCardCharlieWins = fiveCardCharlieWins;
    }

    /**
     * Returns fiveCard21Wins
     * @return fiveCard21Wins
     */
    public boolean fiveCard21Wins()
    {
        return fiveCard21Wins;
    }

    /**
     * Sets fiveCard21Wins
     * @param fiveCard21Wins
     */
    public void setFiveCard21Wins(boolean fiveCard21Wins)
    {
        this.fiveCard21Wins = fiveCard21Wins;
    }

    /**
     * Returns fiveCard21Amount
     * @return fiveCard21Amount
     */
    public double fiveCard21Amount()
    {
        return fiveCard21Amount;
    }

    /**
     * Sets fiveCard21Amount
     * @param fiveCard21Amount
     */
    public void setFiveCard21Amount(double fiveCard21Amount)
    {
        this.fiveCard21Amount = fiveCard21Amount;
        ROI.setFiveCard21Amount(fiveCard21Amount);
    }
    
    public boolean showBankHistory()
    {
    	return showBankHistory;
    }
    
    public void setShowBankHistory(boolean showBankHistory)
    {
    	this.showBankHistory = showBankHistory;
    }

}








