package de.tudarmstadt.ukp.dkpro.tc.mallet.util;

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
public class MalletFoldDimensionBundle<T> extends DimensionBundle<Collection<T>> implements DynamicDimension
{
	private Dimension<T> foldedDimension;
	private List<T>[] buckets;
	private int validationBucket = -1;
	private int folds;
	
	public MalletFoldDimensionBundle(String aName, Dimension<T> aFoldedDimension, int aFolds)
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
		
		T remainingFile = null;
		while (foldedDimension.hasNext()) {
			int bucket = i % folds;
			
			if (buckets[bucket] == null) {
				buckets[bucket] = new ArrayList<T>();
			}
			
			if (remainingFile != null) {
				buckets[bucket].add(remainingFile);
			}
			
			T firstFile = foldedDimension.next();
			String firstEssayName = getEssayName(firstFile);
			buckets[bucket].add(firstFile);
			
			// Ensure that all instances belonging to same essay are put in the same bucket
			while (foldedDimension.hasNext()) {
				T currentFile = foldedDimension.next();
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

	private String getEssayName(T file) {
		String fileString = file.toString();
		String simpleFileName = fileString.substring(fileString.lastIndexOf('/') + 1, 
				fileString.length());
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
	public Map<String, Collection<T>> next()
	{
		validationBucket++;
		return current();
	}

	@Override
	public Map<String, Collection<T>> current()
	{
		List<T> trainingData = new ArrayList<T>();
		for (int i = 0; i < buckets.length; i++) {
			if (i != validationBucket) {
				trainingData.addAll(buckets[i]);
			}
		}
		
		Map<String, Collection<T>> data = new HashMap<String, Collection<T>>();
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
