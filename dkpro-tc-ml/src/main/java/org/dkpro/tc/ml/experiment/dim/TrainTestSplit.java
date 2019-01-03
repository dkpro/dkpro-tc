/*******************************************************************************
 * Copyright 2019
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

import java.util.Collections;
import java.util.List;

class TrainTestSplit{
    Integer test;
    List<Integer> train;

    TrainTestSplit(List<Integer> train, Integer test){
        this.train = train;
        Collections.sort(this.train);
        this.test = test;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Train: (");
        for(int i=0; i < train.size(); i++) {
            sb.append(train.get(i));
            if (i+1 < train.size()) {
                sb.append(" ");
            }
        }
        sb.append("), Test: ("+test+")]");
        return sb.toString();
    }

}
