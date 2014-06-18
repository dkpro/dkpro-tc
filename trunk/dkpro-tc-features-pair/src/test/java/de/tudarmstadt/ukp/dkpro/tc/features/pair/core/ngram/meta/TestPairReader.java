package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderMultiLabel;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;

public class TestPairReader
    extends AbstractPairReader
    implements TCReaderMultiLabel
{

    public static final String PARAM_INPUT_FILE = "InputFile";
    @ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
    protected File inputFile;

    private List<String> texts1;
    private List<String> texts2;

    private int fileOffset;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        fileOffset = 0;
        texts1 = new ArrayList<String>();
        texts2 = new ArrayList<String>();

        try {
            for (String line : FileUtils.readLines(inputFile)) {
                String parts[] = line.split("\t");

                if (parts.length != 2) {
                    throw new ResourceInitializationException(new Throwable("Wrong file format: "
                            + line));
                }

                texts1.add(parts[0]);
                texts2.add(parts[1]);
            }      
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
        return fileOffset < texts1.size();
    }

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        super.getNext(jcas);

        for (String outcomeValue : getTextClassificationOutcomes(jcas)) {
            TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
            outcome.setOutcome(outcomeValue);
            outcome.addToIndexes();
        }

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
    protected String getCollectionId()
    {
        return inputFile.getParent();
    }

    @Override
    protected String getLanguage()
    {
        return "en";
    }

    @Override
    protected String getInitialViewText()
    {
        return texts1.get(fileOffset) + "\t" + texts2.get(fileOffset);
    }

    @Override
    protected String getInitialViewDocId()
    {
        return inputFile.getName() + "-" + fileOffset;
    }

    @Override
    protected String getInitialViewTitle()
    {
        return inputFile.getName() + "-" + fileOffset;
    }

    @Override
    protected String getBaseUri()
    {
        return inputFile.getParent();
    }

    @Override
    protected String getText(String part)
    {
        if (part.equals(PART_ONE)) {
            return texts1.get(fileOffset);
        }
        else if (part.equals(PART_TWO)) {
            return texts2.get(fileOffset);
        }
        return "";
    }

    @Override
    public Set<String> getTextClassificationOutcomes(JCas jcas)
    {
        Set<String> outcomes = new HashSet<String>();
        outcomes.add("test");
        return outcomes;
    }
}