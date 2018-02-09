/**
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
 */

package org.dkpro.tc.ml.svmhmm.writer;

import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatWriter;

public class SvmHmmDataWriter extends LibsvmDataFormatWriter {
	
	int currSeqId=0;
	int lastId=-1;

	@Override
	protected String injectSequenceId(Instance instance) {
		/*
		 * The sequence id must continuously increase, TC's id is Cas-relative
		 * and restarts for a new Cas at zero again
		 */
		if (lastId < 0) {
			lastId = instance.getJcasId();
		}
		
		if(lastId > -1 && lastId != instance.getJcasId()){
			currSeqId++;
		}
		
		return "qid:" + currSeqId + "\t";
	}
	
	@Override
	protected Integer getStartIndexForOutcomeMap() {
		//SvmHmm extension, which starts counting at 1
		return 1;
	}
}
