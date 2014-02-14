package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.fit.util.JCasUtil.toText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringListIterable;

public class NGramUtils
{

    public static String NGRAM_GLUE = "_";
    
    public static FrequencyDistribution<String> getAnnotationNgrams(JCas jcas, Annotation focusAnnotation,
            boolean lowerCaseNGrams, boolean filterPartialMatches, int minN, int maxN)
    {
        Set<String> empty = Collections.emptySet();
        return getAnnotationNgrams(jcas, focusAnnotation, lowerCaseNGrams, filterPartialMatches, minN, maxN, empty);
    }

    public static FrequencyDistribution<String> getAnnotationNgrams(
            JCas jcas,
            Annotation focusAnnotation,
            boolean lowerCaseNGrams,
            boolean filterPartialMatches,
            int minN,
            int maxN,
            Set<String> stopwords)
    {
        FrequencyDistribution<String> annoNgrams = new FrequencyDistribution<String>();

        // If the focusAnnotation contains sentence annotations, extract the ngrams sentence-wise
        // if not, extract them from all tokens in the focusAnnotation
        if (JCasUtil.selectCovered(jcas, Sentence.class, focusAnnotation).size() > 0) {
            for (Sentence s : selectCovered(jcas, Sentence.class, focusAnnotation)) {
                for (List<String> ngram : new NGramStringListIterable(toText(selectCovered(
                        Token.class, s)), minN, maxN)) {
                	
                	if(lowerCaseNGrams){
                		ngram = lower(ngram);
                	}

                    if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                        String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                        annoNgrams.inc(ngramString);
                    }
                }
            }
        }
        // FIXME the focus annotation branch doesn't make much sense
        else {
            for (List<String> ngram : new NGramStringListIterable(toText(selectCovered(Token.class,
                    focusAnnotation)), minN, maxN)) {
            	
            	if(lowerCaseNGrams){
            		ngram = lower(ngram);
            	}

                if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                    String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                    annoNgrams.inc(ngramString);
                }
            }
        }
        return annoNgrams;
    }

    /**
     * Get combinations of ngrams from a pair of documents.
     * 
     * @param document1NGrams
     *            ngrams from document 1
     * @param document2NGrams
     *            ngrams from document 2
     * @param minN
     *            minimum size for a new combined ngram
     * @param maxN
     *            max size for a new combined ngram
     * @return
     */
    public static FrequencyDistribution<String> getCombinedNgrams(
            FrequencyDistribution<String> document1NGrams,
            FrequencyDistribution<String> document2NGrams, 
            int minN, 
            int maxN, 
            boolean ngramUseSymmetricalCombos
            )
    {
        FrequencyDistribution<String> documentComboNGrams = new FrequencyDistribution<String>();
        for (String ngram1 : document1NGrams.getKeys()) {
            int ngram1size = StringUtils.countMatches(ngram1, NGRAM_GLUE) + 1;
            for (String ngram2 : document2NGrams.getKeys()) {
                int ngram2size = StringUtils.countMatches(ngram2, NGRAM_GLUE) + 1;
                if (ngram1size + ngram2size >= minN && ngram1size + ngram2size <= maxN) {
                    String comboNgram = ngram1 + NGRAM_GLUE + ngram2;
                    documentComboNGrams.inc(comboNgram);
                    if(ngramUseSymmetricalCombos){
                    	comboNgram = ngram2 + NGRAM_GLUE + ngram1;
                    	documentComboNGrams.inc(comboNgram);
                    }
                }
            }
        }
        return documentComboNGrams;
    }

    public static FrequencyDistribution<String> getDocumentNgrams(JCas jcas,
            boolean lowerCaseNGrams, boolean filterPartialMatches, int minN, int maxN)
    {
        Set<String> empty = Collections.emptySet();
        return getDocumentNgrams(jcas, lowerCaseNGrams, filterPartialMatches, minN, maxN, empty);
    }

    public static FrequencyDistribution<String> getDocumentNgrams(
            JCas jcas,
            boolean lowerCaseNGrams,
            boolean filterPartialMatches,
            int minN,
            int maxN,
            Set<String> stopwords)
    {
        FrequencyDistribution<String> documentNgrams = new FrequencyDistribution<String>();
        for (Sentence s : select(jcas, Sentence.class)) {
            for (List<String> ngram : new NGramStringListIterable(toText(selectCovered(Token.class,
                    s)), minN, maxN)) {

            	if(lowerCaseNGrams){
            		ngram = lower(ngram);
            	}

                if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                    String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                    documentNgrams.inc(ngramString);
                }
            }
        }
        return documentNgrams;
    }

    public static FrequencyDistribution<String> getDocumentPosNgrams(JCas jcas, int minN, int maxN, boolean useCanonical)
    {
        FrequencyDistribution<String> posNgrams = new FrequencyDistribution<String>();
        for (Sentence s : select(jcas, Sentence.class)) {        
            List<String> postagstrings = new ArrayList<String>();
            for (POS p : JCasUtil.selectCovered(jcas, POS.class, s)) {
                if (useCanonical) {
                    postagstrings.add(p.getClass().getSimpleName());
                }
                else {
                    postagstrings.add(p.getPosValue());
                }
            }
            String[] posarray = postagstrings.toArray(new String[postagstrings.size()]);
    
            for (List<String> ngram : new NGramStringListIterable(posarray, minN, maxN)) {
                posNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));

            }
        }
        return posNgrams;
    }
    
    public static FrequencyDistribution<String> getAnnotationPosNgrams(JCas jcas, Annotation anno, int minN, int maxN, boolean useCanonical) {
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        
        System.err.println("Attention: unit classification POS ngrams are not yet implemented");
        // FIXME implement this
        return fd;
    }


    /**
     * An ngram (represented by the list of tokens) does not pass the stopword filter:
     * a) filterPartialMatches=true - if it contains any stopwords
     * b) filterPartialMatches=false - if it entirely consists of stopwords
     * 
     * @param tokenList The list of tokens in a single ngram
     * @param stopwords The set of stopwords used for filtering
     * @param filterPartialMatches Whether ngrams where only parts are stopwords should also be filtered. For example, "United States of America" would be filtered, as it contains the stopword "of".
     * @return Whether the ngram (represented by the list of tokens) passes the stopword filter or not. 
     */
    public static boolean passesNgramFilter(List<String> tokenList, Set<String> stopwords, boolean filterPartialMatches)
    {
    	List<String> filteredList = new ArrayList<String>();
        for (String ngram : tokenList) {
            if (!stopwords.contains(ngram)) {
                filteredList.add(ngram);
            }
        }
        
        if (filterPartialMatches) {
            return filteredList.size() == tokenList.size();
        }
        else {
            return filteredList.size() != 0;
        }
    }

    public static FrequencyDistribution<String> getDocumentSkipNgrams(
            JCas jcas,
            boolean lowerCaseNGrams,
            boolean filterPartialMatches,
            int minN,
            int maxN,
            int skipN,
            Set<String> stopwords)
    {
        FrequencyDistribution<String> documentNgrams = new FrequencyDistribution<String>();
        for (Sentence s : select(jcas, Sentence.class)) {
            for (List<String> ngram : new SkipNgramStringListIterable(
                    toText(selectCovered(Token.class, s)), minN, maxN, skipN))
            {
            	if(lowerCaseNGrams){
            		ngram = lower(ngram);
            	}

                if (passesNgramFilter(ngram, stopwords, filterPartialMatches)) {
                    String ngramString = StringUtils.join(ngram, NGRAM_GLUE);
                    documentNgrams.inc(ngramString);
                }
            }
        }
        return documentNgrams;
    }
    
    public static List<String> lower(List<String> ngram){
    	List<String> newNgram = new ArrayList<String>();
    	for(String token: ngram){
    		newNgram.add(token.toLowerCase());
    	}
    	return newNgram;
    }
}