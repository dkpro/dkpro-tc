package org.cleartk.classifier.weka;

import weka.classifiers.misc.SerializedClassifier;
import weka.core.Instances;

public class TrainableSerializedClassifier extends SerializedClassifier {

	private static final long serialVersionUID = 5134642087575856484L;

	@Override
	public void buildClassifier(Instances data) throws Exception {
		super.buildClassifier(data);
		this.m_Model.buildClassifier(data);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

}
