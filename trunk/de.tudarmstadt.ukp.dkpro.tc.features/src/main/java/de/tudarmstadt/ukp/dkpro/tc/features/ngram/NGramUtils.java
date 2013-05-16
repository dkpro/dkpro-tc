package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;
import static org.uimafit.util.JCasUtil.toText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;

public class NGramUtils
{

    public static FrequencyDistribution<String> getAnnotationNgrams(JCas jcas, Annotation anno, boolean lowerCaseNGrams, int minN, int maxN) {
        Set<String> empty = Collections.emptySet();
        return getAnnotationNgrams(jcas, anno, lowerCaseNGrams, minN, maxN, empty);
    }

    public static FrequencyDistribution<String> getAnnotationNgrams(JCas jcas, Annotation focusAnnotation, boolean lowerCaseNGrams, int minN, int maxN, Set<String> stopwords) {
		FrequencyDistribution<String> annoNgrams = new FrequencyDistribution<String>();

		//If the focusAnnotation contains sentence annotations, extract the ngrams sentence-wise
		//if not, extract them from all tokens in the focusAnnotation
		if(JCasUtil.selectCovered(jcas, Sentence.class,	focusAnnotation).size()>0){
	        for (Sentence s : selectCovered(jcas, Sentence.class, focusAnnotation)) {
				for (List<String> ngram : new NGramStringListIterable(
						toText(selectCovered(Token.class, s)), minN, maxN)) {

					ngram = filterNgram(ngram, lowerCaseNGrams, stopwords);

					// filter might have reduced size to zero => don't add in this case
					if (ngram.size() > 0) {
						annoNgrams.inc(StringUtils.join(ngram, "_"));
					}
				}
	        }
		}else{
			for (List<String> ngram : new NGramStringListIterable(
					toText(selectCovered(Token.class, focusAnnotation)), minN, maxN)) {

				ngram = filterNgram(ngram, lowerCaseNGrams, stopwords);

				// filter might have reduced size to zero => don't add in this case
				if (ngram.size() > 0) {
					annoNgrams.inc(StringUtils.join(ngram, "_"));
				}
			}
		}
		return annoNgrams;
	}

	public static FrequencyDistribution<String> getDocumentNgrams(JCas jcas, boolean lowerCaseNGrams, int minN, int maxN) {
        Set<String> empty = Collections.emptySet();
        return getDocumentNgrams(jcas, lowerCaseNGrams, minN, maxN, empty);
    }

    public static FrequencyDistribution<String> getDocumentNgrams(JCas jcas, boolean lowerCaseNGrams, int minN, int maxN, Set<String> stopwords) {
        FrequencyDistribution<String> documentNgrams = new FrequencyDistribution<String>();
        for (Sentence s : select(jcas, Sentence.class)) {
            // TODO parameterize type
            for (List<String> ngram : new NGramStringListIterable(toText(selectCovered(Token.class, s)), minN, maxN)) {

                ngram = filterNgram(ngram, lowerCaseNGrams, stopwords);

                // filter might have reduced size to zero => don't add in this case
                if (ngram.size() > 0) {
                    documentNgrams.inc(StringUtils.join(ngram, "_"));
                }
            }
        }
        return documentNgrams;
    }
    
    public static FrequencyDistribution<String> getDocumentPOSNgrams(JCas jcas, int minN, int maxN) {
        FrequencyDistribution<String> posNgrams = new FrequencyDistribution<String>();
        List<POS> postags = new ArrayList<POS>(JCasUtil.select(jcas, POS.class));
        //FIXME: this is ugly and slow - shall be able to get ngram iterable without these type conversions...
        List<String> postagstrings = new ArrayList<String>();
        for (POS p : postags) {
        	postagstrings.add(p.getPosValue());
        }
        String[] posarray = postagstrings.toArray(new String[postagstrings.size()]);
        
        for (Sentence s : select(jcas, Sentence.class)) { 
        	 for (List<String> ngram : new NGramStringListIterable(posarray, minN, maxN)) {
                    posNgrams.inc(StringUtils.join(ngram, "_"));

            }
        }               
        return posNgrams;
    }
    

    public static List<String> filterNgram(List<String> ngram, boolean lowerCase, Set<String> stopwords) {
        List<String> filteredNgram = new ArrayList<String>();
        for (String token : ngram) {
            if (lowerCase) {
                token = token.toLowerCase();
            }
            if (!stopwords.contains(token)) {
                filteredNgram.add(token.toLowerCase());
            }
        }
        return filteredNgram;
    }
}
