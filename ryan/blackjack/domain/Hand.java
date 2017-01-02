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

/**
 * Represent's a hand of 0 to 11 cards
 */
public abstract class Hand
{
    //----------------------------------------- Constants ------------------------------------------
    public static final int     INVALID = -1;               //used for hashCode 


    //------------------------------------ Instance Attributes -------------------------------------
    protected Card[] cards;          //this hand's Cards. Presentation only. Not modified during probability evaluation.
    protected int[]  cardTypes;      //this hand's card types, used in probability calculations
    protected int    countCards;     //how many cards are in this hand
    protected int    countAces;      //how many Aces are in this hand
    protected int    countSoftAces;  //how many Aces valued @11 are in the hand
    protected int    score;          //hand's current score
    protected int    hashCode;       //unique for each unique (D|P) Hand
                                     // Note that D.hashCode() == P.hashCode()
                                     // does not imply that D == P

    /**
     * Constructor. Used for creating new empty Hand
     */
    protected Hand(int maxCountCards)
    {
        cards = new Card[maxCountCards];
        cardTypes = new int[maxCountCards];
        countCards = 0;
        countAces = 0;
        countSoftAces = 0;
        score = 0;
        hashCode = INVALID;
    }

    /**
     * Constructor. Fully specified. Used by clone().
     * 
     * @param cards              a clone() of cards[] in original hand 
     * @param cardTypes          a clone() of cardTypes[] in original hand 
     * @param countCards         number of cards in new hand
     * @param countAces          number of Aces in new hand
     * @param countSoftAces number of non-reduced Aces in new hand
     * @param score              score of new hand
     * @return new Hand with specified attributes
     */
    protected Hand(Card[] cards, int[] cardTypes, int countCards, int countAces, 
            int countSoftAces, int score, int hashCode)
    {
        this.cards = cards;
        this.cardTypes = cardTypes;
        this.countCards = countCards;
        this.countAces = countAces;
        this.countSoftAces = countSoftAces;
        this.score = score;
        this.hashCode = hashCode;
    }

    /**
     * Returns the number of cards of the specified type currently in this hand
     * Used by both DealerHand and PlayerHand to compute hashCode()
     * @param cardType cardType to count. Works for both DealerHand and PlayerHand types
     * @return number of cards of specified card type currently in this hand
     */
    public Card cardAt(int cIdx)
    {
        if(cIdx >= countCards)
            throw new IllegalArgumentException("countCards: " + countCards + ", cIdx: " + cIdx);
        return cards[cIdx];
    }


    /**
     * Returns the number of cards of the specified type currently in this hand
     * Used by both DealerHand and PlayerHand to compute hashCode()
     * @param cardType cardType to count. Works for both DealerHand and PlayerHand types
     * @return number of cards of specified card type currently in this hand
     */
    public int cardTypeCount(int cardType)
    {
        int countCardType = 0;
        for(int i = 0; i < countCards; i++)
            if(cardTypes[i] == cardType)
                countCardType++;
        return countCardType;
    }

    /**
     * Returns the number of cards in this hand
     * @return number of cards in this hand
     */
    public int countCards()
    {
        return countCards;
    }


    /**
     * Tests if two Hands contains same combination of cards [& same splitLevels (0-4)]
     * For (maxSplitLevels = 4)
     * As hashCode() produces a unique value for each unique hand, use hashCode() to equals().
     * Empirically tested, this is 3x faster.
     * Caller should never try to compare a DealerHand to a PlayerHand
     * @param o other Hand to compare this Hand to
     * @return whether the two Hands contain same combination of cards [and splitLevels]
     */
    @Override
    public final boolean equals(Object o)
    {
        return hashCode() == o.hashCode();
    }


    /**
     * Temporarily inserts the specified cardType in this hand. 
     * Does not modify Card[].
     * (Must be fast as this method is called many times during probability evaluation)
     * @param cardType the card type to insert
     */
    public abstract void insert(int cardType);



    /**
     * Inserts the specified Card in this hand.
     * Can be slow as not used during probability calculation. 
     * @param card the Card to insert
     */
    public abstract void insertCard(Card card);


    /**
     * Whether this hand is BlackJack
     * @return whether this hand is BlackJack
     */
    public boolean isBlackJack()
    {
        return score == 21 && countCards == 2;
    }

    /**
     * Whether the hand is Bust. ie, its score is > 21
     * @return whether the hand is bust
     */
    public boolean isBust()
    {
        return score > 21;
    }


    /**
     * Whether this hand contains a single 10-valued card
     * @return whether this hand contains a single 10-valued card
     */
    public boolean isSingle10()
    {
        return countCards == 1 && score == 10;
    }

    /**
     * Whether this hand contains a single Ace
     * @return whether this hand contains a single Ace
     */
    public boolean isSingleAce()
    {
        return countCards == 1 && score == 11;
    }

    /**
     * Whether this hand is soft, ie, contains a Ace valued at 11
     * @return whether this hand is soft
     */
    public boolean isSoft()
    {
        return countSoftAces > 0;
    }


    /**
     * Whether this hand has a Ace valued at 1
     * @return whether this hand contains a hard Ace (ie an Ace valued at 1)
     */
    public boolean containsHardAce()
    {
        return countAces - countSoftAces > 0;
    }

    /**
     * Removes the most recently inserted cardType from this hand
     * Reverses a temporary change to the hand. Does not modify Card[].
     * (Must be fast as this method is called many times during prob. evaluation)
     */
    public abstract void removeLast();


    /**
     * Returns the score of this hand
     * @return the score of this hand
     */
    public int score()
    {
        return score;
    }


}
















