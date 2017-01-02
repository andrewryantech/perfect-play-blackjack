/**
 * 
 */
package ryan.blackjack.domain;

import java.util.TreeMap;

/**
 * @author Andrew Ryan
 */
public class Bank {
	
    //----------------------------------------- Constants ------------------------------------------
	public static final int 		BANK_HISTORY_MAX_SIZE = 400;	 //How many data points to maintain
	
    //------------------------------------ Instance Attributes -------------------------------------	
    //private double                 	initialBank;              	//Starting bank balance - first element of history
    private double                 	currentBank;              	//Current bank balance
    private double                 	minBank;                  	//Lowest bank balance ever experienced
    private double                 	maxBank;                  	//Highest bank balance ever experienced
    private int						bankHistoryIncrements;		//increment between rounds ,ie initial is {1,2,3...} => 1
    
    private int						roundsPlayed;				//how many rounds played by this player (might not equal total rounds if player sat out)
    
    private TreeMap<Integer,Double>		bankHistory;              	//Maps round number to bank level prior to round. eg {2=>$1000} is AFTER round 2 is complete
    
    public Bank(double initialBank)
    {
    	bankHistory = new TreeMap<Integer,Double>();
    	roundsPlayed = 0;
        bankHistoryIncrements = 1;
        
    	bankHistory.put(0, initialBank);
        currentBank = initialBank;
        minBank = initialBank;
        maxBank = initialBank;
    }
    
    public double initialBank()
    {
    	return bankHistory.get(0);
    }
    
    public double currentBank()
    {
    	return currentBank;
    }
    
    public double minBank()
    {
    	return minBank;
    }
    
    public double maxBank()
    {
    	return maxBank;
    }
    
    public void adjustBank(double adjustment, boolean isInitialBet)
    {
    	//if is an initial bet
    	if(isInitialBet)
    	{
    		//if we are recording this round
    		if(0 == roundsPlayed % bankHistoryIncrements)
    		{
    			bankHistory.put(roundsPlayed, currentBank);
    			
    			//if bank history is full, reduce number of data points by increasing increment
        		if(BANK_HISTORY_MAX_SIZE == bankHistory.size())
        			reduceBankHistory();
    		}
    		roundsPlayed++;
    	}
    	
    	currentBank += adjustment;
    	minBank = Math.min(currentBank, minBank);
    	maxBank = Math.max(currentBank, maxBank);
    }
    
    
    //increases the increment between data points
    private void reduceBankHistory()
    {
    	bankHistoryIncrements *= 2;
    	TreeMap<Integer,Double> newBankHistory = new TreeMap<Integer,Double>();
    	for(int i = 0; i <= bankHistory.lastKey(); i += bankHistoryIncrements)
    	{
    		newBankHistory.put(i, bankHistory.get(i));
    	}
    	bankHistory = newBankHistory;
    }
    
    @SuppressWarnings("unchecked")
	public TreeMap<Integer,Double> bankHistory()
    {
    	return (TreeMap<Integer,Double>)bankHistory.clone();
    }
}
