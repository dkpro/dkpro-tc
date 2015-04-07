package de.tudarmstadt.ukp.dkpro.tc.crfsuite.writer;

public class LabelSubstitutor
{
    /**
     * There is a bug in CRFsuite with certain characters as labels. These labels are not written
     * correctly to the stdout by CRFsuite resulting in blank labels. This cause problems for
     * downstream tasks in particular for the TC evaluation which would have to deal with blank
     * labels. These methods here provide a substitution of known problematic labels to values which
     * do work. The provided methods perform a substitution of the known problem-labels and
     * substitute them back after crfsuite ran. (TH 2015-01-26)
     */
    
    private final static String COLON = ":";
    private final static String COLON_SUBSTITUTE = "XXCol";

    public static String labelReplacement(String label)
    {
        switch (label) {
        case COLON:
            return COLON_SUBSTITUTE;
        }

        return label;
    }

    public static String undoLabelReplacement(String label)
    {
        switch(label){
        case COLON_SUBSTITUTE:
            return COLON;
        }
        return label;
    }

}
