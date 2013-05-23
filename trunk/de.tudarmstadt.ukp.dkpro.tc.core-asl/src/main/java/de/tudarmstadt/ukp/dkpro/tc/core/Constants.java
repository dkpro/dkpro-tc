package de.tudarmstadt.ukp.dkpro.tc.core;

import org.apache.uima.cas.CAS;

public class Constants
{
    /*
     * Pairwise classification
     */

    public static String INITIAL_VIEW = CAS.NAME_DEFAULT_SOFA;
    public static String PART_ONE = "PART_ONE";
    public static String PART_TWO = "PART_TWO";
    
    /*
     * Instance storage
     */

    public static final String CLASS_ATTRIBUTE_PREFIX = "__";
    public static final String CLASS_ATTRIBUTE_NAME = "outcome";
}
