/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.simple.builder;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.dkpro.lab.Lab;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.ml.base.Experiment_ImplBase;
import org.dkpro.tc.ml.builder.FeatureMode;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.experiment.builder.ExperimentBuilder;
import org.dkpro.tc.ml.experiment.builder.ExperimentType;

public class TcCrossValidationExperiment
    extends SimpleBaseExperiment
{

    private CollectionReaderDescription trainReader;
    private LearningMode lm;
    private FeatureMode fm;
    private TcShallowLearningAdapter adapter;
    private TcFeatureSet featureSet;
    private AnalysisEngineDescription preprocessing;
    private int numberFolds;

    public TcCrossValidationExperiment(int numberFolds, CollectionReaderDescription trainReader,
            LearningMode lm, FeatureMode fm,
            TcShallowLearningAdapter adapter, TcFeatureSet featureSet,
            AnalysisEngineDescription preprocessing)
        throws Exception
    {
        this.numberFolds = numberFolds;
        this.trainReader = trainReader;
        this.lm = lm;
        this.fm = fm;
        this.adapter = adapter;
        this.featureSet = featureSet;
        if (preprocessing == null) {
            this.preprocessing = AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
        }else {
            this.preprocessing = preprocessing;
        }
    }

    public void run() throws Exception
    {
        ExperimentBuilder builder = new ExperimentBuilder();
        Experiment_ImplBase experiment = builder.experiment(ExperimentType.CROSS_VALIDATION, "crossValidationExperiment")
                .numFolds(numberFolds)
                .dataReaderTrain(trainReader)
                .featureSets(featureSet)
                .learningMode(lm)
                .featureMode(fm)
                .machineLearningBackend(getDefault(adapter, lm))
                .preprocessing(preprocessing).build();
        Lab.getInstance().run(experiment);
    }

}
