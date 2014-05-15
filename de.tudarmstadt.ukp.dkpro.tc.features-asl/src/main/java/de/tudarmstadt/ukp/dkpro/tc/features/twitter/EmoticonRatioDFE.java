package de.tudarmstadt.ukp.dkpro.tc.features.twitter;

import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.tweet.EMO;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * A feature extracting the ratio of emoticons to tokens in tweets.
 * 
 * This example is taken from the paper:
 * 
 * <pre>
 * Johannes Daxenberger and Oliver Ferschke and Iryna Gurevych and Torsten Zesch (2014).
 * DKPro TC: A Java-based Framework for Supervised Learning Experiments on Textual Data.
 * In: Proceedings of the 52nd Annual Meeting of the ACL.
 * </pre>
 * 
 */
public class EmoticonRatioDFE
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    @Override
    public List<Feature> extract(JCas jCas)
        throws TextClassificationException
    {
        int nrOfEmoticons = JCasUtil.select(jCas, EMO.class).size();
        int nrOfTokens = JCasUtil.select(jCas, Token.class).size();
        double ratio = (double) nrOfEmoticons / nrOfTokens;
        return new Feature(EmoticonRatioDFE.class.getSimpleName(), ratio).asList();
    }
}
