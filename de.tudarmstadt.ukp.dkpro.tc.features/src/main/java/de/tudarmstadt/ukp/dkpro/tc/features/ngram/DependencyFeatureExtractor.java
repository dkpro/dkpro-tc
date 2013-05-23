package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.factory.initializable.Initializable;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.DependencyMetaCollector;

public class DependencyFeatureExtractor
    implements FeatureExtractor, Initializable
{

    public static final String PARAM_DEP_FILE = "DepFile";
    public static final String PARAM_DEP_FREQ_THRESHOLD = "DepFreqThreshold";
    public static final String PARAM_LOWER_CASE = "LowerCaseDeps";

    private String depFile;
    private int depFreqThreshold = 2;
    protected Set<String> depSet;
    private boolean lowerCaseDeps = true;

    private FrequencyDistribution<String> trainingDepsFD;

    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws TextClassificationException
    {
        if(focusAnnotation!=null){
        	throw new TextClassificationException(new UnsupportedOperationException("FocusAnnotation not yet supported!"));
        }

    	List<Feature> features = new ArrayList<Feature>();

    	Set<String> depStrings = new HashSet<String>(); 
        for (Dependency dep : JCasUtil.select(jcas, Dependency.class)) {
            String type = dep.getDependencyType();
            String governor = dep.getGovernor().getCoveredText();
            String dependent = dep.getDependent().getCoveredText();

            depStrings.add(DependencyMetaCollector.getDependencyString(governor, dependent, type, lowerCaseDeps));
        }
        
        for (String topDep : depSet) {
            if (depStrings.contains(topDep)) {
                features.add(new Feature(topDep, 1));
            }
            else {
                features.add(new Feature(topDep, 0));
            }
        }

        return features;
    }

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {

        depSet = getTopDeps();
    }

    private Set<String> getTopDeps()
            throws ResourceInitializationException
    {
        Set<String> depSet = new HashSet<String>();
        try {
            trainingDepsFD = new FrequencyDistribution<String>();
            trainingDepsFD.load(new File(depFile));
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        for (String key : trainingDepsFD.getKeys()) {
            if (trainingDepsFD.getCount(key) > depFreqThreshold) {
                depSet.add(key);
            }
        }

        return depSet;
    }
}