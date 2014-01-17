package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.IFeature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.DependencyMetaCollector;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" })
public class DependencyFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor, MetaDependent
{

    public static final String PARAM_DEP_FD_FILE = "depFdFile";
    @ConfigurationParameter(name = PARAM_DEP_FD_FILE, mandatory = true)
    private String depFdFile;

    public static final String PARAM_DEP_FREQ_THRESHOLD = "depFreqThreshold";
    @ConfigurationParameter(name = PARAM_DEP_FREQ_THRESHOLD, mandatory = false, defaultValue = "2")
    private int depFreqThreshold;

    public static final String PARAM_LOWER_CASE_DEPS = "lowerCaseDeps";
    @ConfigurationParameter(name = PARAM_LOWER_CASE_DEPS, mandatory = false, defaultValue = "true")
    private boolean lowerCaseDeps;

    protected Set<String> depSet;

    private FrequencyDistribution<String> trainingDepsFD;

    @Override
    public List<IFeature> extract(JCas jcas)
        throws TextClassificationException
    {
        // if(focusAnnotation!=null){
        // throw new TextClassificationException(new
        // UnsupportedOperationException("FocusAnnotation not yet supported!"));
        // }

        List<IFeature> features = new ArrayList<IFeature>();

        Set<String> depStrings = new HashSet<String>();
        for (Dependency dep : JCasUtil.select(jcas, Dependency.class)) {
            String type = dep.getDependencyType();
            String governor = dep.getGovernor().getCoveredText();
            String dependent = dep.getDependent().getCoveredText();

            depStrings.add(DependencyMetaCollector.getDependencyString(governor, dependent, type,
                    lowerCaseDeps));
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
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        depSet = getTopDeps();

        return true;
    }

    private Set<String> getTopDeps()
        throws ResourceInitializationException
    {
        Set<String> depSet = new HashSet<String>();
        try {
            trainingDepsFD = new FrequencyDistribution<String>();
            trainingDepsFD.load(new File(depFdFile));
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

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(DependencyMetaCollector.class);

        return metaCollectorClasses;
    }
}