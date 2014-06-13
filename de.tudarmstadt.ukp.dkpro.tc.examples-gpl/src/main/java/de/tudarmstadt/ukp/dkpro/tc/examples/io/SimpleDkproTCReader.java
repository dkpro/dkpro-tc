package de.tudarmstadt.ukp.dkpro.tc.examples.io;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSingleLabel;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.examples.single.document.SimpleDkproTCReaderDemo;

/**
 * A very basic DKPro TC reader, which reads sentences from a text file and labels from another text
 * file. It is used in {@link SimpleDkproTCReaderDemo}.
 * 
 */
public class SimpleDkproTCReader
    extends JCasResourceCollectionReader_ImplBase
    implements TCReaderSingleLabel

{
    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    /**
     * Path to the file containing the gold standard labels.
     */
    public static final String PARAM_GOLD_LABEL_FILE = "GoldLabelFile";
    @ConfigurationParameter(name = PARAM_GOLD_LABEL_FILE, mandatory = true)
    private String goldLabelFile;

    private List<String> golds;

    private List<String> texts;
    private int offset;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        // read file with gold labels
        golds = new ArrayList<String>();
        try {
            URL resourceUrl = ResourceUtils.resolveLocation(goldLabelFile, this, context);
            for (String label : FileUtils.readLines(new File(resourceUrl.toURI()))) {
                golds.add(label);
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        catch (URISyntaxException ex) {
            throw new ResourceInitializationException(ex);
        }

        // read file with instances
        offset = 0;
        Resource res = nextFile();
        BufferedReader reader = null;
        texts = new ArrayList<String>();
        try {
            reader = new BufferedReader(new InputStreamReader(res.getInputStream(), encoding));

            String line;
            while ((line = reader.readLine()) != null) {
                texts.add(line);
            }
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            closeQuietly(reader);
        }
    }

    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
        return offset < texts.size();
    }

    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        // as we are creating more than one CAS out of a single file, we need to have different
        // document titles and URIs for each CAS
        // otherwise, serialized CASes will be overwritten
        DocumentMetaData dmd = DocumentMetaData.create(aJCas);
        dmd.setDocumentTitle(dmd.getDocumentTitle() + "-" + offset);
        dmd.setDocumentUri(dmd.getDocumentUri() + "-" + offset);
        dmd.setDocumentId(String.valueOf(offset));

        // setting the document text
        aJCas.setDocumentText(texts.get(offset));

        // setting the outcome / label for this document
        TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas);
        outcome.setOutcome(getTextClassificationOutcome(aJCas));
        outcome.addToIndexes();

        offset++;
    }

    @Override
    public String getTextClassificationOutcome(JCas jcas)
        throws CollectionException
    {
        return golds.get(offset);
    }

}