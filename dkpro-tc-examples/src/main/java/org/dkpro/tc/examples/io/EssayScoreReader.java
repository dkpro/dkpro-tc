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
package org.dkpro.tc.examples.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

public class EssayScoreReader
    extends JCasCollectionReader_ImplBase

{
    public static final String PARAM_SOURCE_LOCATION = ComponentParameters.PARAM_SOURCE_LOCATION;
    @ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true)
    private File inputFile;

    /**
     * Character encoding of the input data
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    /**
     * Language of the input data
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    private String language;

    String text = null;
    String score = null;

    private BufferedReader br;

    int id=0;
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        DocumentMetaData dmd = new DocumentMetaData(aJCas);
        dmd.setDocumentTitle("");
        dmd.setDocumentId("Document " + id++ + "");
        dmd.addToIndexes();

        aJCas.setDocumentText(text);

        TextClassificationOutcome o = new TextClassificationOutcome(aJCas, 0,
                aJCas.getDocumentText().length());
        o.setOutcome(score);
        o.addToIndexes();
    }

    @Override
    public boolean hasNext()
    {
        try {
            if (br == null) {
                br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(inputFile), "utf-8"));
            }

            String entry = br.readLine();

            if (entry == null || entry.isEmpty()) {
                return false;
            }

            String[] split = entry.split("\t");
            score = split[0];
            text = split[1];
            return true;
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public Progress[] getProgress()
    {
        return null;
    }

}