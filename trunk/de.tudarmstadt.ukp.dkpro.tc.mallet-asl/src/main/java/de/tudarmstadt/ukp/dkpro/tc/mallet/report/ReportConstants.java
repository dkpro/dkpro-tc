package de.tudarmstadt.ukp.dkpro.tc.mallet.report;

public interface ReportConstants
{
    // accuracy
    public static final String CORRECT = "Correctly Classified Examples";
    public static final String INCORRECT = "Incorrectly Classified Examples";
    public static final String PCT_CORRECT = "Percentage Correct";
    public static final String PCT_INCORRECT = "Percentage Incorrect";

    // P/R/F/Accuracy
    public static final String PRECISION = "Precision";
    public static final String RECALL = "Recall";
    public static final String FMEASURE = "F-Measure";
    
    public static final String MACRO_AVERAGE_FMEASURE = "Macro-averaged F-Measure";
    
//    public static final String WGT_PRECISION = "Weighted Precision";
//    public static final String WGT_RECALL = "Weighted Recall";
//    public static final String WGT_FMEASURE = "Weighted F-Measure";
    
    public static final String NUMBER_EXAMPLES = "Absolute Number of Examples";
    public static final String NUMBER_LABELS = "Absolute Number of Labels";
}
