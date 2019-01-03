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

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Interface that should be implemented by readers for sequence labeling setups.
 */
public interface TCReaderSequence
{
    /**
     * Returns the text classification outcome for each classification unit
     * 
     * @param jcas
     *            The JCas containing the outcomes
     * @param aTarget
     *            The target span in which the outcome is located
     * @return Set of outcomes as string values
     * @throws CollectionException
     *             In case of error
     */
    String getTextClassificationOutcome(JCas jcas, TextClassificationTarget aTarget)
        throws CollectionException;
}