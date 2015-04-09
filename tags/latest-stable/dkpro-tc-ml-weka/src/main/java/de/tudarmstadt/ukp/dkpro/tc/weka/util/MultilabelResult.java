/**
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.weka.util;

import java.io.Serializable;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;

public class MultilabelResult implements Serializable {
	
	private static final long serialVersionUID = -5066149060024459615L;
	
	/**
	 * Wrapper for classification result from multi-label classification.
	 * 
	 * @param goldstandard the gold standard as integer matrix; labels x instances
	 * @param predictions the predictions as double matrix; labels x instances
	 * @param bipartitionThreshold a threshold to create bipartition from ranking
	 * @throws TextClassificationException
	 */
	public MultilabelResult(int[][] goldstandard, double[][] predictions, String bipartitionThreshold) throws TextClassificationException{
		this.actuals = goldstandard;
		this.predictions = predictions;
		try {
			this.bipartitionThreshold = Double.parseDouble(bipartitionThreshold);
		} catch (NumberFormatException e) {
			// TODO
			throw new TextClassificationException("Currenty, only one global bipartition threshold value is supported. Please set a double as threshold.");
		}
	}
	
	/**
	 * Returns the predictions
	 * @return predictions as LxN matrix
	 */
	public double[][] getPredictions() {
		return predictions;
	}

	/**
	 * Returns the gold standard
	 * @return gold standard as LxN matrix
	 */
	public int[][] getGoldstandard() {
		return actuals;
	}

	/**
	 * Returns the bipartition threshold. Only one global value is supported.
	 * @return bipartition threshold
	 */
	public double getBipartitionThreshold() {
		return bipartitionThreshold;
	}
	
	/**
	 * predictions 
	 */
	double[][] predictions;
	/**
	 * gold standard
	 */
	int[][] actuals;
	/**
	 * bipartition threshold
	 */
	double bipartitionThreshold;
	/**
	 * instanceIds
	 */
	String[] instanceIds;
	public String[] getInstanceIds() {
		return instanceIds;
	}

	/**
	 * Calculates a bipartition from the predictions (which are usually rankings)
	 * 
	 * @return a bipartition from the predictions as LxN matrix, using the specified threshold
	 */
	public int[][] getPredictionsBipartition() {
		int[][] bipartitionMatrix = new int[predictions.length][predictions[0].length];
		for (int i = 0; i < predictions.length; i++) {
			for(int j = 0; j < predictions[i].length; j++) {
				bipartitionMatrix[i][j] = predictions[i][j] >= bipartitionThreshold ? 1 : 0;
			}
		}
		return bipartitionMatrix;
	}
	
}
