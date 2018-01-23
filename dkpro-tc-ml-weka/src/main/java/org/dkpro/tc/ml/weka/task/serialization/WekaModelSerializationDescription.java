/**
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.ml.weka.task.serialization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;
import org.dkpro.tc.ml.weka.util.WekaUtils;

import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Knows what to do in order to serialize a model - is called as task by the main class.
 * 
 * Requires MetaTask and the output for the feature extraction stage. Should be called after
 * instantiation, using addImport(...), e.g.:
 * 
 * ModelSerializationTask saveModelTask = new ModelSerializationTask(...);
 * saveModelTask.addImport(metaTask, MetaInfoTask.META_KEY);
 * saveModelTask.addImport(featuresTrainTask, ExtractFeaturesTask.OUTPUT_KEY,
 * Constants.TEST_TASK_INPUT_KEY_TRAINING_DATA);
 */
public class WekaModelSerializationDescription
    extends ModelSerializationTask
    implements Constants
{

    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    protected List<String> classificationArguments;
    @Discriminator(name = DIM_FEATURE_SEARCHER_ARGS)
    protected List<String> featureSearcher;
    @Discriminator(name = DIM_ATTRIBUTE_EVALUATOR_ARGS)
    protected List<String> attributeEvaluator;
    @Discriminator(name = DIM_LABEL_TRANSFORMATION_METHOD)
    protected String labelTransformationMethod;
    @Discriminator(name = DIM_NUM_LABELS_TO_KEEP)
    protected int numLabelsToKeep;
    @Discriminator(name = DIM_APPLY_FEATURE_SELECTION)
    protected boolean applySelection;

    public WekaModelSerializationDescription()
    {
        // required for groovy (?)
    }

    public WekaModelSerializationDescription(String type, File outputFolder)
    {
        this.setType(type);
        this.setOutputFolder(outputFolder);
    }

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        writeWekaSpecificInformation(aContext);
        writeModelConfiguration(aContext, WekaClassificationAdapter.class.getName());
        writeBipartitionThreshold(outputFolder, threshold);
    }

    private void writeBipartitionThreshold(File outputFolder, String bipartitionThreshold)
        throws IOException
    {
        if (bipartitionThreshold == null) {
            bipartitionThreshold = "0.5";
        }

        Properties properties = new Properties();
        properties.setProperty(DIM_BIPARTITION_THRESHOLD, bipartitionThreshold);

        File file = new File(outputFolder + "/" + MODEL_BIPARTITION_THRESHOLD);
        FileOutputStream fileOut = new FileOutputStream(file);
        properties.store(fileOut,
                "Bipartition threshold used to train this model (only multi-label classification)");
        fileOut.close();
    }

    private void writeWekaSpecificInformation(TaskContext aContext)
        throws Exception
    {
        boolean isMultiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);
        boolean isRegression = learningMode.equals(Constants.LM_REGRESSION);

        File arffFileTrain = new File(
                aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA, AccessMode.READONLY).getPath()
                        + "/" + WekaClassificationAdapter.getInstance()
                                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile));

        Instances trainData = WekaUtils.getInstances(arffFileTrain, isMultiLabel);
        trainData = WekaUtils.removeInstanceId(trainData, isMultiLabel);

        // FEATURE SELECTION
        if (!isMultiLabel) {
            if (featureSearcher != null && attributeEvaluator != null) {
                // AttributeSelection attSel = WekaUtils.featureSelectionSinglelabel(aContext,
                // trainData, featureSearcher, attributeEvaluator);
                // trainData = attSel.reduceDimensionality(trainData);
                // Logger.getLogger(getClass()).info("APPLYING FEATURE SELECTION");
                throw new Exception(
                        "Feature Selection is currently not supported in Save Model mode.");
            }
        }
        else {
            if (attributeEvaluator != null && labelTransformationMethod != null
                    && numLabelsToKeep > 0) {
                // Remove attSel = WekaUtils.featureSelectionMultilabel(aContext, trainData,
                // attributeEvaluator, labelTransformationMethod, numLabelsToKeep);
                // trainData = WekaUtils.applyAttributeSelectionFilter(trainData, attSel);
                // Logger.getLogger(getClass()).info("APPLYING FEATURE SELECTION");
                throw new Exception(
                        "Feature Selection is currently not supported in Save Model mode.");
            }
        }

        // write training data header
        ObjectOutputStream outT = new ObjectOutputStream(
                new FileOutputStream(new File(outputFolder, "training_data")));
        Instances emptyTrainCopy = new Instances(trainData);
        emptyTrainCopy.delete();
        outT.writeObject(emptyTrainCopy);
        outT.close();

        // write model file
        Classifier cl = WekaUtils.getClassifier(learningMode, classificationArguments);
        cl.buildClassifier(trainData);
        File model = new File(outputFolder, MODEL_CLASSIFIER);
        model.getParentFile().mkdir();
        weka.core.SerializationHelper.write(model.getAbsolutePath(), cl);

        // write class labels file
        List<String> classLabels;
        if (!isRegression) {
            classLabels = WekaUtils.getClassLabels(trainData, isMultiLabel);
            String classLabelsString = StringUtils.join(classLabels, "\n");
            FileUtils.writeStringToFile(new File(outputFolder, MODEL_CLASS_LABELS),
                    classLabelsString, "utf-8");
        }

    }
}
