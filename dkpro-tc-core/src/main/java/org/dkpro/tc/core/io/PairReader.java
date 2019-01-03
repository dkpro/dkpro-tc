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
package org.dkpro.tc.core.io;

import org.dkpro.tc.api.exception.TextClassificationException;

public interface PairReader
{
    /**
     * ID of the collection the first document was sampled from
     * 
     * @throws org.dkpro.tc.api.exception.TextClassificationException
     *             excpetion in case of error
     * @return id as string
     */
    String getCollectionId1() throws TextClassificationException;

    /**
     * ID of the collection the second document was sampled from
     * 
     * @throws org.dkpro.tc.api.exception.TextClassificationException
     *             excpetion in case of error
     * @return id as string
     */
    String getCollectionId2() throws TextClassificationException;

    /**
     * ID of the first document
     * 
     * @throws org.dkpro.tc.api.exception.TextClassificationException
     *             excpetion in case of error
     * @return id as string
     */
    String getDocumentId1() throws TextClassificationException;

    /**
     * ID of the second document
     * 
     * @throws org.dkpro.tc.api.exception.TextClassificationException
     *             excpetion in case of error
     * @return id as string
     */
    String getDocumentId2() throws TextClassificationException;

    /**
     * Title of the first document
     * 
     * @throws TextClassificationException
     *             excpetion in case of error
     * @return string value
     * 
     */
    String getTitle1() throws TextClassificationException;

    /**
     * Title of the second document
     * 
     * @return title as string
     * @throws TextClassificationException
     *             excpetion in case of error
     */
    String getTitle2() throws TextClassificationException;

    /**
     * Language of the first document
     * 
     * @return title as string
     * @throws org.dkpro.tc.api.exception.TextClassificationException
     *             excpetion in case of error
     */
    String getLanguage1() throws TextClassificationException;

    /**
     * Language of the second document
     * 
     * @return title as string
     * @throws org.dkpro.tc.api.exception.TextClassificationException
     *             excpetion in case of error
     */
    String getLanguage2() throws TextClassificationException;

    /**
     * The text of the first document in the pair
     * 
     * @return text
     * @throws org.dkpro.tc.api.exception.TextClassificationException
     *             excpetion in case of error
     */
    String getText1() throws TextClassificationException;

    /**
     * The text of the second document in the pair
     * 
     * @return text
     * @throws org.dkpro.tc.api.exception.TextClassificationException
     *             excpetion in case of error
     */
    String getText2() throws TextClassificationException;

}