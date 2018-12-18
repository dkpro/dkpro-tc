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

package org.dkpro.tc.ml.experiment.dim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.DynamicDimension;
import org.dkpro.tc.core.Constants;

public class LearningCurveDimBundleFixedTestSet extends DimensionBundle<Collection<String>>
		implements DynamicDimension {
	private Dimension<String> foldedDimension;
	private List<String>[] buckets;
	private int folds;
	private List<TrainSet> runs = new ArrayList<>();
	private int numFoldsIdx = 0;
	private int aStageLimit;

	public LearningCurveDimBundleFixedTestSet(String aName, Dimension<String> aFoldedDimension, int aFolds,
			int aLimit) {
		super(aName, new Object[0]);
		this.foldedDimension = aFoldedDimension;
		this.folds = aFolds;
		this.aStageLimit = aLimit;
	}

	@SuppressWarnings("unchecked")
	private void init() {
		buckets = new List[folds];
		for (int bucket = 0; bucket < buckets.length; bucket++) {
			buckets[bucket] = new ArrayList<String>();
		}

		// Capture all data from the dimension into buckets, one per fold
		foldedDimension.rewind();

		int i = 0;
		while (foldedDimension.hasNext()) {
			int bucket = i % folds;

			if (buckets[bucket] == null) {
				buckets[bucket] = new ArrayList<String>();
			}

			buckets[bucket].add(foldedDimension.next());
			i++;
		}

		if (i < folds) {
			throw new IllegalStateException("Requested [" + folds + "] folds, but only got [" + i
					+ "] values. There must be at least as many values as folds.");
		}

		String foldsAndSizes = "";
		for (int bucket = 0; bucket < buckets.length; bucket++) {
			foldsAndSizes = foldsAndSizes + " fold " + bucket + ": size " + buckets[bucket].size() + ".  ";
			if (buckets[bucket].size() == 0) {
				throw new IllegalStateException("Detected an empty fold: " + bucket + ". "
						+ "Maybe your fold control is causing all of your instances to be put in very few buckets?  "
						+ "Previous folds and buckets: " + foldsAndSizes);
			}
		}

		createLearningCurveRuns();
	}

	private void createLearningCurveRuns() {
		runs = new ArrayList<>();
		int maxCount = 1; //
		for (int j = 0; j < buckets.length; j++) {
			List<List<Integer>> s = getLearningCurveStage(maxCount);

			if (aStageLimit != -1 && s.size() > aStageLimit) {
				LogFactory.getLog(getClass())
						.debug("Reducing number of runs per stage from [" + s.size() + "] to [" + aStageLimit + "]");
				s = s.subList(0, aStageLimit);
			}

			if (maxCount > buckets.length) {
				// in this case, the train-set variations will always be the same - so one of
				// the generated sets is sufficient
				s = s.subList(0, 1);
			}

			for (List<Integer> train : s) {
				TrainSet trainSet = new TrainSet(train);
				LogFactory.getLog(getClass()).debug(trainSet.toString());
				runs.add(trainSet);
			}
			maxCount++;
		}
		LogFactory.getLog(getClass()).info("Created [" + runs.size() + "] runs for the learning curve experiment");
	}

	/**
	 * Creates a single learning curve run. A run consists of N buckets in the
	 * training set a single fold in the test set.
	 * 
	 * @param maxCount                   the maximal number of training buckets to
	 *                                   be used in the training set
	 * @param keptOutValidationBucketIdx the index of the validation bucket that is
	 *                                   not included into the training set
	 * @return
	 */
	private List<List<Integer>> getLearningCurveStage(int maxCount) {
		List<List<Integer>> trainData = new ArrayList<>();

		List<Integer> seq = new ArrayList<>();
		for (int i = 0; i < buckets.length; i++) {
			seq.add(i);
		}

		List<Integer> it = new ArrayList<>(seq);
		for (int i = 0; i < buckets.length; i++) {
			List<Integer> subList = null;
			if (i + maxCount < buckets.length) {
				subList = it.subList(i, i + maxCount);
			} else {
				subList = it.subList(i, buckets.length);
				List<Integer> end = it.subList(0, maxCount - subList.size());
				subList.addAll(end);
			}
			trainData.add(new ArrayList<>(subList));

			if (maxCount == buckets.length) {
				// if we reached the length of maximum N = bucket size we need only one entry
				// otherwise we create N times the same entry.
				break;
			}
		}

		return trainData;
	}

	@Override
	public boolean hasNext() {
		return numFoldsIdx + 1 < runs.size();
	}

	@Override
	public void rewind() {
		init();
		numFoldsIdx = -1;
	}

	@Override
	public Map<String, Collection<String>> next() {
		numFoldsIdx++;
		return current();
	}

	@Override
	public Map<String, Collection<String>> current() {
		List<String> trainingData = new ArrayList<String>();
		List<String> usedBucketSet = new ArrayList<>();
		TrainSet learningCurveRun = runs.get(numFoldsIdx);

		for (Integer idx : learningCurveRun.train) {
			trainingData.addAll(buckets[idx]);
			usedBucketSet.add("bucket_" + idx);
		}

		Map<String, Collection<String>> data = new HashMap<String, Collection<String>>();
		data.put(getName() + "_training", trainingData);
		data.put(Constants.DIM_NUM_TRAINING_FOLDS, usedBucketSet);

		return data;
	}

	@Override
	public void setConfiguration(Map<String, Object> aConfig) {
		if (foldedDimension instanceof DynamicDimension) {
			((DynamicDimension) foldedDimension).setConfiguration(aConfig);
		}
	}
}
