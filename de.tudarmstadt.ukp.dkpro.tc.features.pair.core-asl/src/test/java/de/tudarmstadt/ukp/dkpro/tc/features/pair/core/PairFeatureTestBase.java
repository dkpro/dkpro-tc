package de.tudarmstadt.ukp.dkpro.tc.features.pair.core;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

public class PairFeatureTestBase
{

    public List<IFeature> runExtractor(AnalysisEngine engine, PairFeatureExtractor extractor)
        throws ResourceInitializationException, TextClassificationException,
        AnalysisEngineProcessException
    {

        JCas jcas1 = engine.newJCas();
        jcas1.setDocumentLanguage("en");
        jcas1.setDocumentText("This is the text of view 1");
        engine.process(jcas1);

        JCas jcas2 = engine.newJCas();
        jcas2.setDocumentLanguage("en");
        jcas2.setDocumentText("This is the text of view 2");
        engine.process(jcas2);

        return extractor.extract(jcas1, jcas2);

    }

}
