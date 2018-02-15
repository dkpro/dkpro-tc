/*******************************************************************************
 * Copyright 2018
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ExternalResourceDescription;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.MaxNrOfCharsPerCasMC;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class NumberOfCharsRatioTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private static String EXTRACTOR_NAME = "56465431";

	@Test
	public void testLuceneMetaCollectorOutput() throws Exception {
		File luceneFolder = folder.newFolder();

		runMetaCollection(luceneFolder);
		evaluateMetaCollection(luceneFolder);

		File output = runFeatureExtractor(luceneFolder);
		evaluateExtractedFeatures(output);
	}

	private void evaluateExtractedFeatures(File output) throws Exception {
		Gson gson = new Gson();
		List<String> lines = FileUtils.readLines(new File(output, JsonDataWriter.JSON_FILE_NAME), "utf-8");
		List<Instance> instances = new ArrayList<>();
		for (String l : lines) {
			instances.add(gson.fromJson(l, Instance.class));
		}

		Collections.sort(instances, new Comparator<Instance>() {

			@Override
			public int compare(Instance o1, Instance o2) {
				String v1 = ((Double) new ArrayList<Feature>(o1.getFeatures()).get(0).getValue()).toString();
				String v2 = ((Double) new ArrayList<Feature>(o2.getFeatures()).get(0).getValue()).toString();
				return v1.compareTo(v2);
			}
		});
		
		
		assertEquals(3, instances.size());
		
		double r = (Double)(new ArrayList<Feature>(instances.get(0).getFeatures()).get(0).getValue());
		assertEquals(23.0/33, r, 0.01);
		
		r = (Double)(new ArrayList<Feature>(instances.get(1).getFeatures()).get(0).getValue());
		assertEquals(29.0/33, r, 0.01);
		
		r = (Double)(new ArrayList<Feature>(instances.get(2).getFeatures()).get(0).getValue());
		assertEquals(33.0/33, r, 0.01);
	}

	private File runFeatureExtractor(File luceneFolder) throws Exception {
		File outputPath = folder.newFolder();

		Object[] parameters = new Object[] { NrOfCharsRatioPerDocument.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
				NrOfCharsRatioPerDocument.PARAM_NGRAM_USE_TOP_K, "1", NrOfCharsRatioPerDocument.PARAM_SOURCE_LOCATION,
				luceneFolder.toString(), MaxNrOfCharsPerCasMC.PARAM_TARGET_LOCATION,
				luceneFolder.toString(), NrOfCharsRatioPerDocument.PARAM_NGRAM_MIN_N, "1",
				NrOfCharsRatioPerDocument.PARAM_NGRAM_MAX_N, "1", };

		ExternalResourceDescription featureExtractor = ExternalResourceFactory
				.createExternalResourceDescription(NrOfCharsRatioPerDocument.class, parameters);
		List<ExternalResourceDescription> fes = new ArrayList<>();
		fes.add(featureExtractor);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_LANGUAGE, "en",
				TestReaderSingleLabel.PARAM_SOURCE_LOCATION, "src/test/resources/ngrams/",
				TestReaderSingleLabel.PARAM_PATTERNS, "text*",
				TestReaderSingleLabel.PARAM_SUPPRESS_DOCUMENT_ANNOTATION, false);

		AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
				outputPath.getAbsolutePath(), JsonDataWriter.class.getName(), Constants.LM_SINGLE_LABEL,
				Constants.FM_DOCUMENT, false, false, false, false, Collections.emptyList(), fes, new String[] {});

		SimplePipeline.runPipeline(reader, featExtractorConnector);

		return outputPath;
	}

	private void evaluateMetaCollection(File luceneFolder) throws Exception {
		List<String> entries = new ArrayList<String>(getEntriesFromIndex(luceneFolder));
		Collections.sort(entries);
		assertEquals(3, entries.size());
		assertEquals("23", entries.get(0).split("_")[0]);
		assertEquals("29", entries.get(1).split("_")[0]);
		assertEquals("33", entries.get(2).split("_")[0]);
	}

	private void runMetaCollection(File luceneFolder) throws Exception {


		Object[] parameters = new Object[] { NrOfCharsRatioPerDocument.PARAM_UNIQUE_EXTRACTOR_NAME, EXTRACTOR_NAME,
				NrOfCharsRatioPerDocument.PARAM_NGRAM_USE_TOP_K, "1", NrOfCharsRatioPerDocument.PARAM_SOURCE_LOCATION,
				luceneFolder.toString(), MaxNrOfCharsPerCasMC.PARAM_TARGET_LOCATION,
				luceneFolder.toString(), NrOfCharsRatioPerDocument.PARAM_NGRAM_MIN_N, "1",
				NrOfCharsRatioPerDocument.PARAM_NGRAM_MAX_N, "1", };

		List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_LANGUAGE, "en",
				TestReaderSingleLabel.PARAM_SOURCE_LOCATION, "src/test/resources/ngrams/",
				TestReaderSingleLabel.PARAM_PATTERNS, "text*"
				);

		AnalysisEngineDescription segmenter = AnalysisEngineFactory
				.createEngineDescription(BreakIteratorSegmenter.class);

		AnalysisEngineDescription metaCollector = AnalysisEngineFactory
				.createEngineDescription(MaxNrOfCharsPerCasMC.class, parameterList.toArray());

		// run meta collector
		SimplePipeline.runPipeline(reader, segmenter, metaCollector);
	}

	private Set<String> getEntriesFromIndex(File luceneFolder) throws Exception {
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
}
