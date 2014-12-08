/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.uima.TcAnnotator;

public class LoadModelTest {
	
	@Test
    @Ignore
	public void loadModelTest()  throws Exception {
		
		SimplePipeline.runPipeline(
				CollectionReaderFactory.createReader(
						StringReader.class,
						StringReader.PARAM_DOCUMENT_TEXT, "This is an example text",
						StringReader.PARAM_LANGUAGE, "en"
				),
				AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
				AnalysisEngineFactory.createEngineDescription(
						TcAnnotator.class,
						TcAnnotator.PARAM_TC_MODEL_LOCATION,
						"src/test/resources/model/"
				)
		);
	}

}
