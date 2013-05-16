package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;

import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureExtractor;

public class DiffNrOfCharactersPairFeatureExtractor
    implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
       throws CleartkExtractorException
    {
			return Arrays.asList(
					new Feature("DiffNrOfCharacters", 						
									view1.getDocumentText().length() -
									view2.getDocumentText().length())
					);
		
			}
}
