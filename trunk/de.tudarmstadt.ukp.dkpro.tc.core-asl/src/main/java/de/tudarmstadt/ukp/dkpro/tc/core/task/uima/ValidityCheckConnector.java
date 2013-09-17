package de.tudarmstadt.ukp.dkpro.tc.core.task.uima;

import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.tc.core.task.ValidityCheckTask;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

/**
 * UIMA analysis engine that is used in the {@link ValidityCheckTask} to test error conditions on the CAS.
 * 
 * @author zesch
 *
 */
public class ValidityCheckConnector
    extends JCasAnnotator_ImplBase
{
    
    public static final String PARAM_IS_REGRESSION = "valCheckerIsRegression";
    @ConfigurationParameter(name = PARAM_IS_REGRESSION, mandatory = true)
    private boolean isRegression;

    private boolean firstCall;
        
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        firstCall = true;
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
                
        // make sure this class is only called once per pipeline
        if (firstCall) {
            firstCall = false;
            
            getLogger().log(Level.INFO, "--- checking validity of experiment setup ---");

            Collection<TextClassificationOutcome> outcomes = JCasUtil.select(jcas, TextClassificationOutcome.class);
            if (outcomes.size() == 0) {
                throw new AnalysisEngineProcessException(new TextClassificationException(
                        "No TextClassificationOutcome annotation found. " +
                        "The reader must make sure that the expected outcome of the classification is annotated accordingly."));
            }
        }
    }
}