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

package org.dkpro.tc.examples.deeplearning.dl4j.seq;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectorsImpl;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesBidirectionalLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.dkpro.tc.examples.deeplearning.dl4j.seq.BinaryWordVectorSerializer.BinaryVectorizer;
import org.dkpro.tc.ml.deeplearning4j.user.TcDeepLearning4jUser;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import com.google.common.io.Files;

public class Dl4jUserCodeStub implements TcDeepLearning4jUser {

	public static void main(String[] args) throws Exception {
		
		String root = "/Users/toobee/Desktop/org.dkpro.lab/repository/";
		String trainVec = root + "/VectorizationTask-Train-DynetSeq2Seq-20170527192957504/output/instanceVectors.txt";
		String trainOutc = root + "/VectorizationTask-Train-DynetSeq2Seq-20170527192957504/output/outcomeVectors.txt";
		String testVec = root + "/VectorizationTask-Test-DynetSeq2Seq-20170527192958054/output/instanceVectors.txt";
		String testOutc = root + "/VectorizationTask-Test-DynetSeq2Seq-20170527192958054/output/outcomeVectors.txt";
		String embedding = root + "/EmbeddingTask-DynetSeq2Seq-20170527192956694/output/prunedEmbedding.txt";
		String pred = "/Users/toobee/Desktop/pred.txt";
		new Dl4jUserCodeStub().run(new File(trainVec), new File(trainOutc), new File(testVec), new File(testOutc),
				new File(embedding), new File(pred));
	}

	@Override
	public void run(File trainVec, File trainOutcome, File testVec, File testOutcome, File embedding, File prediction)
			throws Exception {

		int featuresSize = getEmbeddingsSize(embedding);
		int maxTagsetSize = 70;
		int batchSize = 1;
		int epochs = 1;
		boolean shuffle = true;
		int iterations = 1;
		double learningRate = 0.1;

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(iterations).seed(12345l)
				.updater(Updater.RMSPROP).regularization(true).l2(1e-5).weightInit(WeightInit.RELU)
				.gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
				.gradientNormalizationThreshold(1.0).learningRate(learningRate).list()
				.layer(0,
						new GravesBidirectionalLSTM.Builder().activation("softsign").nIn(featuresSize).nOut(200)
								.build())
				.layer(1,
						new RnnOutputLayer.Builder().activation("softmax")
								.lossFunction(LossFunctions.LossFunction.MCXENT).nIn(200).nOut(maxTagsetSize).build())
				.pretrain(false).backprop(true).build();

		DataSetIterator train = new ListDataSetIterator(toDataSet(trainVec, trainOutcome, embedding), batchSize);
		MultiLayerNetwork mln = new MultiLayerNetwork(conf);
		mln.fit(train);
	}

	private Collection<DataSet> toDataSet(File trainVec, File trainOutcome, File embedding) throws IOException {

		List<String> sentences = FileUtils.readLines(trainVec);
		List<String> outcomes = FileUtils.readLines(trainOutcome);

		Vectorize vectorize = new Vectorize();

		int maxLen = sentences.stream().mapToInt(s -> s.split(" ").length).max().getAsInt();

		Set<String> labels = new HashSet<>();
		for (String s : outcomes) {
			labels.addAll(asList(s.substring(1, s.length() - 1).split(" ")));
		}

		WordVectors wordVectors = WordVectorSerializer.loadTxtVectors(embedding);
		File f = File.createTempFile("embedding", ".emb");
		BinaryWordVectorSerializer.convertWordVectorsToBinary(wordVectors, f.toPath());
		BinaryVectorizer bw = BinaryVectorizer.load(f.toPath());
		List<DataSet> data = new ArrayList<>();
		data.add(vectorize.vectorize(transformToList(sentences), transformToList(outcomes), bw, maxLen, labels.size(),
				true));

		return data;
	}

	private List<List<String>> transformToList(List<String> sentences) {

		List<List<String>> out = new ArrayList<>();
		
		for(String s : sentences){
			out.add(asList(s.substring(1, s.length()-1).split(" ")));
		}
		
		return out;
	}

	private int getEmbeddingsSize(File embedding) throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(embedding)));
		String readLine = br.readLine();
		br.close();
		return readLine.split(" ").length - 1;
	}
}
