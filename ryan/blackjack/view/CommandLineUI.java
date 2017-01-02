//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
package ryan.blackjack.view;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import ryan.blackjack.domain.BetStrategy;
import ryan.blackjack.domain.BetStrategyConstantRisk;
import ryan.blackjack.domain.BetStrategyDiscreteBinary;
import ryan.blackjack.domain.BetStrategyFlat;
import ryan.blackjack.domain.BetStrategyRamped;
import ryan.blackjack.domain.BetStrategyUserSpecified;
import ryan.blackjack.domain.Card;
import ryan.blackjack.domain.CountStrategy;
import ryan.blackjack.domain.DealerHand;
import ryan.blackjack.domain.GameLogic;
import ryan.blackjack.domain.Hand;
import ryan.blackjack.domain.Option;
import ryan.blackjack.domain.Player;
import ryan.blackjack.domain.PlayerHand;
import ryan.blackjack.domain.Recommendations;
import ryan.blackjack.domain.Settings;
import ryan.blackjack.domain.Shoe;

/**
 * Text-based UI class
 */
public final class CommandLineUI implements UserInterface
{

    //----------------------------------------- Constants ------------------------------------------
    public static final char[] SUIT_CHARS = {(char)5, (char)4, (char)3, (char)6};//{Clb,Dmd,Hrt,Spd}
    public static final int INVALID = -1;

    //-------------------------------------- Class Attributes --------------------------------------

    //-------------------------------------- Class Behaviour ---------------------------------------
    /**
     * Program starts here
     * @param args Command line arguments
     */
    public static void main(String[] args)
    {
        new CommandLineUI().run();                        // Instantiates system objects
    }

    //------------------------------------------ Defaults ------------------------------------------

    //------------------------------------ Instance Attributes -------------------------------------
    private Scanner   console;                              // to get user input
    private GameLogic gL;                                   // contains the game logic
    private int       gameType;                             // either UsrSpcf, RndDlt, or AutoPlay
    private int       statIntroLength = 36;                 // usual length of Ace cnc. intro String
                                                            //  used for display formatting only
    private DecimalFormat fmt2;                             // Rounds double to 2 decimal places

   

    //---------------------------------- Public Instance Behaviour ---------------------------------

    /**
     * Constructor. Instantiates instance variables.
     */
    public CommandLineUI()
    {
        console = new Scanner(System.in);
        gL = new GameLogic(this);
        fmt2 = new DecimalFormat("0.00");
    }

    /**
     * Displays count of Aces seen, Aces left, shoe size, and standard and current Ace concentration
     * eg "Aces dealt: 4, remaining: 28 / 312. Std: 7.692%  Crr: 8.324%  Variance: 8.221%"
     * @param shoe the shoe
     */
    @Override
    public void displayAceConcentration(Shoe shoe)
    {
        int acesRemaining = shoe.cardTypeCount(Card.PLAYER_ACE);
        int acesDealt = shoe.capacity() / 13 - acesRemaining;
        double currentAceConcentration = 100.0 * acesRemaining / shoe.size();
        double variance = currentAceConcentration / Shoe.STANDARD_ACE_CONCENTRATION - 100.0;

        String intro = "Aces dealt: " + acesDealt + ", " +
                "remaining: " + acesRemaining + " / " + shoe.size() + ". ";
        statIntroLength = intro.length();     //used so we can align BlackJack stats
        String std = Double.toString(Shoe.STANDARD_ACE_CONCENTRATION * 100);
        if(std.length() > 5)
            std = std.substring(0, 5);
        String crr = Double.toString(currentAceConcentration);
        if(crr.length() > 5)
            crr = crr.substring(0, 5);
        String var = Double.toString(variance);
        if(var.length() > 5)
            if(var.charAt(0) == '-')
                var = var.substring(0, 6);
            else
                var = " " + var.substring(0, 5);

        System.out.print(intro);
        System.out.printf("Std: %5s%%  ", std);
        System.out.printf("Crr: %5s%%  ", crr);
        System.out.printf("Variance:%6s%%\n", var);
    }

    /**
     * Displays the bank balance of the specified player
     * eg "Player#1 Bank: $100"
     * @param pIdx which player
     * @return bank balance of this player
     */
    @Override
    public void displayBank(int pIdx, double bank)
    {
        System.out.println("Player#" + (pIdx + 1) + " Bank: $" + fmt2.format(bank));
    }


    /**
     * Displays a list of BetStrategies. 
     * A single list displayed, then one getChoice() for each player
     */
    @Override
    public void displayBetStrategies()
    {
        System.out.println("\nAvailable Bet Strategies:");
        System.out.println(BetStrategy.USER_SPECIFIED + ". User Specified");
        System.out.println(BetStrategy.FLAT + ". Flat");
        System.out.println(BetStrategy.RAMPED + ". Ramped");
        System.out.println(BetStrategy.DISCRETE_BINARY + ". Discrete Binary");
        System.out.println(BetStrategy.CONSTANT_RISK + ". Constant Risk of Ruin");
    }

    /**
     * Displays the probability Dealer will be dealt 10-value given Dealer Hand is single Ace
     * eg "{X,J,Q,K} dealt: 1, remaining: 2 / 9. Std conc.: 7.69%  Curr conc.: 8.32%  Var:  8.22%
     * @param shoe the shoe from which we derive probability next card will have value=10
     */
    @Override
    public void displayChanceDealerBlackJack(Shoe shoe)
    {
        
        int tensRemainging = shoe.cardTypeCount(Card.TEN) + shoe.cardTypeCount(Card.JACK) +
                shoe.cardTypeCount(Card.QUEEN) + shoe.cardTypeCount(Card.KING);
        int tensDealt = shoe.capacity() * 4 / 13 - tensRemainging;
        double curr10ValConc = (double)tensRemainging / shoe.size();
        double variance = curr10ValConc / Shoe.STANDARD_TEN_VAL_CONCENTRATION;

        String std = Double.toString(Shoe.STANDARD_TEN_VAL_CONCENTRATION * 100);
        if(std.length() > 5)
            std = std.substring(0, 5);
        String crr = Double.toString(curr10ValConc);
        if(crr.length() > 5)
            crr = crr.substring(0, 5);
        String var = Double.toString(variance);
        if(var.length() > 5)
            if(var.charAt(0) == '-')
                var = var.substring(0, 6);
            else
                var = " " + var.substring(0, 5);

        System.out.print("{X,J,Q,K} dealt: " + tensDealt + ", ");
        System.out.print("remain: " + tensRemainging + " / " + shoe.size() + ". ");
        System.out.print("Std: " + std + "%  ");
        System.out.print("Crr: " + crr + "%  ");
        System.out.println("Variance:" + var + "%");

    }

    /**
     * Displays the relative probability that a player will be dealt blackJack.
     * eg "Player BlackJack Probability:  Std prob.: 5.128%  Curr prob.: 5.023%  Variance: -1.60%"
     * @param playerBJLikelyChance relative probability of player blackJack
     */
    @Override
    public void displayChancePlayerBlackJack(Shoe shoe)
    {
        int tensRmng = shoe.cardTypeCount(Card.TEN) + shoe.cardTypeCount(Card.JACK) +
                shoe.cardTypeCount(Card.QUEEN) + shoe.cardTypeCount(Card.KING);
        int acesRmng = shoe.cardTypeCount(Card.PLAYER_ACE);
        double stdProbBJ = 800.0 / 13.0 / 13.0 * shoe.capacity() / (shoe.capacity() - 1);
        double currProbBJ = 200.0 * tensRmng / shoe.size() * acesRmng / (shoe.size() - 1);
        currProbBJ = Math.max(currProbBJ, 0.0);
        double variance = (currProbBJ / stdProbBJ - 1.0) * 100.0;

        String intro = "Player BlackJack Probability:";
        String std = Double.toString(stdProbBJ);
        if(std.length() > 5)
            std = std.substring(0, 5);
        String crr = Double.toString(currProbBJ);
        if(crr.length() > 5)
            crr = crr.substring(0, 5);
        String var = Double.toString(variance);
        if(var.length() > 5)
            if(var.charAt(0) == '-')
                var = var.substring(0, 6);
            else
                var = " " + var.substring(0, 5);

        System.out.printf("%-" + statIntroLength + "s", intro);
        System.out.printf("Std: %5s%%  ", std);
        System.out.printf("Crr: %5s%%  ", crr);
        System.out.printf("Variance:%6s%%\n", var);
    }

    /**
     * Displays a list of displayable CountStrategies and their current Running and True Counts
     * Will only be called if at least one Count is displayable
     * @param shoe the Shoe
     * @param settings current Game Settings
     */
    @Override
    public void displayCountStatistics(Shoe shoe, Settings settings)
    {
        System.out.println("           Running Count  True Count");

        boolean[] displayableCountStrategies = settings.displayableCountStrategies();
        for(int countStrat = 0; countStrat < displayableCountStrategies.length; countStrat++)
            if(displayableCountStrategies[countStrat])
            {
                System.out.printf("%10s ", CountStrategy.getName(countStrat));
                System.out.printf("%13.3f  ", shoe.runningCount(countStrat));
                System.out.printf("%10.3f\n", shoe.trueCount(countStrat));
            }
    }


    /**
     * Displays a list of CountStrategies. 
     * A single list displayed, then one getChoice() for each player
     */
    @Override
    public void displayCountStrategies()
    {
        System.out.println("\nAvailable Count Strategies:");
        for(int cntStrt = CountStrategy.NONE; cntStrt <= CountStrategy.CUSTOM; cntStrt++)
        {
            System.out.printf("%2d. ", cntStrt);
            if(cntStrt == CountStrategy.NONE)
                System.out.println("None");
            else
                System.out.println(CountStrategy.getName(cntStrt));
        }
    }

    /**
     * Displays a message indicating the dealer's advantage at this point in the game
     * @param settings current system settings
     * @param shoe current system shoe prior to any cards being dealt
     * @param recommendations encapsulates insurance choices and best Option for each initial deal
     * @param playerAdvantage Player advantage for current state. -ve value indicates dealer adv.
     */
    @Override
    public void displayPlayerAdvantage(Settings settings, Shoe shoe, 
            Recommendations recommendations, double playerAdvantage)
    {
        Option bestOption;                  //best Option for a specific initial deal

        //Print current settings
        //TODO...

        displayShoe(shoe);

        //Print table headings
        System.out.println(lineBreak(' ', 46) + "DEALER FACE CARD");
        System.out.println(lineBreak(' ', 12) + lineBreak('-', 79));
        System.out.print("PLAYER HAND |");
        for(int cardType = Card.TWO; cardType < Card.PLAYER_ACE; cardType++)
            System.out.print(" [" + cardChar(cardType) + "] |");
        System.out.println(" [A] |");
        System.out.print(lineBreak('-', 91));

        //print the table of takeInsurance and bestOption values
        for(int pCard1 = Card.TWO; pCard1 <= Card.PLAYER_ACE; pCard1++)
        {
            for(int pCard2 = pCard1; pCard2 <= Card.PLAYER_ACE; pCard2++)
            {
                System.out.print("\n| [" + cardChar(pCard1) + "] | [" + cardChar(pCard2) + "] |");

                //for each column (ie dealer face card)
                for(int dCard1 = Card.TWO; dCard1 <= Card.PLAYER_ACE; dCard1++)
                {
                    //print whether to take insurance
                    if(recommendations.takeInsurance(dCard1, pCard1, pCard2))
                        System.out.print('*');
                    else
                        System.out.print('_');

                    //print recommended option
                    bestOption = recommendations.bestOption(dCard1, pCard1, pCard2);
                    if(bestOption == null)
                        System.out.print("____|");
                    else if(bestOption.name().equals(Option.STAND))
                        System.out.print("Stnd|");
                    else if(bestOption.name().equals(Option.HIT))
                        System.out.print("Hit_|");
                    else if(bestOption.name().equals(Option.SPLIT))
                        System.out.print("Splt|");
                    else if(bestOption.name().equals(Option.DOUBLE_DOWN))
                        System.out.print("Dbl_|");
                    else if(bestOption.name().equals(Option.SURRENDER))
                        System.out.print("Surr|");
                    else if(bestOption.name().equals(Option.ACCEPT_EVEN_MONEY))
                        System.out.print("Evn$|");
                    else
                        throw new IllegalArgumentException("Unknown option type: " + bestOption);
                }
            }
        }

        //print the table trailing info
        System.out.println("\n" + lineBreak('-', 91));
        System.out.println("|_Stnd| Stand");
        System.out.println("|_Hit_| Hit");
        System.out.println("|_Splt| Split");
        System.out.println("|_Dbl_| Double Down");
        System.out.println("|_Surr| Surrender");
        System.out.println("|_Evn$| Take Even Money");
        System.out.println("|*____| Take Insurance");
        System.out.println("Table only valid for current game settings and shoe state");

        //print total player/dealer advantage
        String who = playerAdvantage > 0 ? "Player" : "Dealer";
        System.out.print("Total " + who + " advantage at this point of game: ");
        System.out.printf("%7.5f%%", Math.abs(playerAdvantage * 100.0));
        System.out.println();
    }

    /**
     * Displays a message indicating dealer is BlackJack therefore player's non-BJ hand loses
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param bet the bet for this hand
     */
    @Override
    public void displayDealerBlackJack(int pIdx, int hIdx, double bet)
    {
        System.out.println("Player#" + (pIdx + 1) + " Hand# " + (hIdx + 1) + 
                ": Dealer has BlackJack. Player loses $" + fmt2.format(bet) + " bet");
    }

    /**
     * Displays a message indicating dealer is bust therefore player's non-blackjack hand wins
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param bet the bet for this hand
     */
    @Override
    public void displayDealerBust(int pIdx, int hIdx, double bet)
    {
        System.out.println("Player#" + (pIdx + 1) + " Hand# " + (hIdx + 1)+ ": Dealer is bust. " +
                "Player wins $" + fmt2.format(bet) + " bet");
    }

    /**
     * Displays a message indicating if the dealer has BlackJack. This result
     * determines whether players win or lose their insurance bets
     * @param dealerHasBJ whether the dealer has BlackJack
     */
    @Override
    public void displayDealerInsureanceResult(boolean dealerHasBJ)
    {
        if(dealerHasBJ)
            System.out.println("Insurance Bet: Dealer has BlackJack");
        else
            System.out.println("Insurance Bet: Dealer doesn't have BlackJack");
    }


    /**
     * Displays an error indicating user tried to give dealer a card that would make them BlackJack
     * after dealer has peeked and confirmed they are not blackjack. 
     * Eg, trying to add a 10val to a single Ace, or a Ace to a single10val
     * @param card the card that couldn't be inserted and caused the error
     */
    @Override
    public void displayErrorPeekViolation(Card card)
    {
        System.out.println("Peek Error Violation:\n" +
                 "[" + cardChar(card.playerType()) + SUIT_CHARS[card.suit()] + "] insertion would " +
                "cause BlackJack but Dealer peek confirmed no BlackJack");
    }


    /**
     * Displays an error indicating the shoe is empty and no more cards are available
     */
    @Override
    public void displayErrorShoeEmpty()
    {
        System.out.println("Error. Shoe is empty");
    }

    /**
     * Display that the specified Player has won with a Five Card 21. Paid out immediately.
     * @param totalWinnings bet plus fiveCard21 amount
     */
    @Override
    public void displayFiveCard21Win(double totalWinnings)
    {
        System.out.println("Player has a five-card-21 and wins a total of $" + 
                fmt2.format(totalWinnings));
    }


    /**
     * Display that the specified Player has won with a Five Card Charlie. Paid out immediately
     * @param bet the bet for this hand
     */
    @Override
    public void displayFiveCardCharlieWin(double bet)
    {
        System.out.println("Player has a five-card-charlie and wins $" + fmt2.format(bet));
    }

    /**
     * Display statistics and results for each player after all rounds are completed
     * @param countRounds how many rounds were played during game
     * @param players an ArrayList<Player> of all players who played the game 
     */
    @Override
    public void displayGameStatistics(int countRounds, ArrayList<Player> players, Settings settings)
    {
        System.out.println(lineBreak('-',28) + " Results and Statistics " + lineBreak('-',28));
        System.out.println("Total Game Rounds: " + countRounds);
       
        Player player;
        for(int pIdx = 0; pIdx < players.size(); pIdx++)
        {
            player = players.get(pIdx);
            System.out.println("\n--> Player#" + (pIdx + 1));
            System.out.print("     Count Strategy: ");
            System.out.printf("%15s\n", CountStrategy.getName(player.countStrategy()));
            System.out.print("       Bet Strategy: ");
            if(player.betStrategy() instanceof BetStrategyUserSpecified)
            {
                System.out.println("User-Specified");
            }
            else if(player.betStrategy() instanceof BetStrategyFlat)
            {
                System.out.println("           Flat");
                System.out.print("           Bet Size: ");
                System.out.printf("%15s\n", "$" + fmt2.format(player.betStrategy().getBet(0)));
            }
            else if(player.betStrategy() instanceof BetStrategyRamped)
            {
                System.out.println("         Ramped");
                System.out.print("True Count Threshld: ");
                System.out.printf("%15.1f\n", 
                        ((BetStrategyRamped)player.betStrategy()).trueCountThreshold());
                System.out.print("     Min Ramped Bet: ");
                System.out.printf("%15s\n", 
                        "$" + fmt2.format(((BetStrategyRamped)player.betStrategy()).minRampedBet()));
                System.out.print("        Ramp Factor: ");
                System.out.printf("%15.1f\n", 
                        ((BetStrategyRamped)player.betStrategy()).rampFactor());

            }
            else if(player.betStrategy() instanceof BetStrategyDiscreteBinary)
            {
                System.out.println("Discrete Binary");
                System.out.print("True Count Threshld: ");
                System.out.printf("%15.1f\n", 
                        ((BetStrategyDiscreteBinary)player.betStrategy()).trueCountThreshold());
                System.out.print("            Low Bet: ");
                System.out.printf("%15s\n", "$" + 
                        fmt2.format(((BetStrategyDiscreteBinary)player.betStrategy()).lowBet()));
                System.out.print("            Big Bet: ");
                System.out.printf("%15s\n", "$" + 
                        fmt2.format(((BetStrategyDiscreteBinary)player.betStrategy()).bigBet()));
            }
            else if(player.betStrategy() instanceof BetStrategyConstantRisk)
            {
                System.out.println("  Constant Risk");
                System.out.print("True Count Threshld: ");
                System.out.printf("%15.1f\n", 
                        ((BetStrategyConstantRisk)player.betStrategy()).trueCountThreshold());
                System.out.print("  Bank-to-Bet Ratio: ");
                System.out.printf("%15.1f\n",
                        ((BetStrategyConstantRisk)player.betStrategy()).bankToBetRatio());
            }
            System.out.println();

            System.out.print("  Hands Surrendered: ");
            System.out.printf("%15d\n", player.countHandsSurrendered());
            System.out.print("        Hands Split: ");
            System.out.printf("%15d\n", player.countHandsSplit());
            System.out.print("Hands Double Downed: ");
            System.out.printf("%15d\n", player.countHandsDoubleDowned());
            System.out.println();

            System.out.print("          Bets Laid: ");
            System.out.printf("%15s\n", "$" + fmt2.format(player.totalBetsLaid()));
            System.out.print("       Bet Winnings: ");
            System.out.printf("%15s\n", "$" + fmt2.format(player.totalBetsWinnings()));
            if(player.totalBetsWinnings() > player.totalBetsLaid())
                 System.out.print("         Bet Profit: ");
            else
                 System.out.print("           Bet Loss: ");
            System.out.printf("%15s\n", "$" + fmt2.format(player.totalBetsWinnings() - 
                    player.totalBetsLaid()));
            System.out.println();

            if(settings.dealerOffersInsurance())
            {
                System.out.print("     Insurance Laid: ");
                System.out.printf("%15s\n", "$" + fmt2.format(player.totalInsuranceLaid()));
                System.out.print(" Insurance Winnings: ");
                System.out.printf("%15s\n", "$" + fmt2.format(player.totalInsuranceWinnings()));
                if(player.totalInsuranceWinnings() > player.totalInsuranceLaid())
                     System.out.print("   Insurance Profit: ");
                else
                     System.out.print("     Insurance Loss: ");
                System.out.printf("%15s\n", "$" + fmt2.format(player.totalInsuranceWinnings() -
                        player.totalInsuranceLaid()));
                System.out.println();
            }

            System.out.print("         Start Bank: ");
            System.out.printf("%15s\n", "$" + fmt2.format(player.initialBank()));
            System.out.print("           Min Bank: ");
            System.out.printf("%15s\n", "$" + fmt2.format(player.minBank()));
            System.out.print("           Max Bank: ");
            System.out.printf("%15s\n", "$" + fmt2.format(player.maxBank()));
            System.out.print("           End Bank: ");
            System.out.printf("%15s\n", "$" + fmt2.format(player.bank()));
            if(player.bank() > player.initialBank())
                 System.out.print("       Total Profit: ");
            else
                 System.out.print("         Total Loss: ");
            System.out.printf("%15s\n", "$" + fmt2.format(player.bank() - 
                    player.initialBank()));
            
            if(settings.showBankHistory())
            {
            	NumberFormat fmt = NumberFormat.getCurrencyInstance();
            	TreeMap<Integer,Double> bankHistory = player.bankHistory();
            	for(Entry<Integer,Double> dataPoint : bankHistory.entrySet())
            	{
            		System.out.println(dataPoint.getKey() + ": " +  fmt.format(dataPoint.getValue()));
            	}
            }
            System.out.println();
        }

    }




    /**
     * Displays a hand. instanceof will be used to determine if player or dealer hand
     * Eg:
     * Dealer:  0 {}
     * Player:  0 {} SplitLevel: 1
     * Dealer: 14 {[A][A][A][A]}
     * Player: 14 {[A][A][A][A]} SplitLevel: 1
     * @param hand the hand
     */
    @Override
    public void displayHand(Hand hand)
    {
        if(hand instanceof DealerHand)
            System.out.print("Dealer: ");
        else
            System.out.print("Player: ");

        if(hand.score() <= 21)
            System.out.printf("%2d {", hand.score());
        else
            System.out.print(" B {");

        Card card;
        for(int cIdx = 0; cIdx < hand.countCards(); cIdx++)
        {
            card = hand.cardAt(cIdx);
            System.out.print("[" + cardChar(card.playerType()) + SUIT_CHARS[card.suit()] + "]");
        }

        System.out.print("}");

        if(hand instanceof DealerHand)
            System.out.println();
        else
            if(((PlayerHand)hand).splitLevel() > 0)
                System.out.println(" SplitLevel: " + ((PlayerHand)hand).splitLevel());
            else
                System.out.println();
    }


    /**
     * Display that the current Player has bust and has lost this hand.
     * Displayed immediately
     * @param bet the bet for this hand
     */
    @Override
    public void displayHandBust(double bet)
    {
        System.out.println("Player busts and loses $" + fmt2.format(bet));
    }


    /**
     * Display that the current Player has redeemed this BlackJack hand for even money
     * Displayed and paid out immediately
     * @param bet the bet for this hand
     */
    @Override
    public void displayHandRedeemed(double bet)
    {
        System.out.println("Player has taken even money. Player wins $" + fmt2.format(bet));
    }

    /**
     * Display that the current Player has surrendered this hand
     * @param hIdx the player's hand
     * @param halfBet half the bet for this hand
     */
    @Override
    public void displayHandSurrendered(double hlfBet)
    {
        System.out.println("Player surrenders hand and receives $" + fmt2.format(hlfBet) + " back");
    }

    /**
     * Display that the specified player has placed an initial bet
     * @param pIdx the player concerned
     * @param bet the bet for this hand
     */
    @Override
    public void displayInitialBet(int pIdx, double bet)
    {
        System.out.println("Player#" + (pIdx + 1) + " has placed a $" + fmt2.format(bet) + " bet");
    }

    /**
     * Display how many rounds have been played at this table
     * @param countRounds how many rounds have been played
     */
    @Override
    public void displayNewRound(int countRounds)
    {
        int cntDigits = (int)Math.floor(Math.log(countRounds) + 1); //how many digits in countRounds
        String dashedLine = lineBreak('-', (int)((81 - 9 - cntDigits) / 2.0));
        System.out.println(dashedLine + " Round #" + countRounds + " " + dashedLine);
    }


    /**
     * Display dealer's offer of even money for this player.
     * @param pIdx the player the offer is made to
     */
    @Override
    public void displayOfferEvenMoney()
    {
        System.out.println("Dealer is Ace. Take even money?");
    }

    /**
     * Display dealer's offer of insurance. Offer is made to all players who respond indiviually
     */
    @Override
    public void displayOfferInsurance()
    {
        System.out.println("Dealer has Ace. Take Insurance?");
    }

    /**
     * Display a list of Options to the User, ordered best first
     * User Interface does NOT need to maintain list of displayed Options, that's done by
     * GameLogic
     * @param options an array of Options, already sorted best to worst
     */
    @Override
    public void displayOptions(ArrayList<Option> options)
    {
        for(int i = 0; i < options.size(); i++)
        {
            System.out.printf("%2d. %17s (", i, options.get(i).name());
            if(options.get(i).roi() < 0)
                System.out.print('-');
            else if(options.get(i).roi() > 0)
                System.out.print('+');
            else
                System.out.print(' ');
            System.out.printf("%10.8f)\n", Math.abs(options.get(i).roi()));
        }
    }


    /**
     * Display that the specified Player has won with a BlackJack. Displayed at round completion
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param winnings the amount won from this hand
     */
    @Override
    public void displayPlayerBlackJack(int pIdx, int hIdx, double winnings)
    {
        System.out.println("Player#" + (pIdx + 1) + " Hand# " + (hIdx + 1) + ": Player is " +
                "BlackJack and Dealer isn't. Player wins $" + fmt2.format(winnings));

    }


    /**
     * Display that the specified Player's Hand has drawn with the Dealer. 
     * Displayed at round completion
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param pScore the player's hand's score
     * @param dScore the dealer's score
     * @param bet the bet for this hand
     */
    @Override
    public void displayPlayerDraws(int pIdx, int hIdx, int pScore, int dScore, double bet)
    {
        System.out.println("Player#" + (pIdx + 1) + " Hand# " + (hIdx + 1) + ": Player's " + 
                pScore + " pushes " + "Dealer's " + dScore + ". Player's $" + fmt2.format(bet) + 
                " bet returned");
    }




    /**
     * Display the result of the specified Player's insurance wager
     * @param pIdx the player concerned
     * @param pWin whether the player has won or lost their insurance wager
     * @param insurance the insurance bet size
     */
    @Override
    public void displayPlayerInsuranceResult(int pIdx, boolean pWin, double insurance)
    {
        System.out.print("Player#" + (pIdx + 1) + " ");
        if(pWin)
            System.out.println("wins $" + fmt2.format(insurance * 3.0) + " from insurance wager");
        else
            System.out.println("loses $" + fmt2.format(insurance) + " insurance wager");
    }

    /**
     * Display that the specified Player's hand has lost
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param pScore the player's hand's score
     * @param dScore the dealer's score
     * @param bet the bet for this hand
     */
    @Override
    public void displayPlayerLoses(int pIdx, int hIdx, int pScore,int dScore, double bet)
    {
        System.out.println("Player#" + (pIdx + 1) + " Hand# " + (hIdx + 1) + ": Dealer's " + 
                dScore + " beats " + "Player's " + pScore + ". Player loses $" + fmt2.format(bet));
    }

    /**
     * Display that the specified Player's hand has won
     * @param pIdx the player concerned
     * @param hIdx the player's hand
     * @param pScore the player's hand's score
     * @param dScore the dealer's score
     * @param bet the bet for this hand
     */
    @Override
    public void displayPlayerWins(int pIdx, int hIdx, int pScore, int dScore, double bet)
    {
        System.out.println("Player#" + (pIdx + 1) + " Hand# " + (hIdx + 1) + ": Player's " + 
                pScore + " beats " + "Dealer's " + dScore + ". Player wins $" + fmt2.format(bet));
    }

    /**
     * Used during the evaluation of a game for specific settings and shoe state
     * @param pC0 player's first card
     * @param pC1 player's second card
     * @param dC  dealer's face card
     * @param insurance whether the player should take insurance
     * @param best the recommended Option. null indicates state is unattainable (eg shoe empty)
     */
/*
    @Override
    public void displayRecomendation(int pCard1, int pCard2, int dCard, boolean insr, String option)
    {
        //if first recommendation in our table, print header
        if(dCard == Card.TWO && pCard1 == Card.TWO && pCard2 == Card.TWO)
        {
            System.out.println(lineBreak(' ', 46) + "DEALER FACE CARD");
            System.out.println(lineBreak(' ', 12) + lineBreak('-', 79));
            System.out.print("PLAYER HAND |");
            for(int cardType = Card.TWO; cardType < Card.PLAYER_ACE; cardType++)
                System.out.print(" [" + cardChar(cardType) + "] |");
            System.out.println(" [A] |");
            System.out.print(lineBreak('-', 91));
        }
 
        //if first column of table, print row info
        if(dCard == Card.TWO)
            System.out.print("\n| [" + cardChar(pCard1) + "] | [" + cardChar(pCard2) + "] |");

        //print whether to take insurance or not
        if(insr)
            System.out.print('*');
        else
            System.out.print('_');

        //print recommended option
        if(option == null)
            System.out.print("____|");
        else if(option.equals(Option.STAND))
            System.out.print("Stnd|");
        else if(option.equals(Option.HIT))
            System.out.print("Hit_|");
        else if(option.equals(Option.SPLIT))
            System.out.print("Splt|");
        else if(option.equals(Option.DOUBLE_DOWN))
            System.out.print("Dbl_|");
        else if(option.equals(Option.SURRENDER))
            System.out.print("Surr|");
        else if(option.equals(Option.ACCEPT_EVEN_MONEY))
            System.out.print("Evn$|");
        else
            throw new IllegalArgumentException("Unknown option type: " + option);

        //if last entry of table, print the table trailing info
        if(pCard1 == Card.PLAYER_ACE && pCard2 == Card.PLAYER_ACE && dCard == Card.PLAYER_ACE)
        {
            System.out.println("\n" + lineBreak('-', 91));
            System.out.println("|_Stnd| Stand");
            System.out.println("|_Hit_| Hit");
            System.out.println("|_Splt| Split");
            System.out.println("|_Dbl_| Double Down");
            System.out.println("|_Surr| Surrender");
            System.out.println("|_Evn$| Take Even Money");
            System.out.println("|*____| Take Insurance");
            System.out.println("Table only valid for current game settings and shoe state");
        }
    }
*/


    /**
     * Displays the Shoe's current contents
     * Print the shoe's contents as a 2-D matrix. Eg
     *          |[2]|[3]|[4]|[5]|[6]|[7]|[8]|[9]|[X]|[J]|[Q]|[K]|[A]|
     *    Clubs*|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|
     * Diamonds*|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|
     *   Hearts*|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|
     *   Spades*|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|  8|
     * Shoe size: 416 / 416
     * @param shoe the Shoe
     */
    @Override
    public void displayShoe(Shoe shoe)
    {
        System.out.println("         |[2]|[3]|[4]|[5]|[6]|[7]|[8]|[9]|[X]|[J]|[Q]|[K]|[A]|");
        for(int suit = Card.CLUBS; suit <= Card.SPADES; suit++)
        {
            //Print the Suit name and symbol
            System.out.printf("%8s" + SUIT_CHARS[suit] + "|", Card.SUIT_NAMES[suit]);

            //print the count of cardTypes13 for this Suit
            for(int type = Card.TWO; type <= Card.PLAYER_ACE; type++)
                System.out.printf("%3d|", shoe.cardCount(new Card(type,suit)));
            System.out.println();
        }
        System.out.println("\nShoe size: " + shoe.size() + " / " + shoe.capacity());
    }



    /**
     * Gets a bet for the next round for the specified player
     * @param pIdx the player concerned
     * @param min minimum bet allowed
     * @param max maximum bet allowed
     * @return bet for this player
     */
    @Override
    public double getBet(int pIdx, double min, double max)
    {
        String msg = "Player#" + (pIdx + 1) + " Bet: ";
        double proposedBet;

        do
        {
            proposedBet = getConsoleDouble(msg);
            if(proposedBet < min)
                System.out.println("Proposed bet too small. Table min is $" + min);
            else if(proposedBet > max)
                System.out.println("Proposed bet too large. Table max is $" + max);
        } while(proposedBet < min || proposedBet > max);
        return proposedBet;
    }

    /**
     * Gets a BetStrategy for the specified player
     * @param pIdx the player concerned
     * @return a BetStrategy for the specified player
     */
    @Override
    public BetStrategy getBetStrategy(int pIdx)
    {
        int betStrat;
        while(true)
        {
            betStrat = (int)getConsoleLong("Player#" + (pIdx + 1) + " Bet Strategy: ");
            if(betStrat < BetStrategy.USER_SPECIFIED || betStrat > BetStrategy.CONSTANT_RISK)
                System.out.println("Invalid choice");
            else
                break;
        }
        switch(betStrat)
        {
            case BetStrategy.USER_SPECIFIED:
                return new BetStrategyUserSpecified(this, pIdx);
            case BetStrategy.FLAT:
                double flatBet = -1.0;
                while(flatBet < BetStrategy.minBet() || flatBet > BetStrategy.maxBet())
                {
                    flatBet = getConsoleDouble("Player#" + (pIdx + 1) + " Flat Bet Size: $");
                    if(flatBet < BetStrategy.minBet() || flatBet > BetStrategy.maxBet())
                        System.out.println("Bet must be between $" + BetStrategy.minBet() + 
                                " and $" + BetStrategy.maxBet());
                }
                return new BetStrategyFlat(flatBet);
            case BetStrategy.RAMPED:
                double trueCountThreshold = 
                        getConsoleDouble("Player#" + (pIdx + 1) + " True Count Threshold: ");
                double minRampedBet = 
                        getConsoleDouble("Player#" + (pIdx + 1) + " Minimum Ramped Bet: $");
                double rampFactor = 
                        getConsoleDouble("Player#" + (pIdx + 1) + " Ramp Factor: ");
                return new BetStrategyRamped(trueCountThreshold, 
                        Math.max(BetStrategy.minBet(), minRampedBet), rampFactor);
            case BetStrategy.DISCRETE_BINARY:
                trueCountThreshold = 
                        getConsoleDouble("Player#" + (pIdx + 1) + " True Count Threshold: ");
                double lowBet = 
                        getConsoleDouble("Player#" + (pIdx + 1) + " LowBet: $");
                double highBet = 
                        getConsoleDouble("Player#" + (pIdx + 1) + " HighBet: $");
                return new BetStrategyDiscreteBinary(trueCountThreshold, lowBet, highBet);
            case BetStrategy.CONSTANT_RISK:
                trueCountThreshold = 
                        getConsoleDouble("Player#" + (pIdx + 1) + " True Count Threshold: ");
                double bankToBetRatio = 
                        getConsoleDouble("Player#" + (pIdx + 1) + " Bank-to-Bet Ratio: ");
                return new BetStrategyConstantRisk(trueCountThreshold, bankToBetRatio);
            default:
                throw new IllegalStateException("Unknown BetStrategy");
        }
    }



    /**
     * Gets a Card to be placed at the specified index in a Hand
     * Card indexes are displayed as starting at 1, but stored internally as starting at 0
     * @param cIdx the index where the card will be placed in a Hand
     * @param shoe so method can ensure shoe contains a card, or deal next Card from shoe
     * @return a Card selected by user, or dealt from shoe
     */
    @Override
    public Card getCard(int cIdx, Shoe shoe)
    {
        if(shoe.size() == 0)
            throw new IllegalStateException("Cannot get Card. Shoe is empty");

        if(gameType != GameLogic.GAME_TYPE_USER_SPECIFIED)
        {
            Card c = shoe.next();
            if(cIdx == INDEX_FINAL_CARD)
                System.out.println("Final Card: [" + cardChar(c.playerType()) +
                    SUIT_CHARS[c.suit()] + "]");
            else
                System.out.println("Card#" + (cIdx + 1) +": [" + cardChar(c.playerType()) +
                    SUIT_CHARS[c.suit()] + "]");
            return c;
        }

        String line, token1, token2;
        int cardType, cardSuit;
        Card proposedCard;

        while(true)                                        //loop till we get a valid card from shoe
        {
            line = token1 = token2 = null;
            cardType = cardSuit = INVALID;
            proposedCard = null;

            if(cIdx == INDEX_FINAL_CARD)
                System.out.print("Final Card: ");
            else
                System.out.print("Card#" + (cIdx + 1) + ": ");
            line = console.nextLine();
            Scanner scan = new Scanner(line);

            if(line.length() != 0 && scan.hasNext())      //if user has given us some input
            {
                //Try to determine card type
                token1 = scan.next();
                try
                {
                    cardType = cardType(token1.charAt(0));
                }
                catch(IllegalArgumentException iae)
                {
                    System.out.println(iae);
                    scan.close();
                    continue;
                }

                //If suit provided, try to determine which one indicated
                if(scan.hasNext())                        //if card type and suit specified
                {
                    token2 = scan.next();
                    try
                    {
                        cardSuit = cardSuit(token2);
                        proposedCard = new Card(cardType, cardSuit);
                        if(shoe.contains(proposedCard)){
                        	scan.close();
                            return proposedCard;
                        }
                        else
                        {
                            System.out.println("Sorry, no [" + 
                                    cardChar(proposedCard.playerType()) + 
                                    SUIT_CHARS[cardSuit] + "] left in shoe");
                            scan.close();
                            continue;
                        }  
                    }
                    catch(IllegalArgumentException iae)
                    {
                        System.out.println(iae);
                        scan.close();
                        continue;
                    }
                }
                else                                      //if only cardType, not suit, is specified
                {
                    try
                    {
                        cardSuit = shoe.probableSuit(cardType, Shoe.REMOVE);
                        scan.close();
                        return new Card(cardType, cardSuit);
                    }
                    catch (IllegalStateException ise)
                    {
                        System.out.println(ise);
                        scan.close();
                        continue;
                    }
                }
            }
            scan.close();
        }
    }



    /**
     * Gets the index of an Option currently being displayed
     * If state=USER_SPECIFIED || RANDOMLY_DEALT, user indicates an Option index
     * If state=AUTO_PLAY, UserInterface returns the best currently available Option, ie idx == 0
     * Can also be used to get a CountStrategy and BetStrategy, just temporarily set UI state to 
     * USER_SPECIFIED
     * @return chosen Option's index
     */
    @Override
    public int getChoice(int min, int max)
    {
        if(gameType == GameLogic.GAME_TYPE_AUTO_PLAY)   //if AutoPlay, return first (ie best) choice
        {
            System.out.println("Choice: 0");
            return 0;
        }
        else
        {
            int choice;
            do
            {
                choice = (int)getConsoleLong("Choice: ");
                if(choice < min || choice > max)
                    System.out.println("Invalid choice. Choice must be between " + min + " and " + max);
            } while(choice < min || choice > max);
            return choice;
        }
            
    }

    /**
     * Gets the index of an Option currently being displayed, for a specified player
     * If state=USER_SPECIFIED || RANDOMLY_DEALT, user indicates an Option index
     * If state=AUTO_PLAY, UserInterface returns the best currently available Option, ie idx == 0
     * Can also be used to get a CountStrategy and BetStrategy, just temporarily set UI state to 
     * USER_SPECIFIED
     * @param pIdx the player concerned
     * @return chosen Option's index
     */
    @Override
    public int getChoice(int pIdx, int min, int max)
    {
        if(gameType == GameLogic.GAME_TYPE_AUTO_PLAY)  //if AutoPlay, return first (ie best) choice
        {
            System.out.println("Player#" + (pIdx + 1) + " Choice: 0");
            return 0;
        }
        else
        {
            int choice;
            do
            {
                choice = (int)getConsoleLong("Player#" + (pIdx + 1) + " Choice: ");
                if(choice < min || choice > max)
                    System.out.println("Invalid choice. Choice must be between " + min + " and " 
                            + max);
            } while(choice < min || choice > max);
            return choice;
        }

    }


    /**
     * Gets whether user wishes to continue Game, (ie play next Round)
     * @return whether user wishes to continue Game, (ie play next Round)
     */
    @Override
    public boolean getChoiceContinueGame()
    {
        return getConsoleBoolean("Continue Game? (y/n): ");
    }




    /**
     * Gets whether the user wishes to reset the shoe. 
     * On RANDOMLY_DEALT games, this will depened on whether penetration > maxPenetration
     * @param shoe the shoe
     * @return whether to refill and shuffle the shoe
     */
    @Override
    public boolean getChoiceResetShoe(Shoe shoe)
    {
        if(gameType == GameLogic.GAME_TYPE_AUTO_PLAY)
        {
            System.out.print("Reset shoe?: ");
            if(shoe.penetration() > shoe.maxPenetration())
            {
                System.out.println("Y\nDealer has reset shoe");
                return true;
            }
            else
            {
                System.out.println("N");
                return false;
            }
        }
        else
        {
            boolean reset = getConsoleBoolean("Reset shoe?: ");
            if(reset)
                System.out.println("Dealer has reset shoe");
            return reset;
        }
    }


    /**
     * Gets the user to indicate how many players are at the table
     * @param minPlayers minimum number of players at a table
     * @param maxPlayers maximum number of players at a table
     * @return number of players playing at table
     */
    @Override
    public int getCountPlayers(int minPlayers, int maxPlayers)
    {
        int countPlayers;
        while(true)
        {
            countPlayers = (int)getConsoleLong("Player Count: ");
            if(countPlayers < minPlayers)
                System.out.println("Minimum player count is " + minPlayers);
            else if(countPlayers > maxPlayers)
                System.out.println("Maximum player count is " + maxPlayers);
            else
                return countPlayers;
        }
    }

    /**
     * Gets a CountStrategy for the specified player
     * @param pIdx the player concerned
     * @return the int representation of a countStrategy
     */
    @Override
    public int getCountStrategy(int pIdx)
    {
        int countStrat;
        while(true)
        {
            countStrat = (int)getConsoleLong("Player#" + (pIdx + 1) + " Count Strategy: ");
            if(countStrat < CountStrategy.NONE || countStrat > CountStrategy.CUSTOM)
                System.out.println("Invalid Count Strategy");
            else
                return countStrat;
        }
    }

    /**
     * Gets whether the dealer has BlackJack. 
     * Only called if dealer has single 10val or Ace, and dealer dealt hole card.
     * @param isBlackJack whether dealer's peek at hole card reveals blackJack (for random deals)
     * @return whether dealer's peek at hole card reveals blackJack
     */
    @Override
    public boolean getDealerPeekRevealsBlackJack(boolean isBlackJack)
    {
        System.out.println("Dealer peeks at hole card...");
        if(gameType == GameLogic.GAME_TYPE_USER_SPECIFIED)
            return getConsoleBoolean("Does dealer have BlackJack? (y/n): ");
        else
        {
            System.out.println("Does dealer have BlackJack? (y/n): " + (isBlackJack ? "y" : "n"));
            return isBlackJack;
        }
        
    }

    /**
     * Gets a the additional bet when double downing for current player
     * @param max maximum additonal bet allowed
     * @return additional bet for current player
     */
    public double getDoubleDownBet(double max)
    {
        String msg = "Additional Bet ($0.00 - $" + fmt2.format(max) + "): $";
        double proposedBet;

        if(gameType == GameLogic.GAME_TYPE_AUTO_PLAY)
        {
            System.out.println(msg + fmt2.format(max));
            return max;
        }

        do
        {
            proposedBet = getConsoleDouble(msg);
            if(proposedBet < 0)
                System.out.println("Additional bet must be > $0.00");
            else if(proposedBet > max)
                System.out.println("Proposed bet too large. Max additional bet is $" + 
                    fmt2.format(max));
        } while(proposedBet < 0 || proposedBet > max);
        return proposedBet;
    }


    /**
     * Gets an insurance bet for the specified player. If Auto Play, returns maximum allowed
     * @param pIdx the player concerned
     * @param maxBet the maximum insurance wager permitted
     * @return insurance wager for this player
     */
    @Override
    public double getInsuranceBet(int pIdx, double maxInsurance)
    {
        if(gameType == GameLogic.GAME_TYPE_AUTO_PLAY)
        {
            System.out.println("Player#" + (pIdx + 1) + " Insurance bet ($0.00 to $" +
                    fmt2.format(maxInsurance) + "): $" + fmt2.format(maxInsurance));
            return maxInsurance;
        }
        else
        {
            double insurance;
            while(true)
            {
                insurance = getConsoleDouble("Player#" + (pIdx + 1) + " Insurance bet ($0.00 to $" +
                        fmt2.format(maxInsurance) + "): $");
                if(insurance < 0)
                    System.out.println("Minimum insurance is $0.00");
                else if(insurance > maxInsurance)
                    System.out.println("Maximum insurance is $" + fmt2.format(maxInsurance));
                else
                    return insurance;
            }
        }
    }

    /**
     * Gets the user to input the specified player's starting bank
     * @param pIdx the player concerned
     * @return starting bank for this player
     */
    @Override
    public double getPlayerBank(int pIdx)
    {
        return getConsoleDouble("Player#" + (pIdx + 1) + " Bank: $");
    }




    /**
     * Sets focus on dealer
     */
    @Override
    public void setFocusDealer()
    {
        System.out.println("\n--> Dealer");
    }

    /**
     * Sets focus on a Player's Hand
     * Hand index displayed as starting at 1, but stored internally as starting at 0
     * @param hIdx the index of the Player's Hand we are now dealing with
     */
    @Override
    public void setFocusHand(int hIdx)
    {
        System.out.println("\n--> Hand#" + (hIdx + 1));
    }


    /**
     * Sets focus on a specified Player
     * Player index displayed as starting at 1, but stored internally as starting at 0
     * @param pIdx the index of the Player we are now dealing with
     */
    @Override
    public void setFocusPlayer(int pIdx)
    {
        System.out.println("\n--> Player#" + (pIdx + 1));
    }


    /**
     * Sets the current gameType
     * USER_SPECIFIED: requests for cards will require user to manually input
     * !USER_SPECIFIED: requests for cards will trigger random selection from Shoe
     * AUTO_PLAY: best Option automatically chosen and returned
     * !AUTO_PLAY: chosen Option selected by user
     * @param gameType current game type
     */
    @Override
    public void setGameType(int gameType)
    {
        this.gameType = gameType;
    }


    /**
     * Creates and starts the Auto Play Simulation keyboard monitor.
     * The monitor will block until the user presses the "Enter" key
     */
    @Override
    public void startAutoPlayMonitor()
    {
        Thread thread2 = new Thread(new UserInputMonitor());
        thread2.start();
    }







    //--------------------------------- Private Instance Behaviour ---------------------------------








    /**
     * Converts the cardType of a card to its char representation 
     * ie {0-12} -> {2,3,4,5,6,7,8,9,X,J,Q,K,A}
     * @param cardType the type (0 - 12) of the card
     * @return char representation relating to card type
     */
    private char cardChar(int cardType)
    {
        if(cardType < 0 || cardType > 12)
            throw new IllegalArgumentException("cardType: " + cardType);
        switch(cardType)
        {

            case 8:
                return 'X';
            case 9:
                return 'J';
            case 10:
                return 'Q';
            case 11:
                return 'K';
            case 12:
                return 'A';
            default:
                return (char) (cardType + 50);            
        }
    }

    /**
     * Converts a String representation of a card suit into the Suit's int value
     * ie "Clubs" -> 0, "Diamonds" -> 1, "Hearts" -> 2, "Spades" -> 3
     * @param suitString a card's suit
     * @return the suit int value (0-3) of this suit

     */
    private int cardSuit(String suitString)
    {
        if(suitString == null || suitString.length() == 0)
            throw new IllegalArgumentException("Empty suit String: " + suitString);

        char firstChar = suitString.charAt(0);
        switch(firstChar)
        {
            case 'C':
            case 'c':
                return 0;
            case 'D':
            case 'd':
                return 1;
            case 'H':
            case 'h':
                return 2;
            case 'S':
            case 's':
                return 3;
            default:
                throw new IllegalArgumentException("Unknown suit: " + suitString);
        }
    }




    /**
     * Converts a card's char representation to card type 
     * ie {2,3,4,5,6,7,8,9,X,J,Q,K,A} -> (0-12)
     * (int)'0' = 48 ... (int)'9' = 57 
     * @param cardChar a card's single character representation
     * @return the type {0 - 12} of the card

     */
    private int cardType(char cardChar)
    {
        switch(cardChar)
        {
            case 'X':
            case 'x':
                return 8;
            case 'J':
            case 'j':
                return 9;
            case 'Q':
            case 'q':
                return 10;
            case 'K':
            case 'k':
                return 11;
            case 'A':
            case 'a':
                return 12;
            default:
                if(cardChar < 50 || cardChar > 57)
                    throw new IllegalArgumentException("Unknown card char: " + cardChar);
                return (cardChar - 50);          
        }
    }

    /**
     * Allows user to add or remove Cards from shoe.
     * Put in UserInterface as it relates to parsing text-based user-input
     * @param the shoe to insert/remove Cards into/from
     */
    private void editShoeContents(Shoe shoe)
    {  
        String line;                                 // a line of user-input
        Scanner scan;                                // to parse the line of user-input
        String[] args = new String[3];               // pieces of user-input 
        int countArgs;                               // number of arguments in line of user-input
        int countCard;                               // how many cards to insert(+ve) or remove(-ve)
        int cardType;                                // the indicated cardType13
        int cardSuit;                                // the indicated suit

        System.out.println("(0 to finish)");
        System.out.println("[-]<count> <cardType> [<cardSuit>]");
        System.out.println("(eg \"-2 A Hearts\" removes 2 Aces of Hearts)");
        System.out.println("(eg \"-2 A\" removes 2 Aces)");
        System.out.println("(eg  \"4 X S\" inserts 4 Tens of Spades)");
        System.out.println("(eg  \"4 X\" inserts 4 Tens)");
        while(true)
        {
            displayShoe(gL.getShoe());
            System.out.print("Choice: ");
            line = console.nextLine();
            
            scan = new Scanner(line);
            countArgs = 0;
            while(scan.hasNext() && countArgs < 3)    //just parse the first 3 arguments
            {
                args[countArgs++] = scan.next();
            }
            if(countArgs < 2)
            {
                if(args[0].charAt(0) == '0')
                    break;
                System.out.println("Invalid sytax");
            }
            else
            {
                try
                {
                    countCard = Integer.parseInt(args[0]);
                    cardType = cardType(args[1].charAt(0));

                    if(countArgs == 3)             //if suit indicated, insert/remove only this suit
                    {
                        Card card = new Card(cardType, cardSuit(args[2]));
                        if(countCard < 0)                 //removal request
                            for(int i = 0; i > countCard; i--)
                                shoe.remove(card);
                        else if(countCard > 0)            //insert request
                            for(int i = 0; i < countCard; i++)
                                shoe.insert(card);
                    }
                    else                              //else, select suit for each insertion/removal
                    {
                        if(countCard < 0)                 //removal request
                            for(int i = 0; i > countCard; i--)
                            {
                                cardSuit = shoe.probableSuit(cardType, Shoe.REMOVE);
                                shoe.remove(new Card(cardType, cardSuit));
                            }
                        else if(countCard > 0)            //insert request
                            for(int i = 0; i < countCard; i++)
                            {
                                cardSuit = shoe.probableSuit(cardType, Shoe.INSERT);
                                shoe.insert(new Card(cardType, cardSuit));
                            }
                    }
                }
                catch(IllegalStateException ise)
                {
                    System.out.println(ise);                          //TODO, just get message
                }
                catch(NumberFormatException nfe)
                {
                    System.out.println("Syntax error: " + nfe);       //TODO, just get message
                }
                catch(IllegalArgumentException iae)
                {
                    System.out.println("Syntax error: " + iae);       //TODO, just get message
                }

            }
        }
        scan.close();
    }

    /**
     * Displays the Top-Level Menu
     */
    private void displayMenuTopLevel()
    {   
        int choice;

        menu:
        while(true)
        {
            System.out.println(lineBreak('-', 22) + " Perfect Play BlackJack : Main Menu " + 
                    lineBreak('-', 22));
            System.out.println(" 0. Exit");
            System.out.println(" 1. Play Game using User-Specified Cards");
            System.out.println(" 2. Play Game using Randomly-Dealt Cards");
            System.out.println(" 3. Auto Play using Randomly-Dealt Cards");
            System.out.println(" 4. Settings");
            System.out.println(" 5. Shoe State (" + gL.getShoe().size() + " / " +
                    gL.getShoe().capacity() + " cards)");
            System.out.println(" 6. Evaluate (Player | Dealer ) advantage");
            System.out.println(" 7. About Perfect Play BlackJack");
            System.out.println(" 8. Help x");
            System.out.println(lineBreak('-', 80));

            choice = (int)getConsoleLong("Choice: ");
 
            switch(choice)
            {
                case 0:
                    break menu;
                case 1:
                    setGameType(GameLogic.GAME_TYPE_USER_SPECIFIED);
                    gL.playGame(gameType);
                    break;
                case 2:
                    setGameType(GameLogic.GAME_TYPE_RANDOMLY_DEALT);
                    gL.playGame(gameType);
                    break;
                case 3:
                    setGameType(GameLogic.GAME_TYPE_AUTO_PLAY);
                    gL.playGame(gameType);
                    break;
                case 4:
                    displayMenuSettings();
                    break;
                case 5:
                    displayMenuShoeState();
                    break;
                case 6:
                    gL.evaluatePlayerAdvantage();
                    break;
                case 7:
                    printAbout();
                    break;
                case 8:
                    int menuItem = (int)getConsoleLong("Menu item: ");
                    switch(menuItem)
                    {
                        case 4:
                            System.out.println("Enables user to edit game parameters to simualte " +
                                    "different rules");
                            break;
                        case 5:
                            System.out.println("Enables user to edit shoe contents and state");
                            break;
                        case 6:
                            System.out.println("For the current game settings, and a full shoe, " +
                                    "computes a table of optimal choices for all possible deals");
                        default:
                            System.out.println("No help available for menu item " + menuItem);
                    }
                    break;
                default:
                    System.out.println("Invalid input. Please input the number to the left of " +
                            " the desired menu item");  
            }
        }
    }



    /**
     * Displays the  Settings menu
     */
    private void displayMenuSettings()
    {
        int choice;
        Settings s;                //only used to get parameters, gL instructed to set

        menu:
        while(true)
        {
            s = gL.getSettings();  //becuase we may have reloaded defaults

            System.out.println(lineBreak('-', 22) + "  Perfect Play BlackJack : Settings " + 
                    lineBreak('-', 23));
            System.out.println(" 0. BACK");
            System.out.println("-- Shoe --");
            System.out.println(" 1. Set Shoe Max Penetration (" + s.shoeMaxPenetration() + ")");
            System.out.print(" 2. Set Shoe Shuffle Type (");
            switch (s.shoeShuffleType())
            {
                case Shoe.SHUFFLE_TYPE_NONE:
                    System.out.println("None)"); 
                    break;
                case Shoe.SHUFFLE_TYPE_PSEUDORANDOM:
                    System.out.println("Pseudorandom)"); 
                    break;
                case Shoe.SHUFFLE_TYPE_CUT_SPLIT_MERGE_LOOP:
                    System.out.print(s.shoeCountShuffleCuts() + " cuts, split, merge, ");
                    System.out.println(s.shoeCountShuffleLoops() + " times)");
            }
            System.out.println(" 3. Set Shoe Capacity (" + ( s.shoeCapacity() / 52) + " decks)");
            System.out.println("\n-- Card Count Strategies --");
            String space = lineBreak(' ',14);
            System.out.println(space + "| [2]| [3]| [4]| [5]| [6]| [7]| [8]| [9]|[X][J][Q][K]| [A]|");
            boolean[] displayableCountStrategies = s.displayableCountStrategies();
            for(int cntStrt = CountStrategy.HI_LO; cntStrt <= CountStrategy.CUSTOM; cntStrt++)
            {
                System.out.printf("%2d. %-9s |", (cntStrt + 4), CountStrategy.getName(cntStrt));
                for(int cardType = Card.TWO; cardType <= Card.DEALER_ACE; cardType++)
                    if(cardType != Card.TEN)
                        System.out.printf("%4s|", Double.toString(
                                CountStrategy.getAdjustment(cntStrt, Card.DEALER_VALUES[cardType])));
                    else
                        System.out.printf("%8s    |", Double.toString(
                                CountStrategy.getAdjustment(cntStrt, Card.DEALER_VALUES[cardType])));
                System.out.println("(" + (displayableCountStrategies[cntStrt] ? "on" : "off") + 
                        ")");
            }
            System.out.println("12. Edit " + CountStrategy.getName(CountStrategy.CUSTOM) + 
                    " name");
            System.out.println("13. Edit " + CountStrategy.getName(CountStrategy.CUSTOM) + 
                    " values");
            System.out.println("14. Toggle Display Shoe Ace Concentration  (" + 
                    (s.displayAceGuage() ? "on" : "off") + ")");
            System.out.println("15. Toggle Display Player BlackJack Chance (" + 
                    (s.displayPlayerBJGuage() ? "on" : "off") + ")");
            System.out.println("\n-- Game Simulation --");
            System.out.println("16. Set AutoPlay Display minimum update Delay (" + 
                    s.autoPlayDisplayDelay() + " ms)");
            System.out.println("\n-- Initial Deal --");
            System.out.println("17. Set table minimum bet ($" + s.minBet() + ")");
            System.out.println("18. Set table maximum bet ($" + s.maxBet() + ")");
            System.out.println("19. Toggle Dealer receives Hole Card (" + 
                    (s.dealerDealtHoleCard() ? "American BlackJack" : "European No Hole-Card") + 
                    ")");
            System.out.print("20. Set Surrender Availability (");
            if(s.dealerDealtHoleCard())
                System.out.println((s.surrenderType() == GameLogic.SURRENDER_TYPE_NONE ? "None" : 
                        (s.surrenderType() == GameLogic.SURRENDER_TYPE_LATE ? "Late" : "Early")) + 
                        ")");
            else
                System.out.println((s.surrenderType() == GameLogic.SURRENDER_TYPE_NONE ?
                        "Off" : "On") + ")");
            System.out.println("21. Toggle Player Hand Dealt Face (" + 
                    (s.playerHandDealtFaceUp() ? "Up" : "Down") + ")");
            System.out.println("22. Toggle Dealer Offers Insurance on Dealer Ace (" + 
                    s.dealerOffersInsurance() + ")");
            System.out.println("23. Toggle Dealer Offers Even Money on Dealer Ace & Player " +
                    "BlackJack (" + s.dealerOffersEvenMoney() + ")");
            System.out.println("\n-- Player Action --");
            System.out.println("24. Toggle Can Hit After Aces Split (" + s.canHitAfterAcesSplit() + 
                    ")");
            System.out.println("25. Toggle Split {X,J,Q,K} by (" + 
                    (s.splitCardEqualityType() == PlayerHand.SPLIT_BY_VALUE  ? "Value" : "Rank") + 
                    ")");
            System.out.println("26. Set Maximum times 2-King can be split (" + 
                    s.times2toK_Splittable() +")");
            System.out.println("27. Set Maximum times Aces can be split (" + 
                    s.timesAcesSplittable() +")");
            System.out.println("28. Toggle Can Double Down after 2-King split (" + 
                    s.canDoubleAfter2toK_Split() +")");
            System.out.println("29. Toggle Can Double Down after Aces split (" + 
                    s.canDoubleAfterAcesSplit() +")");

            System.out.print("30. Set Double Down conditions (");
            if(s.doublableScores().size() == 0 || s.doublableCardCounts().size() == 0)
                System.out.println("off)");
            else if(s.doublableScores().size() == 17)                     // all scores from 4 to 20
            {
                Iterator<Integer> itr = s.doublableCardCounts().iterator();
                System.out.print("any " + itr.next());
                while(itr.hasNext())
                    System.out.print(", " + itr.next());
                System.out.println(" cards)");
            }
            else if(s.doublableCardCounts().size() == 18)                           // 2 to 19 cards
            {
                Iterator<Integer> itr = s.doublableScores().iterator();
                System.out.print("score = " + itr.next());
                while(itr.hasNext())
                    System.out.print(", " + itr.next());
                System.out.println(")");
            }
            else       // eg 2,3 cards & score = 9,10,11
            {
                Iterator<Integer> itr = s.doublableCardCounts().iterator();
                System.out.print(itr.next());
                while(itr.hasNext())
                    System.out.print("," + itr.next());
                System.out.print(" cards & score = ");
                itr = s.doublableScores().iterator();
                System.out.print(itr.next());
                while(itr.hasNext())
                    System.out.print("," + itr.next());
                System.out.println(")");
            }
        
            System.out.println("\n-- Dealer Action --");
            System.out.println("31. Toggle Dealer Hits Soft 17s (" + s.dealerHitsSoft17() + ")");
            System.out.println("\n-- Settling Bets --");
            System.out.println("32. Toggle Dealer wins Tie (" + s.dealerWinsTies() + ")");
            System.out.println("33. Toggle Player only loses original bet on Dealer Blackjack (" +
                    s.originalBetsOnly() + ")");
            System.out.println("34. Set Player BlackJack Win rate (" + s.blackJackROI() + ")");
            System.out.println("35. Set Player Split-Hand BlackJack Win Rate (" + 
                    s.splitBlackJackROI() + ")");
            System.out.println("36. Toggle Five Card Charlie wins (" + s.fiveCardCharlieWins() + 
                    ")");
            System.out.println("37. Toggle Five Card 21 wins (" + s.fiveCard21Wins() + ")");
            System.out.println("38. Set Five Card 21 Jackpot ($" + s.fiveCard21Amount() + ")");
            System.out.println("39. Toggle show bank history after game (" + s.showBankHistory() + ")");
            System.out.println("\n-- Admin --");
            System.out.println("40. Save Settings");
            System.out.println("41. Revert to System default settings");
            System.out.println("42. Help");
            System.out.println(lineBreak('-', 80));

            choice = (int)getConsoleLong("Choice: ");
            switch(choice)
            {
                case 0:
                    break menu;
                case 1:
                    while(true)
                    {
                        double maxPenetration = getConsoleDouble("New max penetration (0.0 - 1.0): ");
                        if(maxPenetration <= 0.0)
                            System.out.println("Max Penetration must be > 0.0");
                        else if(maxPenetration >= 1.0)
                            System.out.println("Max Penetration must be < 1.0");
                        else
                        {
                            gL.setShoeMaxPenetration(maxPenetration);
                            break;
                        }
                    }
                    break;
                case 2:
                    while(true)
                    {
                        int shuffleType, countCuts = 0, countLoops = 0;
                        System.out.println("Available shuffle types:");
                        System.out.println(Shoe.SHUFFLE_TYPE_NONE + ". None");
                        System.out.println(Shoe.SHUFFLE_TYPE_PSEUDORANDOM + ". PseudoRandom");
                        System.out.println(Shoe.SHUFFLE_TYPE_CUT_SPLIT_MERGE_LOOP + ". x cuts, " + 
                                "split, merge, N times");
                        shuffleType = (int)getConsoleLong("Shoe shuffle type:");
                        if(shuffleType < Shoe.SHUFFLE_TYPE_NONE || 
                                shuffleType > Shoe.SHUFFLE_TYPE_CUT_SPLIT_MERGE_LOOP)
                            System.out.println("Invalid shuffle type");
                        else
                        {
                            if(shuffleType == Shoe.SHUFFLE_TYPE_CUT_SPLIT_MERGE_LOOP)
                            {
                                while(true)
                                {
                                    countCuts = (int)getConsoleLong("Count shoe cuts?: ");
                                    if(countCuts < 1)
                                        System.out.println("Minimum cuts is 1");
                                    else
                                        break;
                                }
                                while(true)
                                {
                                    countLoops = (int)getConsoleLong("How many cut, split & merge" +
                                        " loops?: ");
                                    if(countLoops < 1)
                                        System.out.println("Minimum loops is 1");
                                    else
                                        break;
                                }
                            }
                            gL.setShoeShuffleType(shuffleType, countCuts, countLoops);
                            break;
                        }
                    }
                    break;
                case 3:
                    while(true)
                    {
                        int countDecks = (int)getConsoleLong("How many decks in shoe?: ");
                        if(countDecks < 1)
                            System.out.println("Minimum deck count is 1");
                        else if(countDecks > 999)
                            System.out.println("Maximum deck count is 999");
                        else
                        {
                            gL.setShoeCapacity(countDecks * 52);
                            break;
                        }
                    }
                    break;
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                    gL.toggleDisplayCountStrategy(choice - 4);
                    break;
                case 12:
                    String customName = null;
                    while(customName == null || customName.length() == 0)
                    {
                        System.out.print("Please enter Custom Count Strategy name: ");
                        customName = console.nextLine().trim();
                        if(customName.length() == 0)
                            System.out.println("Invalid name");
                        else if(customName.length() <= 9)
                            gL.setCustomCountStrategyName(customName);
                        else
                            gL.setCustomCountStrategyName(customName.substring(0,9));
                    }
                    break;
                case 13:
                    double[] adjustments = new double[10];
                    for(int cardType = Card.TWO; cardType <= Card.DEALER_ACE; cardType++)
                    {
                        while(true)
                        {
//TODO find a cleaner way to get 1 significant figure
                            adjustments[cardType] = Math.round(10 * getConsoleDouble("Card value(" + 
                                    Card.DEALER_VALUES[cardType] + ") count adjustment: ")) / 10.0;
                            if(adjustments[cardType] < -9.9 || adjustments[cardType] > 9.9)
                                System.out.println("Adjustment must be between -9.9 and 9.9");
                            else
                                break;
                        }
                    }
                    gL.setCustomCountStrategyAdjustments(adjustments);
                    break;
                case 14:
                    gL.toggleDisplayAceGuage();
                    break;
                case 15:
                    gL.toggleDisplayPlayerBJGuage();
                    break;
                case 16:
                    int delay = -1;
                    while(delay < 0 || delay > 10000)
                    {
                        delay = (int)getConsoleLong("Please input AutoPlay display delay: ");
                        if(delay < 0)
                            delay = 0;
                        else if(delay > 10000)
                            delay = 10000;
                    }
                    gL.setAutoPlayDisplayDelay(delay);
                    break;
                case 17:
                    double minBet = getConsoleDouble("Please enter table's minimum Bet: ");
                    gL.setMinBet(Math.max(0,minBet));
                    break;
                case 18:
                    double maxBet = getConsoleDouble("Please enter table's maximum Bet: ");
                    gL.setMaxBet(maxBet);
                    break;
                case 19:
                    gL.toggleDealerDealtHoleCard();
                    break;
                case 20:
                    int surrType = -1;
                    while(surrType != GameLogic.SURRENDER_TYPE_NONE && 
                            surrType != GameLogic.SURRENDER_TYPE_EARLY && 
                            surrType != GameLogic.SURRENDER_TYPE_LATE)
                    {
                        System.out.println("Surrender Types:");
                        if(s.dealerDealtHoleCard())
                        {
                            System.out.println(GameLogic.SURRENDER_TYPE_NONE + ". None");
                            System.out.println(GameLogic.SURRENDER_TYPE_LATE + ". Late");
                            System.out.println(GameLogic.SURRENDER_TYPE_EARLY + ". Early");
                        }
                        else
                        {
                            System.out.println(GameLogic.SURRENDER_TYPE_NONE + ". Off");
                            System.out.println(GameLogic.SURRENDER_TYPE_LATE + ". On");
                        }
                        surrType = (int)getConsoleLong("Surrender type: " );
                        if(surrType != GameLogic.SURRENDER_TYPE_NONE && 
                                surrType != GameLogic.SURRENDER_TYPE_EARLY && 
                                surrType != GameLogic.SURRENDER_TYPE_LATE)
                            System.out.println("Invalid choice");
                    } 
                    gL.setSurrenderType(surrType);
                    break;
                case 21:
                    gL.togglePlayerHandDealtFaceUp();
                    break;
                case 22:
                    gL.toggleDealerOffersInsurance();
                    break;
                case 23:
                    gL.toggleDealerOffersEvenMoney();
                    break;
                case 24:
                    gL.toggleCanHitAfterAcesSplit();
                    break;
                case 25:
                    int splitType = -1;
                    while(splitType != PlayerHand.SPLIT_BY_VALUE && 
                            splitType != PlayerHand.SPLIT_BY_RANK)
                    {
                        System.out.println("Split pair of cards:");
                        System.out.println(PlayerHand.SPLIT_BY_VALUE + ". By Value");
                        System.out.println(PlayerHand.SPLIT_BY_RANK + ". By Rank");
                        splitType = (int)getConsoleLong("Choice: ");
                        if(splitType != PlayerHand.SPLIT_BY_VALUE && 
                                splitType != PlayerHand.SPLIT_BY_RANK)
                            System.out.println("Invalid choice. Please try again");
                    }
                    gL.setSplitCardEqualityType(splitType);
                    break;
                case 26:
                    int timesSplittable = -1;
                    while(timesSplittable < 0 || timesSplittable > PlayerHand.MAX_SPLIT_LEVELS)
                    {
                        timesSplittable = 
                                (int)getConsoleLong("Times pair of 2s to Kings can be split?: ");
                        if(timesSplittable < 0 || timesSplittable > PlayerHand.MAX_SPLIT_LEVELS)
                            System.out.println("Value must be >= 0 and <= " + 
                                    PlayerHand.MAX_SPLIT_LEVELS);
                    }
                    gL.setTimes2toK_Splittable(timesSplittable);
                    break;
                case 27:
                    timesSplittable = -1;
                    while(timesSplittable < 0 || timesSplittable > PlayerHand.MAX_SPLIT_LEVELS)
                    {
                        timesSplittable = 
                                (int)getConsoleLong("Times pair of Aces can be split?: ");
                        if(timesSplittable < 0 || timesSplittable > PlayerHand.MAX_SPLIT_LEVELS)
                            System.out.println("Value must be >= 0 and <= " + 
                                    PlayerHand.MAX_SPLIT_LEVELS);
                    }
                    gL.setTimesAcesSplittable(timesSplittable);
                    break;
                case 28:
                    gL.toggleCanDoubleAfter2toKingSplit();
                    break;
                case 29:
                    gL.toggleCanDoubleAfterAcesSplit();
                    break;
                case 30:
                    TreeSet<Integer> countCards = new TreeSet<Integer>();
                    TreeSet<Integer> scores = new TreeSet<Integer>();
                    int input = 0;
                    System.out.println("Enter double downable card counts (-1 when done): ");
                    while(input != -1)
                    {
                        input = (int)getConsoleLong("Card Count: ");
                        countCards.add(input);
                    }
                    Iterator<Integer> itr = countCards.iterator();
                    while(itr.hasNext())
                    {
                        input = itr.next();
                        if(input < 2 || input > 19) //only card counts 2-19 are valid
                            itr.remove();
                    }
                    gL.setDoublableCardCounts(countCards);
                    if(countCards.size() > 0)      //if there is at least one doublable card count
                    {
                        System.out.println("Enter double downable scores (-1 when done): ");
                        while(input != -1)
                        {
                            input = (int)getConsoleLong("Score: ");
                            scores.add(input);
                        }
                        itr = scores.iterator();
                        while(itr.hasNext())
                        {
                            input = itr.next();
                            if(input < 4 || input > 20) //scores from 4 to 20
                                itr.remove();
                        }
                        gL.setDoublableScores(scores);
                    }              
                    break;
                case 31:
                    gL.toggleDealerHitsSoft17();
                    break;
                case 32:
                    gL.toggleDealerWinsTies();
                    break;
                case 33:
                    gL.toggleOriginalBetsOnly();
                    break;
                case 34:
                    double bjRate = -1;
                    while(bjRate < 1.0)
                    {
                        bjRate = getConsoleDouble("BlackJack winnings from a $1 bet: ");
                        if(bjRate < 1.0)
                            System.out.println("Rate must be more than $1 ($1.2 - $1.5 is common)");
                    }
                    gL.setBlackJackROI(bjRate);
                    break;
                case 35:
                    double sbjRate = -1;
                    while(sbjRate < 1.0)
                    {
                        sbjRate = getConsoleDouble("Split BlackJack winnings from a $1 bet: ");
                        if(sbjRate < 1.0)
                            System.out.println("Rate must be more than $1 ($1.2 - $1.5 is common)");
                    }
                    gL.setSplitBlackJackROI(sbjRate);
                    break;
                case 36:
                    gL.toggleFiveCardCharlieWins();
                    break;
                case 37:
                    gL.toggleFiveCard21Wins();
                    break;
                case 38:
                    double fiveCard21Amount = -1;
                    while(fiveCard21Amount < 0)
                    {
                        fiveCard21Amount = getConsoleLong("Amount won on a five-card-21: ");
                        if(fiveCard21Amount < 0)
                            System.out.println("Amount must be >= $0");
                    }
                    gL.setFiveCard21Amount(fiveCard21Amount);
                    if((fiveCard21Amount == 0) == s.fiveCard21Wins())    //if mismatch
                        gL.toggleFiveCard21Wins();
                    break;
                case 39:
                	gL.toggleShowBankHistoryAfterGame();
                	break;
                case 40:
                    if(gL.saveSettings())
                        pressEnterToContinue("Settings successfully saved. ");
                    else
                        pressEnterToContinue("Settings could not be saved. ");
                    break;
                case 41:
                    gL.applyDefaultSettings();
                    break;
                case 42:
//TODO make help for some menu items
                    System.out.println("TODO... Help for some menu items will go here"); //TODO
                    break;
                default:
                    pressEnterToContinue("Invalid input. Input integer at left of menu item\n");
            } //end process settings menu choice
        } //end while display menu
    } //end display menu method

    /**
     * Displays the Shoe State Menu
     */
    private void displayMenuShoeState()
    {
        int choice;
        Settings s = gL.getSettings();
        Shoe shoe = gL.getShoe();

        menu:
        while(true)
        { 
            System.out.println(lineBreak('-', 21) + " Perfect Play BlackJack : Shoe State " + 
                    lineBreak('-', 22));
            System.out.println(" 0. Back");
            System.out.println(" 1. Display current Shoe Card Counts");
            System.out.println(" 2. Display current Shoe Permutation");
            System.out.println(" 3. Edit Shoe Contents");
            System.out.println(" 4. Refill Shoe (" + shoe.size() + " / " + shoe.capacity() + " cards)");
            switch (s.shoeShuffleType())
            {
                case Shoe.SHUFFLE_TYPE_NONE:
                    break;
                case Shoe.SHUFFLE_TYPE_PSEUDORANDOM:
                    System.out.println(" 5. Shuffle Shoe (Pseudorandom)"); 
                    break;
                case Shoe.SHUFFLE_TYPE_CUT_SPLIT_MERGE_LOOP:
                    System.out.print(" 5. Shuffle Shoe ("); 
                    System.out.print(s.shoeCountShuffleCuts() + " cuts, split, merge, ");
                    System.out.println(s.shoeCountShuffleLoops() + " times)");
            }
            System.out.println(" 6. Help x");
            System.out.println(lineBreak('-', 80));

            choice = (int)getConsoleLong("Choice: ");
            switch(choice)
            {
                case 0:
                    break menu;
                case 1:
                    displayShoe(shoe);
                    break;
                case 2:
                    displayShoePermutation(shoe);
                    break;
                case 3:
                    editShoeContents(shoe);
                    break;
                case 4:
                    gL.refillShoe();
                    break;
                case 5:
                    gL.shuffleShoe();
                    displayShoePermutation(shoe);
                    break;
                case 6:
//TODO create help msg for some menu items
                    System.out.println("TODO... display help msg for some menu items");
                default:
                    System.out.println("Invalid choice");
            }

        }
    }

    /**
     * Displays the Shoe's current permutation (ie ordering), 13 Cards per line
     * eg [2C][3D][4H][5S][6C][7D][8H][9S][XC][JD][QH][KS][AC]
     * @param shoe the Shoe
     */
    private void displayShoePermutation(Shoe shoe)
    {
        Card c;
        Shoe shoeClone = (Shoe)shoe.clone();
        
        for(int idx = 0; shoeClone.size() > 0; idx++)
        {
            if(idx != 0 && idx % 13 == 0)  //13 Cards printed per row
                System.out.println();
            c = shoeClone.next();
            shoeClone.remove(c);
            System.out.print("[" + cardChar(c.playerType()) + SUIT_CHARS[c.suit()] + "]");
        }
        System.out.println("\nShoe size: " + shoe.size() + " / " + shoe.capacity());
    }




    /**
     * Gets a valid long boolean from the console
     */
    private boolean getConsoleBoolean(String message)
    {
        while(true)
        {
            System.out.print(message);
            String line = console.nextLine();

            if(line.length() > 0)
                if(line.charAt(0) == 't' || line.charAt(0) == 'T' ||
                        line.charAt(0) == 'y' || line.charAt(0) == 'Y')
                    return true;
                else if(line.charAt(0) == 'f' || line.charAt(0) == 'F' ||
                        line.charAt(0) == 'n' || line.charAt(0) == 'N')
                    return false;
                else
                    System.out.println("Invalid input. Please try again");
        }
    }


    /**
     * Gets a valid double from the console
     */
    private double getConsoleDouble(String message)
    {
        while(true)
        {
            System.out.print(message);
            String line = console.nextLine();
           
            try
            {
                return Double.parseDouble(line);
            } catch (Exception e)
            {
                System.out.println("Not a valid double, please try again"); 
            }
        }
    }


    /**
     * Gets a valid long integer from the console
     */
    private long getConsoleLong(String message)
    {
        while(true)
        {
            System.out.print(message);
            String line = console.nextLine();
           
            try
            {
                return Long.parseLong(line);
            } catch (Exception e)
            {
                System.out.println("Not a valid integer, please try again");
            }
        }
    }




    /**
     * Generates a String of some character and length
     */
    private String lineBreak(char character, int count)
    {
        char[] result = new char[count];
        for(int i = 0; i < count; i++)
            result[i] = character;
        return new String(result);
    }

    /**
     * Pauses output, requests user to press "Enter" key to continue
     */
    private void pressEnterToContinue(String msg)
    {
        System.out.print(msg + "Press Enter to continue...");
            console.nextLine();
    }




    /**
     * Displays the About-Optimal-Play-BlackJack message
     */
    private void printAbout()
    {   
        System.out.println("\nBlackJack Card-Counter Trainer v1.0\n");
//        System.out.println("This program is designed to teach the art of card-counting");
//        System.out.println("through development and evaluation of card-counting strategies.\n");
        System.out.println("The program uses a minimax recursive strategy with alpha-beta pruning.\n");
        System.out.println("All 6.64*10^37 possible card permutations are evaluated.\n");
//        System.out.println("Designed and developed by Andrew Ryan (contact details on FaceBook)\n");
        pressEnterToContinue("");
    }



    /**
     * Displays Top-Level Menu. Prints Goodbye message on program exit
     */
    private void run()
    {   
        displayMenuTopLevel();
        System.out.println("Goodbye, have a great day");        
    }

    //---------------------------------------- INNER CLASSES ---------------------------------------
    /**
     * Auto Play Simulation keyboard monitor.
     * 
     * When user presses "Enter", informs GameLogic. GameLogic will then request whether user
     * wishes to continue or quit simulation
     */
    private class UserInputMonitor implements Runnable
    {
        public void run()
        {
            Scanner console = new Scanner(System.in);
            console.nextLine();                  //just blocks waiting for input
            gL.pauseAutoPlayGame();
            if(!gL.isPaused()){
            	console.close();
            }
        }
    }
}