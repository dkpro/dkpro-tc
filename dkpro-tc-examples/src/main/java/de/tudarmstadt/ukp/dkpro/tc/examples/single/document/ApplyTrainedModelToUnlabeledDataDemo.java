/**
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.tc.examples.single.document;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.ml.uima.TcAnnotatorDocument;

/**
 * Demo to show-case how trained models can be loaded and used to classify unlabeled documents. 
 */
public class ApplyTrainedModelToUnlabeledDataDemo
    implements Constants {
	
	/**
	 * Where the labeled CAS files are written
	 */
	public static final String OUTPUT_FILE_PATH = "target/output";

	/**
	 * some example text
	 */
	public static final String EXAMPLE_TEXT = "This is an exmaple.";

	/**
     * language of input files
     */
    public static final String LANGUAGE_CODE = "en";

    /**
	 * directory where the model and related information is stored 
	 */
	public static final String MODEL_PATH = "src/test/resources/model/";

	/**
	 * example text id
	 */
	public static final String EXAMPLE_TEXT_ID = "example_text";	

    /** 
     * Start the demo.
     * Writes annotated CAS files to the output directory. 
     * 
     * @param args
     * @throws ResourceInitializationException
     * @throws UIMAException
     * @throws IOException
     */
    public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException{
		
		SimplePipeline.runPipeline(
				CollectionReaderFactory.createReader(
						StringReader.class,
						StringReader.PARAM_DOCUMENT_TEXT, EXAMPLE_TEXT,
						StringReader.PARAM_DOCUMENT_ID, EXAMPLE_TEXT_ID,
						StringReader.PARAM_LANGUAGE, LANGUAGE_CODE),
				AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
				AnalysisEngineFactory.createEngineDescription(
						TcAnnotatorDocument.class,
						TcAnnotatorDocument.PARAM_TC_MODEL_LOCATION,
						MODEL_PATH),
				AnalysisEngineFactory.createEngineDescription(
						XmiWriter.class,
						XmiWriter.PARAM_TARGET_LOCATION, OUTPUT_FILE_PATH));
	}
}
