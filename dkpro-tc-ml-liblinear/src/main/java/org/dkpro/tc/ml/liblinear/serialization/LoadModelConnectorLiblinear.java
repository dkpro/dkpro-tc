/*******************************************************************************
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
 ******************************************************************************/

package org.dkpro.tc.ml.liblinear.serialization;

import static org.dkpro.tc.core.Constants.MODEL_CLASSIFIER;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.util.SaveModelUtils;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.ml.liblinear.FeatureNodeArrayEncoder;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.uima.TcAnnotator;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Problem;

public class LoadModelConnectorLiblinear
    extends ModelSerialization_ImplBase
{

    @ConfigurationParameter(name = TcAnnotator.PARAM_TC_MODEL_LOCATION, mandatory = true)
    private File tcModelLocation;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    @ConfigurationParameter(name = PARAM_FEATURE_STORE_CLASS, mandatory = true)
    private String featureStoreImpl;
    
    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
    private String featureMode;

    private Model liblinearModel;
    private Map<Integer, String> outcomeMapping;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            // TODO: Load mapping
            liblinearModel = Linear.loadModel(new File(tcModelLocation, MODEL_CLASSIFIER));
            outcomeMapping=loadOutcome2IntegerMapping(tcModelLocation);
            SaveModelUtils.verifyTcVersion(tcModelLocation, getClass());
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    private Map<Integer, String> loadOutcome2IntegerMapping(File tcModelLocation)
        throws IOException
    {
        Map<Integer, String> map = new HashMap<>();
        List<String> readLines = FileUtils
                .readLines(new File(tcModelLocation, LiblinearAdapter.getOutcomeMappingFilename()));
        for (String l : readLines) {
            String[] split = l.split("\t");
            map.put(Integer.valueOf(split[1]), split[0]);
        }
        return map;
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        try {
            FeatureStore featureStore = (FeatureStore) Class.forName(featureStoreImpl)
                    .newInstance();

                Instance inst = TaskUtils.getSingleInstance(featureMode, featureExtractors, jcas, false, true, featureStore.supportsSparseFeatures());
                    featureStore.addInstance(inst);


            FeatureNodeArrayEncoder encoder = new FeatureNodeArrayEncoder();
            FeatureNode[][] nodes = encoder.featueStore2FeatureNode(featureStore);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nodes.length; i++) {
                List<String> elements = new ArrayList<String>();
                for (int j = 0; j < nodes[i].length; j++) {
                    FeatureNode node = nodes[i][j];
                    int index = node.getIndex();
                    double value = node.getValue();

                    // write sparse values, i.e. skip zero values
                    if (Math.abs(value) > 0.00000000001) {
                        elements.add(index + ":" + value);
                    }
                }
                sb.append("999999999"); //DUMMY value for our outcome
                sb.append("\t");
                sb.append(StringUtils.join(elements, "\t"));
                sb.append("\n");
            }

            File inputData = File.createTempFile("libLinearePrediction",LiblinearAdapter.getInstance()
                    .getFrameworkFilename(AdapterNameEntries.featureVectorsFile));
            FileUtils.writeStringToFile(inputData, sb.toString());

            Problem predictionProblem = Problem.readFromFile(inputData, 1.0);

            Feature[][] testInstances = predictionProblem.x;
            for (int i = 0; i < testInstances.length; i++) {
                Feature[] instance = testInstances[i];
                Double prediction = Linear.predict(liblinearModel, instance);
                System.out.println(prediction);
            }

        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

}