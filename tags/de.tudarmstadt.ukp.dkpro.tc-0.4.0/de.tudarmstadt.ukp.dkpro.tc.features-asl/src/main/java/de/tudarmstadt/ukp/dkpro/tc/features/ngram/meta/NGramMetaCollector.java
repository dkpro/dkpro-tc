package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;

public class NGramMetaCollector
    extends FreqDistBasedMetaCollector
{
	public static final String NGRAM_FD_KEY = "ngrams.ser";

	@ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_FD_FILE, mandatory = true)
	private File ngramFdFile;

	@ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
	private int minN;

	@ConfigurationParameter(name = NGramFeatureExtractor.PARAM_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
	private int maxN;

	@ConfigurationParameter(name = NGramFeatureExtractor.PARAM_STOPWORDS_FILE, mandatory = false)
	private String stopFile;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException
			{
		super.initialize(context);
		fdFile = ngramFdFile;
			}

	@Override
	public void process(JCas jcas)
			throws AnalysisEngineProcessException
			{
		FrequencyDistribution<String> documentNGrams = null;
		if (stopFile != null && !stopFile.isEmpty()) {
			try {
                URL stopUrl = ResourceUtils.resolveLocation(stopFile, null);
                InputStream is = stopUrl.openStream();
                Set<String> stopwords = new HashSet<String>();
                stopwords.addAll(IOUtils.readLines(is, "UTF-8"));
				documentNGrams = NGramUtils.getDocumentNgrams(jcas, lowerCase, minN, maxN, stopwords);
				
			}
			catch (Exception e) {
				throw new AnalysisEngineProcessException(e);
			}
		}                
		else {
			documentNGrams = NGramUtils.getDocumentNgrams(jcas, lowerCase, minN, maxN);
		}

		for (String ngram : documentNGrams.getKeys()) {
			fd.addSample(ngram, documentNGrams.getCount(ngram));
		}
	}

    @Override
    public Map<String, String> getParameterKeyPairs()
    {
        Map<String,String> mapping = new HashMap<String,String>();
        mapping.put(NGramFeatureExtractor.PARAM_NGRAM_FD_FILE, NGRAM_FD_KEY);
        return mapping;
    }
}