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
package de.tudarmstadt.ukp.dkpro.tc.ml.liblinear;

import java.util.Collection;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.FoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;

public class LiblinearAdapter 
	implements TCMachineLearningAdapter
{

	public static TCMachineLearningAdapter getInstance() {
		return new LiblinearAdapter();
	}
	
	public static String getOutcomeMappingFilename() {
		return "outcome-mapping.txt";
	}
	
	@Override
	public ExecutableTaskBase getTestTask() {
		return new LiblinearTestTask();
	}

	@Override
	public Class<? extends ReportBase> getClassificationReportClass() {
		return LiblinearClassificationReport.class;
	}

	@Override
	public Class<? extends ReportBase> getOutcomeIdReportClass() {
		// FIXME return real report
		return LiblinearClassificationReport.class;
	}

	@Override
	public Class<? extends ReportBase> getBatchTrainTestReportClass() {
		return LiblinearBatchTrainTestReport.class;
	}

	@Override
	public DimensionBundle<Collection<String>> getFoldDimensionBundle(
			String[] files, int folds) {
		return  new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
	}
	
	@Override
	public String getFrameworkFilename(AdapterNameEntries name) {

        switch (name) {
            case featureVectorsFile:  return "training-data.txt";
            case predictionsFile      :  return "predictions.txt";
            case evaluationFile       :  return "evaluation.txt";
            case featureSelectionFile :  return "attributeEvaluationResults.txt";
        }
        
        return null;
	}

	@Override
	public Class<? extends DataWriter> getDataWriterClass(String learningMode) {
		return LiblinearDataWriter.class;
	}
	
	@Override
	public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass() {
		// FIXME to be implemented
		throw new UnsupportedOperationException();
	}
}
