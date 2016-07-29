/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.api.features;

import java.util.ArrayList;

import org.dkpro.lab.task.Discriminable;

public class TcFeatureList extends ArrayList<TcFeature> implements Discriminable
{

    /**
     * 
     */
    private static final long serialVersionUID = 2065704241626247807L;

    public TcFeatureList(TcFeature... features)
    {
        for(TcFeature f : features){
            add(f);
        }
    }

    @Override
    public Object getDiscriminatorValue()
    {
        StringBuilder sb = new StringBuilder();
        
        int size = this.size();
        for(int i=0; i < size; i++){
            TcFeature tcFeature = get(i);
            sb.append(tcFeature.getDiscriminatorValue());
            if(i+1 < size()){
                sb.append(", ");
            }
        }
        
        return sb.toString();
    }

    @Override
    public Object getActualValue()
    {
        // TODO Auto-generated method stub
        return null;
    }

}