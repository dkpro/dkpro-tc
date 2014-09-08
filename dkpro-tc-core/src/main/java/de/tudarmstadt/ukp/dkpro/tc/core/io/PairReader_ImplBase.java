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

import java.io.IOException;

import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

/**
 * Abstract base class for readers used in pair-classification. We assume pair classification
 * consists of two texts with one or more outcomes for the pair.
 * <p>
 * The <code>jcas</code> representing the instance contains two <code>views</code>. Each text in the
 * pair of texts is set to one of the <code>views</code>. Information about the instance, such as
 * <code>title</code> and <code>instance id</code>, is set to the original <code>jcas</code>.
 * 
 */
public abstract class PairReader_ImplBase
    extends JCasCollectionReader_ImplBase
    implements Constants, PairReader
{
    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        try {
            createMetaData(
            		jcas,
            		getCollectionId1() + "_" + getCollectionId2(),
            		getDocumentId1() + "_" + getDocumentId2(),
            		getTitle1() + " " + getTitle2()
           );

            createView(PART_ONE, jcas, getLanguage1(), getText1(), getDocumentId1(),
                    getTitle1());
            createView(PART_TWO, jcas, getLanguage2(), getText2(), getDocumentId2(),
                    getTitle2());
        }
        catch (TextClassificationException e) {
            throw new CollectionException(e);
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }
    }

    protected void createView(String part, JCas jCas, String language, String text, String docId,
            String docTitle)
        throws CASException
    {
        JCas view = jCas.createView(part.toString());
        view.setDocumentText(text);
        view.setDocumentLanguage(language);

        DocumentMetaData baseMetaData = DocumentMetaData.get(jCas);
        createMetaData(
                view,
                baseMetaData.getCollectionId(),
                docId,
                docTitle
        );
    }

    protected void createMetaData(JCas jcas, String collectionId, String docId,
            String docTitle)
    {
        DocumentMetaData metaData = DocumentMetaData.create(jcas);
        metaData.setCollectionId(collectionId);
        metaData.setDocumentBaseUri("");
        metaData.setDocumentUri("/" + docId);
        metaData.setDocumentTitle(docTitle);
        metaData.setDocumentId(docId);
    }
}