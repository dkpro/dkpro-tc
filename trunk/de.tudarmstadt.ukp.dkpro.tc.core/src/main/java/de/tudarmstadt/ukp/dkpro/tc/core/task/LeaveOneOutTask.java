package de.tudarmstadt.ukp.dkpro.tc.core.task;

import weka.core.Instances;

public class LeaveOneOutTask extends CrossValidationTask {
	
	@Override
	protected int getFolds(Instances data) {
		return data.numInstances();
	}
}
