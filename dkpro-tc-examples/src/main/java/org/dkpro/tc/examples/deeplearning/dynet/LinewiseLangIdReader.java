package org.dkpro.tc.examples.deeplearning.dynet;
/**
 * Copyright 2018
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData", })
public class LinewiseLangIdReader
    extends JCasResourceCollectionReader_ImplBase
{

    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, defaultValue = "UTF-8")
    protected String encoding;

    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    private BufferedReader br;

    private List<BufferedReader> bfs = new ArrayList<BufferedReader>();
    private int currentReader = 0;

    private int instanceId = 1;

    private String documentText = null;
    private String label = null;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            for (Resource r : getResources()) {
                String name = r.getResource().getFile().getName();
                InputStreamReader is = null;
                if (name.endsWith(".gz")) {
                    is = new InputStreamReader(new GZIPInputStream(r.getInputStream()), encoding);
                }
                else {
                    is = new InputStreamReader(r.getInputStream(), encoding);
                }
                br = new BufferedReader(is);
                bfs.add(br);
            }
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {

        DocumentMetaData md = new DocumentMetaData(aJCas);
        md.setDocumentTitle("");
        md.setDocumentId("" + (instanceId++));
        md.setLanguage(language);
        md.addToIndexes();

        aJCas.setDocumentText(documentText);
        
        TextClassificationOutcome o = new TextClassificationOutcome(aJCas, 0, documentText.length());
        o.setOutcome(label);
        o.addToIndexes();
    }


    public boolean hasNext()
        throws IOException, CollectionException
    {
        BufferedReader br = getBufferedReader();
        String line=null;
        	if(((line = br.readLine())!=null)){
        		
        		String[] split = line.split("\t");
        		documentText=split[0];
        		label=split[1];
        		
        		return true;
        	}
        	
        return closeReaderOpenNext();

    }

    private boolean closeReaderOpenNext()
        throws CollectionException, IOException
    {
        bfs.get(currentReader).close();

        if (currentReader + 1 < bfs.size()) {
            currentReader++;
            return hasNext();
        }
        return false;
    }

    private BufferedReader getBufferedReader()
    {
        return bfs.get(currentReader);
    }

    public Progress[] getProgress()
    {
        return null;
    }
}
