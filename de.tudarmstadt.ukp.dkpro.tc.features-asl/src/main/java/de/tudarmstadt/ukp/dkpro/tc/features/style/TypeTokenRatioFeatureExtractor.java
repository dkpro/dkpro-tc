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

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TypeTokenRatioFeatureExtractor
    implements SimpleFeatureExtractor
{
    public static final String FN_TTR = "TypeTokenRatio";
    
    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws CleartkExtractorException
    {

        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();

        for (Token token : JCasUtil.select(jcas, Token.class)) {
            fd.inc(token.getCoveredText().toLowerCase());
        }
        double ttr = 0.0;
        if (fd.getN() > 0) {
            ttr = (double) fd.getB() / fd.getN();
        }
        
        List<Feature> featList = new ArrayList<Feature>();
        featList.addAll(Arrays.asList(new Feature(FN_TTR, ttr)));    

        return featList;
    }
}