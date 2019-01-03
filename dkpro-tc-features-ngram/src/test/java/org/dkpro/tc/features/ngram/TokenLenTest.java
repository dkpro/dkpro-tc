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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.Resource_ImplBase;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.features.maxnormalization.TokenLengthRatio;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabelUnitReader;
import org.dkpro.tc.features.ngram.meta.maxnormalization.MaxTokenLenMC;
import org.junit.Before;

import com.google.common.collect.Lists;

public class TokenLenTest
    extends LuceneMetaCollectionBasedFeatureTestBase
{
    private static String EXTRACTOR_NAME = "5646536754431";

    @Before
    public void setup()
    {
        super.setup();

        featureClass = TokenLengthRatio.class;
        metaCollectorClass = MaxTokenLenMC.class;
    }

    @Override
    protected void evaluateMetaCollection(File luceneFolder) throws Exception
    {
        List<String> entries = new ArrayList<String>(getEntriesFromIndex(luceneFolder));
        Collections.sort(entries, new Comparator<String>()
        {

            @Override
            public int compare(String o1, String o2)
            {
                return Integer.valueOf(o1.split("_")[0])
                        .compareTo(Integer.valueOf(o2.split("_")[0]));
            }
        });
        entries = Lists.reverse(entries);

        assertEquals(35, entries.size());
        assertEquals("5", entries.get(0).split("_")[0]);
    }

    @Override
    protected void evaluateExtractedFeatures(File output) throws Exception
    {
        List<Instance> instances = readInstances(output);
        Collections.sort(instances, new Comparator<Instance>()
        {

            @Override
            public int compare(Instance o1, Instance o2)
            {
                Double v1 = (Double) new ArrayList<Feature>(o1.getFeatures()).get(0).getValue();
                Double v2 = (Double) new ArrayList<Feature>(o2.getFeatures()).get(0).getValue();
                return v1.compareTo(v2);
            }
        });

        instances = Lists.reverse(instances);

        assertEquals(33, instances.size());

        Double r = (Double) (new ArrayList<Feature>(instances.get(0).getFeatures()).get(0).getValue());
        assertEquals(1, r, 0.01);
        
        r = (Double) (new ArrayList<Feature>(instances.get(32).getFeatures()).get(0).getValue());
        assertEquals(0.2, r, 0.01);

    }
    
    @Override
    protected AnalysisEngineDescription prepareFeatureExtractor(File outputPath,
            Class<? extends Resource_ImplBase> class1, Object[] parameters)
        throws ResourceInitializationException
    {
        List<ExternalResourceDescription> fes = makeResource(class1, parameters);

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_UNIT, false, false, false, false,
                Collections.emptyList(), fes, new String[] {});

        return featExtractorConnector;
    }

    @Override
    protected CollectionReaderDescription getMetaReader() throws Exception
    {
        return CollectionReaderFactory.createReaderDescription(TestReaderSingleLabelUnitReader.class,
                TestReaderSingleLabelUnitReader.PARAM_LANGUAGE, "en",
                TestReaderSingleLabelUnitReader.PARAM_SOURCE_LOCATION, "src/test/resources/ngrams/",
                TestReaderSingleLabelUnitReader.PARAM_PATTERNS, "text*");
    }

    @Override
    protected CollectionReaderDescription getFeatureReader() throws Exception
    {
        return getMetaReader();
    }

    @Override
    protected Object[] getMetaCollectorParameters(File luceneFolder)
    {
        return new Object[] { TokenLengthRatio.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
                TokenLengthRatio.PARAM_NGRAM_USE_TOP_K, "1",
                TokenLengthRatio.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
                MaxTokenLenMC.PARAM_TARGET_LOCATION, luceneFolder.toString(),
                TokenLengthRatio.PARAM_NGRAM_MIN_N, "1",
                TokenLengthRatio.PARAM_NGRAM_MAX_N, "1", };
    }

    @Override
    protected Object[] getFeatureExtractorParameters(File luceneFolder)
    {
        return new Object[] { MaxTokenLenMC.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
                TokenLengthRatio.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
                MaxTokenLenMC.PARAM_TARGET_LOCATION, luceneFolder.toString() };
    }
}
