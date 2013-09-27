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

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ValidityCheckTask;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

/**
 * UIMA analysis engine that is used in the {@link ValidityCheckTask} to test error conditions on
 * the CAS.
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

    public static final String PARAM_IS_MULTILABEL = "valCheckerIsMultiLabel";
    @ConfigurationParameter(name = PARAM_IS_MULTILABEL, mandatory = true)
    private boolean isMultiLabel;

    public static final String PARAM_IS_UNIT_CLASSIFICATION = "valCheckerIsUnitClassification";
    @ConfigurationParameter(name = PARAM_IS_UNIT_CLASSIFICATION, mandatory = true)
    private boolean isUnitClassification;

    public static final String PARAM_DATA_WRITER = "valCheckerDataWriter";
    @ConfigurationParameter(name = PARAM_DATA_WRITER, mandatory = true)
    private String dataWriter;

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

            Collection<TextClassificationOutcome> outcomes = JCasUtil.select(jcas,
                    TextClassificationOutcome.class);
            Collection<TextClassificationUnit> classificationUnits = JCasUtil.select(jcas,
                    TextClassificationUnit.class);

            // whether outcome annotation are present at all
            if (outcomes.size() == 0) {
                throw new AnalysisEngineProcessException(
                        new TextClassificationException(
                                "No TextClassificationOutcome annotation found. "
                                        +
                                        "The reader must make sure that the expected outcome of the classification is annotated accordingly."));
            }

            // iff multi-label classification is active, no single-label data writer may be used
            if (isMultiLabel) {
                if (dataWriter.equals(Constants.WEKA_DATA_WRITER_NAME)) {
                    throw new AnalysisEngineProcessException(
                            new TextClassificationException(
                                    "Your experiment is configured to be multi-label. Please use a DataWriter, which is able to handle multi-label data."));
                }
            }

            // iff single-label is configured, there may not be more than one outcome annotation per
            // CAS, except this is the unit classification
            if (!isMultiLabel && !isUnitClassification && outcomes.size() > 2) {
                throw new AnalysisEngineProcessException(
                        new TextClassificationException(
                                "Your experiment is configured to be single-label, but I found more than one outcome annotation for "
                                        + DocumentMetaData.get(jcas).getDocumentUri()
                                        + ". Please configure your project to be multi-label or make sure to have only one outcome per instance."));
            }

            // iff unit classification is active, there must be classificationUnit annotations, each
            // labeled with an outcome annotation
            if (isUnitClassification) {
                if (classificationUnits.size() == 0) {
                    throw new AnalysisEngineProcessException(
                            new TextClassificationException(
                                    "Your experiment is configured to have classification units. Please add classification unit annotations to the CAS while reading your initial files."));
                }
                else {
                    for (TextClassificationUnit classificationUnit : classificationUnits) {
                        if (JCasUtil.selectCovered(jcas,
                                TextClassificationOutcome.class, classificationUnit).size() == 0) {
                            throw new AnalysisEngineProcessException(
                                    new TextClassificationException(
                                            "I did not find an outcome annotation for "
                                                    + classificationUnit.getCoveredText()
                                                    + ". Please add outcome annotations for all classification units."));
                        }
                    }
                }
            }
        }
    }
}