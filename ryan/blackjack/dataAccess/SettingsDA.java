//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  

// *********************************************ERRORS**********************************************
package ryan.blackjack.dataAccess;

import ryan.blackjack.domain.Settings;
import ryan.blackjack.domain.PlayerHand;
import ryan.blackjack.domain.ROI;
import ryan.blackjack.domain.CountStrategy;
import ryan.blackjack.domain.BetStrategy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Enables reading and writing Settings to config.dat file
 *
 * An object data file is used instead of a human-readable text file as it hinders a malicious
 * user who may try to set settings to invalid values. It also makes the implementation simpler
 */
public final class SettingsDA
{
    //----------------------------------------- Constants ------------------------------------------
    public static final String FILE_NAME = "config.dat";  //name of data file
    public static final String SAVE_DIR = System.getProperty("user.dir");

    //-------------------------------------- Class Attributes --------------------------------------
    private static SettingsDA singleton;                    //single instance of this class

    //-------------------------------------- Class Behaviour ---------------------------------------
    /**
     * Gets the single instance of this class
     * @return the single instance of this class
     */
    public static synchronized SettingsDA getInstance()
    {
        if(singleton == null)
            singleton = new SettingsDA();
        return singleton;
    }


    /**
     * TODO. TESTING ONLY: Unit test.
     */
    public static void main(String[] args)
    {
        SettingsDA test = SettingsDA.getInstance();
        Settings settings = test.loadSettings();
        test.saveSettings(settings);
        
    }

    /**
     * Constructor. Private to ensure only single DA object ever exists and one time
     */
    private SettingsDA()
    {
    }

    /**
     * Creates default settings, applies to system, and returns settings
     * @return default Settings instance
     */
    public Settings defaultSettings()
    {
        Settings settings = new Settings();
        applySettingsToSystem(settings);
        return settings;
    }

    /**
     * Attempts to load saved Settings.
     * 
     * If successful, applies relevant settings to domain class static variables
     * @return System settings
     */
    public Settings loadSettings()
    {
        ObjectInputStream in = null;
        Settings settings = null;

        try
        {
            in = new ObjectInputStream(new FileInputStream(SAVE_DIR + FILE_NAME));
            settings = (Settings)in.readObject();
        }
        catch(FileNotFoundException fnfe)
        {
            System.err.println(FILE_NAME + " not found. Loading default settings");
        }
        catch(IOException ioe)
        {
            System.err.println("Load error. Loading default settings");
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cnfe)
        {
            System.err.println("Load error. Could not cast to Settings object. Loading defaults");
        }
        finally
        {
            try
            {
                if(in != null)
                    in.close();
            }
            catch(IOException ioe)
            {
                System.out.println(ioe);
            }
        }
 
        //If load unsuccessful, create new default Settings
        if(settings == null)
            settings = defaultSettings();

        applySettingsToSystem(settings);

        return settings;
    }

    /**
     * Update all relevant system static variables
     * @return System settings
     */
    public void applySettingsToSystem(Settings settings)
    {
        CountStrategy.setCustomName(settings.customCountName());
        CountStrategy.setCustomAdjustments(settings.customCountAdjustments());
        BetStrategy.setMinBet(settings.minBet());
        BetStrategy.setMaxBet(settings.maxBet());
        ROI.setBlackJackROI(settings.blackJackROI());
        ROI.setSplitBlackJackROI(settings.splitBlackJackROI());
        ROI.setFiveCard21Amount(settings.fiveCard21Amount());
        PlayerHand.setCanHitAfterAcesSplit(settings.canHitAfterAcesSplit());
        PlayerHand.setSplitCardEqualityType(settings.splitCardEqualityType());
        PlayerHand.setTimes2toK_Splittable(settings.times2toK_Splittable());
        PlayerHand.setTimesAcesSplittable(settings.timesAcesSplittable());
        PlayerHand.setDoublableStates(
        		settings.doublableCardCounts(),
        		settings.doublableScores(),
                settings.canDoubleAfter2toK_Split(),
                settings.canDoubleAfterAcesSplit()
        );
    }


    /**
     * Saves current system settings to file
     * @param settings current system settings
     */
    public boolean saveSettings(Settings settings)
    {
        ObjectOutputStream out = null;

        try
        {
            out = new ObjectOutputStream(new FileOutputStream(SAVE_DIR + FILE_NAME));
            out.writeObject(settings);
            out.close();
        }
        catch(FileNotFoundException fnfe)
        {
            System.err.println("Save error");
            fnfe.printStackTrace();
            return false;
        }
        catch(IOException ioe)
        {
            System.err.println("Save error");
            ioe.printStackTrace();
            return false;
        }
        return true;
    }
}








