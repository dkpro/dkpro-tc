package de.tudarmstadt.ukp.dkpro.tc.features.length;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeature;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class NrOfSentencesFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{

    public static final String FN_NR_OF_SENTENCES = "NrofSentences";

    @Override
    public List<Feature> extract(JCas jcas, TextClassificationUnit classificationUnit)
        throws TextClassificationException
    {

        if (classificationUnit == null) {
            return Arrays.<Feature>asList(new SimpleFeature(FN_NR_OF_SENTENCES, JCasUtil.select(jcas,
                    Sentence.class).size()));
        }
        else {
            return Arrays.<Feature>asList(new SimpleFeature(FN_NR_OF_SENTENCES, JCasUtil.selectCovered(jcas,
                    Sentence.class, classificationUnit).size()));
        }
    }
}
