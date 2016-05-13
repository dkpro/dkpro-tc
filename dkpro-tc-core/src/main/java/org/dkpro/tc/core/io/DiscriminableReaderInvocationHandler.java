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
package org.dkpro.tc.core.io;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.uima.collection.CollectionReaderDescription;
import org.dkpro.lab.task.Discriminable;

public class DiscriminableReaderInvocationHandler implements InvocationHandler, Discriminable, DiscriminableReaderDescription
{
    
    private CollectionReaderDescription crd;


    public DiscriminableReaderInvocationHandler(Object crd){
        this.crd = (CollectionReaderDescription)crd;
    }
    

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
        
        if(method.getName().equals("getDiscriminatorValue")){
            return getDiscriminatorValue();
        }
        if(method.getName().equals("getActualValue")){
            return getActualValue();
        }
        if(method.getName().equals("getReaderDescription")){
            return getReaderDescription();
        }
        
        return method.invoke(crd, args);
    }


    @Override
    public Object getDiscriminatorValue()
    {
        String string = crd.getCollectionReaderMetaData().toString();
        return DigestUtils.md5Hex(string);
    }


    @Override
    public Object getActualValue()
    {
        return getDiscriminatorValue();
    }


    @Override
    public CollectionReaderDescription getReaderDescription()
    {
        return crd;
    }

}
