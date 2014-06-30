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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.tc.examples.io;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

/**
 * Reads the GermEval 2014 NER Shared Task dataset.
 * Can be used for Unit or Sequence Classification.
 *
 */
public class NERDemoReader
    extends JCasResourceCollectionReader_ImplBase
	implements TCReaderSequence

{
    private static final int OFFSET = 0;
    private static final int TOKEN = 1;
    private static final int IOB = 2;
    private static final int IOB_EMBEDDED = 3;


    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    /**
     * The language.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    private String language;
    
    /**
     * Load the chunk tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_CHUNK_MAPPING_LOCATION = ComponentParameters.PARAM_CHUNK_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_CHUNK_MAPPING_LOCATION, mandatory = false)
    protected String chunkMappingLocation;
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        
        Resource res = nextFile();
        initCas(aJCas, res);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(res.getInputStream(), encoding));
            convert(aJCas, reader);
        }
        finally {
            closeQuietly(reader);
        }
    }

    private void convert(JCas aJCas, BufferedReader aReader)
        throws IOException
    {
        JCasBuilder doc = new JCasBuilder(aJCas);
        
        List<String[]> words;
        while ((words = readSentence(aReader)) != null) {
            if (words.isEmpty()) {
                continue;
            }

            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;

            List<Token> tokens = new ArrayList<Token>();
            
            // Tokens, NER-IOB
            for (String[] word : words) {
                // Read token
                Token token = doc.add(word[TOKEN], Token.class);
                sentenceEnd = token.getEnd();
                doc.add(" ");
                
                TextClassificationUnit unit = new TextClassificationUnit(aJCas, token.getBegin(), token.getEnd());
                unit.addToIndexes();
                
                TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas, token.getBegin(), token.getEnd());
                outcome.setOutcome(word[IOB]);
                outcome.addToIndexes();
            
                tokens.add(token);
            }

            // Sentence
            Sentence sentence = new Sentence(aJCas, sentenceBegin, sentenceEnd);
            sentence.addToIndexes();
            
            TextClassificationSequence sequence = new TextClassificationSequence(aJCas, sentenceBegin, sentenceEnd);
            sequence.addToIndexes();

            // Once sentence per line.
            doc.add("\n");
        }

        doc.close();
    }

    /**
     * Read a single sentence.
     */
    private static List<String[]> readSentence(BufferedReader aReader)
        throws IOException
    {
        List<String[]> words = new ArrayList<String[]>();
        String line;
        while ((line = aReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                break; // End of sentence
            }
            if (line.startsWith("#")) {
            	continue;
            }
            
            String[] fields = line.split("\t");
            if (fields.length != 4) {
                throw new IOException(
                        "Invalid file format. Line needs to have 4 tab-separted fields.");
            }
            words.add(fields);
        }

        if (line == null && words.isEmpty()) {
            return null;
        }
        else {
            return words;
        }
    }

	@Override
	public String getTextClassificationOutcome(JCas jcas,
			TextClassificationUnit unit) throws CollectionException
	{
		// without function here, as we do not represent this in the CAS
		return null;
	}
}