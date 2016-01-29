/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.serialization;

import java.io.File;
import java.util.List;
import java.util.SortedSet;

import org.apache.commons.collections.BidiMap;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ModelSerializationTask;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.SVMHMMAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.SVMHMMTestTask;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.util.SVMHMMUtils;

public class SvmhmmModelSerializationDescription  
    extends ModelSerializationTask
    implements Constants
{

    @Discriminator
    protected List<Object> pipelineParameters;
    @Discriminator
    protected List<String> featureSet;
    @Discriminator
    private String[] classificationArguments;
    @Discriminator
    protected String featureMode;
    @Discriminator
    protected String learningMode;


    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        trainAndStoreModel(aContext);
        writeModelConfiguration(aContext, SVMHMMAdapter.class.getName());
    }

    private void trainAndStoreModel(TaskContext aContext)
        throws Exception
    {

        File trainingDataStorage = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                StorageService.AccessMode.READONLY);

        // file name of the data; THE FILES HAVE SAME NAME FOR BOTH TRAINING AND
        // TESTING!!!!!!
        String fileName = new SVMHMMAdapter()
                .getFrameworkFilename(TCMachineLearningAdapter.AdapterNameEntries.featureVectorsFile);

        File trainingFile = new File(trainingDataStorage, fileName);

        SortedSet<String> outcomeLabels = SVMHMMUtils
                .extractOutcomeLabelsFromFeatureVectorFiles(trainingFile);
        BidiMap labelsToIntegersMapping = SVMHMMUtils.mapVocabularyToIntegers(outcomeLabels);

        // // save mapping to file
        File mappingFile = new File(outputFolder.toString() + "/"
                + SVMHMMUtils.LABELS_TO_INTEGERS_MAPPING_FILE_NAME);
        SVMHMMUtils.saveMapping(labelsToIntegersMapping, mappingFile);

        File augmentedTrainingFile = SVMHMMUtils.replaceLabelsWithIntegers(trainingFile,
                labelsToIntegersMapping);

        File classifier = new File(outputFolder.getAbsolutePath() + "/" + MODEL_CLASSIFIER);
        // train the model
        new SVMHMMTestTask().trainModel(classifier, augmentedTrainingFile);
    }

}
