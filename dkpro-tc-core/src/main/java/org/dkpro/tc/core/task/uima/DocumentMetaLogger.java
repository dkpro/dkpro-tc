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
package org.dkpro.tc.core.task.uima;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.core.Constants;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import static java.nio.charset.StandardCharsets.UTF_8;
public class DocumentMetaLogger
    implements Constants
{
    private BufferedWriter logger;
	private String notAvailable="n/a";

    public DocumentMetaLogger(File outputDirectory) throws Exception
    {
        File file = new File(outputDirectory, Constants.FILENAME_DOCUMENT_META_DATA_LOG);
        logger = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF_8));
        write("# Order in which JCas documents have been processed. Shown information are the document id and title from DocumentMetaData");
        write("# ID\tTitle\tLanguage\tisLast");
    }

    public void close()
    {
    	IOUtils.closeQuietly(logger);
    }

    /**
	 * Writes the {@link DocumentMetaData} of a JCas to a file. If no
	 * {@link DocumentMetaData} is available a placeholder is written to the
	 * file instead. The first {@link DocumentMetaData} found is the one written
	 * to file in case multiple annotations are present.
	 * 
	 * @param aJCas
	 *            the JCas which contains a {@link DocumentMetaData} annotation
	 * @throws AnalysisEngineProcessException
	 *             in case of an error
	 */
	public void writeMeta(JCas aJCas) throws AnalysisEngineProcessException {
		
		DocumentMetaData data = null;
		
    	Collection<DocumentMetaData> select = JCasUtil.select(aJCas, DocumentMetaData.class);
    	
    	if(!select.isEmpty()){
    		Iterator<DocumentMetaData> iterator = select.iterator();
    		if(iterator.hasNext()){
    			data = iterator.next();
    		}
    	}
    	
		if (data == null) {
			write(notAvailable + "\t" + 
				  notAvailable + "\t" + 
				  notAvailable + "\t" + 
				  notAvailable);
		} else {
    		write(data.getDocumentId() + "\t" + 
    			  data.getDocumentTitle() + "\t" + 
    			  data.getLanguage() + "\t" + 
    			  data.getIsLastSegment());
    	}
	}
	
	private void write(String s) throws AnalysisEngineProcessException 
    {
        try {
			logger.write(s + "\n");
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
    }

}