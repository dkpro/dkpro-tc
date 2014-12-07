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
package de.tudarmstadt.ukp.dkpro.tc.core.io;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;

public interface PairReader
{

    /**
     * @return ID of the collection the first document was sampled from
     * @throws TextClassificationException
     */
    public String getCollectionId1() throws TextClassificationException;
    /**
     * @return ID of the collection the second document was sampled from
     * @throws TextClassificationException
     */
    public String getCollectionId2() throws TextClassificationException;  

    /**
     * @return ID of the first document
     * @throws TextClassificationException
     */
    public String getDocumentId1() throws TextClassificationException;
    /**
     * @return ID of the second document
     * @throws TextClassificationException
     */
    public String getDocumentId2() throws TextClassificationException;

    /**
     * @return Title of the first document
     * @throws TextClassificationException
     */
    public String getTitle1() throws TextClassificationException;
    /**
     * @return Title of the second document
     * @throws TextClassificationException
     */
    public String getTitle2() throws TextClassificationException;

    /**
     * @return Language of the first document
     * @throws TextClassificationException
     */
    public String getLanguage1() throws TextClassificationException;
    /**
     * @return Language of the second document
     * @throws TextClassificationException
     */
    public String getLanguage2() throws TextClassificationException;

    /**
     * @return The text of the first document in the pair
     * @throws TextClassificationException
     */
    public String getText1() throws TextClassificationException;
    /**
     * @return The text of the second document in the pair
     * @throws TextClassificationException
     */
    public String getText2() throws TextClassificationException;

}