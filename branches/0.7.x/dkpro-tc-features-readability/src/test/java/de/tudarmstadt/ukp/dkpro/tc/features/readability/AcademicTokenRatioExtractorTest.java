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

package de.tudarmstadt.ukp.dkpro.tc.features.readability;


public class AcademicTokenRatioExtractorTest
{
    // Need to check the licenses of the lists
    // @Test
    // public void testAcademicTokenRatioExtractor()
    // throws Exception
    // {
    //
    // String text = FileUtils
    // .readFileToString(new File("src/test/resources/test_document_en.txt"));
    //
    // AnalysisEngineDescription desc = createEngineDescription(
    // createEngineDescription(OpenNlpSegmenter.class),
    // createEngineDescription(OpenNlpPosTagger.class),
    // createEngineDescription(ClearNlpLemmatizer.class));
    // AnalysisEngine engine = createEngine(desc);
    // JCas jcas = engine.newJCas();
    // jcas.setDocumentLanguage("en");
    // jcas.setDocumentText(text);
    // engine.process(jcas);
    //
    // AcademicTokenRatioExtractor extractor = new AcademicTokenRatioExtractor();
    // List<Feature> features = extractor.extract(jcas);
    //
    // Assert.assertEquals(2, features.size());
    // Assert.assertEquals((double) features.get(0).getValue(), 0.08, 0.1);
    // Assert.assertEquals((double) features.get(1).getValue(), 0.16, 0.1);
    // }
}