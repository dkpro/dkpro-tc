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

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.task.Discriminable;

public class DiscriminableReaderCollectionFactory 
{
    public static Object newInstance(Object... ob) throws ResourceInitializationException {
        
        List<Object> l = new ArrayList<Object>(Arrays.asList(ob));
        
        @SuppressWarnings("unchecked")
        Class<? extends CollectionReader> cdr = (Class<? extends CollectionReader>) l.get(0);
        l.remove(0);
        
        CollectionReaderDescription readerDescription = CollectionReaderFactory.createReaderDescription(cdr, l.toArray());
        
        return Proxy.newProxyInstance(readerDescription.getClass().getClassLoader(),
                new Class<?>[] { CollectionReaderDescription.class, Discriminable.class, DiscriminableReaderDescription.class }, new DiscriminableReaderInvocationHandler(readerDescription));
    }
}
