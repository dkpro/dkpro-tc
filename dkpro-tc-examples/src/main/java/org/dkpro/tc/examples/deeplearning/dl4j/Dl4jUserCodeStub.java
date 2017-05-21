/**
 * Copyright 2017
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

package org.dkpro.tc.examples.deeplearning.dl4j;

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
import org.dkpro.tc.ml.deeplearning4j.user.TcDeepLearning4jUser;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class Dl4jUserCodeStub implements TcDeepLearning4jUser {

	@Override
	public void run(File trainVec, File trainOutcome, File testVec, File testOutcome, File embedding, File prediction) throws Exception {
		
		int batchSize = 2;     //Number of examples in each minibatch
        int nEpochs = 5;        //Number of epochs (full passes of training data) to train on

        //DataSetIterators for training and testing respectively
        //Using AsyncDataSetIterator to do data loading in a separate thread; this may improve performance vs. waiting for data to load
        WordVectors wordVectors = WordVectorSerializer.loadTxtVectors(embedding);

        NewsIterator iTrain = new NewsIterator.Builder()
            .dataDirectory(trainVec.getParent())
            .wordVectors(wordVectors)
            .batchSize(batchSize)
            .build();

        NewsIterator iTest = new NewsIterator.Builder()
            .dataDirectory(testVec.getParent())
            .wordVectors(wordVectors)
            .batchSize(batchSize)
            .build();

        //DataSetIterator train = new AsyncDataSetIterator(iTrain,1);
        //DataSetIterator test = new AsyncDataSetIterator(iTest,1);

        int inputNeurons = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length; // 100 in our case
        int outputs = iTrain.getLabels().size();

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
            

            System.out.println(evaluation.stats());
        }
		
	}

}
