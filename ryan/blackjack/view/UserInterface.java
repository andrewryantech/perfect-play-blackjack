//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
package ryan.blackjack.view;

import ryan.blackjack.domain.BetStrategy;
import ryan.blackjack.domain.Card;
import ryan.blackjack.domain.Hand;
import ryan.blackjack.domain.Player;
import ryan.blackjack.domain.Option;
import ryan.blackjack.domain.Settings;
import ryan.blackjack.domain.Shoe;
import ryan.blackjack.domain.Recommendations;

import java.util.ArrayList;

/**
 * User Interface interface
 */
public interface UserInterface
{

    //----------------------------------------- Constants ------------------------------------------
    public static final int INDEX_FINAL_CARD = -1;         //for double down, request idx=final card

    //-------------------------------------- Class Attributes --------------------------------------

    //-------------------------------------- Class Behaviour ---------------------------------------

    //------------------------------------------ Defaults ------------------------------------------


    //------------------------------------ Instance Attributes -------------------------------------

    //------------------------------------- Instance Behaviour -------------------------------------


    /**
     * Displays count of Aces seen, Aces left, shoe size, and standard and current Ace concentration
     * @param shoe the shoe
     */
    public void displayAceConcentration(Shoe shoe);

    /**
     * Displays the bank balance of the specified player
     * @param pIdx which player
     * @return bank balance of this player
     */
    public void displayBank(int pIdx, double bank);


    /**
     * Displays a list of BetStrategies. 
     * A single list displayed, then one getChoice() for each player
     */
    public void displayBetStrategies();

    /**
     * Displays the probability Dealer will be dealt 10-value given Dealer Hand is single Ace
     * eg "{X,J,Q,K} dealt: 1, remaining: 2 / 9. Std conc.: 7.69%  Curr conc.: 8.32%  Var:  8.22%
     * @param shoe the shoe from which we derive probability next card will have value=10
     */
    public void displayChanceDealerBlackJack(Shoe shoe);

    /**
     * Displays the relative probability that a player will be dealt blackJack.
     * @param shoe the Shoe
     */
    public void displayChancePlayerBlackJack(Shoe shoe);

    /**
     * Displays a list of displayable CountStrategies and their current Running and True Counts
     * @param shoe the Shoe
     * @param settings current Game Settings
     */
    public void displayCountStatistics(Shoe shoe, Settings settings);

    /**
     * Displays a list of CountStrategies. 
     * A single list displayed, then one getChoice() for each player
     */
    public void displayCountStrategies();

    /**
     * Displays a message indicating the dealer's advantage at this point in the game
     * @param settings current system settings
     * @param shoe current system shoe prior to any cards being dealt
     * @param recommendations encapsulates insurance choices and best Option for each initial deal
     * @param playerAdvantage Player advantage for current state. -ve value indicates dealer adv.
     */
    public void displayPlayerAdvantage(Settings settings, Shoe shoe, 
            Recommendations recommendations, double playerAdvantage);

    /**
     * Displays a message indicating dealer is BlackJack therefore player's non-BJ hand loses
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param bet the bet for this hand
     */
    public void displayDealerBlackJack(int pIdx,int hIdx, double bet);

    /**
     * Displays a message indicating dealer is bust therefore player's non-blackjack hand wins
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param bet the bet for this hand
     */
    public void displayDealerBust(int pIdx,int hIdx, double bet);


    /**
     * Displays a message indicating if the dealer has BlackJack. This result
     * determines whether players win or lose their insurance bets
     * @param dealerHasBJ whether the dealer has BlackJack
     */
    public void displayDealerInsureanceResult(boolean dealerHasBJ);


    /**
     * Displays an error indicating user tried to give dealer a card that would make them BlackJack
     * after dealer has peeked and confirmed they are not blackjack. 
     * Eg, trying to add a 10val to a single Ace, or a Ace to a single10val
     * @param card the card that couldn't be inserted and caused the error
     */
    public void displayErrorPeekViolation(Card card);



    /**
     * Displays an error indicating the shoe is empty and no more cards are available
     */
    public void displayErrorShoeEmpty();

    /**
     * Display that the specified Player has won with a Five Card 21. Paid out immediately.
     * @param totalWinnings bet plus fiveCard21 amount
     */
    public void displayFiveCard21Win(double totalWinnings);

    /**
     * Display that the specified Player has won with a Five Card Charlie. Paid out immediately
     * @param bet the bet for this hand
     */
    public void displayFiveCardCharlieWin(double bet);

    /**
     * Display statistics and results for each player after all rounds are completed
     * @param countRounds how many rounds were played during game
     * @param players an ArrayList<Player> of all players who played the game
     * @param settings the current system settings
     */
    public void displayGameStatistics(int cntRounds, ArrayList<Player> players, Settings settings);

    /**
     * Displays a hand. instanceof will be used to determine if player or dealer hand
     * @param hand the hand
     */
    public void displayHand(Hand hand);


    /**
     * Display that the current Player has bust and has lost this hand.
     * Displayed immediately
     * @param bet the bet for this hand
     */
    public void displayHandBust(double bet);


    /**
     * Display that the current Player has redeemed this BlackJack hand for even money
     * Displayed and paid out immediately
     * @param bet the bet for this hand
     */
    public void displayHandRedeemed(double bet);

    /**
     * Display that the current Player has surrendered this hand
     * @param hIdx the player's hand
     * @param halfBet half the bet for this hand
     */
    public void displayHandSurrendered(double bet);


    /**
     * Display that the specified player has placed an initial bet
     * @param pIdx the player concerned
     * @param bet the bet for this hand
     */
    public void displayInitialBet(int pIdx, double bet);

    /**
     * Display how many rounds have been played at this table
     * @param countRounds how many rounds have been played
     */
    public void displayNewRound(int countRounds);


    /**
     * Display dealer's offer of even money for this player.
     * @param pIdx the player the offer is made to
     */
    public void displayOfferEvenMoney();

    /**
     * Display dealer's offer of insurance. Offer is made to all players who respond indiviually
     */
    public void displayOfferInsurance();

    /**
     * Display a list of Options to the User, ordered best first
     * User Interface does NOT need to maintain list of displayed Options, that's done by
     * GameLogic
     * @param options an array of Options
     */
    public void displayOptions(ArrayList<Option> options);


    /**
     * Display that the specified Player has won with a BlackJack. Displayed at round completion
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param winnings the amount won from this hand
     */
    public void displayPlayerBlackJack(int pIdx, int hIdx, double winnings);

    /**
     * Display that the specified Player's Hand has drawn with the Dealer. 
     * Displayed at round completion
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param pScore the player's hand's score
     * @param dScore the dealer's score
     * @param bet the bet for this hand
     */
    public void displayPlayerDraws(int pIdx, int hIdx, int pScore, int dScore, double bet);




    /**
     * Display the result of the specified Player's insurance wager
     * @param pIdx the player concerned
     * @param pWin whether the player has won or lost their insurance wager
     * @param insurance the insurance bet size
     */
    public void displayPlayerInsuranceResult(int pIdx, boolean pWin, double insurance);

    /**
     * Display that the specified Player's hand has lost
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param pScore the player's hand's score
     * @param dScore the dealer's score
     * @param bet the bet for this hand
     */
    public void displayPlayerLoses(int pIdx, int hIdx, int pScore,int dScore, double bet);

    /**
     * Display that the specified Player's hand has won
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param pScore the player's hand's score
     * @param dScore the dealer's score
     * @param bet the bet for this hand
     */
    public void displayPlayerWins(int pIdx, int hIdx, int pScore, int dScore, double bet);



    /**
     * Displays the Shoe's current contents
     * @param shoe the Shoe
     */
    public void displayShoe(Shoe shoe);


    /**
     * Gets a bet for the next round for the specified player
     * @param pIdx the player concerned
     * @param min minimum bet allowed
     * @param max maximum bet allowed
     * @return bet for this player
     */
    public double getBet(int pIdx, double min, double max);

    /**
     * Gets a BetStrategy for the specified player
     * @param pIdx the player concerned
     * @return a BetStrategy for the specified player
     */
    public BetStrategy getBetStrategy(int pIdx);


    /**
     * Gets a Card to be placed at the specified index in a Hand
     * @param cIdx the index where the card will be placed in a Hand
     * @param shoe so method can ensure shoe contains a card, or deal next Card from shoe
     * @return a Card selected by user, or dealt from shoe
     */
    public Card getCard(int cIdx, Shoe shoe);



    /**
     * Gets the index of an Option currently being displayed
     * If state=USER_SPECIFIED || RANDOMLY_DEALT, user indicates an Option index
     * If state=AUTO_PLAY, UserInterface returns the best currently available Option, ie idx == 0
     * Can also be used to get a CountStrategy and BetStrategy, just temporarily set UI state to 
     * USER_SPECIFIED
     * @param min the minimum valid choice
     * @param max the maximum valid choice
     * @return chosen Option's index
     */
    public int getChoice(int min, int max);

    /**
     * Gets the index of an Option currently being displayed, for a specified player
     * If state=USER_SPECIFIED || RANDOMLY_DEALT, user indicates an Option index
     * If state=AUTO_PLAY, UserInterface returns the best currently available Option, ie idx == 0
     * Can also be used to get a CountStrategy and BetStrategy, just temporarily set UI state to 
     * USER_SPECIFIED
     * @param pIdx the player concerned
     * @param min the minimum valid choice
     * @param max the maximum valid choice
     * @return chosen Option's index
     */
    public int getChoice(int pIdx, int min, int max);


    /**
     * Gets whether user wishes to continue Game, (ie play next Round)
     * @return whether user wishes to continue Game, (ie play next Round)
     */
    public boolean getChoiceContinueGame();


    /**
     * Gets whether the user wishes to reset the shoe. 
     * On RANDOMLY_DEALT games, this will depened on whether penetration > maxPenetration
     * @param shoe the shoe
     * @return whether to refill and shuffle the shoe
     */
    public boolean getChoiceResetShoe(Shoe shoe);

    /**
     * Gets the user to indicate how many players are at the table
     * @param minPlayers minimum number of players at a table
     * @param maxPlayers maximum number of players at a table
     * @return number of players playing at table
     */
    public int getCountPlayers(int minPlayers, int maxPlayers);

    /**
     * Gets a CountStrategy for the specified player
     * @param pIdx the player concerned
     * @return the int representation of a countStrategy
     */
    public int getCountStrategy(int pIdx);

    /**
     * Gets whether the dealer has BlackJack. 
     * Only called if dealer has single 10val or Ace, and dealer dealt hole card.
     * @param isBlackJack whether dealer's peek at hole card reveals blackJack
     * @return whether dealer's peek at hole card reveals blackJack
     */
    public boolean getDealerPeekRevealsBlackJack(boolean isBlackJack);

    /**
     * Gets a the additional bet when double downing for current player
     * @param max maximum additonal bet allowed
     * @return additional bet for current player
     */
    public double getDoubleDownBet(double max);


    /**
     * Gets an insurance bet for the specified player
     * @param pIdx the player concerned
     * @param maxBet the maximum insurance wager permitted
     * @return insurance wager for this player
     */
    public double getInsuranceBet(int pIdx, double maxInsurance);

    /**
     * Gets the user to input the specified player's starting bank
     * @param pIdx the player concerned
     * @return starting bank for this player
     */
    public double getPlayerBank(int pIdx);

    /**
     * Sets focus on dealer
     */
    public void setFocusDealer();

    /**
     * Sets focus on a Player's Hand
     * @param hIdx the index of the Player's Hand we are now dealing with
     */
    public void setFocusHand(int hIdx);


    /**
     * Sets focus on a specified Player
     * @param pIdx the index of the Player we are now dealing with
     */
    public void setFocusPlayer(int pIdx);


    /**
     * Sets the current gameType
     * USER_SPECIFIED: requests for cards will require user to manually input
     * !USER_SPECIFIED: requests for cards will trigger random selection from Shoe
     * AUTO_PLAY: best Option automatically chosen and returned
     * !AUTO_PLAY: chosen Option selected by user
     * @param gameType current game type
     */
    public void setGameType(int gameType);

    /**
     * Starts a 2nd thread to monitor whether user wishes to continue Auto Play Game simulation.
     * For CommandLine, this will be a 2nd thread blocked waiting for input.
     * For a GUI, this will simply be the UI thread linked to a button, for example.
     */
    public void startAutoPlayMonitor();



}