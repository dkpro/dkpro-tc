package de.tudarmstadt.ukp.dkpro.tc.experiments.regression.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;

public class STSReader
    extends AbstractPairReader
{

    public static final String PARAM_INPUT_FILE = "InputFile";
    @ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
    protected File inputFile;

    public static final String PARAM_GOLD_FILE = "GoldFile";
    @ConfigurationParameter(name = PARAM_GOLD_FILE, mandatory = true)
    protected File goldFile;

    private List<String> texts1;
    private List<String> texts2;
    private List<Double> golds;

    private int fileOffset;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        fileOffset = 0;
        texts1 = new ArrayList<String>();
        texts2 = new ArrayList<String>();
        golds = new ArrayList<Double>();

        try {
            for (String line : FileUtils.readLines(inputFile)) {
                String parts[] = line.split("\t");

                if (parts.length != 2) {
                    throw new ResourceInitializationException(new Throwable("Wrong file format: " + line));
                }

                texts1.add(parts[0]);
                texts2.add(parts[1]);
            }

            for (String line : FileUtils.readLines(goldFile)) {
                try {
                    double goldValue = Double.parseDouble(line);
                    golds.add(goldValue);
                }
                catch (NumberFormatException e) {
                    throw new ResourceInitializationException(e);
                }
            }

            if (texts1.size() != golds.size()) {
                throw new ResourceInitializationException(new Throwable("Size of text list does not match size of gold list."));
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

    // FIXME - abstractpairreader is designed in a way that we need to overwrite getNext() too
    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        super.getNext(jcas);

        // as we are creating more than one CAS out of a single file, we need to have different document titles and URIs for each cas
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
        return inputFile.getAbsolutePath();
    }

    @Override
    protected String getInitialViewTitle()
    {
        return inputFile.getName();
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
    protected String getId(String part)
    {
        return part + "-" + fileOffset;
    }

    @Override
    protected String getTitle(String part)
    {
        return part + "-" + fileOffset;
    }

    @Override
    protected String[] getTextClassificationOutcome()
    {
        return new String[] {golds.get(fileOffset).toString()};
    }
}
