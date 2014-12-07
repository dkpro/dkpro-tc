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
package de.tudarmstadt.ukp.dkpro.tc.api.exception;

/**
 * Exception thrown by DKPro TC components.
 * 
 * @author zesch
 *
 */
public class TextClassificationException
    extends Exception
{

    static final long serialVersionUID = 1L;

    /**
     * 
     */
    public TextClassificationException()
    {
        super();
    }

    /**
     * @param txt
     */
    public TextClassificationException(String txt)
    {
        super(txt);
    }

    /**
     * @param message
     * @param cause
     */
    public TextClassificationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public TextClassificationException(Throwable cause)
    {
        super(cause);
    }

}
