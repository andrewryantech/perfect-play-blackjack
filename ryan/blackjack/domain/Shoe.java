//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.domain;
import java.util.Random;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a shoe filled with cards
 */
public final class Shoe implements Cloneable
{
    
    //----------------------------------------- Constants ------------------------------------------
    public static final int    SHUFFLE_TYPE_NONE                 = 0;
    public static final int    SHUFFLE_TYPE_PSEUDORANDOM         = 1;
    public static final int    SHUFFLE_TYPE_CUT_SPLIT_MERGE_LOOP = 2;
    public static final double STANDARD_ACE_CONCENTRATION        = 1.0 / 13.0;
    public static final double STANDARD_TEN_VAL_CONCENTRATION    = 4.0 / 13.0;

    public static final int    REMOVE                            = 0;
    public static final int    INSERT                            = 1;
    


    //------------------------------------------ Defaults ------------------------------------------
    public static final double DEFAULT_SHOE_MAX_PENETRATION      = 0.8;
    public static final int    DEFAULT_SHOE_CAPACITY             = 416;
    public static final int    DEFAULT_SHUFFLE_TYPE              = SHUFFLE_TYPE_PSEUDORANDOM;
    public static final int    DEFAULT_COUNT_SHUFFLE_CUTS        = 5;   //used for cut,split,merge,N
    public static final int    DEFAULT_COUNT_SHUFFLE_LOOPS       = 5;   //used for cut,split,merge,N
    

    //-------------------------------------- Class Attributes --------------------------------------
    private static final Random rand = new Random();              //Random number generator        

    //-------------------------------------- Class Behaviour ---------------------------------------

    //------------------------------------ Instance Attributes -------------------------------------
    private int shuffleType;                                      //Pseudorandom or cutSplitMergeN
    private int countShuffleCuts;                                 //used by cut,split,merge
    private int countShuffleLoops;                                //used by cut,split,merge
    private double[] runningCounts;                               //one for each countStrategy
    private int capacity;                                         //number of cards in full shoe
    private LinkedList<Card> shoe;                                //not used in prob. calculations
    private double maxPenetration;                                //if here, shoe reset at round-end

    //------------------------------------- Instance Behaviour -------------------------------------
    /**
     * Constructor
     * Sets instance attributes. Inserts cards into shoe. Does not shuffle.
     */
    public Shoe(Settings settings)
    {
        this.shuffleType       = settings.shoeShuffleType();
        this.countShuffleCuts  = settings.shoeCountShuffleCuts();
        this.countShuffleLoops = settings.shoeCountShuffleLoops();

        runningCounts = new double[8];

        if(capacity % 52 != 0)
            throw new IllegalArgumentException("Shoe size must be mulitple of 52: " + capacity);
        this.capacity = settings.shoeCapacity();

        refill();

        this.maxPenetration    = settings.shoeMaxPenetration();
    }

    /**
     * Constructor (private) used by clone()
     */
    private Shoe()
    {
    }

    /**
     *
     */
    public int capacity()
    {
        return capacity;
    }

    /**
     *
     */
    public int cardCount(Card card)
    {
        int cardCount = 0;
        for(Card c : shoe)
            if(c.equals(card))
                cardCount++;
        return cardCount;
    }

    /**
     *
     */
    public int[] cardTypeCounts()
    {
        int[] cardTypeCounts13 = new int[13];
        for(Card c : shoe)
            cardTypeCounts13[c.playerType()]++;
        return cardTypeCounts13;
    }

    /**
     *
     */
    public int cardTypeCount(int cardType)
    {
        int cardTypeCount = 0;
        for(Card c : shoe)
            if(c.playerType() == cardType)
                cardTypeCount++;
        return cardTypeCount;
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
	public Shoe clone()
    {
        Shoe clone = new Shoe();
        clone.shuffleType = shuffleType;
        clone.countShuffleCuts = countShuffleCuts;
        clone.countShuffleLoops = countShuffleLoops;
        clone.runningCounts = runningCounts.clone();
        clone.capacity = capacity;
        clone.shoe = (LinkedList<Card>)shoe.clone();
        clone.maxPenetration = maxPenetration;
        return clone;
    }




    /**
     *
     */
    public boolean contains(Card card)
    {
        if(card == null)
            throw new NullPointerException("Cannot search for Null card in shoe");
        return shoe.contains(card);
    }



    /**
     * Inserts a card at a random point in shoe.
     * Allows user to manually configure initial shoe contents.
     * Will not insert Card if shoe contains maximum number of that Card
     */
    public boolean insert(Card card)
    {
        //Get each cardType52 count
        int[] cardTypeCounts52 = new int[52];
        for(Card c : shoe)
            cardTypeCounts52[c.suit() * 13 + c.playerType()]++;

        //If shoe already full of specified Card
        if(cardTypeCounts52[card.suit() * 13 + card.playerType()] == capacity / 52)
            return false;
        else
        {
            shoe.add(rand.nextInt(shoe.size()), card);   //add Card at random position
            adjustRunningCounts(card, INSERT);
            return true;   
        }
    }

    /**
     *
     */
    public double maxPenetration()
    {
        return maxPenetration;
    }

    /**
     *
     */
    public double penetration()
    {
        return 1.0 - (double)shoe.size() / capacity;
    }

    /**
     * Doesn't consider that dealer may have confirmed not BlackJack
     */
    public double probability10Value()
    {
        int count10Vals = 0;
        for(Card c : shoe)
            if(c.value() == 10)
                count10Vals++;
        return (double)count10Vals / shoe.size();
    }

    /**
     * Doesn't consider that dealer may have confirmed not BlackJack
     */
    public double probabilityAce()
    {
        int countAces = 0;
        for(Card c : shoe)
            if(c.value() == 11)
                countAces++;
        return (double)countAces / shoe.size();
    }

    /**
     *
     */
    public double probabilityBlackJack()
    {
        int count10Vals = 0, countAces = 0;
        for(Card c : shoe)
            if(c.value() == 10)
                count10Vals++;
            else if(c.value() == 11)
                countAces++;

        return 2.0 *                                             //can be [X][A] or [A][X]
                ((double)count10Vals / shoe.size()) * 
                ((double)countAces / (shoe.size() - 1));
    }

    /**
     * Randomly selects a suit of the specfied card type from the shoe.
     * Counts how many of each suit are present for specified card type. Then selects a suit
     * with a likelihood in proportion to relative counts.
     * @param direction either INSERT or REMOVE.
     */
    public int probableSuit(int playerCardType, int direction)
    {
        int[] countSuits = new int[Card.COUNT_CARD_SUITS];     //how many of each suit of this type
        int countPlayerCardType = 0;                           //how many of this type eg 3*Ace
        for(Card c : shoe)
        {
            if(c.playerType() == playerCardType)
            {
                countPlayerCardType++;
                switch(c.suit())
                {
                    case Card.CLUBS:
                        countSuits[Card.CLUBS]++;
                        break;
                    case Card.DIAMONDS:
                        countSuits[Card.DIAMONDS]++;
                        break;
                    case Card.HEARTS:
                        countSuits[Card.HEARTS]++;
                        break;
                    case Card.SPADES:
                        countSuits[Card.SPADES]++;                        
                }
            }
        }
        if(direction == REMOVE)
        {
            if(countPlayerCardType == 0)
                throw new IllegalStateException("No Cards of specifed type remain in shoe");
    
            int randomIdx = rand.nextInt(countPlayerCardType);        //pick index at random
            for(int suit = 0; suit < Card.COUNT_CARD_SUITS; suit++)   //get suit at this index
            {
                if(randomIdx < countSuits[suit])
                    return suit;
                else
                    randomIdx -= countSuits[suit];
            }
            throw new IllegalStateException("Suit should already be chosen: " + randomIdx);
        }
        else //(direction == INSERT)   //we must invert the counts
        {
            if(countPlayerCardType == capacity / 13)
                throw new IllegalStateException("Shoe already full of specified Card Type");
            int maxCountSuit = capacity / 52;
            for(int suit = Card.CLUBS; suit <= Card.SPADES; suit++)
                countSuits[suit] = maxCountSuit - countSuits[suit];
            int randomIdx = rand.nextInt(capacity / 13 - countPlayerCardType);//pick index at random
            for(int suit = 0; suit < Card.COUNT_CARD_SUITS; suit++)        //get suit at this index
            {
                if(randomIdx < countSuits[suit])
                    return suit;
                else
                    randomIdx -= countSuits[suit];
            }
            throw new IllegalStateException("Suit should already be chosen: " + randomIdx);
        }
    }

    /**
     * Fill shoe up to its capacity with Cards
     */
    public void refill()
    {
        shoe = new LinkedList<Card>();
        for(int deck = 0; deck < capacity / 52; deck++)
            for(int suit = 0; suit < Card.COUNT_CARD_SUITS; suit++)
                for(int type = 0; type < Card.COUNT_PLAYER_CARD_TYPES; type++)
                    shoe.add(new Card(type, suit));

        resetRunningCounts();   
    }

    /**
     *
     */
    public boolean remove(Card card)
    {
        boolean cardRemoved = shoe.remove(card);
        if(cardRemoved)
        {
            adjustRunningCounts(card, REMOVE);
            return true;
        }
        else
            return false;
    }

    /**
     * Returns the next card from the shoe. Does not remove it.
     */
    public Card next()
    {
        if(shoe.size() == 0)
            throw new IllegalStateException("Cannot remove next Card. Shoe is empty");
        return shoe.get(0);    
    }

    /**
     *
     */
    public void resetRunningCounts()
    {
        for(int countStrat = 0; countStrat < runningCounts.length; countStrat++)
            runningCounts[countStrat] = 0.0;
    }

    /**
     *
     */
    public double runningCount(int countStrategy)
    {
        return runningCounts[countStrategy];
    }

    /**
     *
     */
    public void setCapacity(int capacity)
    {
        if(capacity % 52 != 0)
            throw new IllegalArgumentException("Shoe capacity must be multiple of 52: " + capacity);
        this.capacity = capacity;
        refill();                 //otherwise would invalidate running counts and statistics
    }



    /**
     *
     */
    public void setMaxPenetration(double maxPenetration)
    {
        if(maxPenetration < 0.0 || maxPenetration > 1.0)
            throw new IllegalArgumentException("Invalid maxPenetration: " + maxPenetration);

        this.maxPenetration = maxPenetration;
    }

    /**
     *
     */
    public void setShuffleType(int shuffleType)
    {
        if(shuffleType != SHUFFLE_TYPE_NONE && shuffleType != SHUFFLE_TYPE_PSEUDORANDOM &&
                shuffleType != SHUFFLE_TYPE_CUT_SPLIT_MERGE_LOOP)
            throw new IllegalArgumentException("Unknown shuffle type: " + shuffleType);

        this.shuffleType = shuffleType;
    }

    /**
     *
     */
    public void shuffle()
    {
        switch(shuffleType)
        {
            case SHUFFLE_TYPE_PSEUDORANDOM:
                ArrayList<Card> tmpArray = new ArrayList<Card>(shoe.size());
                for(Card c : shoe)
                    tmpArray.add(c);
                for(int idx = 1; idx < tmpArray.size(); idx++)
                {
                    Card tmp = tmpArray.get(idx);
                    int swapIdx = rand.nextInt(idx + 1);
                    tmpArray.set(idx, tmpArray.get(swapIdx));
                    tmpArray.set(swapIdx, tmp);
                }
                shoe.clear();
                for(Card c : tmpArray)
                    shoe.addLast(c);
                break;
            case SHUFFLE_TYPE_CUT_SPLIT_MERGE_LOOP:
                LinkedList tmpShoe = shoe;                     //note the lack of parameterised type
                List[] shoeCuts = new List[countShuffleCuts];  //holds shoe cuts
                double cutSize = shoe.size() / (double)countShuffleCuts;
                int start;                                                 //start of cut
                int end;                                                   //end of cut
                int middle = (int)Math.round(shoe.size() / 2.0);           //middle of shoe
                int idx;                                                   //current shoe index
                ArrayList left = new ArrayList(), right = new ArrayList(); //so we can split & merge
                ListIterator itrLeft, itrRight;                            //to merge halves,no type

                for(int n = 0; n < countShuffleLoops; n++)                 //for each shuffle loop
                {
                    start = 0;                                             //start shoe idx of this cut
                    for(int cut = 1; cut <= countShuffleCuts; cut++)        //for each cut of shoe
                    {
                        end = (int)Math.round(cut * cutSize);
                        if(end > start)
                        {
                            shoeCuts[cut-1] = tmpShoe.subList(start, end);
                            start = end;
                        }
                        else
                            shoeCuts[cut-1] = null;
                    }
                    idx = 0;
                    left.clear();
                    right.clear();
                    for(int cut = countShuffleCuts - 1; cut >= 0; cut--)
                        if(shoeCuts[cut] != null)
                            for(Object o : shoeCuts[cut])
                                if(idx++ < middle)
                                    left.add(o);
                                else
                                    right.add(o);
                    tmpShoe.clear();
                    itrLeft = left.listIterator();
                    itrRight = right.listIterator();
                    while(itrLeft.hasNext() && itrRight.hasNext())
                        if(rand.nextInt(2) == 0)
                            tmpShoe.add(itrLeft.next());
                        else
                            tmpShoe.add(itrRight.next());
                    while(itrLeft.hasNext())
                        tmpShoe.add(itrLeft.next());
                    while(itrRight.hasNext())
                        tmpShoe.add(itrRight.next());
                }
                shoe = (LinkedList<Card>)tmpShoe;
        }
    }

    /**
     *
     */
    public int size()
    {
        return shoe.size();
    }

    /**
     *
     */
    public double trueCount(int countStrategy)
    {
        return runningCounts[countStrategy] / ((double)shoe.size() / 52);
    }

    /**
     *
     */
    private void adjustRunningCounts(Card card, int direction)
    {
        for(int countStrat = 0; countStrat < runningCounts.length; countStrat++)
            if(direction == REMOVE)
                runningCounts[countStrat] += CountStrategy.getAdjustment(countStrat, card);
            else if(direction == INSERT)
                runningCounts[countStrat] -= CountStrategy.getAdjustment(countStrat, card);
    }


}







