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
package org.dkpro.tc.features.ngram.meta;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.meta.MetaCollector;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

public abstract class LuceneMC
    extends MetaCollector
{
    public final static String LUCENE_DIR = "lucene";

    public static final String LUCENE_ID_FIELD = "id";

    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    protected File luceneDir;

    // this is a static singleton as different Lucene-based meta collectors will
    // use the same writer
    static IndexWriter indexWriter = null;
    static AtomicInteger activeWriter = null; // used to known when we can close the index

    protected Document currentDocument;
    long entryCounter = 0;

    protected FieldType fieldType;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        initializeWriter();

        initDocument();

        fieldType = new FieldType();
        fieldType.setIndexed(true);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        fieldType.setStored(true);
        fieldType.setOmitNorms(true);
        fieldType.setTokenized(false);
        fieldType.freeze();

        activeWriter.incrementAndGet();
    }

    protected synchronized void initializeWriter() throws ResourceInitializationException
    {
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44, null);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        if (indexWriter == null) {
            try {
                indexWriter = new IndexWriter(FSDirectory.open(luceneDir), config);
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
        }

        if (activeWriter == null) {
            activeWriter = new AtomicInteger(0);
        }
    }

    protected void initDocument()
    {
        currentDocument = new Document();
        currentDocument.add(new StringField(LUCENE_ID_FIELD,
                "metaCollection" + System.currentTimeMillis(), Field.Store.YES));
    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException
    {
        try {

            FrequencyDistribution<String> documentNGrams;
            documentNGrams = getNgramsFD(jcas);
            for (String ngram : documentNGrams.getKeys()) {
                // As a result of discussion, we add a field for each ngram per
                // doc, not just each ngram type per doc.
                Field field = new Field(getFieldName(), ngram, fieldType);
                for (int i = 0; i < documentNGrams.getCount(ngram); i++) {
                    currentDocument.add(field);
                    documentSizeControll();
                }
            }

        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    // We write documents to disk after a thousand entries. This threshold is
    // arbitrarily set, this decouples the index size from the number of CAS
    // objects that are being processed. If, for instance, many postings are
    // processed where each posting is
    // an own CAS, we write extremely many documents. Likewise, if everything is in
    // one CAS, we write one fat document which might require unreasonable much
    // memory. This attempts to keep the index in a constant range of 'costs' that
    // is independent of the number of actually processed CAS objects.
    protected void documentSizeControll() throws IOException
    {
        entryCounter++;
        if (entryCounter > 10000) {
            writeToIndex();
            indexWriter.commit();
            entryCounter = 0;
            initDocument();
        }
    }

    protected void writeToIndex() throws IOException
    {
        if (currentDocument == null) {
            throw new IOException("Lucene document not initialized. Fatal error.");
        }
        indexWriter.addDocument(currentDocument);
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            writeToIndex();
            indexWriter.commit();
            closeWriter();
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private synchronized void closeWriter() throws IOException
    {
        int accessingMetaWriters = activeWriter.decrementAndGet();
        if (accessingMetaWriters == 0) {
            indexWriter.close();
            indexWriter = null;
        }
    }

    protected String getDocumentId(JCas jcas)
    {
        return DocumentMetaData.get(jcas).getDocumentId();
    }

    protected abstract FrequencyDistribution<String> getNgramsFD(JCas jcas)
        throws TextClassificationException;

    protected abstract String getFieldName();
}