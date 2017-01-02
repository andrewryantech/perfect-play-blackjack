//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  
//                 --> 
//                                        --> 

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;

import java.util.TreeSet;

/**
 * Represent's a Player's hand of 0 to 11 cards
 */
public final class PlayerHand extends Hand
{
    //----------------------------------------- Constants ------------------------------------------
    public static final int     MAX_SPLIT_LEVELS =  4;     //max times Player's Hand can be split
    public static final int     MAX_COUNT_CARDS  = 20;     //{A A A A A A A A A A 2 A A A A A A A A}
    public static final int     SPLIT_BY_VALUE   =  0;
    public static final int     SPLIT_BY_RANK    =  1;

    //------------------------------------------ Defaults ------------------------------------------
    public static final boolean DEFAULT_DEALT_FACE_UP               = false;
    public static final boolean DEFAULT_CAN_HIT_AFTER_ACES_SPLIT    = true;
    public static final int     DEFAULT_SPLIT_CARD_EQUALITY_TYPE    = SPLIT_BY_VALUE;
    public static final int     DEFAULT_TIMES_2_K_SPLITTABLE        = 2;
    public static final int     DEFAULT_TIMES_ACE_SPLITTABLE        = 4;
    public static final boolean DEFAULT_CAN_DOUBLE_AFTER_2toK_SPLIT = false;
    public static final boolean DEFAULT_CAN_DOUBLE_AFTER_ACE_SPLIT  = true;

    public static final TreeSet<Integer> DEFAULT_DOUBLABLE_CARD_COUNTS = new TreeSet<Integer>();
    static
    {
        DEFAULT_DOUBLABLE_CARD_COUNTS.add(2);
    }

    public static final TreeSet<Integer> DEFAULT_DOUBLABLE_SCORES = new TreeSet<Integer>();
    static
    {
        DEFAULT_DOUBLABLE_SCORES.add(9);
        DEFAULT_DOUBLABLE_SCORES.add(10);
        DEFAULT_DOUBLABLE_SCORES.add(11);
    }

    //-------------------------------------- Class Attributes --------------------------------------
    private static boolean      canHitAfterAcesSplit = DEFAULT_CAN_HIT_AFTER_ACES_SPLIT;
    private static boolean[][]  splitCardEqualities  = new boolean[13][13];
    static
    {
        setSplitCardEqualityType(DEFAULT_SPLIT_CARD_EQUALITY_TYPE);
    }

    private static int          times2toK_Splittable  = DEFAULT_TIMES_2_K_SPLITTABLE;
    private static int          timesAcesSplittable   = DEFAULT_TIMES_ACE_SPLITTABLE;

    //an array of states where for each state, we can either double down or not double down.
    //every Player Hand will be mapped to one of these states
    private static boolean[][][][] doublableStates = 
            new boolean[MAX_COUNT_CARDS + 1][21 + 1][2][MAX_SPLIT_LEVELS + 1];
    static
    {
        setDoublableStates(DEFAULT_DOUBLABLE_CARD_COUNTS, DEFAULT_DOUBLABLE_SCORES,
                DEFAULT_CAN_DOUBLE_AFTER_2toK_SPLIT, DEFAULT_CAN_DOUBLE_AFTER_ACE_SPLIT);
    }

    //-------------------------------------- Class Behaviour ---------------------------------------
    /**
     * Sets whether player can hit again after Aces split. Used by isHittable()
     * @parm canHitAfterAcesSplit whether we can hit a hand after splitting Aces
     */
    public static void setCanHitAfterAcesSplit(boolean canHitAfterAcesSplit)
    {
        PlayerHand.canHitAfterAcesSplit = canHitAfterAcesSplit;
    }

    /**
     * Configures whether cards are equal by value or rank
     * Eg, whether [X][J] can be split
     * @param equalityType whether 10-value cards are splittable by value or rank
     */
    public static void setSplitCardEqualityType(int equalityType)
    {
        for(int card1 = Card.TWO; card1 <= Card.PLAYER_ACE; card1++)
            for(int card2 = Card.TWO; card2 <= Card.PLAYER_ACE; card2++)
                splitCardEqualities[card1][card2] = equalityType == SPLIT_BY_VALUE ?
                        Card.PLAYER_VALUES[card1] == Card.PLAYER_VALUES[card2] :
                        card1 == card2;
    }

    /**
     * Sets number of times cards {2,3,4,5,6,7,8,9,X,J,Q,K} can be split
     * @param times2toK_Splittable number of times {2,3,4,5,6,7,8,9,X,J,Q,K} can be split
     */
    public static void setTimes2toK_Splittable(int times2toK_Splittable)
    {
        PlayerHand.times2toK_Splittable = times2toK_Splittable;
    }

    /**
     * Sets number of times cards {A} can be split
     * @param timesAcesSplittable number of times {A} can be split
     */
    public static void setTimesAcesSplittable(int timesAcesSplittable)
    {
    	PlayerHand.timesAcesSplittable = timesAcesSplittable;
    }

    /**
     * Configures the states that can be double downed
     * @param doublableCardCounts list of card counts that can be double downed
     * @param doublableScores list of scores that can be double downed
     * @param canDoubleAfter2toK_Split whether can double after {2,3,4,5,6,7,8,9,X,J,Q,K} split
     * @param canDoubleAfterAcesSplit whether can double after {A} split
     */
    public static void setDoublableStates(TreeSet<Integer> doublableCardCounts,
            TreeSet<Integer> doublableScores, boolean canDoubleAfter2toK_Split, 
            boolean canDoubleAfterAcesSplit)
    {
        int PRIOR_SPLIT_WAS_2toK = 0, PRIOR_SPLIT_WAS_A = 1;  //only relevant when splitLevel >= 1

        for(int _countCards = 0; _countCards <= MAX_COUNT_CARDS; _countCards++)
            for(int _score = 0; _score <= 21; _score++)
                for(int priorSplitType = PRIOR_SPLIT_WAS_2toK; 
                        priorSplitType <= PRIOR_SPLIT_WAS_A; priorSplitType++)
                    for(int _splitLevel = 0; _splitLevel <= MAX_SPLIT_LEVELS; _splitLevel++)
                        doublableStates[_countCards][_score][priorSplitType][_splitLevel] =
                                doublableCardCounts.contains(_countCards) &&
                                doublableScores.contains(_score) &&
                                (_splitLevel == 0 ||
                                priorSplitType == PRIOR_SPLIT_WAS_2toK && canDoubleAfter2toK_Split || 
                                priorSplitType == PRIOR_SPLIT_WAS_A && canDoubleAfterAcesSplit);
    }

    //------------------------------------ Instance Attributes -------------------------------------
    private double  bet;
    private boolean isRedeemed;                    //set by take evenMoney, 5CardCharlie, or 5Card21
    private boolean isDoubleDowned;
    private boolean isSurrendered;
    private int     splitLevel;

    //------------------------------------- Instance Behaviour -------------------------------------
    /**
     * Constructor. Zero parameters. Creates an new empty PlayerHand
     * @return new empty PlayerHand
     */
    public PlayerHand(double bet)
    {
        super(MAX_COUNT_CARDS);
        this.bet = bet;
        isRedeemed = false;
        isDoubleDowned = false;
        isSurrendered = false;
        splitLevel = 0;
    }


    /**
     * Constructor. Fully specified. Used by clone()
     * @return new empty PlayerHand
     */
    protected PlayerHand(Card[] cards, int[] cardTypes, int countCards, int countAces, 
            int countSoftAces, int score, int hashCode, double bet, boolean isRedeemed, 
            boolean isDoubleDowned, boolean isSurrendered, int splitLevel)
    {
        super(cards, cardTypes, countCards, countAces, countSoftAces, score, hashCode);
        this.bet = bet;
        this.isRedeemed = isRedeemed;
        this.isDoubleDowned = isDoubleDowned;
        this.isSurrendered = isSurrendered;
        this.splitLevel = splitLevel;
    }

    /**
     * Returns this PlayerHand's bet
     * @return this PlayerHand's bet
     */
    public double bet()
    {
        return bet;
    }

    /**
     * Clones and returns the clone of this hand. Passes a clone of arrays
     * @return a clone of this hand
     */
    @Override
    public Object clone()
    {
        return new PlayerHand(
                cards.clone(), cardTypes.clone(), countCards, countAces, countSoftAces, score, 
                hashCode, bet, isRedeemed, isDoubleDowned, isSurrendered, splitLevel);
    }

    /**
     * Double downs this hand. Increases bet to maximum double its current amount.
     * Ensures hand is double downable and that we can only add one more card to this hand.
     * DO NOT CALL DURING IN-GAME OPTION ROI CALCULATIONS (too slow)
     * @param additionalBet amount to increase this hand's bet
     */
    public void doubleDown(double additionalBet, Card finalCard)
    {
        if(!isDoublable())
            throw new IllegalStateException("hand state not double downable");
        if(additionalBet < 0 || additionalBet > bet)
            throw new IllegalArgumentException("additionalBet < 0: " + additionalBet);
        if(additionalBet > bet)
            throw new IllegalArgumentException("additionalBet: " + additionalBet + ", bet: " + bet);

        bet += additionalBet;
        insertCard(finalCard);
        isDoubleDowned = true;
    }


    /**
     * Produces a unique hashCode for the 89,010 unqiue bust/!bust (card combo / splitLevel) combos
     * @return hashcode of this hand
     */
    @Override
    public int hashCode()
    {
        if(hashCode != Hand.INVALID)
            return hashCode;
        hashCode = cardTypeCount(0) + 1;
        for(int i = 1; i < 13; i++)
        {
            hashCode = hashCode * 23;
            hashCode += cardTypeCount(i) + 1;
        }
        hashCode *= (splitLevel + 1);
        if(hashCode == Hand.INVALID)
            hashCode--;
        return hashCode;
    }

    /**
     * Temporarily inserts the specified cardType in this hand. 
     * Does not modify Card[].
     * (Must be fast as this method is called many times during prob. evaluation)
     * @param cardType the card type to insert
     */
    @Override
    public void insert(int cardType)
    {
        cardTypes[countCards++] = cardType;
        score += Card.PLAYER_VALUES[cardType];
        if(cardType == Card.PLAYER_ACE)
        {
            countAces++;
            countSoftAces++;
        }
        if(score > 21 && countSoftAces > 0)
        {
            score -= 10;
            countSoftAces--;
        }
        hashCode = Hand.INVALID;                                //to reflect that hand has changed
    }

    /**
     * Inserts the specified Card in this hand.
     * Can be slow as not used during probability calculation. 
     * @param card the Card to insert
     */
    @Override
    public void insertCard(Card card)
    {
        cards[countCards] = card;
        insert(card.playerType());
    }


    /**
     * Determines if this Hand Can be Double Downed. This method must be fast.
     * static boolean array already pre-configured. Lookup requires 1 Comparison, 1 conditional
     * and 4 array lookups. This is assumed to be faster that multiple comparisons and conditionals
     * @return whether this hand can be double downed
     */
    public boolean isDoublable()
    {
        return doublableStates[countCards][score][cardTypes[0]== Card.PLAYER_ACE ? 0:1][splitLevel];
    }


    /**
     * Whether this hand is Hittable. Not used during probability calculations. Can be slow.
     * Cannot hit a redeemed, doubleDowned or surrendered Hand. Sometimes can't hit after Aces split
     * @return whether this hand is Hittable
     */
    public boolean isHittable()
    {
        return score < 21 && !isRedeemed && !isDoubleDowned && !isSurrendered &&
                (splitLevel == 0 || cardTypes[0] != Card.PLAYER_ACE || canHitAfterAcesSplit);
    }

    /**
     * Whether this hand is a pair of Tens, Jacks, Queens, Kings, or Aces
     * If so, we can still split hand and get a split BlackJack, which would push dealer's blackjack
     * @return whether this hand is a pair of Tens, Jacks, Queens, Kings, or Aces
     */
    public boolean isPairTensOrAces()
    {
        return countCards == 2 && cardTypes[0] == cardTypes[1] && cards[0].value() >= 10;
    }


    /**
     * Gets whether this hand has been redeemed for even money, a 5CardCharlie, or a 5Card21
     * @return whether this hand has been redeemed for even money, a 5CardCharlie, or a 5Card21
     */
    public boolean isRedeemed()
    {
        return isRedeemed;
    }


    /**
     * Returns whether this Hand is Splittable
     * @return whether this Hand is Splittable
     */
    public boolean isSplittable()
    {
        return (countCards == 2) &&                                //most likely short-circuit first
                splitCardEqualities[cardTypes[0]][cardTypes[1]] && 
                (splitLevel < 
                (cardTypes[0] == Card.PLAYER_ACE ? timesAcesSplittable : times2toK_Splittable));
    }


    /**
     * Gets whether this hand has been surrendered
     * @return whether this hand has been surrendered
     */
    public boolean isSurrendered()
    {
        return isSurrendered;
    }

    /**
     * Redeems this Hand. Bet paid out immediately. Hand ignored when settling bets.
     * Returns the 1 * bet  to caller
     * @return 1 * bet so caller can add to bank if appropriate
     */
    public double redeem()
    {
        if(!isBlackJack() && countCards == 5)
            throw new IllegalStateException("Hand must be BlackJack, 5CardChalie, or 5Card21");
        if(isRedeemed)
            throw new IllegalStateException("Hand already redeemed");

        isRedeemed = true;
        return bet;
    }



    /**
     * Removes the most recently inserted cardType from this hand
     * Reverses a temporary change to the hand. Does not modify Card[].
     * (Must be fast as this method is called many times during prob. evaluation)
     */
    @Override
    public void removeLast()
    {
        score -= Card.PLAYER_VALUES[cardTypes[--countCards]];
        if(cardTypes[countCards] == Card.PLAYER_ACE)
        {
            countAces--;
            countSoftAces--;
        }
        if(score <= 11 && containsHardAce())
        {
            score += 10;
            countSoftAces++;
        }
        hashCode = -1;
    }


    /**
     * Splits this hand. Removes second card, increments splitLevel, returns a clone of itself
     * (We could avoid clone() and make faster by having GameLogic
     *   removeLast(), incrementSplitLevel(), 
     *   splitROI = playerTurn()
     *   insert(cardTypes[0]), decrementSplitLevel()
     *  but as it's not called very often, and this is cleaner, probably use this implementation)
     * @return cloned single-card hand
     */
    public PlayerHand split()
    {
        removeLast();
        splitLevel++;
        return (PlayerHand)clone();
    }

    /**
     * Returns how many times this hand has been split
     * @return how many times this hand has been split
     */
    public int splitLevel()
    {
        return splitLevel;
    }




    /**
     * Surrenders this hand. Returns 1/2 * Bet size. 
     * @return 1/2 * bet of this hand
     */
    public double surrender()
    {
        if(isSurrendered)
            throw new IllegalStateException("Hand already surrendered");
        isSurrendered = true;
        return bet / 2;
    }

    /**
     * Unsplits this Hand. Called after evaluating a splitROI
     */
    public void unsplit()
    {
        insert(cardTypes[0]);
        splitLevel--;
    }


}