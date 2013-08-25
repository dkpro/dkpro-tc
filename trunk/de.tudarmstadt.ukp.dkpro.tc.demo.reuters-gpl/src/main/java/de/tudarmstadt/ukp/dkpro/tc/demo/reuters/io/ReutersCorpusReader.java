package de.tudarmstadt.ukp.dkpro.tc.demo.reuters.io;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.tc.io.TCReaderMultiLabel;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class ReutersCorpusReader
    extends TextReader
    implements TCReaderMultiLabel
{

    /**
     * Path to the file containing the gold standard labels.
     */
    public static final String PARAM_GOLD_LABEL_FILE = "GoldLabelFile";
    @ConfigurationParameter(name = PARAM_GOLD_LABEL_FILE, mandatory = true)
    private String goldLabelFile;

    private Map<String, List<String>> goldLabelMap;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        goldLabelMap = new HashMap<String, List<String>>();

        try {
            URL resourceUrl = ResourceUtils.resolveLocation(goldLabelFile, this, context);

            for (String line : FileUtils.readLines(new File(resourceUrl.toURI()))) {
                String[] parts = line.split(" ");

                if (parts.length < 2) {
                    throw new IOException("Wrong file format in line: " + line);
                }
                String fileId = parts[0].split("/")[1];

                List<String> labels = new ArrayList<String>();
                for (int i=1; i<parts.length; i++) {
                    labels.add(parts[i]);
                }

                goldLabelMap.put(fileId, labels);
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        catch (URISyntaxException ex) {
            throw new ResourceInitializationException(ex);
        }
    }

    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        super.getNext(aCAS);

        JCas jcas;
        try {
            jcas = aCAS.getJCas();
        }
        catch (CASException e) {
            throw new CollectionException();
        }

        for (String outcomeValue : getTextClassificationOutcomes(jcas)) {
            TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
            outcome.setOutcome(outcomeValue);
            outcome.addToIndexes();
        }
    }

    @Override
    public Set<String> getTextClassificationOutcomes(JCas jcas)
        throws CollectionException
    {
        Set<String> outcomes = new HashSet<String>();
    
        DocumentMetaData dmd = DocumentMetaData.get(jcas);
        String titleWithoutExtension = FilenameUtils.removeExtension(dmd.getDocumentTitle());

        if (!goldLabelMap.containsKey(titleWithoutExtension)) {
            throw new CollectionException(new Throwable("No gold label for document: " + dmd.getDocumentTitle()));
        }

        for (String label : goldLabelMap.get(titleWithoutExtension)) {
            outcomes.add(label);
        }
        return outcomes;
    }
}
