package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.chunk;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureExtractor;

public class DiffNounChunkCharacterLength
     implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws CleartkExtractorException
    {
    	
		return Arrays.asList(
		        new Feature("DiffNounPhraseCharacterLength", 
		        		getAverageNounPhraseCharacterLength(view1) - getAverageNounPhraseCharacterLength(view2))
		);
		
    }

	private double getAverageNounPhraseCharacterLength(JCas view) {
		int totalNumber = 0;
		for(Chunk chunk : JCasUtil.select(view, Chunk.class)){
			totalNumber += chunk.getCoveredText().length();
		}
		return totalNumber/(double) JCasUtil.select(view, Chunk.class).size();
	}
}
