/**
 * Copyright 2019
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

package org.dkpro.tc.examples.deeplearning.dl4j.sequence;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.eval.EvaluationUtils;
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
import org.dkpro.tc.examples.deeplearning.dl4j.sequence.BinaryWordVectorSerializer.BinaryVectorizer;
import org.dkpro.tc.ml.deeplearning4j.user.TcDeepLearning4jUser;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.primitives.Pair;

public class Dl4jSeq2SeqUserCode
    implements TcDeepLearning4jUser
{

    Vectorize vectorize = new Vectorize();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void run(File trainVec, File trainOutcome, File testVec, File testOutcome,
            File embedding, int seed, int maximumLength, double threshold, File prediction)
        throws Exception
    {

        vectorize = new Vectorize(getOutcomes(trainOutcome, testOutcome));

        int featuresSize = getEmbeddingsSize(embedding);
        int maxTagsetSize = getNumberOfOutcomes(trainOutcome, testOutcome);
        int batchSize = 1;
        int epochs = 2;
        int iterations = 1;
        double learningRate = 0.1;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(iterations).seed(12345l).updater(Updater.SGD).regularization(true)
                .l2(1e-5).weightInit(WeightInit.RELU)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .gradientNormalizationThreshold(1.0).learningRate(learningRate).list()
                .layer(0,
                        new GravesLSTM.Builder().activation(Activation.TANH).nIn(featuresSize)
                                .nOut(200).build())
                .layer(1,
                        new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
                                .lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                .nIn(200).nOut(maxTagsetSize).build())
                .pretrain(false).backprop(true).build();

        int maxLen = getLongestSentence(trainVec, testVec);

        List<DataSet> trainDataSet = new ArrayList<DataSet>(
                toDataSet(trainVec, trainOutcome, maxLen, maxTagsetSize, embedding));
        MultiLayerNetwork mln = new MultiLayerNetwork(conf);
        mln.init();
        mln.setListeners(new ScoreIterationListener(1));

        for (int i = 0; i < epochs; i++) {
            System.out.println("Epoche " + (i + 1));
            Collections.shuffle(trainDataSet);
            DataSetIterator train = new ListDataSetIterator(trainDataSet, batchSize);
            mln.fit(train);
        }

        List<DataSet> testDataSet = new ArrayList<DataSet>(
                toDataSet(testVec, testOutcome, maxLen, maxTagsetSize, embedding));
        DataSetIterator iTest = new ListDataSetIterator(testDataSet, batchSize);
        StringBuilder sb = new StringBuilder();
        sb.append("#Gold\tPrediction" + "\n");
        // Run evaluation. This is on 25k reviews, so can take some time
        while (iTest.hasNext()) {
            DataSet t = iTest.next();
            INDArray features = t.getFeatureMatrix();
            INDArray lables = t.getLabels();
            // System.out.println("labels : " + lables);
            INDArray outMask = t.getLabelsMaskArray();
            INDArray predicted = mln.output(features, false);
            // System.out.println("predicted : " + predicted);
            eval(lables, predicted, outMask, sb);
        }
        iTest.reset();

        FileUtils.writeStringToFile(prediction, sb.toString(), "utf-8");
    }

    private String[] getOutcomes(File trainOutcome, File testOutcome) throws IOException
    {

        List<String> trainOutcomes = FileUtils.readLines(trainOutcome, "utf-8");
        List<String> testOutcomes = FileUtils.readLines(testOutcome, "utf-8");

        Set<String> s = new HashSet<>();
        trainOutcomes.stream().forEach(x -> asList(x.split(" ")).forEach(y -> s.add(y)));
        testOutcomes.stream().forEach(x -> asList(x.split(" ")).forEach(y -> s.add(y)));

        return s.toArray(new String[0]);
    }

    private int getLongestSentence(File trainVec, File testVec) throws IOException
    {
        List<String> trainSent = FileUtils.readLines(trainVec, "utf-8");
        List<String> testSent = FileUtils.readLines(testVec, "utf-8");

        int maxTrain = trainSent.stream().mapToInt(s -> s.split(" ").length).max().getAsInt();
        int maxTest = testSent.stream().mapToInt(s -> s.split(" ").length).max().getAsInt();
        return Math.max(maxTrain, maxTest);
    }

    private void eval(INDArray labels, INDArray p, INDArray outMask, StringBuilder sb)
    {
        Pair<INDArray, INDArray> pair = EvaluationUtils.extractNonMaskedTimeSteps(labels, p,
                outMask);

        INDArray realOutcomes = pair.getFirst();
        INDArray guesses = pair.getSecond();

        // Length of real labels must be same as length of predicted labels
        if (realOutcomes.length() != guesses.length())
            throw new IllegalArgumentException(
                    "Unable to evaluate. Outcome matrices not same length");

        INDArray guessIndex = Nd4j.argMax(guesses, 1);
        INDArray realOutcomeIndex = Nd4j.argMax(realOutcomes, 1);

        int nExamples = guessIndex.length();
        for (int i = 0; i < nExamples; i++) {
            int actual = (int) realOutcomeIndex.getDouble(i);
            int predicted = (int) guessIndex.getDouble(i);
            sb.append(
                    vectorize.getTagset()[actual] + "\t" + vectorize.getTagset()[predicted] + "\n");
        }
    }

    private int getNumberOfOutcomes(File trainOutcome, File testOutcome) throws IOException
    {
        Set<String> outcomes = new HashSet<>();
        List<String> lines = FileUtils.readLines(trainOutcome, "utf-8");
        lines.forEach(x -> outcomes.addAll(asList(x.split(" "))));

        lines = FileUtils.readLines(testOutcome, "utf-8");
        lines.forEach(x -> outcomes.addAll(asList(x.split(" "))));

        return outcomes.size();
    }

    private Collection<DataSet> toDataSet(File trainVec, File trainOutcome, int maxLen,
            int numOutcomes, File embedding)
        throws IOException
    {

        List<String> sentences = FileUtils.readLines(trainVec, "utf-8");
        List<String> outcomes = FileUtils.readLines(trainOutcome, "utf-8");

        @SuppressWarnings("deprecation")
        WordVectors wordVectors = WordVectorSerializer.loadTxtVectors(embedding);
        File f = File.createTempFile("embedding", ".emb");
        BinaryWordVectorSerializer.convertWordVectorsToBinary(wordVectors, f.toPath());
        BinaryVectorizer bw = BinaryVectorizer.load(f.toPath());
        List<DataSet> data = new ArrayList<>();

        for (int i = 0; i < sentences.size(); i++) {
            // each sent/label pairing is in an own data set
            List<String> singleSent = asList(sentences.get(i));
            List<String> singleOutcome = asList(outcomes.get(i));
            data.add(vectorize.vectorize(transformToList(singleSent),
                    transformToList(singleOutcome), bw, maxLen, numOutcomes, true));
        }

        return data;
    }

    private List<List<String>> transformToList(List<String> sentences)
    {

        List<List<String>> out = new ArrayList<>();

        for (String s : sentences) {
            out.add(asList(s.split(" ")));
        }

        return out;
    }

    private int getEmbeddingsSize(File embedding) throws Exception
    {
        String readLine = null;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(embedding), StandardCharsets.UTF_8))) {
            readLine = br.readLine();
            br.close();
        }

        if (readLine != null) {
            return readLine.split(" ").length - 1;
        }
        throw new NullPointerException("Value is null");
    }
}
