//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;

/**
 * Card class
 */
public final class Card
{
    //----------------------------------------- Constants ------------------------------------------
    public static final int COUNT_CARD_SUITS =  4;
    
    public static final int CLUBS    = 0;
    public static final int DIAMONDS = 1;
    public static final int HEARTS   = 2;
    public static final int SPADES   = 3;

    public static final String[] SUIT_NAMES = {"Clubs", "Diamonds", "Hearts", "Spades"};

    public static final int COUNT_DEALER_CARD_TYPES = 10;
    public static final int COUNT_PLAYER_CARD_TYPES = 13;
    public static final int TWO   =  0;
    public static final int THREE =  1;
    public static final int FOUR  =  2;
    public static final int FIVE  =  3;
    public static final int SIX   =  4;
    public static final int SEVEN =  5;
    public static final int EIGHT =  6;
    public static final int NINE  =  7;
    public static final int TEN   =  8;
    public static final int JACK  =  9;
    public static final int QUEEN = 10;
    public static final int KING  = 11;
    public static final int DEALER_ACE =  9;
    public static final int PLAYER_ACE = 12;

    // maps index to values, depends whether cardType is for a player or dealer
    public static final int[] DEALER_VALUES = new int[COUNT_DEALER_CARD_TYPES];
    static
    {
        for(int i = 0; i < 9; i++)                                              // {2 - 10} = 2 - 10
            DEALER_VALUES[i] = i + 2;
        DEALER_VALUES[8] = 10;                                                  //  {J,Q,K} = 10
        DEALER_VALUES[9] = 11;                                                  //      {A} = 11
    }
    public static final int[] PLAYER_VALUES = new int[COUNT_PLAYER_CARD_TYPES];         
    static
    {
        for(int i = 0; i < 9; i++)                                              // {2 - 10} = 2 - 10
            PLAYER_VALUES[i] = i + 2;
        PLAYER_VALUES[9] = PLAYER_VALUES[10] = PLAYER_VALUES[11] = 10;          //  {J,Q,K} = 10
        PLAYER_VALUES[12] = 11;                                                 //      {A} = 11
    }


    /**
     * Converts a Player CardType to its equivalent Dealer CardType
     */
    public static int dealerType(int playerCardType)
    {
        if(playerCardType < TWO || playerCardType > PLAYER_ACE)
            throw new IllegalArgumentException("Unknown playerCardType: " + playerCardType);
        else if(playerCardType <= TEN)
            return playerCardType;
        else if(playerCardType <= KING)
            return TEN;
        else //(playerCardType == PLAYER_ACE)
            return DEALER_ACE;
    }

    private int type;                                            //{2,3,4,5,6,7,8,9,X,J,Q,K,A}
    private int suit;                                            //{Club,Diamond,Heart,Spade}

    /**
     * Constructor
     */
    public Card(int type, int suit)
    {
        if(type < TWO || type > PLAYER_ACE || suit < CLUBS || suit > SPADES)
            throw new IllegalArgumentException("type: " + type + ", suit: " + suit);

        this.type = type;
        this.suit = suit;
    }

    /**
     * Returns the type of this card. {2,3,4,5,6,7,8,9,X,A}
     * 
     * @return cardType of this card {2,3,4,5,6,7,8,9,X,A}
     */
    public int dealerType()
    {
        if(type <= TEN)
            return type;
        else if(type <= KING)
            return TEN;
        else //ACE
            return DEALER_ACE;
    }


    /**
     * Returns the type of this card {0-12} -> {2,3,4,5,6,7,8,9,X,J,Q,K,A}
     * 
     * @return cardType of this card {0-12} -> {2,3,4,5,6,7,8,9,X,J,Q,K,A}
     */
    public int playerType()
    {
        return type;
    }


    /**
     * Returns the suit of this card. {Club,Diamond,Heart,Spade}
     * 
     * @return suit of this card. {Club,Diamond,Heart,Spade}
     */
    public int suit()
    {
        return suit;
    }

    /**
     * Returns the value {2 - 11} of this card. Doesn't consider whether Ace is hard or soft
     * 
     * @return value {2 - 11} of this card 
     */
    public int value()
    {
        return PLAYER_VALUES[type];
    }


    /**
     * Determines whether two cards are equal. Cards are equal if suit and type match.
     * 
     * @return whether other Card is equal to this Card
     */
    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof Card))
            return false;

        Card o = (Card)other;

        if(o.playerType() == playerType() && o.suit() == suit())
            return true;
        else
            return false;    
    }
}