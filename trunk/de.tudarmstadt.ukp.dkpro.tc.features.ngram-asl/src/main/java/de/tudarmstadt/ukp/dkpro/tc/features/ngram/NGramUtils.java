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
            boolean lowerCaseNGrams, int minN, int maxN)
    {
        Set<String> empty = Collections.emptySet();
        return getAnnotationNgrams(jcas, focusAnnotation, lowerCaseNGrams, minN, maxN, empty);
    }

    public static FrequencyDistribution<String> getAnnotationNgrams(JCas jcas,
            Annotation focusAnnotation, boolean lowerCaseNGrams, int minN, int maxN,
            Set<String> stopwords)
    {
        FrequencyDistribution<String> annoNgrams = new FrequencyDistribution<String>();

        // If the focusAnnotation contains sentence annotations, extract the ngrams sentence-wise
        // if not, extract them from all tokens in the focusAnnotation
        if (JCasUtil.selectCovered(jcas, Sentence.class, focusAnnotation).size() > 0) {
            for (Sentence s : selectCovered(jcas, Sentence.class, focusAnnotation)) {
                for (List<String> ngram : new NGramStringListIterable(toText(selectCovered(
                        Token.class, s)), minN, maxN)) {

                    ngram = filterNgram(ngram, lowerCaseNGrams, stopwords);

                    // filter might have reduced size to zero => don't add in this case
                    if (ngram.size() > 0) {
                        annoNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));
                    }
                }
            }
        }
        else {
            for (List<String> ngram : new NGramStringListIterable(toText(selectCovered(Token.class,
                    focusAnnotation)), minN, maxN)) {

                ngram = filterNgram(ngram, lowerCaseNGrams, stopwords);

                // filter might have reduced size to zero => don't add in this case
                if (ngram.size() > 0) {
                    annoNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));
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
            FrequencyDistribution<String> document2NGrams, int minN, int maxN)
    {
        FrequencyDistribution<String> documentComboNGrams = new FrequencyDistribution<String>();
        for (String ngram1 : document1NGrams.getKeys()) {
            int ngram1size = StringUtils.countMatches(ngram1, NGRAM_GLUE) + 1;
            for (String ngram2 : document2NGrams.getKeys()) {
                int ngram2size = StringUtils.countMatches(ngram2, NGRAM_GLUE) + 1;
                if (ngram1size + ngram2size >= minN && ngram1size + ngram2size <= maxN) {
                    String comboNgram = ngram1 + "_" + ngram2;
                    documentComboNGrams.inc(comboNgram);
                }
            }
        }
        return documentComboNGrams;
    }

    public static FrequencyDistribution<String> getDocumentNgrams(JCas jcas,
            boolean lowerCaseNGrams, int minN, int maxN)
    {
        Set<String> empty = Collections.emptySet();
        return getDocumentNgrams(jcas, lowerCaseNGrams, minN, maxN, empty);
    }

    public static FrequencyDistribution<String> getDocumentNgrams(JCas jcas,
            boolean lowerCaseNGrams, int minN, int maxN, Set<String> stopwords)
    {
        FrequencyDistribution<String> documentNgrams = new FrequencyDistribution<String>();
        for (Sentence s : select(jcas, Sentence.class)) {
            // TODO parameterize type
            for (List<String> ngram : new NGramStringListIterable(toText(selectCovered(Token.class,
                    s)), minN, maxN)) {

                ngram = filterNgram(ngram, lowerCaseNGrams, stopwords);

                // filter might have reduced size to zero => don't add in this case
                if (ngram.size() > 0) {
                    documentNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));
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
     * Ngrams will only be filtered out, if they exactly match an entry in the stopword list.
     * No partial matches will be filtered.
     * 
     * @param ngramList The list of ngrams
     * @param lowerCase Whether to lowercase all ngrams
     * @param stopwords The set of stopwords used for filtering
     * @return The list of ngrams filtered using the stopword list. 
     */
    public static List<String> filterNgram(List<String> ngramList, boolean lowerCase,
            Set<String> stopwords)
    {
        List<String> filteredNgram = new ArrayList<String>();
        for (String ngram : ngramList) {
            if (lowerCase) {
                ngram = ngram.toLowerCase();
            }
            
            if (!stopwords.contains(ngram)) {
                filteredNgram.add(ngram);
            }
        }
        return filteredNgram;
    }

    public static FrequencyDistribution<String> getDocumentSkipNgrams(
            JCas jcas,
            boolean lowerCaseNGrams,
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
                ngram = filterNgram(ngram, lowerCaseNGrams, stopwords);

                // filter might have reduced size to zero => don't add in this case
                if (ngram.size() > 0) {
                    documentNgrams.inc(StringUtils.join(ngram, NGRAM_GLUE));
                }
            }
        }
        return documentNgrams;
    }
}