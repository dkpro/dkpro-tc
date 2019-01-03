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
package org.dkpro.tc.api.io;

import java.util.Set;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

/**
 * Interface that should be implemented by readers for multi label setups.
 */
public interface TCReaderMultiLabel
{
    /**
     * Returns the set of text classification outcomes for the current multi-label instance
     * 
     * @param jcas
     *            The JCas containing the outcomes
     * @return Set of outcomes as string values
     * @throws CollectionException
     *             In case of error
     */
    Set<String> getTextClassificationOutcomes(JCas jcas) throws CollectionException;
}