package de.tudarmstadt.ukp.dkpro.tc.features.style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;

public class PronounRatioFeatureExtractor
    implements SimpleFeatureExtractor
{
    public static final String FN_I_RATIO    = "PronounRatioI";
    public static final String FN_HE_RATIO   = "PronounRatioHe";
    public static final String FN_SHE_RATIO  = "PronounRatioShe";
    public static final String FN_WE_RATIO   = "PronounRatioWe";
    public static final String FN_THEY_RATIO = "PronounRatioThey";
    public static final String FN_US_RATIO   = "PronounRatioUs";
    
    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws CleartkExtractorException
    {
            
        int heCount = 0;
        int sheCount = 0;
        int iCount = 0;
        int weCount = 0;
        int theyCount = 0;
        int usCount = 0;
        
        int n = 0;
        for (PR pronoun : JCasUtil.select(jcas, PR.class)) {
            n++;
            
            String text = pronoun.getCoveredText().toLowerCase();
            if (text.equals("he")) {
                heCount++;
            }
            else if (text.equals("she")) {
                sheCount++;
            }
            else if (text.equals("i")) {
                iCount++;
            }
            else if (text.equals("we")) {
                weCount++;
            }
            else if (text.equals("they")) {
                theyCount++;
            }
            else if (text.equals("us")) {
                usCount++;
            }
        }
        
        List<Feature> featList = new ArrayList<Feature>();
        if (n > 0) {
            featList.addAll(Arrays.asList(new Feature(FN_HE_RATIO, (double) heCount / n)));    
            featList.addAll(Arrays.asList(new Feature(FN_SHE_RATIO, (double) sheCount / n)));    
            featList.addAll(Arrays.asList(new Feature(FN_I_RATIO, (double) iCount / n)));    
            featList.addAll(Arrays.asList(new Feature(FN_WE_RATIO, (double) weCount / n)));    
            featList.addAll(Arrays.asList(new Feature(FN_THEY_RATIO, (double) theyCount / n)));    
            featList.addAll(Arrays.asList(new Feature(FN_US_RATIO, (double) usCount / n)));    
        }

        return featList;
    }
}    