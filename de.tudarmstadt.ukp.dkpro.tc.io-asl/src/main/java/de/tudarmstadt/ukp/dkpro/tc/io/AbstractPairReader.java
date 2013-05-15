package de.tudarmstadt.ukp.dkpro.tc.io;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasCollectionReader_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public abstract class AbstractPairReader
    extends JCasCollectionReader_ImplBase
{

    public static String INITIAL_VIEW = CAS.NAME_DEFAULT_SOFA;
    public static String PART_ONE = "PART_ONE";
    public static String PART_TWO = "PART_TWO";

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        try {
            fillInitialView(jcas, getCollectionId(), getLanguage(), getInitialViewText(),
                    getInitialViewDocId(), getInitialViewTitle(), getBaseUri());
            createView(PART_ONE, jcas, getText(PART_ONE), getId(PART_ONE), getTitle(PART_ONE));
            createView(PART_TWO, jcas, getText(PART_TWO), getId(PART_TWO), getTitle(PART_TWO));
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }
    }

    abstract protected String getCollectionId();

    abstract protected String getLanguage();

    abstract protected String getInitialViewText();

    abstract protected String getInitialViewDocId();

    abstract protected String getInitialViewTitle();

    abstract protected String getBaseUri();

    abstract protected String getText(String part);

    abstract protected String getId(String part);

    abstract protected String getTitle(String part);

    protected void fillInitialView(JCas jCas, String collectionId, String language,
            String documentText, String docId, String docTitle, String baseUri)
        throws CASException
    {

        jCas.setDocumentText(documentText);

        createMetaData(jCas, collectionId, language, docId, docTitle, baseUri);

        for (int i = 0; i < getTextClassificationOutcome().length; i++) {
            TextClassificationOutcome outcome = new TextClassificationOutcome(jCas);
            outcome.setOutcome(getTextClassificationOutcome()[i]);
            outcome.addToIndexes();
        }
    }

    abstract protected String[] getTextClassificationOutcome();

    protected void createView(String part, JCas jCas, String text, String docId, String docTitle)
        throws CASException
    {
        JCas view = jCas.createView(part.toString());
        view.setDocumentText(text);

        DocumentMetaData baseMetaData = DocumentMetaData.get(jCas);
        createMetaData(view, baseMetaData.getCollectionId(), baseMetaData.getLanguage(), docId,
                docTitle, baseMetaData.getDocumentBaseUri() + "/" + docId);
    }

    protected void createMetaData(JCas jCas, String collectionId, String language, String docId,
            String docTitle, String baseUri)
    {
        DocumentMetaData metaData = DocumentMetaData.create(jCas);
        metaData.setCollectionId(collectionId);
        metaData.setLanguage(language);
        metaData.setDocumentBaseUri(baseUri);
        metaData.setDocumentUri(baseUri + "/" + docId);
        metaData.setDocumentTitle(docTitle);
        metaData.setDocumentId(docId);
    }
}
