/**
 * Copyright 2019
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
package org.dkpro.tc.examples.shallow.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.io.TCReaderSingleLabel;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.core.io.PairReader_ImplBase;

/**
 * Reads the Semantic Text Similarity (STS) SemEval format.
 */
public class STSReader
    extends PairReader_ImplBase
    implements TCReaderSingleLabel
{

    /**
     * File that lists the the text pairs to be used. One pair per line, tab separated.
     */
    public static final String PARAM_INPUT_FILE = "InputFile";
    @ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
    protected File inputFile;

    /**
     * The gold standard values for each pair. Same line offsets as in the text pair file.
     */
    public static final String PARAM_GOLD_FILE = "GoldFile";
    @ConfigurationParameter(name = PARAM_GOLD_FILE, mandatory = true)
    protected File goldFile;

    private List<String> texts1;
    private List<String> texts2;
    private List<Double> golds;

    private int fileOffset;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        fileOffset = 0;
        texts1 = new ArrayList<String>();
        texts2 = new ArrayList<String>();
        golds = new ArrayList<Double>();

        try {
            for (String line : FileUtils.readLines(inputFile, "utf-8")) {
                String parts[] = line.split("\t");

                if (parts.length != 2) {
                    throw new ResourceInitializationException(
                            new Throwable("Wrong file format: " + line));
                }

                texts1.add(parts[0]);
                texts2.add(parts[1]);
            }

            for (String line : FileUtils.readLines(goldFile, "utf-8")) {
                try {
                    double goldValue = Double.parseDouble(line);
                    golds.add(goldValue);
                }
                catch (NumberFormatException e) {
                    throw new ResourceInitializationException(e);
                }
            }

            if (texts1.size() != golds.size()) {
                throw new ResourceInitializationException(
                        new Throwable("Size of text list does not match size of gold list."));
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException
    {
        return fileOffset < texts1.size();
    }

    @Override
    public void getNext(JCas jcas) throws IOException, CollectionException
    {
        super.getNext(jcas);
        jcas.setDocumentText("");

        TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
        outcome.setOutcome(getTextClassificationOutcome(jcas));
        outcome.addToIndexes();

        // as we are creating more than one CAS out of a single file, we need to have different
        // document titles and URIs for each CAS
        // otherwise, serialized CASes will be overwritten
        DocumentMetaData dmd = DocumentMetaData.get(jcas);
        dmd.setDocumentTitle(dmd.getDocumentTitle() + "-" + fileOffset);
        dmd.setDocumentUri(dmd.getDocumentUri() + "-" + fileOffset);
        fileOffset++;

    }

    @Override
    public Progress[] getProgress()
    {
        return new Progress[] { new ProgressImpl(fileOffset, texts1.size(), Progress.ENTITIES) };
    }

    @Override
    public String getTextClassificationOutcome(JCas jcas)
    {
        return golds.get(fileOffset).toString();
    }

    @Override
    public String getCollectionId1() throws TextClassificationException
    {
        return inputFile.getParent();
    }

    @Override
    public String getCollectionId2() throws TextClassificationException
    {
        return inputFile.getParent();
    }

    @Override
    public String getDocumentId1() throws TextClassificationException
    {
        return inputFile.getName() + "-" + fileOffset;
    }

    @Override
    public String getDocumentId2() throws TextClassificationException
    {
        return inputFile.getName() + "-" + fileOffset;
    }

    @Override
    public String getTitle1() throws TextClassificationException
    {
        return inputFile.getName() + "-" + fileOffset;
    }

    @Override
    public String getTitle2() throws TextClassificationException
    {
        return inputFile.getName() + "-" + fileOffset;
    }

    @Override
    public String getLanguage1() throws TextClassificationException
    {
        return "en";
    }

    @Override
    public String getLanguage2() throws TextClassificationException
    {
        return "en";
    }

    @Override
    public String getText1() throws TextClassificationException
    {
        return texts1.get(fileOffset);
    }

    @Override
    public String getText2() throws TextClassificationException
    {
        return texts2.get(fileOffset);
    }
}