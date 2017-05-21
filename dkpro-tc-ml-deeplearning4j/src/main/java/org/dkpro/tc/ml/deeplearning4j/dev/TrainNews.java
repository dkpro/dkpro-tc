/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.ml.deeplearning4j.dev;

import java.io.File;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class TrainNews {
    public static String userDirectory = "";
    public static String DATA_PATH = "";
    public static String WORD_VECTORS_PATH = "";
    public static WordVectors wordVectors;
    private static TokenizerFactory tokenizerFactory;

    public static void main(String[] args) throws Exception {
//        DATA_PATH = "/Users/toobee/Documents/Eclipse/dl4j-examples/dl4j-examples/src/main/resources/NewsData/LabelledNews/";
//        DATA_PATH= "/Users/toobee/Desktop/org.dkpro.lab/repository/VectorizationTask-Train-DeepLearning-20170521182025528/output";

        int batchSize = 2;     //Number of examples in each minibatch
        int nEpochs = 50;        //Number of epochs (full passes of training data) to train on

        //DataSetIterators for training and testing respectively
        //Using AsyncDataSetIterator to do data loading in a separate thread; this may improve performance vs. waiting for data to load
        wordVectors = WordVectorSerializer.loadTxtVectors(new File("/Users/toobee/Desktop/org.dkpro.lab/repository/EmbeddingTask-DeepLearning-20170521182024802/output/prunedEmbedding.txt"));

        NewsIterator iTrain = new NewsIterator.Builder()
            .dataDirectory("/Users/toobee/Desktop/org.dkpro.lab/repository/VectorizationTask-Train-DeepLearning-20170521182025528/output")
            .wordVectors(wordVectors)
            .batchSize(batchSize)
            .build();

        NewsIterator iTest = new NewsIterator.Builder()
            .dataDirectory("/Users/toobee/Desktop/org.dkpro.lab/repository/VectorizationTask-Test-DeepLearning-20170521182026162/output")
            .wordVectors(wordVectors)
            .batchSize(batchSize)
            .build();

        //DataSetIterator train = new AsyncDataSetIterator(iTrain,1);
        //DataSetIterator test = new AsyncDataSetIterator(iTest,1);

        int inputNeurons = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length; // 100 in our case
        int outputs = iTrain.getLabels().size();

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        //Set up network configuration
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
            .updater(Updater.RMSPROP)
            .regularization(true).l2(1e-5)
            .weightInit(WeightInit.XAVIER)
            .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
            .learningRate(0.0018)
            .list()
            .layer(0, new GravesLSTM.Builder().nIn(inputNeurons).nOut(200)
                .activation("softsign").build())
            .layer(1, new RnnOutputLayer.Builder().activation("softmax")
                .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(200).nOut(outputs).build())
            .pretrain(false).backprop(true).build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));

        System.out.println("Starting training");
        for (int i = 0; i < nEpochs; i++) {
            net.fit(iTrain);
            iTrain.reset();
            System.out.println("Epoch " + i + " complete. Starting evaluation:");

            //Run evaluation. This is on 25k reviews, so can take some time
            Evaluation evaluation = new Evaluation();
            while (iTest.hasNext()) {
                DataSet t = iTest.next();
                INDArray features = t.getFeatureMatrix();
                INDArray lables = t.getLabels();
                //System.out.println("labels : " + lables);
                INDArray inMask = t.getFeaturesMaskArray();
                INDArray outMask = t.getLabelsMaskArray();
                INDArray predicted = net.output(features, false);

                //System.out.println("predicted : " + predicted);
                evaluation.evalTimeSeries(lables, predicted, outMask);
            }
            iTest.reset();

            System.out.println(evaluation.stats());
        }

        ModelSerializer.writeModel(net, userDirectory + "NewsModel.net", true);
        System.out.println("----- Example complete -----");
    }

}
