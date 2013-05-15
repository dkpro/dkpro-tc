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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class PluralRatioFeatureExtractor 
    implements SimpleFeatureExtractor
{
    public static final String FN_PLURAL_RATIO = "PluralRatio";

    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws CleartkExtractorException
    {
        int plural = 0;
        int singular = 0;
        
        for (POS tag : JCasUtil.select(jcas, N.class)) {
            // FIXME depends on tagset
            if ((tag.getPosValue().equals("NNS")) || (tag.getPosValue().equals("NNPS")) || (tag.getPosValue().equals("NNS"))) {
                plural++;
            }
            else if ((tag.getPosValue().equals("NNP")) || (tag.getPosValue().equals("NN"))) {
                singular++;
            }
        }
        List<Feature> featList = new ArrayList<Feature>();
        if ((singular+plural)>0) {
  		   featList.addAll(Arrays.asList(new Feature(FN_PLURAL_RATIO, (double) plural / (singular+plural))));	
        }
		return featList;
    }
}