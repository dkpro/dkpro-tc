package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.TripleMetaCollector;

public class TripleFeatureExtractor
    extends FeatureExtractorResource_ImplBase
{

    public static final String PARAM_TRIPLE_FD_FILE = "TripleFDFile";
    @ConfigurationParameter(name=PARAM_TRIPLE_FD_FILE, mandatory=true)
    private String fdFile;

    public static final String PARAM_THRESHOLD = "TripleThreshold";
    @ConfigurationParameter(name=PARAM_THRESHOLD, mandatory=false, defaultValue = "2")
    private int threshold;

    public static final String PARAM_LOWER_CASE = "LowerCaseTriples";
    @ConfigurationParameter(name=PARAM_LOWER_CASE, mandatory=false, defaultValue = "true")
    private boolean lowerCaseTriples;


    protected Set<String> tripleSet;

    private FrequencyDistribution<String> trainingFD;

    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws TextClassificationException
    {
        if(focusAnnotation!=null){
        	throw new TextClassificationException(new UnsupportedOperationException("FocusAnnotation not yet supported!"));
        }
    	List<Feature> features = new ArrayList<Feature>();

        Set<String> triples = TripleMetaCollector.getTriples(jcas, lowerCaseTriples);
        for (String featureTriple : tripleSet) {
            if (triples.contains(featureTriple)) {
                features.add(new Feature("lexicalTriple_" + featureTriple, 1));
            }
            else {
                features.add(new Feature("lexicalTriple_" + featureTriple, 0));
            }
        }

        return features;
    }

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        tripleSet = loadTriples();

        return true;
    }

    private Set<String> loadTriples()
            throws ResourceInitializationException
    {

        Set<String> tripleSet = new HashSet<String>();

        try {
            trainingFD = new FrequencyDistribution<String>();
            trainingFD.load(new File(fdFile));
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        for (String key : trainingFD.getKeys()) {
            if (trainingFD.getCount(key) > threshold) {
                tripleSet.add(key);
            }
        }

        return tripleSet;
    }
}