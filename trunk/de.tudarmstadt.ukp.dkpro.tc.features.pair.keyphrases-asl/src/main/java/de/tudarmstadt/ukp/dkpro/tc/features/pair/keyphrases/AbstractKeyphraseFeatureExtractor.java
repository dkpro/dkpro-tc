package de.tudarmstadt.ukp.dkpro.tc.features.pair.keyphrases;


import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.keyphrases.core.evaluator.util.EvaluatorUtils;
import de.tudarmstadt.ukp.dkpro.keyphrases.core.type.Keyphrase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;

public abstract class AbstractKeyphraseFeatureExtractor implements PairFeatureExtractor {
	
	protected List<Keyphrase> getKeyphrases(JCas jcas, int number){
		List<Keyphrase> keyphrases = 
				EvaluatorUtils.
				filterAndSortKeyphrases(JCasUtil.select(jcas, Keyphrase.class), true);
		
		
		if(number==0){
			return keyphrases;
		}
		else{
			return keyphrases.subList(0, Math.min(number,keyphrases.size()));
		}
	}



}
