/**
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.tc.examples.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.core.io.SingleLabelReaderBase;

/**
 * Reads the classical TwentyNewsgroups text classification corpus with instance weights.
 */
public class WeightedTwentyNewsgroupsCorpusReader
    extends SingleLabelReaderBase
{
    public static final String PARAM_WEIGHT_FILE_LOCATION = "weightFileLocation";
    @ConfigurationParameter(name = PARAM_WEIGHT_FILE_LOCATION, mandatory = true)
    protected String weightFile;	
	
    private static final char SEPARATOR_CHAR = '=';
    
    HashMap<String, String> weights;
	
    @Override
    public String getTextClassificationOutcome(JCas jcas)
            throws CollectionException
    {
        try {
            String uriString = DocumentMetaData.get(jcas).getDocumentUri();
            return new File(new URI(uriString).getPath()).getParentFile().getName();
        }
        catch (URISyntaxException e) {
            throw new CollectionException(e);
        }
    }

    //This is the important method to override. If not defined, all weights are one.
	@Override
	public double getTextClassificationOutcomeWeight(JCas jcas)
			throws CollectionException {
		
	   //read a user-provided file with weights and assign the weight to each instance based on its ID
		double doubleWeight = 0;
		
		try {
			doubleWeight = Double.parseDouble(weights.get((DocumentMetaData.get(jcas).getDocumentId().split("/")[1]).split("\\.")[0]));
        }
        catch (NumberFormatException e) {
            throw new CollectionException(e);
		}			
		return doubleWeight;
	}
	
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
    	super.initialize(context);
    	List<String> lines = new ArrayList<String>();
        try {
			lines = FileUtils.readLines(new File(weightFile));
		} catch (IOException e) {
            throw new ResourceInitializationException(e);
		}
        weights = new HashMap<String, String>();

        for (String l : lines) {
            String[] splitted = l.split(String.valueOf(SEPARATOR_CHAR));
        		if (splitted.length==2) 
        			weights.put(splitted[0], splitted[1]);
        	}
        }
}