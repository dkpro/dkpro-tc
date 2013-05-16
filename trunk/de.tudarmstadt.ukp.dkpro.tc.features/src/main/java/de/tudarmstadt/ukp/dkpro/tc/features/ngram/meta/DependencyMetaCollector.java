package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;


import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class DependencyMetaCollector
    extends FreqDistBasedMetaCollector
{
    public static final String DEP_FD_KEY = "dep.ser";
    
    public static final String PARAM_DEP_FD_FILE = "DepFdFile";
    @ConfigurationParameter(name = PARAM_DEP_FD_FILE, mandatory = true)
    private File depFdFile;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        fdFile = depFdFile;
    }
    
    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        for (Dependency dep : JCasUtil.select(jcas, Dependency.class)) {
            String type = dep.getDependencyType();
            String governor = dep.getGovernor().getCoveredText();
            String dependent = dep.getDependent().getCoveredText();

            String dependencyString = getDependencyString(governor, dependent, type, lowerCase);
            fd.inc(dependencyString);
        }
    }

    public static String getDependencyString(String governor, String dependent, String type, boolean lowerCase) {
        if (lowerCase) {
            governor = governor.toLowerCase();
            dependent = dependent.toLowerCase();
        }
        
        return governor + "-" + type + "-" + dependent;
    }
}