package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureExtractor;

public class DiffNrOfSentencesPairFeatureExtractor
    implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
       throws CleartkExtractorException
    {
			return Arrays.asList(
			        new Feature("DiffNrOfSentences",	
			        				JCasUtil.select(view1, Sentence.class).size() - 
			        				JCasUtil.select(view2, Sentence.class).size())
			);
    }
}
