package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.io.TCReaderMultiLabel;
import de.tudarmstadt.ukp.dkpro.tc.io.TCReaderSingleLabel;

/**
 * Abstract base class for readers used in pair-classification. Please remember that, additionally
 * to the information set in this class, you need to implement one {@link TCReaderSingleLabel} or
 * {@link TCReaderMultiLabel} to set one or more outcome for each instance.
 * 
 * @author Nico Erbs
 * @author zesch
 * @author daxenberger
 * 
 */
public abstract class AbstractPairReader
    extends JCasCollectionReader_ImplBase
{
    public static String INITIAL_VIEW = CAS.NAME_DEFAULT_SOFA;
    public static String PART_ONE = "PART_ONE";
    public static String PART_TWO = "PART_TWO";

    protected abstract String getCollectionId();

    protected abstract String getLanguage();

    protected abstract String getInitialViewText();

    protected abstract String getInitialViewDocId();

    protected abstract String getInitialViewTitle();

    protected abstract String getBaseUri();

    protected abstract String getText(String part);

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        try {
            fillInitialView(
                    jcas,
                    getCollectionId(),
                    getLanguage(),
                    getInitialViewText(),
                    getInitialViewDocId(),
                    getInitialViewTitle(),
                    getBaseUri());

            createView(PART_ONE, jcas, getLanguage(), getText(PART_ONE), getId(PART_ONE),
                    getTitle(PART_ONE));
            createView(PART_TWO, jcas, getLanguage(), getText(PART_TWO), getId(PART_TWO),
                    getTitle(PART_TWO));
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }
    }

    protected void fillInitialView(JCas jCas, String collectionId, String language,
            String documentText, String docId, String docTitle, String baseUri)
        throws CollectionException
    {
        jCas.setDocumentText(documentText);
        jCas.setDocumentLanguage(language);

        createMetaData(jCas, collectionId, language, docId, docTitle, baseUri);
    }

    protected void createView(String part, JCas jCas, String language, String text, String docId,
            String docTitle)
        throws CASException
    {
        JCas view = jCas.createView(part.toString());
        view.setDocumentText(text);
        view.setDocumentLanguage(language);

        DocumentMetaData baseMetaData = DocumentMetaData.get(jCas);
        createMetaData(
                view,
                baseMetaData.getCollectionId(),
                baseMetaData.getLanguage(),
                docId,
                docTitle,
                baseMetaData.getDocumentBaseUri() + "/" + docId);
    }

    protected void createMetaData(JCas jcas, String collectionId, String language, String docId,
            String docTitle, String baseUri)
    {
        DocumentMetaData metaData = DocumentMetaData.create(jcas);
        metaData.setCollectionId(collectionId);
        metaData.setLanguage(language);
        metaData.setDocumentBaseUri(baseUri);
        metaData.setDocumentUri(baseUri + "/" + docId);
        metaData.setDocumentTitle(docTitle);
        metaData.setDocumentId(docId);
    }

    protected String getId(String part)
    {
        return part + "-" + getInitialViewDocId();
    }

    protected String getTitle(String part)
    {
        return part + "-" + getInitialViewTitle();
    }
}
