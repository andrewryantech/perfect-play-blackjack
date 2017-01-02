//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  
package ryan.blackjack.domain;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeMap;

/**
 * Player class.
 * A player can go into debt at a table (ie. negative bank), this may modify their bet strategy
 */
public final class Player
{
    //----------------------------------------- Constants ------------------------------------------
    //------------------------------------------ Defaults ------------------------------------------

    //-------------------------------------- Class Attributes --------------------------------------

    //-------------------------------------- Class Behaviour ---------------------------------------

    //------------------------------------ Instance Attributes -------------------------------------
    private Bank                   bank;                     //Encapsulates initial, current, history etc
    private double                 totalBetsLaid;            //total laid on initial, split, & double
    private double                 totalBetsWinnings;        //DOES include returned bet if win
    private double                 totalInsuranceLaid;       //total insurance wagered
    private double                 totalInsuranceWinnings;   //DOES include returned wager if win
    private LinkedList<PlayerHand> hands;                    //list of this Player's PlayerHands
    private double                 insurance;                //current insurance, if any
    private int                    countStrategy;            //which CountStrategy this player uses
    private BetStrategy            betStrategy;              //method to determine initial bet
    private long                   countHandsSplit;          //how many hands split
    private long                   countHandsSurrendered;    //how many hands surrendered
    private long                   countHandsDoubleDowned;   //how many hands double downed



    //------------------------------------- Instance Behaviour -------------------------------------
    /**
     * Constructor
     * @param initialBank this Player's starting bank
     * @param betStrategy how this player determines initial bet size
     * @param countStrategy method used by this player to count cards
     */    
    public Player(double initialBank, int countStrategy, BetStrategy betStrategy)
    {
    	bank = new Bank(initialBank);
        totalBetsLaid = 0.0;
        totalBetsWinnings = 0.0;
        totalInsuranceLaid = 0.0;
        totalInsuranceWinnings = 0.0;
        hands = new LinkedList<PlayerHand>();
        insurance = 0.0;
        this.countStrategy = countStrategy;
        this.betStrategy = betStrategy;    
        if(betStrategy instanceof BetStrategyConstantRisk)   
            ((BetStrategyConstantRisk)betStrategy).setPlayer(this);
        countHandsSplit = countHandsSurrendered = countHandsDoubleDowned = 0;

    }


    /**
     * Returns this Player's current bank balance. Chips on active hands are not in Player Bank.
     * @return this Player's current bank balance
     */    
    public double bank()
    {
        return bank.currentBank();
    }

    /**
     * Returns this Player's BetStrategy
     * @return this Player's BetStrategy
     */    
    public BetStrategy betStrategy()
    {
        return betStrategy;
    }

    /**
     * Cancels this Player's insurance wager. Used when redeeming a Hand for even money.
     * Chips are simply returned to Player.  
     */    
    public void cancelInsurance()
    {
        bank.adjustBank(insurance, false);                  //return chips to player
        totalInsuranceWinnings += insurance;       //symmetrical with placing chips
        insurance = 0.0;                           //no insurance chips on table
    }

    /**
     * Returns the number of Hands this Player double downed
     * @return the number of Hands this Player double downed
     */    
    public long countHandsDoubleDowned()
    {
        return countHandsDoubleDowned;
    }

    /**
     * Returns the number of Hands this Player split
     * @return the number of Hands this Player split
     */    
    public long countHandsSplit()
    {
        return countHandsSplit;
    }

    /**
     * Returns the number of Hands this Player surrendered
     * @return the number of Hands this Player surrendered
     */    
    public long countHandsSurrendered()
    {
        return countHandsSurrendered;
    }

    /**
     * Returns this Player's Count Strategy
     * @return this Player's Count Strategy
     */    
    public int countStrategy()
    {
        return countStrategy;
    }


    /**
     * Instructs this Player to Double Down the specified Hand.
     * Player will take additional chips from bank and place on PlayerHand
     * @param pH the PlayerHand to double down
     * @param additionalBet additional chips to bet on this Hand
     * @param finalCard the final Card to add to this Hand
     */    
    public void doubleDownHand(PlayerHand pH, double additionalBet, Card finalCard)
    {
        bank.adjustBank(-additionalBet, false);                   //reduce player's chips
        
        totalBetsLaid += additionalBet;                 //register additional bet
        pH.doubleDown(additionalBet, finalCard);        //place chips on hand
        countHandsDoubleDowned++;
    }

    /**
     * Returns a ListIterator of this Player's Hands, pointing to first Hand
     * Caller can then iterate forwards and backwards over this player's Hands
     * @return a ListIterator of this Player's Hands, pointing to first Hand
     */    
    public ListIterator<PlayerHand> hands()
    {
        return hands.listIterator(0);
    }

    /**
     * Returns this Player's starting Bank
     * @return this Player's starting Bank
     */    
    public double initialBank()
    {
        return bank.initialBank();
    }


    /**
     * Returns this Player's current insurance wager
     * @return this Player's current insurance wager
     */    
    public double insurance()
    {
        return insurance;
    }


    /**
     * Instructs this Player that their insurance wager was lost. Chips forfeited.
     */    
    public void loseInsurance()
    {
        insurance = 0.0;                               //chips transferred to dealer
    }

    /**
     * Returns this Player's maximum bank level during game
     * @return this Player's maximum bank level during game
     */    
    public double maxBank()
    {
        return bank.maxBank();
    }

    /**
     * Returns this Player's minimum bank level during game
     * @return this Player's minimum bank level during game
     */    
    public double minBank()
    {
        return bank.minBank();
    }


    /**
     * Clears Player's old Hands. Creates a new PlayerHand with initial bet.
     * Updates Player's bank and betting history
     */    
    public double placeInitialBet(Shoe shoe)
    {
        double initialBet = betStrategy.getBet(shoe.trueCount(countStrategy));
        bank.adjustBank(-initialBet, true);                     //reduce player's chips
        totalBetsLaid += initialBet;                   //register bet

        hands.clear();
        hands.add(new PlayerHand(initialBet));         //place chips on new PlayerHand
        return initialBet;
    }

    /**
     * Instructs Player to treat this Hand as a push(draw). Returns original bet to bank
     * GameLogic must settle bets so results can be displayed to user
     * @param pH the PlayerHand that has beaten the DealerHand
     */    
    public void pushHand(PlayerHand pH)
    {
        bank.adjustBank(pH.bet(), false);                    //add original bet back to Player's bank
        totalBetsWinnings += pH.bet();              //register return of bet;
    }


    /**
     * Used when Player redeems a Hand for even money, or a 5CardCharlie, or a 5Card21.
     * Any winnings are paid out immediately and the Hand is not considered upon Bet Settlement
     * @param pH the PlayerHand that will no longer be active for Bet Settlement
     * @return amount bet on this PlayerHand
     */    
    public double redeemHand(PlayerHand pH)
    {
        double winnings = pH.redeem();               //get amount bet on this Hand
        
        bank.adjustBank(winnings, false);                     //adds Hand's winnings to bank
        bank.adjustBank(winnings, false);                     //returns original bet to bank
        totalBetsWinnings += winnings * 2;           //register win
      
        return winnings;                             //so won amount can be displayed to user
    }

    /**
     * Splits the specified player's hand, decrements Player's bank
     * @return cloned single-card PlayerHand
     */    
    public PlayerHand splitHand(PlayerHand  pH)
    {
        double additionalBet = pH.bet();
        bank.adjustBank(-additionalBet, false);
        totalBetsLaid += additionalBet;
        countHandsSplit++;    

        //so caller(ie, GameLogic) can insert split hand into hands without invalidating iterator
        return pH.split(); 
    }



    /**
     * Surrenders this Player's sole hand
     * @return portion of bet that the dealer returns
     */    
    public double surrenderHand()
    {
        double betPortionReturned = hands.get(0).surrender();    //get half this Hand's bet 
        bank.adjustBank(betPortionReturned, false);                       //add returned chips to bank
        totalBetsWinnings += betPortionReturned;                 //register return of chips
        countHandsSurrendered++;

        return betPortionReturned;        //so returned bet portion amount can be displayed to user
    }


    /**
     * Makes an insurance wager for this Player
     * @param insurance the insurance wager to make for this player
     */    
    public void takeInsurance(double insurance)
    {
        bank.adjustBank(-insurance, false);                           //take chips from player
        totalInsuranceLaid += insurance;                    //register insurance wager
        this.insurance = insurance;                         //put chips on table
    }

    /**
     * Returns this Player's total Bets Laid during game
     * @return this Player's total Bets Laid during game
     */    
    public double totalBetsLaid()
    {
        return totalBetsLaid;
    }

    /**
     * Returns this Player's total Bets Winnings during game
     * @return this Player's total Bets Winnings during game
     */    
    public double totalBetsWinnings()
    {
        return totalBetsWinnings;
    }

    /**
     * Returns this Player's total Insurance Laid during game
     * @return this Player's total Insurance Laid during game
     */    
    public double totalInsuranceLaid()
    {
        return totalInsuranceLaid;
    }

    /**
     * Returns this Player's total Insurance Winnings during game
     * @return this Player's total Insurance Winnings during game
     */    
    public double totalInsuranceWinnings()
    {
        return totalInsuranceWinnings;
    }


    /**
     * Register that this player has won a Five-Card-21
     * @param fiveCard21Amount the amount this Player has won for getting a Five-Card-21
     */    
    public void winFiveCard21(double fiveCard21Amount)
    {
        bank.adjustBank(fiveCard21Amount, false);                       //add chips to player's bank
        totalBetsWinnings += fiveCard21Amount;                 //register win
    }


    /**
     * Instructs Player to treat this Hand as won. Adds original bet and winnings back to bank.
     * Amount won depends on whether hand is BlackJack, SplitBlackJack, or normal win
     * GameLogic must settle bets so results can be displayed to user
     * @param pH the PlayerHand that has beaten the DealerHand
     * @param settings the current Settings for the game
     */    
    public void winHand(PlayerHand pH, Settings settings)
    {
        double winnings;

        if(pH.isBlackJack())
            if(pH.splitLevel() == 0)
                winnings = pH.bet() * settings.blackJackROI();
            else
                winnings = pH.bet() * settings.splitBlackJackROI();
        else
            winnings = pH.bet();

        bank.adjustBank(winnings, false);                    //add winnings to Player's bank
        bank.adjustBank(pH.bet(), false);                    //add original bet back to Player's bank
        totalBetsWinnings += winnings + pH.bet();   //register win;
    }


    /**
     * Informs this Player that they have won their insurance wager. Original and won chips 
     * added to bank.
     */    
    public void winInsurance()
    {
    	bank.adjustBank(insurance * 3, false);                 //add insurance winnings to bank
    	bank.adjustBank(insurance, false);                     //receive original insurance wager back
        totalInsuranceWinnings += insurance * 4;      //register insurance win
        insurance = 0.0;
    }
    
    /**
     * Returns a copy of this player's bank history
     * @return
     */
    public TreeMap<Integer,Double> bankHistory()
    {
    	return bank.bankHistory();
    }
}








