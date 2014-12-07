package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.uima.TcAnnotator;

public class LoadModelTest {
	
	@Test
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
