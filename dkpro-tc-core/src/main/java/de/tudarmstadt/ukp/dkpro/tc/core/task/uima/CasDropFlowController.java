package de.tudarmstadt.ukp.dkpro.tc.core.task.uima;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.metadata.FixedFlow;
import org.apache.uima.analysis_engine.metadata.FlowConstraints;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.CasFlowController_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.flow.CasFlow_ImplBase;
import org.apache.uima.flow.FinalStep;
import org.apache.uima.flow.Flow;
import org.apache.uima.flow.FlowControllerContext;
import org.apache.uima.flow.SimpleStep;
import org.apache.uima.flow.Step;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.util.ValidityCheckUtils;

/**
 * Flow controller that drops the CAS for multiple reasons including:
 * - empty CAS
 * - no @TextClassificationOutcome in document mode
 * - no @TextClassificationUnit in unit mode 
 * - no @TextClassificationSequence in sequence mode
 * 
 * In general in all cases where the CAS is not properly initialized
 * in order to work with TC.
 *
 */
public class CasDropFlowController 
	extends CasFlowController_ImplBase
{
	
    @ConfigurationParameter(name = ConnectorBase.PARAM_LEARNING_MODE, mandatory = true, defaultValue = Constants.LM_SINGLE_LABEL)
    private String learningMode;

    @ConfigurationParameter(name = ConnectorBase.PARAM_FEATURE_MODE, mandatory = true, defaultValue = Constants.FM_DOCUMENT)
    private String featureMode;

    private int featureModeI;
    private int learningModeI;
   
   private String[] mSequence;

	public void initialize(FlowControllerContext aContext)
			throws ResourceInitializationException
	{
		super.initialize(aContext);
		FlowConstraints flowConstraints = aContext.getAggregateMetadata().getFlowConstraints();
		mSequence = ((FixedFlow) flowConstraints).getFixedFlow();
	}

	public Flow computeFlow(CAS aCAS) throws AnalysisEngineProcessException {
		FixedFlowObject ffo = new FixedFlowObject(aCAS, 0);
		ffo.setCas(aCAS);
		return ffo;
	}

	class FixedFlowObject 
		extends CasFlow_ImplBase
	{
		private int currentStep;


		public FixedFlowObject(CAS cas, int startStep) {
			setCas(cas);
			currentStep = startStep;
		}


		public Step next() throws AnalysisEngineProcessException {
			
			// check before multiplier is applied, i.e. after preprocessing
			if (mSequence[currentStep].contains("ClassificationUnitCasMultiplier")) {
				if (featureModeI == 0) {
		        	featureModeI = ValidityCheckUtils.featureModeLabel2int(featureMode);
		        }
	
		        if (learningModeI == 0) {
		        	learningModeI = ValidityCheckUtils.learningModeLabel2int(learningMode);
		        }
		        
		        JCas jcas;
				try {
					jcas = getCas().getJCas();
				} catch (CASException e) {
					throw new AnalysisEngineProcessException(e);
				}
				
				// drop conditions
				if (jcas.getDocumentText().length() == 0) {
					return new FinalStep();
				}
				
		        Collection<TextClassificationOutcome> outcomes = JCasUtil.select(jcas,
		                TextClassificationOutcome.class);
		        Collection<TextClassificationUnit> classificationUnits = JCasUtil.select(jcas,
		                TextClassificationUnit.class);
		        
		        // whether outcome annotation are present at all
		        if (outcomes.size() == 0) {
		            getLogger().log(Level.WARNING, 
	                        "No TextClassificationOutcome annotation found. "
	                        		+ "The reader must make sure that the expected outcome of the classification is annotated accordingly.");
					return new FinalStep();
		        }
	
		        // iff unit/sequence classification is active, there must be classificationUnit
		        // annotations, each
		        // labeled with an outcome annotation
		        if (featureModeI == 2 || featureModeI == 4) {
		            if (classificationUnits.size() == 0) {
			            getLogger().log(Level.WARNING, 
		                        "Your experiment is configured to have classification units, but no classification unit present.");
						return new FinalStep();
		            }
		        }
				
				// final step if sequence ends
			    if (currentStep >= mSequence.length) {
			          return new FinalStep(); // this CAS has finished the sequence
			    }
			}
			    
			return new SimpleStep(mSequence[currentStep++]);
		}


		@Override
		protected Flow newCasProduced(CAS newCas, String producedBy)
				throws AnalysisEngineProcessException
		{
		      return new FixedFlowObject(newCas, currentStep);
		}
	}
}
