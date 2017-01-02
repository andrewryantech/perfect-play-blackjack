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
 * Represent's the dealer's hand
 */
public final class DealerHand extends Hand
{
    //----------------------------------------- Constants ------------------------------------------
    public static final int MAX_COUNT_CARDS = 13;   // {[A][A][A][A][A][A][A][5][A][A][A][A][A]}

    //------------------------------------------ Defaults ------------------------------------------
    public static final boolean DEFAULT_DEALER_HITS_SOFT_17S   = true;
    public static final boolean DEFAULT_DEALER_DEALT_HOLE_CARD = false;

    //------------------------------------ Instance Attributes -------------------------------------
    private boolean confirmedNotBlackJack;           //if true, dealer has peeked and continued
    

    //------------------------------------- Instance Behaviour -------------------------------------
    /**
     * Constructor. Zero parameters. Creates an empty Hand
     * @return new empty Hand
     */
    public DealerHand()
    {
        super(MAX_COUNT_CARDS);
        confirmedNotBlackJack = false;
    }

    /**
     * Constructor. Fully-specified. Used by clone()
     * @return a clone of this DealerHand
     */
    protected DealerHand(boolean confirmedNotBlackJack)
    {
        super(MAX_COUNT_CARDS);
        this.confirmedNotBlackJack = confirmedNotBlackJack;
    }

    /**
     * Gets whether Dealer has peeked at this Hand and confirmed it is not BlackJack
     * @return whether this hand is confirmed not to be BlackJack, ie, if Dealer has peeked
     */
    public boolean confirmedNotBlackJack()
    {
        return confirmedNotBlackJack;
    }

    /**
     * Confirms that this hand (which must have been single Ace or 10val) is not blackjack
     */
    public void confirmNotBlackJack()
    {
        if(confirmedNotBlackJack)
            throw new IllegalStateException("Dealer has already peeked and confirmed no BlackJack");

        if(!isSingleAce() && !isSingle10())
            throw new IllegalStateException("Can only peek at single Ace or 10value Card");

        if(countCards != 1)
            throw new IllegalStateException("Can only peek when dealer has a single face card");

        confirmedNotBlackJack = true;
    }

    /** 
     * Produces a unique hashCode for the 4,171 unique bust/!bust card combinations
     * @return hashcode of this DealerHand
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
        score += Card.DEALER_VALUES[cardType];
        if(cardType == Card.DEALER_ACE)
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
        insert(card.dealerType());
    }

    /**
     * Removes the most recently inserted cardType from this hand
     * Reverses a temporary change to the hand. Does not modify Card[].
     * (Must be fast as this method is called many times during probability evaluation)
     */
    @Override
    public void removeLast()
    {
        score -= Card.DEALER_VALUES[cardTypes[--countCards]];
        if(cardTypes[countCards] == Card.DEALER_ACE)
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

}
















