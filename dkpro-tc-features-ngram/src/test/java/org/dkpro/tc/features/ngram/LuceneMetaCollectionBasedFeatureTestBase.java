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
package org.dkpro.tc.features.ngram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.Resource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.util.TaskUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public abstract class LuceneMetaCollectionBasedFeatureTestBase
{

    @Before
    public void setup()
    {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public static Class<? extends AnalysisComponent> metaCollectorClass;
    public static Class<? extends Resource_ImplBase> featureClass;

    @Test
    public void runTest() throws Exception
    {
        File luceneFolder = folder.newFolder();
        File outputPath = folder.newFolder();

        Object[] metaParams = getMetaCollectorParameters(luceneFolder);
        AnalysisEngineDescription metaCollector = prepareMetaCollector(metaCollectorClass,
                metaParams);
        runMetaCollection(luceneFolder, metaCollector);
        evaluateMetaCollection(luceneFolder);

        Object[] featureParams = getFeatureExtractorParameters(luceneFolder);
        AnalysisEngineDescription featureExtractr = prepareFeatureExtractor(outputPath,
                featureClass, featureParams);
        runFeatureExtractor(luceneFolder, featureExtractr);
        evaluateExtractedFeatures(outputPath);
    }

    protected abstract void evaluateMetaCollection(File luceneFolder) throws Exception;

    protected abstract void evaluateExtractedFeatures(File output) throws Exception;

    protected abstract CollectionReaderDescription getMetaReader() throws Exception;

    protected abstract CollectionReaderDescription getFeatureReader() throws Exception;

    protected abstract Object[] getMetaCollectorParameters(File luceneFolder);

    protected abstract Object[] getFeatureExtractorParameters(File luceneFolder);

    protected AnalysisEngineDescription prepareMetaCollector(
            Class<? extends AnalysisComponent> class1, Object[] parameters)
        throws Exception
    {
        return AnalysisEngineFactory.createEngineDescription(class1, parameters);
    }

    protected void runFeatureExtractor(File luceneFolder,
            AnalysisEngineDescription featureExtractor)
        throws Exception
    {

        CollectionReaderDescription reader = getFeatureReader();

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        SimplePipeline.runPipeline(reader, segmenter, featureExtractor);
    }

    protected void runMetaCollection(File luceneFolder, AnalysisEngineDescription metaCollector)
        throws Exception
    {

        CollectionReaderDescription reader = getMetaReader();

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        SimplePipeline.runPipeline(reader, segmenter, metaCollector);
    }

    protected AnalysisEngineDescription prepareFeatureExtractor(File outputPath,
            Class<? extends Resource_ImplBase> class1, Object[] parameters)
        throws ResourceInitializationException
    {
        List<ExternalResourceDescription> fes = makeResource(class1, parameters);

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_DOCUMENT, false, false, false, false,
                Collections.emptyList(), fes, new String[] {});

        return featExtractorConnector;
    }

    protected Set<String> getEntriesFromIndex(File luceneFolder) throws Exception
    {
        Set<String> token = new HashSet<>();
        @SuppressWarnings("deprecation")
        IndexReader idxReader = IndexReader.open(FSDirectory.open(luceneFolder));
        Fields fields = MultiFields.getFields(idxReader);
        for (String field : fields) {
            if (field.equals("id")) {
                continue;
            }
            Terms terms = fields.terms(field);
            TermsEnum termsEnum = terms.iterator(null);
            BytesRef text;
            while ((text = termsEnum.next()) != null) {
                token.add(text.utf8ToString());
            }
        }
        return token;
    }

    protected List<ExternalResourceDescription> makeResource(
            Class<? extends Resource_ImplBase> class1, Object[] parameters)
    {
        ExternalResourceDescription featureExtractor = ExternalResourceFactory
                .createExternalResourceDescription(class1, parameters);
        List<ExternalResourceDescription> fes = new ArrayList<>();
        fes.add(featureExtractor);
        return fes;
    }

    protected List<Instance> readInstances(File output) throws IOException
    {
        Gson gson = new Gson();
        List<String> lines = FileUtils.readLines(new File(output, JsonDataWriter.JSON_FILE_NAME),
                "utf-8");
        List<Instance> instances = new ArrayList<>();
        for (String l : lines) {
            instances.add(gson.fromJson(l, Instance.class));
        }

        return instances;
    }

}
