package de.tudarmstadt.ukp.dkpro.tc.weka.task.uima;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import meka.classifiers.multilabel.MultilabelClassifier;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.ConnectorBase;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.ExtractFeaturesAndPredictTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * 
 * UIMA analysis engine that is used in the {@link ExtractFeaturesAndPredictTask} to apply the
 * feature extractors on each CAS, and classify them using a previously trained model.
 * 
 * @author daxenberger
 * 
 */
public class ExtractFeaturesAndPredictConnector
    extends ConnectorBase
{

    /**
     * Public name of the prediction map file
     */
    public static final String PREDICTION_MAP_FILE_NAME = "prediction_map.ser";

    public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";
    @ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, mandatory = true)
    private File outputDirectory;

    public static final String PARAM_ARFF_FILE_TRAINING = "arffFileTrainingData";
    @ConfigurationParameter(name = PARAM_ARFF_FILE_TRAINING, mandatory = true)
    private File arffFileTrainingData;

    public static final String PARAM_BIPARTITION_THRESHOLD = "bipartitionThreshold";
    @ConfigurationParameter(name = PARAM_BIPARTITION_THRESHOLD, mandatory = true, defaultValue = "0.5")
    private String bipartitionThreshold;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    @ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
    private String learningMode;

    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
    private String featureMode;

    @ConfigurationParameter(name = PARAM_DEVELOPER_MODE, mandatory = true, defaultValue = "false")
    private boolean developerMode;

    public static final String PARAM_CLASSIFICATION_ARGUMENTS = "classificationArguments";
    @ConfigurationParameter(name = PARAM_CLASSIFICATION_ARGUMENTS, mandatory = true)
    private List<String> classificationArguments;

    private Map<String, List<String>> predictionMap;

    private Classifier wekaClassifier;
    List<Attribute> attributes;
    List<String> allClassLabels;

    boolean isRegression;
    boolean isMultiLabel;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        isRegression = learningMode.equals(Constants.LM_REGRESSION);
        isMultiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        predictionMap = new HashMap<String, List<String>>();

        if (featureExtractors.length == 0) {
            context.getLogger().log(Level.SEVERE, "No feature extractors have been defined.");
            throw new ResourceInitializationException();
        }

        try {
            if (isMultiLabel) {
                List<String> mlArgs = classificationArguments
                        .subList(1, classificationArguments.size());
                wekaClassifier = AbstractClassifier.forName(classificationArguments.get(0),
                        new String[] {});
                ((MultilabelClassifier) wekaClassifier).setOptions(mlArgs.toArray(new String[0]));
            }
            else {
                wekaClassifier = AbstractClassifier.forName(classificationArguments.get(0),
                        classificationArguments
                                .subList(1, classificationArguments.size()).toArray(new String[0]));
            }

            weka.core.Instances trainData = TaskUtils.getInstances(arffFileTrainingData,
                    isMultiLabel);
            trainData = WekaUtils.removeOutcomeId(trainData, isMultiLabel);
            wekaClassifier.buildClassifier(trainData);

            attributes = new ArrayList<Attribute>();
            Enumeration<Attribute> atts = trainData.enumerateAttributes();
            while (atts.hasMoreElements()) {
                attributes.add(atts.nextElement());
            }
            attributes.add(trainData.classAttribute());
            if (!isRegression) {
                allClassLabels = TaskUtils.getClassLabels(trainData, isMultiLabel);
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        Instance instance = de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils.getSingleInstance(
                featureMode, featureExtractors, jcas,
                developerMode, false);

        weka.core.Instance wekaInstance;
        try {
            if (!isMultiLabel) {
                wekaInstance = WekaUtils.tcInstanceToWekaInstance(instance, attributes,
                        allClassLabels, isRegression);
            }
            else {
                wekaInstance = WekaUtils.tcInstanceToMekaInstance(instance, attributes,
                        allClassLabels);
            }
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        List<String> predicted = new ArrayList<String>();

        try {
            // singlelabel
            if (!isMultiLabel) {
                double val = wekaClassifier.classifyInstance(wekaInstance);
                if (!isRegression) {
                    predicted.add(wekaInstance.classAttribute().value((int) val));
                }
                else {
                    // regression
                    predicted.add(String.valueOf(val));
                }
            }
            // multilabel
            else {
                double[] prediction =
                        wekaClassifier.distributionForInstance(wekaInstance);
                for (int i = 0; i < prediction.length; i++) {
                    if (prediction[i] >= Double.valueOf(bipartitionThreshold)) {
                        String label = wekaInstance.attribute(i).name()
                                .split(WekaDataWriter.CLASS_ATTRIBUTE_PREFIX)[1];
                        predicted.add(label);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

        String docId = DocumentMetaData.get(jcas).getDocumentId();
        if (docId == null) {
            throw new AnalysisEngineProcessException("DocumentId cannot be empty", null);
        }
        predictionMap.put(docId, predicted);
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        File file = new File(outputDirectory, PREDICTION_MAP_FILE_NAME);
        try {
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(predictionMap);
            s.close();
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}