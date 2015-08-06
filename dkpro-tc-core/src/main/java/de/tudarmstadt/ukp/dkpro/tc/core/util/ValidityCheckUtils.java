package de.tudarmstadt.ukp.dkpro.tc.core.util;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

public class ValidityCheckUtils {

	public static int learningModeLabel2int(String learningMode)
			throws AnalysisEngineProcessException
	{
		int learningModeI = 0;

        if (learningMode.equals(Constants.LM_SINGLE_LABEL))
            learningModeI = 1;
        else if (learningMode.equals(Constants.LM_MULTI_LABEL))
            learningModeI = 2;
        else if (learningMode.equals(Constants.LM_REGRESSION))
            learningModeI = 3;
        else
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "Please set a valid learning mode"));
        
        return learningModeI;
	}
	
	public static int featureModeLabel2int(String featureMode)
			throws AnalysisEngineProcessException
	{
		int featureModeI = 0;

        if (featureMode.equals(Constants.FM_DOCUMENT))
            featureModeI = 1;
        else if (featureMode.equals(Constants.FM_UNIT))
            featureModeI = 2;
        else if (featureMode.equals(Constants.FM_PAIR))
            featureModeI = 3;
        else if (featureMode.equals(Constants.FM_SEQUENCE)) {
            featureModeI = 4;
        }
        else
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "Please set a valid feature mode"));
        
        return featureModeI;
	}
}
