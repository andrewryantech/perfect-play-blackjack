//--------------------------------------------------------------------------------------------------
//Andrew Ryan
//2014 Mar
//Optimal Play BlackJack app
//--------------------------------------------------------------------------------------------------
// ****************************************PUBLIC OPERATIONS****************************************  
package ryan.blackjack.domain;


/**
 * An Option that a user can select
 */
public final class Option implements Comparable<Option>
{
    //----------------------------------------- Constants ------------------------------------------
    public static final String ACCEPT_EVEN_MONEY = "Accept Even Money";
    public static final String REFUSE_EVEN_MONEY = "Refuse Even Money";
    public static final String ACCEPT_INSURANCE  = "Accept Insurance";
    public static final String REFUSE_INSURANCE  = "Refuse Insurance";
    public static final String SURRENDER         = "Surrender";
    public static final String DONT_SURRENDER    = "Don't Surrender";
    public static final String STAND             = "Stand";
    public static final String HIT               = "Hit";
    public static final String SPLIT             = "Split";
    public static final String DOUBLE_DOWN       = "Double Down";

    //------------------------------------------ Defaults ------------------------------------------

    //-------------------------------------- Class Attributes --------------------------------------
    //-------------------------------------- Class Behaviour ---------------------------------------

    //------------------------------------ Instance Attributes -------------------------------------
    private String name;
    private double roi;

    //------------------------------------- Instance Behaviour -------------------------------------
    public Option(String name, double roi)
    {
        this.name = name;
        this.roi = roi;
    }

    public String name()
    {
        return name;
    }

    public double roi()
    {
        return roi;
    }

    public int compareTo(Option o)
    {
        if(roi > o.roi())
            return -1;
        else if(roi < o.roi())
            return 1;
        else
            return 0;
    }
}








