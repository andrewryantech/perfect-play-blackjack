//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
package ryan.blackjack.domain;

import ryan.blackjack.view.UserInterface;
import ryan.blackjack.dataAccess.SettingsDA;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Random;

/**
 * Contains the BlackJack game logic 
 */
public final class GameLogic
{
    //----------------------------------------- Constants ------------------------------------------
    public static final int     GAME_TYPE_USER_SPECIFIED = 0;
    public static final int     GAME_TYPE_RANDOMLY_DEALT = 1;
    public static final int     GAME_TYPE_AUTO_PLAY      = 2;
    public static final int     SURRENDER_TYPE_NONE      = 0;
    public static final int     SURRENDER_TYPE_LATE      = 1;
    public static final int     SURRENDER_TYPE_EARLY     = 2;
    public static final int     COUNT_MIN_PLAYERS        = 1;
    public static final int     COUNT_MAX_PLAYERS        = 35;


    //------------------------------------------ Defaults ------------------------------------------
    public static final int     DEFAULT_AUTOPLAY_DISPLAY_DELAY = 0;  //so user can track going ons
    public static final int     DEFAULT_SURRENDER_TYPE = SURRENDER_TYPE_NONE;
    public static final boolean DEFAULT_DEALER_OFFERS_INSURANCE = true;
    public static final boolean DEFAULT_DEALER_OFFERS_EVEN_MONEY = true;
    public static final boolean DEFAULT_DEALER_HITS_SOFT_17 = false;
    public static final boolean DEFAULT_DEALER_WINS_TIES = false;
    public static final boolean DEFAULT_ORIGINAL_BETS_ONLY = false;
    public static final boolean DEFAULT_FIVE_CARD_CHARLIE_WINS = false;
    public static final boolean DEFAULT_FIVE_CARD_21_WINS = false;


    //------------------------------------ Instance Attributes -------------------------------------
    private UserInterface           ui;                     //a UserInterface 
    private Settings                settings;               //current System settings
    private Shoe                    shoe;                   //not changed during probability evaluation
    private int                     gameType;               //current Game Type
    private DealerHand              dH;                     //the dealer's Hand
    private ArrayList<Player>       players;                //array of Players at table
    private Player                  player;                 //the current Player
    private PlayerHand              pH;                     //the current PlayerHand
    private int[]                   cardTypeCounts10;       //get from shoe to evaluate probability
    private int[]                   cardTypeCounts13;       //get from shoe to evaluate probability
    private int                     shoeSize;               //get from shoe to evaluate probability
    private HashMap<Integer,Double> playerHandROIs;         //ROIs for each sub-hand of pH
    private HashMap<Integer,Double> playerStandHandROIs;    //ROIs for each stand sub-hand of pH
    private HashMap<Integer,Double> dealerHandROIs;         //ROIs for each sub-Hand of dh
    private boolean                 playNextRound;          //whether to continue game
    private boolean                 gamePaused;             //if set, user asked to quit or continue
    private int                     countRounds;            //count of game rounds




    /**
     * Constructor
     */
    public GameLogic(UserInterface ui)
    {
        this.ui = ui;                                       //so can message
        settings = SettingsDA.getInstance().loadSettings(); //current settings
        shoe = new Shoe(settings);                          //the game's shoe
        cardTypeCounts10 = new int[10];                     //values updated start dealerTurn
        playerHandROIs = new HashMap<Integer,Double>();     //cleared after state changed
        playerStandHandROIs = new HashMap<Integer,Double>();//cleared after state changed
        dealerHandROIs = new HashMap<Integer,Double>();     //cleared after state changed
    }

    /**
     * Saves current Settings to file on disk
     */
    public void applyDefaultSettings()
    {
        settings = SettingsDA.getInstance().defaultSettings();
    }


    /**
     * Evaluates the dealer or player advantage for the current game settings and shoe state
     * 
     * Using a separate transposition table for each dealer face card, then printing all at once
     * rather than 1 at a time reduces running time from 84s to 27s.
     */
    public void evaluatePlayerAdvantage()
    {
        boolean           insrChc;                  //whether player should taken insurance
        double            insuranceROI;             //either 0.0 or positive if available and worth it
        double            probDenominator;          //always same value, so just calculate and store
        ArrayList<ROI>    rois;                     //ROI for each initial deal.
        ArrayList<Option> options;                  //Available Options for a initial deal
        Recommendations   recommendations;          //stores bestOption and insuranceChoice
        double            countD1, countP1, prob;   //used in probability evaluation

        refreshState();
        probDenominator = (double)shoeSize * (shoeSize - 1) * (shoeSize - 2);
        shoeSize -= 3;
        rois = new ArrayList<ROI>(14 * 13 / 2 * 13);
        options = new ArrayList<Option>();
        recommendations = new Recommendations();
        dH = new DealerHand();
        pH = new PlayerHand(0.0);

        for(int dC1Type = Card.TWO; dC1Type <= Card.PLAYER_ACE; dC1Type++)
        {
            clearPlayerHandROIs();                           //different tables for each dealer card
            countD1 = (double) cardTypeCounts13[dC1Type];
            cardTypeCounts13[dC1Type]--;
            dH.insert(Card.dealerType(dC1Type));

            for(int pC1Type = Card.TWO; pC1Type <= Card.PLAYER_ACE; pC1Type++)
            {
                countP1 = (double) cardTypeCounts13[pC1Type];
                cardTypeCounts13[pC1Type]--;
                pH.insert(pC1Type);

                for(int pC2Type = pC1Type; pC2Type <= Card.PLAYER_ACE; pC2Type++)
                {
                    if(countD1 > 0 && countP1 > 0 && cardTypeCounts13[pC2Type] > 0)
                    {
                        prob = countD1 * countP1 * cardTypeCounts13[pC2Type] / probDenominator;
                        cardTypeCounts13[pC2Type]--;
                        if(pC1Type != pC2Type)
                            prob *= 2.0;
                        pH.insert(pC2Type);

                        //Determine if taking insurance is a positive ROI
                        insuranceROI = settings.dealerOffersInsurance() ? 
                                Math.max(evaluateAcceptInsuranceROI(), 0.0) : 0.0;

                        //Take insurance if available and gives a positive ROI
                        insrChc = dH.isSingleAce() && insuranceROI > 0.0;
                
                        options.clear();

                        //if no insurance taken and surrender available, add Surrender Option
                        if(!insrChc && 
                                settings.surrenderType() == SURRENDER_TYPE_EARLY || 
                                (settings.surrenderType() == SURRENDER_TYPE_LATE && 
                                !settings.dealerDealtHoleCard()))
                            options.add(new Option(Option.SURRENDER, evaluateSurrenderROI()));

                        //if player is BJ, stand is BJ ROI, else stand is stand ROI
                        if(pH.isBlackJack())
                            options.add(new Option(Option.STAND, 
                                    evaluateBlackJackROI() + insuranceROI));
                        else
                            options.add(new Option(Option.STAND, evaluateStandROI() + insuranceROI));

                        options.add(new Option(Option.HIT, evaluateHitROI() + insuranceROI));

                        if(pH.isSplittable())
                            options.add(new Option(Option.SPLIT, 
                                    evaluateSplitROI() + insuranceROI));

                        if(pH.isDoublable())
                            options.add(new Option(Option.DOUBLE_DOWN, 
                                    evaluateDoubleROI() + insuranceROI));

                        Collections.sort(options);

                        rois.add(new ROI(prob, options.get(0).roi()));
                        recommendations.insert(dC1Type, pC1Type, pC2Type, insrChc, options.get(0));
                    
                        pH.removeLast();
                        cardTypeCounts13[pC2Type]++;
                    }
                }
                pH.removeLast();
                cardTypeCounts13[pC1Type]++;
            }
            dH.removeLast();
            cardTypeCounts13[dC1Type]++;
        }

        double totalPlayerROI = ROI.merge(rois.toArray(new ROI[rois.size()]));
        ui.displayPlayerAdvantage(settings, shoe, recommendations, totalPlayerROI);
        shoeSize += 3;                                             //leave state consistent
       


/* OLD METHOD = 84s, prints 1 at at time
        boolean takeInsurance;                      //whether player should taken insurance
        double insuranceROI;                        //either 0.0 or +ve if available and worth it
        String option;                              //Recommended Option (null if deal impossible)
        ArrayList<Option> options = new ArrayList<Option>();
 
        ArrayList<ROI> rois = new ArrayList<ROI>();
        double prob1, prob2, prob3, prob;
        Card card;
        int dC0;

        ui.displayShoe(shoe);

System.out.println("start: " + System.nanoTime());

        for(int pC0 = Card.TWO; pC0 <= Card.PLAYER_ACE; pC0++)    
        {
            for(int pC1 = pC0; pC1 <= Card.PLAYER_ACE; pC1++)
            {
                for(int dC = Card.TWO; dC <= Card.PLAYER_ACE; dC++)
                {
                    //determine if this deal is possible
                    refreshState();

                    prob1 = (double)cardTypeCounts13[pC0] / shoeSize;
                    cardTypeCounts13[pC0]--;
                    shoeSize--;

                    prob2 = (double)cardTypeCounts13[pC1] / shoeSize;
                    cardTypeCounts13[pC1]--;
                    shoeSize--;

                    prob3 = (double)cardTypeCounts13[dC] / shoeSize;
                    cardTypeCounts13[dC]--;
                    shoeSize--;

                    prob = prob1 * prob2 * prob3;

                    //if this deal was possible
                    if(cardTypeCounts13[pC0] >= 0 && cardTypeCounts13[pC1] >= 0 && 
                            cardTypeCounts13[dC] >= 0)
                    {
                        pH = new PlayerHand(0.0);        //new Player Hand with $0.00 bet
                        card = new Card(pC0, shoe.probableSuit(pC0, shoe.REMOVE));
                        shoe.remove(card);
                        pH.insertCard(card);
                        
                        card = new Card(pC1, shoe.probableSuit(pC1, shoe.REMOVE));
                        shoe.remove(card);
                        pH.insertCard(card);

                        dH = new DealerHand();
                        card = new Card(dC, shoe.probableSuit(dC, shoe.REMOVE));
                        shoe.remove(card);
                        dH.insertCard(card);

                        refreshState();
                        clearPlayerHandROIs();

                        //if player is BJ, dealer is A, && dealer offers even money
                        if(pH.isBlackJack() && dH.isSingleAce() && 
                                settings.dealerOffersEvenMoney() &&
                                shoe.probability10Value() > 1.0 / 3.0)
                        {
                            if(pC0 != pC1)       //if not a pair, twice the chance of occurrence
                                prob *= 2.0;
                            rois.add(new ROI(prob, 1.0));
                            ui.displayRecomendation(pC0, pC1, dC, false, Option.ACCEPT_EVEN_MONEY);
                        }
                        else
                        { 
                            insuranceROI = settings.dealerOffersInsurance() ? 
                                    Math.max(evaluateAcceptInsuranceROI(), 0.0) : 0.0;
                              
                            takeInsurance = dH.isSingleAce() && insuranceROI > 0.0;
                
                            options.clear();


                            //if no insurance taken and surrender available
                            if(!takeInsurance && 
                                    settings.surrenderType() == SURRENDER_TYPE_EARLY || 
                                    (settings.surrenderType() == SURRENDER_TYPE_LATE && 
                                    !settings.dealerDealtHoleCard()))
                                options.add(new Option(Option.SURRENDER, evaluateSurrenderROI()));

                            if(pH.isBlackJack())
                                options.add(new Option(Option.STAND, 
                                        evaluateBlackJackROI() + insuranceROI));
                            else
                                options.add(new Option(Option.STAND, 
                                        evaluateStandROI() + insuranceROI));
                            options.add(new Option(Option.HIT, evaluateHitROI() + insuranceROI));
                            if(pH.isSplittable())
                                options.add(new Option(Option.SPLIT, 
                                        evaluateSplitROI() + insuranceROI));
                            if(pH.isDoublable())
                                options.add(new Option(Option.DOUBLE_DOWN, 
                                        evaluateDoubleROI() + insuranceROI));

                            Collections.sort(options);
                          
                            ui.displayRecomendation(pC0, pC1, dC, takeInsurance, 
                                    options.get(0).name());

                            //if player does not have a pair, double the probability of occurence
                            if(pC0 != pC1)
                                prob *= 2.0;

                            rois.add(new ROI(prob, options.get(0).roi()));
                        }

                        //if we removed cards from shoe, we must put them back
                        shoe.insert(pH.cardAt(0));
                        shoe.insert(pH.cardAt(1));
                        shoe.insert(dH.cardAt(0));

                    }
                    else
                        System.out.println("Shouldn't be here if shoe full...");
//UP TO HERE

                }
            }
        }
System.out.println("end: " + System.nanoTime());

        double totalPlayerROI = ROI.merge(rois.toArray(new ROI[rois.size()]));
        ui.displayPlayerAdvantage(totalPlayerROI);
*/
    }

    /**
     * Returns the shoe
     * 
     * @return the shoe
     */
    public Shoe getShoe()
    {
        return shoe;
    }

    /**
     * Returns the game's settings
     * 
     * @return the game's settings
     */
    public Settings getSettings()
    {
        return settings;
    }

    /**
     * Attempts to insert the specified card into the shoe
     * 
     * @param card the card to insert into the shoe
     * @return whether the card was successfully inserted.
     */
    public boolean insertShoeCard(Card card)
    {
        return shoe.insert(card);
    }

    /**
     * Pauses an Auto Play game. User will then be asked whether they wish to quit or continue
     */
    public void pauseAutoPlayGame()
    {
        gamePaused = true;
    }
    
    public boolean isPaused(){
    	return gamePaused;
    }

    /**
     * Plays a Game of BlackJack. Creates players, plays some rounds, then prints results
     * @param gameType User-Specified Cards, Randomly Dealt Cards, or Auto Play with Random Cards
     */
    public void playGame(int gameType)
    {
        this.gameType = gameType;
        createPlayers();               //gets their initial bet, CountStrategy, and BetStrategy
        
        if(gameType == GAME_TYPE_AUTO_PLAY)
        {
            gamePaused = false;
            ui.startAutoPlayMonitor(); //creates a 2nd thread to monitor if user wishes to quit game
        }

        playNextRound = true;
        countRounds = 0;
        while(playNextRound)           //play multiple rounds, until user decides to finish
        {
            playRound();

            if(gamePaused)             //only Auto Play can pause game. If set, monitor is finished
            {
                playNextRound = ui.getChoiceContinueGame();
                if(playNextRound)
                {
                    gamePaused = false;
                    ui.startAutoPlayMonitor();   //prev monitor is done, so create new one
                }
            }
        }

        ui.displayGameStatistics(countRounds, players, settings);  //display results of simulation
    }
        



    /**
     * Refills the shoe to its current capacity. Does not shuffle.
     */
    public void refillShoe()
    {
        shoe.refill();
    }


    /**
     * Gets cardTypeCounts13 and shoeSize from shoe, and copies them to static variables.
     * These are then used for all probability calculations. This must be called whenever
     * a card is given to a player (hence cardTypeCounts and shoeSize has changed)
     */
    private void refreshState()
    {
        cardTypeCounts13 = shoe.cardTypeCounts();
        shoeSize = shoe.size();
    }

    /**
     * Clears PlayerHandROIs transposition tables. This must be called after any
     * card is given to a player or dealer, or after dealer peeks
     */
    private void clearPlayerHandROIs()
    {
        playerHandROIs.clear();
        playerStandHandROIs.clear();
    }

    /**
     * Attempts to remove the specified card fromthe shoe
     * @param card the card to remove from the shoe
     * @return whether the card was successfully removed
     */
    public boolean removeShoeCard(Card card)
    {
        return shoe.remove(card);
    }

    /**
     * Saves current Settings to file on disk
     * @return whether the save was successful
     */
    public boolean saveSettings()
    {
        return SettingsDA.getInstance().saveSettings(settings);
    }




    /**
     * Sets the minimum delay between displaying steps in AutoPlay simulation
     * @param delay delay in ms. Must be between 0 and 10000
     */
    public void setAutoPlayDisplayDelay(int delay)
    {
        int autoPlayDelay = delay;
        if(delay < 0)
            autoPlayDelay = 0;
        else if(delay > 10000)
            autoPlayDelay = 10000;
        settings.setAutoPlayDisplayDelay(autoPlayDelay);
    }

    /**
     * Sets the winnings on Player BlackJack win from a $1 bet
     * @param bjRate winnings on Player BlackJack win from a $1 bet
     */
    public void setBlackJackROI(double bjRate)
    {
        settings.setBlackJackROI(bjRate);
    }

    /**
     * Sets the Custom Count Strategy Running Count adjustments
     * @param values for each card by which to adjust running count 
     */
    public void setCustomCountStrategyAdjustments(double[] adjustments)
    {
        settings.setCustomCountAdjustments(adjustments); //settings updates CountStrategy class var
    }

    /**
     * Sets the Custom Count Strategy name
     * @param name the Custom Count Strategy name
     */
    public void setCustomCountStrategyName(String name)
    {
        settings.setCustomCountName(name);   //settings updates CountStrategy static var
    }



    /**
     * Sets doublableCardCounts
     * @param doublableCardCounts
     */
    public void setDoublableCardCounts(TreeSet<Integer> doublableCardCounts)
    {
        settings.setDoublableCardCounts(doublableCardCounts); //callee will update BetStrategy
    }

    /**
     * Sets doublableScores
     * @param doublableScores
     */
    public void setDoublableScores(TreeSet<Integer> doublableScores)
    {
        settings.setDoublableScores(doublableScores); //callee will update BetStrategy
    }

    /**
     * Sets fiveCard21Amount
     * @param fiveCard21Amount
     */
    public void setFiveCard21Amount(double fiveCard21Amount)
    {
        settings.setFiveCard21Amount(fiveCard21Amount); //callee will update ROI
    }

    /**
     * Sets the table's maximum bet. Will not permit maxBet to be less than minBet
     * @param maxBet table's maximum bet
     */
    public void setMaxBet(double maxBet)
    {
        settings.setMaxBet(Math.max(maxBet,settings.minBet())); //callee will update BetStrategy
    }

    /**
     * Sets the table's minimum bet. Will not permit minBet to exceed maxBet
     * @param minBet table's minimum bet
     */
    public void setMinBet(double minBet)
    {
        settings.setMinBet(Math.min(minBet,settings.maxBet())); //callee will update BetStrategy
    }

    /**
     * Sets the shoe's capacity in cards
     * @param shoeCapacity how many cards can fit in shoe
     */
    public void setShoeCapacity(int shoeCapacity)
    {
        settings.setShoeCapacity(shoeCapacity);
        shoe.setCapacity(shoeCapacity);   //shoe will refill and reset to avoids RnCnt errors
    }


    /**
     * Sets the shoe's maxPenetration. Doesn't require shoe refill or reshuffle.
     * @param maxPenetration
     */
    public void setShoeMaxPenetration(double maxPenetration)
    {
        settings.setShoeMaxPenetration(maxPenetration);
        shoe.setMaxPenetration(maxPenetration);
    }



    /**
     * Sets the shoe's shuffleType. Refills, but does not shuffle shoe
     * @param shuffleType how to shuffle shoe
     * @param countCuts only used for type CUT_SPLIT_MERGE_LOOP
     * @param countLoops only used for type CUT_SPLIT_MERGE_LOOP
     */
    public void setShoeShuffleType(int shuffleType, int countCuts, int countLoops)
    {
        settings.setShoeShuffleType(shuffleType);
        if(shuffleType == Shoe.SHUFFLE_TYPE_CUT_SPLIT_MERGE_LOOP)
        {
            settings.setShoeCountShuffleCuts(countCuts);
            settings.setShoeCountShuffleLoops(countLoops);
        }
        shoe = new Shoe(settings);
    }


    /**
     * Sets the winnings on Player Split BlackJack win from a $1 bet
     * @param bjRate winnings on Player Split BlackJack win from a $1 bet
     */
    public void setSplitBlackJackROI(double sbjRate)
    {
        settings.setSplitBlackJackROI(sbjRate);
    }

    /**
     * Sets the whether pairs are splittable by equal value or equal rank
     * @param splitType SPLIT_BY_VALUE or SPLIT_BY_RANK
     */
    public void setSplitCardEqualityType(int splitType)
    {
        settings.setSplitCardEqualityType(splitType); //callee will update static var
    }





    /**
     * Sets the Surrender Type.
     * If dealer does not receive a hole card, both late and early surr are simply "On"
     * @param surrType the type of surrender offered by dealer during intial deal
     */
    public void setSurrenderType(int surrType)
    {
        settings.setSurrenderType(surrType);
    }


    /**
     * Sets the number of times we can split a pairs of 2s to Kings can be split
     * @param timesSplittable times a pair of 2s to Kings can be split
     */
    public void setTimes2toK_Splittable(int timesSplittable)
    {
        settings.setTimes2toK_Splittable(timesSplittable); //callee will update static var
    }

    /**
     * Sets the number of times we can split a pair of Aces can be split
     * @param timesSplittable times a pair of Aces can be split
     */
    public void setTimesAcesSplittable(int timesSplittable)
    {
        settings.setTimesAcesSplittable(timesSplittable); //callee will update static var
    }


    /**
     * Shuffles the shoe
     */
    public void shuffleShoe()
    {
        shoe.shuffle();
    }



    /**
     * Toggles whether Player can double down after splitting a pair of 2s to Kings
     */
    public void toggleCanDoubleAfter2toKingSplit()
    {
        if(settings.canDoubleAfter2toK_Split())
            settings.setCanDoubleAfter2toK_Split(false);
        else
            settings.setCanDoubleAfter2toK_Split(true);
    }


    /**
     * Toggles whether Player can double down after splitting a pair of Aces
     */
    public void toggleCanDoubleAfterAcesSplit()
    {
        if(settings.canDoubleAfterAcesSplit())
            settings.setCanDoubleAfterAcesSplit(false);
        else
            settings.setCanDoubleAfterAcesSplit(true);
    }

    /**
     * Toggles whether Player can hit after they have split a pair of Aces.
     */
    public void toggleCanHitAfterAcesSplit()
    {
        if(settings.canHitAfterAcesSplit())
            settings.setCanHitAfterAcesSplit(false);
        else
            settings.setCanHitAfterAcesSplit(true);
    }


    /**
     * Toggles whether Dealer hits a soft 17
     */
    public void toggleDealerHitsSoft17()
    {
        if(settings.dealerHitsSoft17())
            settings.setDealerHitsSoft17(false);
        else
            settings.setDealerHitsSoft17(true);
    }


    /**
     * Toggles whether a Player is offered Insurance if Dealer has a single Ace
     */
    public void toggleDealerOffersInsurance()
    {
        if(settings.dealerOffersInsurance())
            settings.setDealerOffersInsurance(false);
        else
            settings.setDealerOffersInsurance(true);
    }


    /**
     * Toggles whether a Player is offered even money of their BJ if dealer has single Ace
     */
    public void toggleDealerOffersEvenMoney()
    {
        if(settings.dealerOffersEvenMoney())
            settings.setDealerOffersEvenMoney(false);
        else
            settings.setDealerOffersEvenMoney(true);
    }

    /**
     * Toggles whether Dealer dealt a Hole Card
     */
    public void toggleDealerDealtHoleCard()
    {
        if(settings.dealerDealtHoleCard())
            settings.setDealerDealtHoleCard(false);
        else
            settings.setDealerDealtHoleCard(true);
    }



    /**
     * Toggles whether Dealer wins ties
     */
    public void toggleDealerWinsTies()
    {
        if(settings.dealerWinsTies())
            settings.setDealerWinsTies(false);
        else
            settings.setDealerWinsTies(true);
    }


    /**
     * Toggles whether the Ace count and concentration is displayed prior to each round
     */
    public void toggleDisplayAceGuage()
    {
        if(settings.displayAceGuage())
            settings.setDisplayAceGuage(false);
        else
            settings.setDisplayAceGuage(true);
    }


    /**
     * Toggles whether Player BlackJack chance stats are displayed prior to each round
     */
    public void toggleDisplayPlayerBJGuage()
    {
        if(settings.displayPlayerBJGuage())
            settings.setDisplayPlayerBJGuage(false);
        else
            settings.setDisplayPlayerBJGuage(true);
    }


    /**
     * Toggles whether the specified cardStrategy count is displayed prior to each round
     * @param countStrat a CountStrategy
     */
    public void toggleDisplayCountStrategy(int countStrat)
    {
        boolean[] displayableCountStrategies = settings.displayableCountStrategies();
        if(displayableCountStrategies[countStrat])
            settings.setDisplayableCountStrategies(countStrat, false);
        else
            settings.setDisplayableCountStrategies(countStrat, true);
    }


    /**
     * Toggles whether a non-bust Player five-card 21 is an automatic win, and bonus
     */
    public void toggleFiveCard21Wins()
    {
        if(settings.fiveCard21Wins())
            settings.setFiveCard21Wins(false);
        else
            settings.setFiveCard21Wins(true);
    }
    
    /**
     * Toggles whether a non-bust Player five-card 21 is an automatic win, and bonus
     */
    public void toggleShowBankHistoryAfterGame()
    {
        if(settings.showBankHistory())
            settings.setShowBankHistory(false);
        else
            settings.setShowBankHistory(true);
    }
    
    
    


    /**
     * Toggles whether a non-bust five-card Player Hand is an automatic win
     */
    public void toggleFiveCardCharlieWins()
    {
        if(settings.fiveCardCharlieWins())
            settings.setFiveCardCharlieWins(false);
        else
            settings.setFiveCardCharlieWins(true);
    }


    /**
     * Toggles whether all subsequent (ie not original) bets are returned if Dealer has BlackJack
     */
    public void toggleOriginalBetsOnly()
    {
        if(settings.originalBetsOnly())
            settings.setOriginalBetsOnly(false);
        else
            settings.setOriginalBetsOnly(true);
    }


    /**
     * Toggles whether a Player's cards are dealt face UP or face DOWN
     */
    public void togglePlayerHandDealtFaceUp()
    {
        if(settings.playerHandDealtFaceUp())
            settings.setPlayerHandDealtFaceUp(false);
        else
            settings.setPlayerHandDealtFaceUp(true);
    }
    


    //--------------------------------- Private Instance Behaviour ---------------------------------

    /**
     * Gets number of players, and a start bank, CountStrategy, and BetStrategy for each.
     */
    private void createPlayers()
    {
        int countPlayers = ui.getCountPlayers(COUNT_MIN_PLAYERS, COUNT_MAX_PLAYERS);
        double[]      startBanks  = new double[countPlayers];
        int[]         countStrats = new int[countPlayers];
        BetStrategy[] betStrats   = new BetStrategy[countPlayers];

        //for each Player, get their initial Bank
        for(int pIdx = 0; pIdx < countPlayers; pIdx++)
            startBanks[pIdx] = ui.getPlayerBank(pIdx);

        //for each Player, get their chosen CountStrategy. 
        ui.displayCountStrategies();
        for(int pIdx = 0; pIdx < countPlayers; pIdx++)
            countStrats[pIdx] = ui.getCountStrategy(pIdx);

        //Ensure selected Count Strategies are displayable
        boolean[] displayableCountStrategies = settings.displayableCountStrategies();
        for(int countStrat = 0; countStrat < displayableCountStrategies.length; countStrat++)
            for(int cS : countStrats)   //for each player
                if(countStrat == cS)    //if player has this CountStrategy
                    displayableCountStrategies[countStrat] = true;
        for(int cntStrt = 0; cntStrt < displayableCountStrategies.length; cntStrt++)
            settings.setDisplayableCountStrategies(cntStrt, displayableCountStrategies[cntStrt]);

        //determine if we must display BetStrategies
        boolean anyPlayerHasCountStrategy = false;
        for(int pIdx = 0; pIdx < countPlayers; pIdx++)
            if(countStrats[pIdx] != CountStrategy.NONE)
            {
                anyPlayerHasCountStrategy = true;
                break;
            }

        //for each Player, get their BetStrategy
        if(anyPlayerHasCountStrategy)
            ui.displayBetStrategies();
        for(int pIdx = 0; pIdx < countPlayers; pIdx++)
        {
            if(countStrats[pIdx] == CountStrategy.NONE)
                betStrats[pIdx] = new BetStrategyUserSpecified(ui, pIdx);
            else
                betStrats[pIdx] = ui.getBetStrategy(pIdx);
        }

        //for each Player, create Player
        players = new ArrayList<Player>();
        for(int pIdx = 0; pIdx < countPlayers; pIdx++)
            players.add(new Player(startBanks[pIdx], countStrats[pIdx], betStrats[pIdx]));

    }


    /**
     * Deals cards until dealer busts or stands. Ensures that if card is randomly dealt, and
     * dealer has confirmed not blackJack, that next card won't violate that peek.
     * If dealer gets 2nd card and is not BJ, and all players have BJ, then return as dealer 
     * knows they have lost.
     */
    private void dealerAction()
    {

        //Test if all PlayerHands are BlackJack
        boolean allPlayerHandsAreBlackJack = true;
        ListIterator<PlayerHand> itr;
        for(int pIdx = 0; pIdx < players.size() && allPlayerHandsAreBlackJack; pIdx++)
        {
            itr = players.get(pIdx).hands();
            while(itr.hasNext() && allPlayerHandsAreBlackJack)
                allPlayerHandsAreBlackJack = itr.next().isBlackJack();
        }

        ui.setFocusDealer();

        while(dH.score() < 17 || (dH.score() == 17 && dH.isSoft() && settings.dealerHitsSoft17()))
        {
            //if all Player hands are BJ, and dealer can't be, just return
            if(allPlayerHandsAreBlackJack && dH.countCards() == 2 && !dH.isBlackJack())
                return;

            ui.displayHand(dH);

            //ensure next card from shoe doesn't violate dealer's peek and confirmed not BJ
            if(dH.confirmedNotBlackJack())
                if(dH.isSingle10())
                    while(shoe.next().value() == 11)
                        shoe.shuffle();
                else if(dH.isSingleAce())
                    while(shoe.next().value() == 10)
                        shoe.shuffle();

            Card card = ui.getCard(dH.countCards(), shoe); //try to get a valid Card

            //ensure user-selected card doesn't violate dealer's peek and confirmed not BJ
            while(dH.confirmedNotBlackJack() && (dH.isSingle10() && card.value() == 11 ||
                    dH.isSingleAce() && card.value() == 10))
            {
                ui.displayErrorPeekViolation(card);
                card = ui.getCard(dH.countCards(), shoe);  //retry getting a valid Card
            } 

            shoe.remove(card);                               //remove Card from shoe
            dH.insertCard(card);                             //insert Card into Player's Hand
            refreshState();                                  //refresh countCards13 and shoeSize
            clearPlayerHandROIs();                           //becuase shoe has changed
        }

    }


    /**
     * Evluates static PlayerHand against recrusive DealerHand
     * Never called when playerHand is BlackJack, becuase if it was, it would call 
     * evaluateBlackJacKROI() or evaluateSplitBlackJackROI(), respectively.
     */
    private double dealerTurnROI()
    {
        //Base Cases                                         //TODO, try switching ordering
        if(dH.isBust())
            return ROI.win();
        else if(dH.isBlackJack())
            if(pH.isBlackJack() && !settings.dealerWinsTies())
                return ROI.draw();
            else
                return ROI.loss();
        else if(dH.score > 17 || dH.score() == 17 && (!settings.dealerHitsSoft17() || !dH.isSoft()))
            if(pH.score() > dH.score())
                return ROI.win();
            else if(pH.score() < dH.score() || settings.dealerWinsTies())
                return ROI.loss();
            else
                return ROI.draw();
        else if(dealerHandROIs.containsKey(dH.hashCode()))
            return dealerHandROIs.get(dH.hashCode());


        //Recursive Cases
        if(shoeSize == 0)
        {
            throw new IllegalStateException("Shoe empty (dealerTurn)");
        	
        }
        double prob;
        int hashCode = dH.hashCode();
        ROI[] rois = new ROI[Card.COUNT_DEALER_CARD_TYPES];

        //if dealer has peeked at A or 10-value, and continued, we know dealer doesn't have BJ
        if(dH.confirmedNotBlackJack() && dH.countCards() == 1)   
        {
            for(int cardType = Card.TWO; cardType <= Card.DEALER_ACE; cardType++)
            {
                //only process possible next Card. We know dealer's hand is single A or 10
                if(cardTypeCounts10[cardType] > 0 &&
                        (dH.score() == 10 && cardType != Card.DEALER_ACE ||     //can have [X][!A]
                        dH.score() == 11 && cardType != Card.TEN))              //can have [A][!X]
                {
                    if(dH.score() == 10)
                        prob = (double)cardTypeCounts10[cardType] / 
                                (shoeSize - cardTypeCounts10[Card.DEALER_ACE]);
                    else
                        prob = (double)cardTypeCounts10[cardType] / 
                                (shoeSize - cardTypeCounts10[Card.TEN]);
                    cardTypeCounts10[cardType]--;
                    shoeSize--;
                    dH.insert(cardType);
                    rois[cardType] = new ROI(prob, dealerTurnROI());
                    dH.removeLast();
                    shoeSize++;
                    cardTypeCounts10[cardType]++;
                }
            }
        }
        else    
        {

            for(int cardType = Card.TWO; cardType <= Card.DEALER_ACE; cardType++)
            {
                if(cardTypeCounts10[cardType] > 0)
                {
                    prob = (double)cardTypeCounts10[cardType] / shoeSize;
//System.out.println(cardType + " " + cardTypeCounts10[cardType] + " " + shoeSize);
                    cardTypeCounts10[cardType]--;
                    shoeSize--;
                    dH.insert(cardType);
                    rois[cardType] = new ROI(prob, dealerTurnROI());
                    dH.removeLast();
                    cardTypeCounts10[cardType]++;
                    shoeSize++;
                }
            }
        }

        double totalROI = ROI.merge(rois);
        dealerHandROIs.put(hashCode, totalROI);
        return totalROI;        
    }



    /**
     * Deals initial Deal at beginning of Round.
     * Gives 2 cards to each player, 1 card to dealer. 
     * May offer insurance, even money, and/or surrender
     */
    private void dealInitialDeal()
    {
        //if player hand dealt face UP, deal first card
        if(settings.playerHandDealtFaceUp())
            for(int pIdx = 0; pIdx < players.size(); pIdx++)
            {
                pH = players.get(pIdx).hands().next();
                ui.setFocusPlayer(pIdx);                  //set focus on this player
                dealCard(pH);
            }    

        dH = new DealerHand();
        ui.setFocusDealer();
        dealCard(dH);                         //Deals face Card

        //if player hand dealt face UP, deal second card
        if(settings.playerHandDealtFaceUp())
            for(int pIdx = 0; pIdx < players.size(); pIdx++)
            {
                pH = players.get(pIdx).hands().next();
                ui.setFocusPlayer(pIdx);                  //set focus on this player
                dealCard(pH);
            }    

        if(dH.score() == 11)                    //if Dealer has [A]
        {
            //if player's cards face up and dealer Offers Even $, determine if offer should be made
            if(settings.playerHandDealtFaceUp() && settings.dealerOffersEvenMoney())
                offerEvenMoney();

            //Determine if insurance should be offered to players
            boolean anyPlayerHandsNotRedeemed = false;
            for(int pIdx = 0; pIdx < players.size(); pIdx++)
            {
                if(!players.get(pIdx).hands().next().isRedeemed())
                {
                    anyPlayerHandsNotRedeemed = true;
                    break;
                }
            }

            //If insurance should be offered
            if(settings.dealerOffersInsurance() && 
                    (!settings.playerHandDealtFaceUp() || anyPlayerHandsNotRedeemed))
                offerInsurance();
        }

        //if player's hand dealt face DOWN, deal 1st and 2nd cards to each player
        if(!settings.playerHandDealtFaceUp())
        {
            for(int pIdx = 0; pIdx < players.size(); pIdx++)
            {
                pH = players.get(pIdx).hands().next();
                ui.setFocusPlayer(pIdx);                  //set focus on this player
                dealCard(pH);
            }   
            for(int pIdx = 0; pIdx < players.size(); pIdx++)
            {
                pH = players.get(pIdx).hands().next();
                ui.setFocusPlayer(pIdx);                  //set focus on this player
                dealCard(pH);
            }   

            if(dH.isSingleAce() && settings.dealerOffersEvenMoney())
                offerEvenMoney();
        }

        //initial cards dealt to players. All players can share playerHandROIs to evaluate
        // each playerHand's unique surrender ROI.
        refreshState();  
        
        //If Early surrender Available (ie before dealer has checked hole card)
        if(settings.surrenderType() == SURRENDER_TYPE_EARLY)
            offerSurrender();

        //Test if dealer must play because any player Hand is !Surrendered & !Redeemed & !BJ
        boolean dealerActionRequired = false;
        for(int pIdx = 0; pIdx < players.size(); pIdx++)
        {
            pH = players.get(pIdx).hands().next();
            if(!pH.isSurrendered() && !pH.isRedeemed())
            {
                dealerActionRequired = true;
                break;
            }
        }

        //if dealer needs to peek at hole card
        if(dealerActionRequired && settings.dealerDealtHoleCard() && 
                (dH.score() == 10 || dH.score() == 11))
            peekAtDealerHoleCard();

        //If Late surrender Available and Dealer does not have BlackJack
        if(settings.surrenderType() == SURRENDER_TYPE_LATE && !dH.isBlackJack())
            offerSurrender();
    }


    /**
     * Deals a single card to a specified Hand (ie during game)
     * (never used by double down)
     * @param pH the Hand to insert card into
     */
    private void dealCard(Hand hand)
    {
        ui.displayHand(hand);                            //display the current PlayerHand
        Card card = ui.getCard(hand.countCards(), shoe); //get a Card
        shoe.remove(card);                               //remove Card from shoe
        hand.insertCard(card);                           //insert Card into Player's Hand
        refreshState();                                  //refresh countCards13 and shoeSize
        clearPlayerHandROIs();                           //becuase shoe has changed
    }



    /**
     * Evaluates the ROI if player accepts Even Money
     * @return ROI of accepting even money = 1.0
     */
    private double evaluateAcceptEvenMoneyROI()
    {
        return 1.0;
    }

    /**
     * Evaluates the ROI if player takes Insurance
     */
    private double evaluateAcceptInsuranceROI()
    {
        return shoe.probability10Value() * 3.0 - 1.0;
    }

    /**
     * Evaluates the ROI given that a player has BlackJack.
     * This is necessary because the dealerTurn does not account for the ROI difference
     * between a win and a BlackjackWin. Better for playerTurnROI() to check this, as it 
     * iterates less times than dealerTurn().  
     */
    private double evaluateBlackJackROI()
    {
        if(dH.confirmedNotBlackJack() || dH.score != 10 || dH.score != 11 || dH.countCards != 1)
            return ROI.blackJack();

        ROI[] rois = new ROI[2];      //ROIs for dealer being BJ and !BJ
        if(dH.score() == 10)          //dealer has [X]
        {
            double aceChance = (double) cardTypeCounts13[Card.PLAYER_ACE] / shoeSize;
            rois[0] = new ROI(aceChance, settings.dealerWinsTies() ? ROI.loss() : ROI.draw());
            rois[1] = new ROI(1 - aceChance, ROI.blackJack());
        }
        else                          //dealer has [A]
        {
            double tenChance = (double)(cardTypeCounts13[Card.TEN] + cardTypeCounts13[Card.JACK] + 
                    cardTypeCounts13[Card.QUEEN] +  cardTypeCounts13[Card.KING]) / shoeSize;
            rois[0] = new ROI(tenChance, settings.dealerWinsTies() ? ROI.loss() : ROI.draw());
            rois[1] = new ROI(1 - tenChance, ROI.blackJack());
        }
        return ROI.merge(rois);
    }


    /**
     * Evaluates the ROI if player hits current hand
     * Non-recursive driver.
     * More complex version if dH isConfirmedNotBlackJack
     */
    private double evaluateHitROI()
    {
        double prob;
        ROI[] rois = new ROI[Card.COUNT_PLAYER_CARD_TYPES];

        if(dH.confirmedNotBlackJack())                      //if dH is [A][?]!BJ or [X][?]!BJ    
        {
            double countNonTens = shoeSize - cardTypeCounts13[Card.TEN] - 
                    cardTypeCounts13[Card.JACK] - cardTypeCounts13[Card.QUEEN] - 
                    cardTypeCounts13[Card.KING];
            double countNonAces = shoeSize - cardTypeCounts13[Card.PLAYER_ACE];
            shoeSize--;                                         // Avoids ss++/ss-- and ?/(ss - 1)
            for(int cardType = Card.TWO; cardType <= Card.PLAYER_ACE; cardType++)
            {
                if(cardTypeCounts13[cardType] > 0)
                {
                    if(dH.score() == 10)                    //if dH is [A][?]!BJ
                        if(cardType != Card.PLAYER_ACE)     //if adding [2]-[K] to pH
                            prob = (cardTypeCounts13[cardType] - cardTypeCounts13[cardType] / 
                                    countNonAces) / shoeSize;
                        else                                //if adding [A] to pH
                            prob = (double)cardTypeCounts13[cardType] / shoeSize;
                    else                                    //if dH is [10][?]!BJ
                        if(Card.PLAYER_VALUES[cardType] != 10) //if adding [2]-[9],[A] to pH
                            prob = (cardTypeCounts13[cardType] - cardTypeCounts13[cardType] /
                                    countNonTens) / shoeSize;
                        else                                //if adding [X],[J],[Q],[K] to pH
                            prob = (double)cardTypeCounts13[cardType] / shoeSize;
                    cardTypeCounts13[cardType]--;
                    pH.insert(cardType);
                    rois[cardType] = new ROI(prob, playerTurnROI());
                    pH.removeLast();
                    cardTypeCounts13[cardType]++;
                }
            }
            shoeSize++;
        }
        else                       // else dH does not have hole-card or face-card != {A,10}
        {
            for(int cardType = Card.TWO; cardType <= Card.PLAYER_ACE; cardType++)
            {
                if(cardTypeCounts13[cardType] > 0)
                {
                    prob = (double)cardTypeCounts13[cardType] / shoeSize;
                    cardTypeCounts13[cardType]--;
                    shoeSize--;
                    pH.insert(cardType);
                    rois[cardType] = new ROI(prob, playerTurnROI());
                    pH.removeLast();
                    shoeSize++;
                    cardTypeCounts13[cardType]++;
                }
            }
        }
        return ROI.merge(rois);
    }


    /**
     * Evaluates the ROI if player refuses offer of Even Money.
     * Pre: Dealer is single Ace
     * @return the ROI if player refuses even money and plays on with BlackJack
     */
    private double evaluateRefuseEvenMoneyROI()
    {
        return (1.0 - shoe.probability10Value()) * settings.blackJackROI();
    }


    /**
     * Evaluates the ROI if player doesn't take Insurance
     * @return ROI 0.0 as no investment has been made
     */
    private double evaluateRefuseInsuranceROI()
    {
        return 0.0;
    }


    /**
     * Evaluates the ROI if player stands with current hand.
     * Non-recursive driver.
     * If this PlayerHand has been evaluated before, its ROI is retrieved from the transosition
     * table. Else the PlayerHand is compared against the set of possible non-bust DealerHands.
     */
    private double evaluateStandROI()
    {
        //base case. It may be already evaluated if we calculated nonSurrender options
        if(playerStandHandROIs.containsKey(pH.hashCode()))
            return playerStandHandROIs.get(pH.hashCode());

        dealerHandROIs.clear();                             //new transposition table for each PH
        updateDealerShoe();                                 //so that cardTypeCounts10 is accurate
        double roi = dealerTurnROI();

        playerStandHandROIs.put(pH.hashCode(), roi);
        return roi;
    }

    /**
     * Evaluates the ROI if player splits current hand
     * Non-recursive driver.
     */
    private double evaluateSplitROI()
    {
        pH.split();                           //ignore returned clone, not needed for calculation
        double splitROI = playerTurnROI();
        pH.unsplit();                         //decrements splitLevel, copies single Card
        return 2.0 * splitROI;
    }


    /**
     * Evaluates the ROI if player doubles current hand
     * Non-recursive driver.
     * This method's call to evaluateStandROI() will duplicate an equivalent call
     * from playerTurnROI(). Therefore both use the playerStandHandROIs<hashCode,ROI> table.
     */
    private double evaluateDoubleROI()
    {
        ROI[] rois = new ROI[Card.COUNT_PLAYER_CARD_TYPES];
        double prob;

        if(dH.confirmedNotBlackJack())                      //if dH is [A][?]!BJ or [X][?]!BJ    
        {
            double countNonTens = shoeSize - cardTypeCounts13[Card.TEN] - 
                    cardTypeCounts13[Card.JACK] - cardTypeCounts13[Card.QUEEN] - 
                    cardTypeCounts13[Card.KING];
            double countNonAces = shoeSize - cardTypeCounts13[Card.PLAYER_ACE];
            shoeSize--;                                         // Avoids ss++/ss-- and ?/(ss - 1)
            for(int cardType = Card.TWO; cardType <= Card.PLAYER_ACE; cardType++)
            {
                if(cardTypeCounts13[cardType] > 0)
                {
                    if(dH.score() == 10)                    //if dH is [A][?]!BJ
                        if(cardType != Card.PLAYER_ACE)     //if adding [2]-[K] to pH
                            prob = (cardTypeCounts13[cardType] - cardTypeCounts13[cardType] / 
                                    countNonAces) / shoeSize;
                        else                                //if adding [A] to pH
                            prob = (double)cardTypeCounts13[cardType] / shoeSize;
                    else                                    //if dH is [10][?]!BJ
                        if(Card.PLAYER_VALUES[cardType] != 10) //if adding [2]-[9],[A] to pH
                            prob = (cardTypeCounts13[cardType] - cardTypeCounts13[cardType] /
                                    countNonTens) / shoeSize;
                        else                                //if adding [X],[J],[Q],[K] to pH
                            prob = (double)cardTypeCounts13[cardType] / shoeSize;
                    cardTypeCounts13[cardType]--;
                    pH.insert(cardType);
                    if(pH.isBust())
                        rois[cardType] = new ROI(prob, ROI.loss());
                    else if(pH.countCards() == 5 && pH.score() == 21 && settings.fiveCard21Wins())
                        rois[cardType] = new ROI(prob, ROI.fiveCard21(pH.bet()));
                    else if(pH.countCards() == 5 && settings.fiveCardCharlieWins())
                        rois[cardType] = new ROI(prob, ROI.win());
                    else
                        rois[cardType] = new ROI(prob, evaluateStandROI()); //will query/update table
                    pH.removeLast();
                    cardTypeCounts13[cardType]++;
                }
            }
            shoeSize++;
        }
        else                       //else dH does not have hole-card or face-card != {A,10}
        {
            for(int cardType = Card.TWO; cardType <= Card.PLAYER_ACE; cardType++)
            {
                if(cardTypeCounts13[cardType] > 0)
                {
                    prob = (double)cardTypeCounts13[cardType] / shoeSize;
                    cardTypeCounts13[cardType]--;
                    shoeSize--;
                    pH.insert(cardType);
                    if(pH.isBust())
                        rois[cardType] = new ROI(prob, ROI.loss());
                    else if(pH.countCards() == 5 && pH.score() == 21 && settings.fiveCard21Wins())
                        rois[cardType] = new ROI(prob, ROI.fiveCard21(pH.bet()));
                    else if(pH.countCards() == 5 && settings.fiveCardCharlieWins())
                        rois[cardType] = new ROI(prob, ROI.win());
                    else
                        rois[cardType] = new ROI(prob, evaluateStandROI());
                    pH.removeLast();
                    shoeSize++;
                    cardTypeCounts13[cardType]++;
                }
            }
        }
        //no need to query/update transposition table of stood hands as evaluateStandROI() 
        // will do that for us.
        return 2.0 * ROI.merge(rois);
    }
    
    /**
     * Returns the ROI if player surrenders current hand
     * Non-recursive driver.
     */
    private double evaluateSurrenderROI()
    {
        return ROI.surrender();
    }


    /**
     * Evaluates the ROI if player does not surrender current hand
     * Non-recursive driver.
     * Gets the best available non-surrender option.
     * TODO. this should leave our trans. tables already complete!!
     */
    private double evaluateNonSurrenderROI()
    {
        ROI[] rois = new ROI[4];
        rois[0] = new ROI(1.0, evaluateStandROI());
        rois[1] = new ROI(1.0, evaluateHitROI());
        if(pH.isSplittable())
            rois[2] = new ROI(1.0, evaluateSplitROI());
        if(pH.isDoublable())
            rois[3] = new ROI(1.0, evaluateDoubleROI());
        return ROI.max(rois);
    }


    /**
     * Evaluates the ROI given that a player has splitBlackJack.
     * This is necessary because the dealerTurn does not account for the ROI difference
     * between a win and a splitBlackjackWin. Better for playerTurnROI() to check this, as it 
     * iterates less times than dealerTurn().  
     */
    private double evaluateSplitBlackJackROI()
    {
        if(dH.confirmedNotBlackJack() || dH.score != 10 || dH.score != 11 || dH.countCards != 1)
            return ROI.splitBlackJack();

        ROI[] rois = new ROI[2];      //ROIs for dealer being BJ and !BJ
        if(dH.score() == 10)          //dealer has [X]
        {
            double aceChance = (double) cardTypeCounts13[Card.PLAYER_ACE] / shoeSize;
            rois[0] = new ROI(aceChance, settings.dealerWinsTies() ? ROI.loss() : ROI.draw());
            rois[1] = new ROI(1 - aceChance, ROI.splitBlackJack());
        }
        else                          //dealer has [A]
        {
            double tenChance = (double)(cardTypeCounts13[Card.TEN] + cardTypeCounts13[Card.JACK] + 
                    cardTypeCounts13[Card.QUEEN] +  cardTypeCounts13[Card.KING]) / shoeSize;
            rois[0] = new ROI(tenChance, settings.dealerWinsTies() ? ROI.loss() : ROI.draw());
            rois[1] = new ROI(1 - tenChance, ROI.splitBlackJack());
        }
        return ROI.merge(rois);
    }


    /**
     * Offers Even Money to each Player that has BlackJack
     * If even money taken, any insurance taken is cancelled
     * Pre: Dealer has single Ace && dealerOffersEvenMoney()
     */
    private void offerEvenMoney()
    {
        boolean anyPlayerIsBlackJack = false;
        for(int pIdx = 0; pIdx < players.size(); pIdx++)
            if(players.get(pIdx).hands().next().isBlackJack())
            {
                anyPlayerIsBlackJack = true;
                break;
            }

        //if no player is BlackJack, don't offer Even Money, just return
        if(!anyPlayerIsBlackJack)
            return;
 
        ui.displayOfferEvenMoney();
        ArrayList<Option> options = new ArrayList<Option>();
        options.add(new Option(Option.ACCEPT_EVEN_MONEY, evaluateAcceptEvenMoneyROI()));
        options.add(new Option(Option.REFUSE_EVEN_MONEY, evaluateRefuseEvenMoneyROI()));
        Collections.sort(options);
        ui.displayOptions(options);

        //for each player, if player is BlackJack, offer Even Money
        for(int pIdx = 0; pIdx < players.size(); pIdx++)
        {
            player = players.get(pIdx);
            pH = player.hands().next();
            if(pH.isBlackJack())
            {
                int choice = ui.getChoice(pIdx, 0, options.size() - 1);
                //if even money taken
                if(options.get(choice).name().equals(Option.ACCEPT_EVEN_MONEY))
                    ui.displayHandRedeemed(player.redeemHand(pH));
            }      
        }
    }

    /**
     * Offers Even Money to the specified Player that has BlackJack
     * If even money taken, any insurance taken is cancelled
     * Pre: Dealer has single Ace && dealerOffersEvenMoney()
     */
    private void offerEvenMoney(int pIdx, PlayerHand pH)
    {
        ui.displayOfferEvenMoney();
        ArrayList<Option> options = new ArrayList<Option>();
        options.add(new Option(Option.ACCEPT_EVEN_MONEY, evaluateAcceptEvenMoneyROI()));
        options.add(new Option(Option.REFUSE_EVEN_MONEY, evaluateRefuseEvenMoneyROI()));
        Collections.sort(options);
        ui.displayOptions(options);

        int choice = ui.getChoice(pIdx, 0, options.size() - 1);
        //if even money taken
        if(options.get(choice).name().equals(Option.ACCEPT_EVEN_MONEY))
            ui.displayHandRedeemed(players.get(pIdx).redeemHand(pH));   
    }

    /**
     * Offers Insurance to each player
     * Pre: Dealer has single Ace
     */
    private void offerInsurance()
    {
        ui.displayOfferInsurance();
        ArrayList<Option> options = new ArrayList<Option>();
        options.add(new Option(Option.ACCEPT_INSURANCE, evaluateAcceptInsuranceROI()));
        options.add(new Option(Option.REFUSE_INSURANCE, evaluateRefuseInsuranceROI()));
        Collections.sort(options);
        ui.displayOptions(options);
                
        //for each player
        for(int pIdx = 0; pIdx < players.size(); pIdx++)
        {
            //if player's sole hand is not redeemed, offer insurance
            player = players.get(pIdx);
            pH = player.hands().next();
            if(!pH.isRedeemed())
            {
                int choice = ui.getChoice(pIdx, 0, options.size() - 1);
                //if insurance taken
                if(options.get(choice).name().equals(Option.ACCEPT_INSURANCE))
                    player.takeInsurance(ui.getInsuranceBet(pIdx, pH.bet() / 2.0));
            }
        }
    }



    /**
     * Offers Surrender to each player that has not taken Even Money or Insurance & !isBJ
     * Does not clear transposition tables. That is caller's responsibility.
     */
    private void offerSurrender()
    {
        ArrayList<Option> options = new ArrayList<Option>();
        //for each player
        for(int pIdx = 0; pIdx < players.size(); pIdx++)
        {
            player = players.get(pIdx);
            pH = player.hands().next();
            //if no insurance taken and hand not redeemed for even money
            if(player.insurance() == 0.0 && !pH.isRedeemed() && !pH.isBlackJack())
            {
                options.clear();
                options.add(new Option(Option.SURRENDER, evaluateSurrenderROI()));
                options.add(new Option(Option.DONT_SURRENDER, evaluateNonSurrenderROI()));
                Collections.sort(options);
                ui.setFocusPlayer(pIdx);
                ui.displayHand(dH);
                ui.displayHand(pH);
                ui.displayOptions(options);
                int choice = ui.getChoice(pIdx, 0, options.size() - 1);
                if(options.get(choice).name().equals(Option.SURRENDER))
                    ui.displayHandSurrendered(player.surrenderHand());
            }
        }
    }


    /**
     * Dealer Peek at Hole Card. If peek reveals BJ, 2nd card automatically given to dealer
     * Pre: dealerActionRequired && dealerDealtHoleCard() && dealer score = 10,11
     *      && dealer count cards = 1
     * Also clears PlayerHandROIs transposition tables as probabilities have changed
     */
    private void peekAtDealerHoleCard()
    {
        ui.setFocusDealer();
        boolean dealerIsBlackJack = false; //will be reset, just to placate compiler
        Random rand = new Random();
        if(dH.score() == 10)
            dealerIsBlackJack = rand.nextDouble() < shoe.probabilityAce();
        else if(dH.score() == 11)
            dealerIsBlackJack = rand.nextDouble() < shoe.probability10Value();

        dealerIsBlackJack = ui.getDealerPeekRevealsBlackJack(dealerIsBlackJack);
        if(dealerIsBlackJack)
        {
            Card c = null;
            if(dH.score() == 10)
                c = new Card(Card.PLAYER_ACE, shoe.probableSuit(Card.PLAYER_ACE, Shoe.REMOVE));
            else if(dH.score() == 11)
                c = new Card(Card.TEN, shoe.probableSuit(Card.TEN, Shoe.REMOVE));
            shoe.remove(c);
            dH.insertCard(c);
            ui.displayHand(dH);
        }
        else
            dH.confirmNotBlackJack();

        clearPlayerHandROIs();               //becuase probabilities have changed
    }


    /**
     * Deals cards until player busts, stands, gets BJ, 5CardCharlie or 5Card21
     * If dealer has peeked and revealed blackJack, skip all players who aren't blackjack
     */
    private void playerAction()
    {
        ArrayList<Option> options;           //available player options, (eg stand, hit ...)
        ListIterator<PlayerHand> handItr;    //iterates over each player's hands
        int hIdx;                            //0-based index of a hand  
        int choice;                          //index of chosen Option           

        for(int pIdx = 0; pIdx < players.size(); pIdx++)
        {
            //if this player's first hand is not hittable, just continue to next player
            if(!players.get(pIdx).hands().next().isHittable())
                continue;

            //if dealer is blackjack and this hand can't be, continue to next hand
            if(dH.isBlackJack() && !(pH.isBlackJack() || pH.isPairTensOrAces()))
                        break;

            ui.setFocusPlayer(pIdx);
            player = players.get(pIdx);
            handItr = player.hands();
            hIdx = -1;                 //so first hand will be hIdx == 0

            while(handItr.hasNext())
            {
                pH = handItr.next();
                hIdx++;                 //so first hand will be hIdx == 0
                ui.setFocusHand(hIdx);

                if(pH.countCards() == 1)     //if this the 2nd of a pair of split Hands
                    dealCard(pH);//only do 2nd card if 2nd of split Hand pair

                //at this point, PlayerHand holds two cards

                while(pH.isHittable())     //score < 21, !srrd, !rdmd, !dd, !spltAc
                {
                    //if dealer is blackjack and this hand can't be, continue to next hand
                    if(dH.isBlackJack() && !(pH.isBlackJack() || pH.isPairTensOrAces()))
                        break;

                    ui.displayHand(dH);
                    ui.displayHand(pH);
                    options = new ArrayList<Option>();
                    options.add(new Option(Option.STAND, evaluateStandROI()));
                    options.add(new Option(Option.HIT, evaluateHitROI()));
                    if(pH.isSplittable())
                        options.add(new Option(Option.SPLIT, evaluateSplitROI()));
                    if(pH.isDoublable())
                        options.add(new Option(Option.DOUBLE_DOWN, evaluateDoubleROI()));
                    Collections.sort(options);
                    ui.displayOptions(options);
                    choice = ui.getChoice(0, options.size() - 1);
                     
                    if(options.get(choice).name().equals(Option.STAND))
                        break;
                    else if(options.get(choice).name().equals(Option.HIT)) 
                    {
                        dealCard(pH);

                        if(pH.isBust())                        //if bust, display bust msg
                        {
                            ui.displayHandBust(pH.bet());      //no need to mod player bank stats
                            break;
                        }
                        else if(pH.countCards() == 5)
                        {
                            //if 5 Card 21, redeem & show msg                      
                            if(pH.score() == 21 && settings.fiveCard21Wins())
                                ui.displayFiveCard21Win(player.redeemHand(pH) + 
                                        settings.fiveCard21Amount());
                            //if 5Card Charlie, redeem & show msg
                            else if(settings.fiveCardCharlieWins())
                                ui.displayFiveCardCharlieWin(player.redeemHand(pH));
                        }
                        refreshState();  
                        clearPlayerHandROIs();
                    }
                    else if(options.get(choice).name().equals(Option.SPLIT)) 
                    {

                        handItr.add(player.splitHand(pH)); //inserts split hand after current
                        handItr.previous();                //retreat pointer to before added Hand
                        dealCard(pH);                      //only get 2nd card for 1st of split pair

                    }
                    else if(options.get(choice).name().equals(Option.DOUBLE_DOWN)) 
                    {
                        double additionalBet = ui.getDoubleDownBet(pH.bet());
                        Card finalCard = ui.getCard(UserInterface.INDEX_FINAL_CARD, shoe);
                        shoe.remove(finalCard);
                        player.doubleDownHand(pH, additionalBet, finalCard);
                        refreshState();  
                        clearPlayerHandROIs();
                        ui.displayHand(pH);
                    }  
                }  //end while this hand is hittable

                //If player split then got blackjack
                if(pH.isBlackJack() && dH.isSingleAce() &&
                        !dH.confirmedNotBlackJack() && settings.dealerOffersEvenMoney() &&
                        settings.splitBlackJackROI() > settings.blackJackROI())
                    offerEvenMoney(pIdx, pH);
            }  //end while Player has more hands
        } //end of each player
    }

    /**
     * Recursive algorithm. Returns the best available ROI for current state.
     * Called by non-recursive evaulateHitROI() and non-recursive evaluateSplitROI()
     * @return the compound ROI for current PlayerHand state
     */
    private double playerTurnROI()
    {
        //Base Cases
        if(pH.isBust())                                     //most likely
            return ROI.loss();
        if(playerHandROIs.containsKey(pH.hashCode()))       //2nd most likely
            return playerHandROIs.get(pH.hashCode());       //3rd most likely
        if(pH.score == 21)
        {
            if(pH.countCards() == 2)                        //if pH is splitBlackJack. Rare.
            {
                return evaluateSplitBlackJackROI();
            }
            else if(pH.countCards() == 5 && settings.fiveCard21Wins())
                return ROI.fiveCard21(pH.bet());            //if 5 card 21
            else                                            //if 3+ card 21
            {
                Double standROI = playerStandHandROIs.get(pH.hashCode);
                if(standROI != null)
                    return standROI;
                else
                    standROI = evaluateStandROI();
                playerStandHandROIs.put(pH.hashCode(), standROI);
                return standROI;
            }
        }
        if(pH.countCards() == 5 && settings.fiveCardCharlieWins())  //5 card Charlie
            return ROI.win();

        //Recursive case (ie. score < 21)
        if(shoeSize == 0)
            throw new IllegalStateException("Shoe empty (playerTurn)");
        int hashCode = pH.hashCode();
        Double standROI = playerStandHandROIs.get(pH.hashCode());
        Double hitROI = -10.0;           //will be reset to higher value
        Double splitROI = -10.0;         //may be reset to higher value
        Double doubleROI = -10.0;        //may be reset to higher value
        if(standROI == null)
        {   
            standROI = evaluateStandROI();
            playerStandHandROIs.put(pH.hashCode(), standROI);
        }
      
        double prob;
        ROI[] rois = new ROI[13];
        if(dH.confirmedNotBlackJack())                      //if dH is [A][?]!BJ or [X][?]!BJ    
        {
            double countNonTens = shoeSize - cardTypeCounts13[Card.TEN] - 
                    cardTypeCounts13[Card.JACK] - cardTypeCounts13[Card.QUEEN] - 
                    cardTypeCounts13[Card.KING];
            double countNonAces = shoeSize - cardTypeCounts13[Card.PLAYER_ACE];
            shoeSize--;                                         // Avoids ss++/ss-- and ?/(ss - 1)
            for(int cardType = Card.TWO; cardType <= Card.PLAYER_ACE; cardType++)
            {
                if(cardTypeCounts13[cardType] > 0)
                {
                    if(dH.score() == 10)                    //if dH is [A][?]!BJ
                        if(cardType != Card.PLAYER_ACE)     //if adding [2]-[K] to pH
                            prob = (cardTypeCounts13[cardType] - cardTypeCounts13[cardType] / 
                                    countNonAces) / shoeSize;
                        else                                //if adding [A] to pH
                            prob = (double)cardTypeCounts13[cardType] / shoeSize;
                    else                                    //if dH is [10][?]!BJ
                        if(Card.PLAYER_VALUES[cardType] != 10) //if adding [2]-[9],[A] to pH
                            prob = (cardTypeCounts13[cardType] - cardTypeCounts13[cardType] /
                                    countNonTens) / shoeSize;
                        else                                //if adding [X],[J],[Q],[K] to pH
                            prob = (double)cardTypeCounts13[cardType] / shoeSize;
                    cardTypeCounts13[cardType]--;
                    pH.insert(cardType);
                    rois[cardType] = new ROI(prob, playerTurnROI());
                    pH.removeLast();
                    cardTypeCounts13[cardType]++;
                }
            }
            shoeSize++;
        }
        else                       //else dH does not have hole-card or face-card != {A,10}
        {
            for(int cardType = Card.TWO; cardType <= Card.PLAYER_ACE; cardType++)
            {
                if(cardTypeCounts13[cardType] > 0)
                {
                    prob = (double)cardTypeCounts13[cardType] / shoeSize;
                    cardTypeCounts13[cardType]--;
                    shoeSize--;
                    pH.insert(cardType);
                    rois[cardType] = new ROI(prob, playerTurnROI());
                    pH.removeLast();
                    shoeSize++;
                    cardTypeCounts13[cardType]++;
                }
            }
        }
        hitROI = ROI.merge(rois);

        if(pH.isSplittable())
            splitROI = evaluateSplitROI();
        if(pH.isDoublable())
            doubleROI = evaluateDoubleROI();

        double maxROI = ROI.max(standROI, hitROI, splitROI, doubleROI);
        playerHandROIs.put(hashCode, maxROI);
        return maxROI;
    }




    /**
     * Plays a single Round of BlackJack.
     * Diplays the round number, True Counts, various statistics.
     * Gets Bet for each player, deals Initial Deal, Player Action, Dealer Action, Settles Bets
     * If not Auto Play game, asks user whether they wish to play another round
     */
    private void playRound()
    {
        ui.displayNewRound(++countRounds);        //Display Round information

        //Display Count Strategy Statistics
        boolean[] displayableCountStrategies = settings.displayableCountStrategies();
        for(int countStrat = 0; countStrat < displayableCountStrategies.length; countStrat++)
            if(displayableCountStrategies[countStrat])
            {
                ui.displayCountStatistics(shoe, settings);
                break;
            }

        //Display Ace Concentration and Player BlackJack chance statistics
        if(settings.displayAceGuage())
            ui.displayAceConcentration(shoe);
        if(settings.displayPlayerBJGuage())
            ui.displayChancePlayerBlackJack(shoe);

        //For each Player, display their current Bank balance and initial Bet
        for(int pIdx = 0; pIdx < players.size(); pIdx++)
        {
            Player player = players.get(pIdx);
            ui.displayBank(pIdx, player.bank());
            if(player.betStrategy() instanceof BetStrategyUserSpecified)
                player.placeInitialBet(shoe);
            else
                ui.displayInitialBet(pIdx, player.placeInitialBet(shoe));
        }
        
        //Deal 2 cards to each player, 1 or 2 cards to dealer, offer insurance, even money, surrendr
        dealInitialDeal();          

        //if any PlayerHand is not surrendered and not redeemed,  playerAction is required
        boolean playerActionRequired = false;
        for(int pIdx = 0; pIdx < players.size(); pIdx++)     //for each player
        {
            pH = players.get(pIdx).hands().next();           //examine their sole hand
            if(!pH.isSurrendered() && !pH.isRedeemed())      //if !surrendered && !redeemed
            {
                playerActionRequired = true;                 //player Action required
                break;
            }
        }
        if(playerActionRequired)
            playerAction();

        //if any PlayerHand is !bust & !surr & !redeem, dealerAction required
        boolean dealerActionRequired = false;
        dealerActionLoop:
        for(int pIdx = 0; pIdx < players.size(); pIdx++)
        {
            ListIterator<PlayerHand> itr = players.get(pIdx).hands();
            while(itr.hasNext())         //for each of this Player's hands
            {
                pH = itr.next();
                //if !bust & !surrenderd & !redeemed & !fiveCardCharlie & !fiveCard21
                if(!(pH.isBust() || pH.isSurrendered() || pH.isRedeemed() ||  
                        (pH.countCards() == 5 && (settings.fiveCardCharlieWins() ||  
                        pH.score() == 21 && settings.fiveCard21Wins()))))     
                {
                    dealerActionRequired = true;
                    break dealerActionLoop;
                }
            }
        }

        //if all players are BJ dealer can't be, no dealerAction required
        boolean allPlayersBlackJack = true;
        for(int pIdx = 0; pIdx < players.size() && allPlayersBlackJack; pIdx++)
            if(!players.get(pIdx).hands().next().isBlackJack())
                allPlayersBlackJack = false;
        if(allPlayersBlackJack && (dH.confirmedNotBlackJack() || dH.score() < 10))
            dealerActionRequired = false;

        if(dealerActionRequired)
            dealerAction();

        settleInsurance();
        settleBets();

        if(shoe.penetration() > shoe.maxPenetration())
            if(ui.getChoiceResetShoe(shoe))
            {
                shoe.refill();
                shoe.shuffle();
            }

        if(gameType != GAME_TYPE_AUTO_PLAY)
            playNextRound = ui.getChoiceContinueGame();
    }

    /**
     * Settles Insurance Wagers. If any player took insurance, display insurance result and
     * outcome for each player that took insurance, then adjust player's bank accordinly
     */
    private void settleInsurance()
    {
        //test to see if any player took insurance
        boolean anyPlayerTookInsurance = false;
        for(int pIdx = 0; pIdx < players.size(); pIdx++)
            if(players.get(pIdx).insurance() > 0.0)
            {
                anyPlayerTookInsurance = true;
                break;
            }

        //if no players took insurance, just return
        if(!anyPlayerTookInsurance)
            return;

        //else at least 1 player took insurance, so display dealer blackJack result
        ui.displayDealerInsureanceResult(dH.isBlackJack());
        for(int pIdx = 0; pIdx < players.size(); pIdx++)
        {
            player = players.get(pIdx);
            
            if(player.insurance() > 0.0)
            {
                ui.displayPlayerInsuranceResult(pIdx, dH.isBlackJack(), player.insurance());
                if(dH.isBlackJack())
                    player.winInsurance();
                else
                    player.loseInsurance();
            }
        }
    }


    /**
     * For each playerHand !bust, !surrendered, !5CardCharlie, & !5Card21, compare against dealer
     */
    private void settleBets()
    {
        int hIdx;
        for(int pIdx = 0; pIdx < players.size(); pIdx++)
        {
            player = players.get(pIdx);
            hIdx = -1;

            ListIterator<PlayerHand> itr = player.hands();

            while(itr.hasNext())        //TODO should we do hasPrevious() & previous()?
            {
                pH = itr.next();
                hIdx++;

                if(!pH.isBust() && !pH.isRedeemed() && !pH.isSurrendered())
                {
                    if(pH.isBlackJack())
                        if(dH.isBlackJack())
                            if(settings.dealerWinsTies())
                                ui.displayPlayerLoses(pIdx, hIdx, pH.score(), dH.score(), pH.bet());
                            else //dealer doesn't win ties
                            {
                                ui.displayPlayerDraws(pIdx, hIdx, pH.score(), dH.score(), pH.bet());
                                player.pushHand(pH);
                            }
                        else  //player is blackJack and dealer is not BlackJack
                        {
                            if(pH.splitLevel() == 0)    // player is non-split BlackJack
                                ui.displayPlayerBlackJack(pIdx, hIdx, pH.bet() * ROI.blackJack());
                            else // player has split BlackJack and dealer is not BlackJack
                                ui.displayPlayerBlackJack(pIdx, hIdx, pH.bet() * ROI.splitBlackJack());
                            player.winHand(pH, settings);
                        }
                    else if(dH.isBust())   //dealer is bust and player isn't
                    {
                        ui.displayDealerBust(pIdx, hIdx, pH.bet());
                        player.winHand(pH, settings);
                    }
                    else if(dH.isBlackJack())
                        ui.displayDealerBlackJack(pIdx, hIdx, pH.bet());
                    else if(pH.score() > dH.score())
                    {
                        ui.displayPlayerWins(pIdx, hIdx, pH.score(), dH.score(), pH.bet());
                        player.winHand(pH, settings);
                    }
                    else if(pH.score() < dH.score())
                        ui.displayPlayerLoses(pIdx, hIdx, pH.score(), dH.score(), pH.bet());
                    else if(settings.dealerWinsTies())
                        ui.displayPlayerLoses(pIdx, hIdx, pH.score(), dH.score(), pH.bet());
                    else
                    {
                        ui.displayPlayerDraws(pIdx, hIdx, pH.score(), dH.score(), pH.bet());
                        player.pushHand(pH);
                    }
                }
            }
        }
    }





    /**
     * Updates the dealer shoe (10cardTypes) from the cardTypeCounts(13cardTypes)
     * This is because cards {X,J,Q,K} are all the same as far as dealer is concerned.
     * Must be called prior to each evaluating dealerTurn
     */
    private void updateDealerShoe()
    {

        for(int cardType = Card.TWO; cardType <= Card.NINE; cardType++)
            cardTypeCounts10[cardType] = cardTypeCounts13[cardType];

        cardTypeCounts10[Card.TEN] = cardTypeCounts13[Card.TEN] + cardTypeCounts13[Card.JACK] +
                cardTypeCounts13[Card.QUEEN] + cardTypeCounts13[Card.KING];

        cardTypeCounts10[Card.DEALER_ACE] = cardTypeCounts13[Card.PLAYER_ACE];

    }


}