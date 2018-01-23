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
package org.dkpro.tc.examples.deeplearning.dl4j.doc;

import static org.nd4j.linalg.indexing.NDArrayIndex.all;
import static org.nd4j.linalg.indexing.NDArrayIndex.point;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.dkpro.tc.core.DeepLearningConstants;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;

//Slightly modified version taken from the official deeplearning4j-example project
@SuppressWarnings("serial")
public class NewsIterator implements DataSetIterator {
	private final WordVectors wordVectors;
	private final int batchSize;
	private final int vectorSize;
	private int maxLength;
	private final String dataDirectory;
	private final List<Pair<String, List<String>>> categoryData = new ArrayList<>();
	private int cursor = 0;
	private int totalNews = 0;
	private int newsPosition = 0;
	private final List<String> labels;
	private int currCategory = 0;

	/**
	 * @param dataDirectory
	 *            the directory of the news headlines data set
	 * @param wordVectors
	 *            WordVectors object
	 * @param batchSize
	 *            Size of each minibatch for training
	 * @param truncateLength
	 *            If headline length exceed this size, it will be truncated to
	 *            this size.
	 * @param train
	 *            If true: return the training data. If false: return the
	 *            testing data.
	 *            <p>
	 *            - initialize various class variables - calls populateData
	 *            function to load news data in categoryData vector - also
	 *            populates labels (i.e. category related inforamtion) in labels
	 *            class variable
	 * @throws Exception 
	 */
	private NewsIterator(String dataDirectory, WordVectors wordVectors, int batchSize) throws Exception {
		this.dataDirectory = dataDirectory;
		this.batchSize = batchSize;
		this.vectorSize = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;
		this.wordVectors = wordVectors;
		this.populateData();
		this.labels = new ArrayList<>();
		for (int i = 0; i < this.categoryData.size(); i++) {
			this.labels.add(this.categoryData.get(i).getKey());
		}
	}

	public static Builder Builder() {
		return new Builder();
	}

	@Override
	public DataSet next(int num) {
		if (cursor >= this.totalNews)
			throw new NoSuchElementException();
		try {
			return nextDataSet(num);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private DataSet nextDataSet(int num) throws IOException {
		 List<String> news = new ArrayList<>(num);
	        int[] category = new int[num];

	        for (int i = 0; i < num && cursor < totalExamples(); i++) {
	            if (currCategory < categoryData.size()) {
	                news.add(this.categoryData.get(currCategory).getValue().get(newsPosition));
	                category[i] = Integer.parseInt(this.categoryData.get(currCategory).getKey());
	                currCategory++;
	                cursor++;
	            } else {
	                currCategory = 0;
	                newsPosition++;
	                i--;
	            }
	        }

	        //Second: tokenize news and filter out unknown words
	        List<List<String>> allTokens = new ArrayList<>(news.size());
	        maxLength = 0;
	        for (String s : news) {
	            List<String> asList = Arrays.asList(s.replaceAll(" 0", "").split(" "));
	            maxLength = maxLength < asList.size() ? asList.size() : maxLength;
	            allTokens.add(asList);
	        }

	        //If longest news exceeds 'truncateLength': only take the first 'truncateLength' words
	        //System.out.println("maxLength : " + maxLength);

	        //Create data for training
	        //Here: we have news.size() examples of varying lengths
	        INDArray features = Nd4j.create(news.size(), vectorSize, maxLength);
	        INDArray labels = Nd4j.create(news.size(), this.categoryData.size(), maxLength);    //Three labels: Crime, Politics, Bollywood

	        //Because we are dealing with news of different lengths and only one output at the final time step: use padding arrays
	        //Mask arrays contain 1 if data is present at that time step for that example, or 0 if data is just padding
	        INDArray featuresMask = Nd4j.zeros(news.size(), maxLength);
	        INDArray labelsMask = Nd4j.zeros(news.size(), maxLength);

	        int[] temp = new int[2];
	        for (int i = 0; i < news.size(); i++) {
	            List<String> tokens = allTokens.get(i);
	            temp[0] = i;
	            //Get word vectors for each word in news, and put them in the training data
	            for (int j = 0; j < tokens.size() && j < maxLength; j++) {
	                String token = tokens.get(j);
	                INDArray vector = wordVectors.getWordVectorMatrix(token);
	                features.put(new INDArrayIndex[]{point(i),
	                    all(),
	                    point(j)}, vector);

	                temp[1] = j;
	                featuresMask.putScalar(temp, 1.0);
	            }
	            int idx = category[i]-1; //Index starts at 1
	            int lastIdx = Math.min(tokens.size(), maxLength);
	            labels.putScalar(new int[]{i, idx, lastIdx - 1}, 1.0);
	            labelsMask.putScalar(new int[]{i, lastIdx - 1}, 1.0);
	        }

	        DataSet ds = new DataSet(features, labels, featuresMask, labelsMask);
	        return ds;
	}

	/**
	 * Used post training to load a review from a file to a features INDArray
	 * that can be passed to the network output method
	 *
	 * @param file
	 *            File to load the review from
	 * @param maxLength
	 *            Maximum length (if review is longer than this: truncate to
	 *            maxLength). Use Integer.MAX_VALUE to not nruncate
	 * @return Features array
	 * @throws IOException
	 *             If file cannot be read
	 */
	public INDArray loadFeaturesFromFile(File file, int maxLength) throws IOException {
		@SuppressWarnings("deprecation")
		String news = FileUtils.readFileToString(file);
		return loadFeaturesFromString(news, maxLength);
	}

	/**
	 * Used post training to convert a String to a features INDArray that can be
	 * passed to the network output method
	 *
	 * @param reviewContents
	 *            Contents of the review to vectorize
	 * @param maxLength
	 *            Maximum length (if review is longer than this: truncate to
	 *            maxLength). Use Integer.MAX_VALUE to not nruncate
	 * @return Features array for the given input String
	 */
	public INDArray loadFeaturesFromString(String reviewContents, int maxLength) {
		// List<String> tokens =
		// tokenizerFactory.create(reviewContents).getTokens();
		List<String> tokens = new ArrayList<>();
		List<String> tokensFiltered = new ArrayList<>();
		for (String t : tokens) {
			if (wordVectors.hasWord(t))
				tokensFiltered.add(t);
		}
		int outputLength = Math.max(maxLength, tokensFiltered.size());

		INDArray features = Nd4j.create(1, vectorSize, outputLength);

		for (int j = 0; j < tokens.size() && j < maxLength; j++) {
			String token = tokens.get(j);
			INDArray vector = wordVectors.getWordVectorMatrix(token);
			features.put(new INDArrayIndex[] { point(0), all(), point(j) }, vector);
		}

		return features;
	}

	private void populateData() throws Exception {

		//FIXME: Implement me a bit more efficiently :)
		List<String> vectors = FileUtils.readLines(new File(this.dataDirectory, DeepLearningConstants.FILENAME_INSTANCE_VECTOR), "utf-8");
		String outcomes = FileUtils.readLines(new File(this.dataDirectory, DeepLearningConstants.FILENAME_OUTCOME_VECTOR), "utf-8").get(0);
		
		//FIXME: We assume that our outcomes have only 1 digit ....
		outcomes = outcomes.replaceAll(" ", "");
		
		Map<String,List<String>> m = new HashMap<>();
		
		for(int i=0; i < vectors.size(); i++){
			String v = vectors.get(i);
			v = v.substring(1, v.length()-1);
			String o = outcomes.charAt(i)+"";
			
			List<String> list = m.get(o);
			if(list == null){
				list = new ArrayList<>();
			}
			list.add(v);
			m.put(o, list);
			
			this.totalNews++;
		}
		
		for(String k : m.keySet()){
			Pair<String, List<String>> tempPair = Pair.of(k, m.get(k));
			this.categoryData.add(tempPair);
		}

	}

	@Override
	public int totalExamples() {
		return this.totalNews;
	}

	@Override
	public int inputColumns() {
		return vectorSize;
	}

	@Override
	public int totalOutcomes() {
		return this.categoryData.size();
	}

	@Override
	public void reset() {
		cursor = 0;
		newsPosition = 0;
		currCategory = 0;
	}

	public boolean resetSupported() {
		return true;
	}

	@Override
	public boolean asyncSupported() {
		return true;
	}

	@Override
	public int batch() {
		return batchSize;
	}

	@Override
	public int cursor() {
		return cursor;
	}

	@Override
	public int numExamples() {
		return totalExamples();
	}

	@Override
	public void setPreProcessor(DataSetPreProcessor preProcessor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getLabels() {
		return this.labels;
	}

	@Override
	public boolean hasNext() {
		return cursor < numExamples();
	}

	@Override
	public DataSet next() {
		return next(batchSize);
	}

	@Override
	public void remove() {

	}

	@Override
	public DataSetPreProcessor getPreProcessor() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public int getMaxLength() {
		return this.maxLength;
	}

	public static class Builder {
		private String dataDirectory;
		private WordVectors wordVectors;
		private int batchSize;

		Builder() {
		}

		public NewsIterator.Builder dataDirectory(String dataDirectory) {
			this.dataDirectory = dataDirectory;
			return this;
		}

		public NewsIterator.Builder wordVectors(WordVectors wordVectors) {
			this.wordVectors = wordVectors;
			return this;
		}

		public NewsIterator.Builder batchSize(int batchSize) {
			this.batchSize = batchSize;
			return this;
		}

		public NewsIterator.Builder tokenizerFactory(TokenizerFactory tokenizerFactory) {
			return this;
		}

		public NewsIterator build() throws Exception {
			return new NewsIterator(dataDirectory, wordVectors, batchSize);
		}

		public String toString() {
			return "org.deeplearning4j.examples.recurrent.ProcessNews.NewsIterator.Builder(dataDirectory="
					+ this.dataDirectory + ", wordVectors=" + this.wordVectors + ", batchSize=" + this.batchSize + ")";
		}
	}
}
