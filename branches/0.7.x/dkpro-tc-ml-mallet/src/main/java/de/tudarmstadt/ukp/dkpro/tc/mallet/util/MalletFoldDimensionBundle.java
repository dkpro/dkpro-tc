/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.mallet.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DynamicDimension;

// FIXME Issue 128: instanceID should contain sequenceID - so we can easily make sure that sequences are completed moved into folds
/**
 * Modification to FoldDimensionBundle in order to add instances belonging to the same sequence in the same fold
 * 
 * @author perumal
 */
public class MalletFoldDimensionBundle extends DimensionBundle<Collection<String>> implements DynamicDimension
{
	private Dimension<String> foldedDimension;
	private List<String>[] buckets;
	private int validationBucket = -1;
	private int folds;
	
	public MalletFoldDimensionBundle(String aName, Dimension<String> aFoldedDimension, int aFolds)
	{
		super(aName, new Object[0] );
		foldedDimension = aFoldedDimension;
		folds = aFolds;
	}
	
	private void init()
	{
		buckets = new List[folds];
		
		// Capture all data from the dimension into buckets, one per fold
		foldedDimension.rewind();
		int i = 0;
		
		String remainingFile = null;
		while (foldedDimension.hasNext()) {
			int bucket = i % folds;
			
			if (buckets[bucket] == null) {
				buckets[bucket] = new ArrayList<String>();
			}
			
			if (remainingFile != null) {
				buckets[bucket].add(remainingFile);
			}
			
			String firstFile = foldedDimension.next();
			String firstEssayName = getEssayName(firstFile);
			buckets[bucket].add(firstFile);
			
			// Ensure that all instances belonging to same sequence are put in the same bucket
			while (foldedDimension.hasNext()) {
				String currentFile = foldedDimension.next();
				String currentEssayName = getEssayName(currentFile);
				if (!firstEssayName.equals(currentEssayName)) {
					remainingFile = currentFile;
					break;
				}
				buckets[bucket].add(currentFile);
			}
			i++;
		}
		
		if (i < folds) {
			throw new IllegalStateException("Requested [" + folds + "] folds, but only got [" + i
					+ "] values. There must be at least as many values as folds.");
		}
	}

	private String getEssayName(String file) {
		String simpleFileName = new File(file).getName();
		return simpleFileName.substring(0, simpleFileName.indexOf('_'));
	}
	
	@Override
	public boolean hasNext()
	{
		return validationBucket < buckets.length-1;
	}

	@Override
	public void rewind()
	{
		init();
		validationBucket = -1;
	}

	@Override
	public Map<String, Collection<String>> next()
	{
		validationBucket++;
		return current();
	}

	@Override
	public Map<String, Collection<String>> current()
	{
		List<String> trainingData = new ArrayList<String>();
		for (int i = 0; i < buckets.length; i++) {
			if (i != validationBucket) {
				trainingData.addAll(buckets[i]);
			}
		}
		
		Map<String, Collection<String>> data = new HashMap<String, Collection<String>>();
		data.put(getName()+"_training", trainingData);
		data.put(getName()+"_validation", buckets[validationBucket]);
		
		return data;
	}

	@Override
	public void setConfiguration(Map<String, Object> aConfig)
	{
		if (foldedDimension instanceof DynamicDimension) {
			((DynamicDimension) foldedDimension).setConfiguration(aConfig);
		}
	}
}
