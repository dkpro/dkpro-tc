package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

public class NGramDFE
    extends NGramFeatureExtractorBase
{

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected FrequencyDistribution<String> getTopNgrams()
        throws ResourceInitializationException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getFeaturePrefix()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected FrequencyDistribution<String> getDocumentNgrams(JCas jcas)
        throws TextClassificationException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected FrequencyDistribution<String> getAnnotationNgrams(JCas jcas, Annotation anno)
        throws TextClassificationException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
