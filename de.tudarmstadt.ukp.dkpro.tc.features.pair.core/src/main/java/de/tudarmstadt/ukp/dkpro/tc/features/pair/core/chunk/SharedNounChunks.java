package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureExtractor;

public class SharedNounChunks
    implements PairFeatureExtractor
{
    
    protected boolean normalizeWithFirst;
    
    public SharedNounChunks(boolean normalizeWithFirst)
    {
        this.normalizeWithFirst = normalizeWithFirst;
    }

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
            throws CleartkExtractorException
    {
    	
			return Arrays.asList(
			        new Feature("DiffNounPhraseTokenLength", getSharedNounChunksCount(view1, view2))
			);
		
    }

	private double getSharedNounChunksCount(JCas view1, JCas view2) {
		
	    // FIXME remove from arraylist is slow, better use set intersection
		List<String> cleanedWords = new ArrayList<String>();
		for(Chunk chunk : JCasUtil.select(view1, Chunk.class)){
			cleanedWords.add(chunk.getCoveredText());
		}
		for(Chunk chunk : JCasUtil.select(view2, Chunk.class)){
			cleanedWords.remove(chunk.getCoveredText());
		}
		if (normalizeWithFirst) {
		      return cleanedWords.size()/(double)JCasUtil.select(view1, Chunk.class).size();
		}
		else {
            return cleanedWords.size()/(double)JCasUtil.select(view2, Chunk.class).size();
		}
	}


}
