package de.tudarmstadt.ukp.dkpro.tc.features.pair.keyphrases;


import java.util.List;

public abstract class AbstractKeyphraseFeatureExtractor implements PairFeature {
	
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
