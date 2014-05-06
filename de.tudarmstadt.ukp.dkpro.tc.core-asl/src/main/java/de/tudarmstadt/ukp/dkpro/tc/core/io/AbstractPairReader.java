package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.base_cpm.BaseCollectionReader;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderMultiLabel;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSingleLabel;

/**
 * Abstract base class for readers used in pair-classification. 
 * We assume pair classification consists of two texts with one or more
 * outcomes for the pair.  Optionally, additional text (such as text common
 * to the pair of texts) can be included as a third text.  The pair of texts,
 * optional third text, and outcomes(s) is the instance.
 * <p>
 * The <code>jcas</code> representing the instance contains two <code>views</code>.  
 * Each text in the pair of texts is set to one of the <code>views</code>.  
 * The optional additional text is set to the original <code>jcas</code> of 
 * the instance.  Information about the instance, such as <code>title</code>
 * and <code>instance id</code>, is set to the original <code>jcas</code>.
 * <p>
 * Basic Implementation:
 * <p>
 * If you have basic pairs of texts with one possible outcome each, 
 * you can create your text pair reader in the following manner:
 * <ul>
 * <li> Create a class that extends this class and implements {@link TCReaderSingleLabel}.
 * <pre>
 * <code>
 * import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
 * import de.tudarmstadt.ukp.dkpro.tc.io.TCReaderSingleLabel;
 * 
 * public class MyReader extends AbstractPairReader implements TCReaderSingleLabel {
 * </code>
 * </pre>
 * <li> Implement the method {@link #getCollectionId()} in your class, so it returns the generic corpus name.
 * <pre>
 * <code>
 * protected String getCollectionId(){
 * 	String collectionId = "MyCorpusName";
 * 	return collectionId; 
 * }
 * 	</code>
 * </pre>
 * <li> Implement {@link #getLanguage()}, returning the language of your corpus.
 * <li> Implement {@link #getInitialViewDocId()}, returning an ID for this instance, 
 * such as "<code>345{@literal &nbsp;}12.csv</code>".  The id may refer to a filename, or other string in which certain 
 * characters neec to be escaped or are not easily human-readable.
 * <li> Implement {@link #getInitialViewTitle()}, returning a human-readable title 
 * for this instance, such as "345part12".
 * <li> Implement {@link #getBaseUri()} if your text pair originates from a single document
 * in one location, to return the absolute location without the filename, 
 * such as "/home/schmidt/MyCorpus/folder3/".  This will be used for recursive reading of 
 * folders and writing the jcas while preserving directory structure.
 * <li> Implement {@link #getText(String)}, conditionally returning either the first or the 
 * second text of your text pair, depending on whether <code>String</code> equals "PART_ONE"
 * or "PART_TWO".
 * <pre>
 * <code>
 * protected String getText(String part) throws IOException{
 * 	if(part.equals("PART_ONE")){
 * 		return "Here is my text 1.";
 * 	}else if(part.equals("PART_TWO")){
 * 		return "Here is my text 2.";
 * 	}
 * 	return null;
 * 	}
 * </code>
 * </pre>
 * <li> Implement {@link de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSingleLabel#TCReaderSingleLabel TCReaderSingleLabel} to return the label for your instance.
 * <pre>
 * <code>
 * {@literal @}Override
 * public String getTextClassificationOutcome(JCas jcas){
 * 	return "positiveInstance";
 * }
 * 	</code>
 * 	</pre>
 * <li> Then implement {@link #getNext(JCas)} to attach the classification outcome to the jcas.
 * <pre>
 * <code>
 * {@literal @}Override
 * public void getNext(JCas jcas)
 * 		throws IOException, CollectionException
 * {
 * 	super.getNext(jcas);
 * 
 * 	TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
 * 	outcome.setOutcome(getTextClassificationOutcome(jcas));
 * 	outcome.addToIndexes();
 * }
 * </code>
 * </pre>
 * <li> Implement {@link BaseCollectionReader#hasNext} to return whether or not there are 
 * more documents to be read from your collection.  Also implement {@link BaseCollectionReader#getProgress}
 * to return the counter of the current document and total number of documents in your collection.
 * <pre>
 * <code>
 * private List<String> myCorpusFilenames;
 * private int currentFile; // start at 0
 * 
 * {@literal @}Override
 * public boolean hasNext(){
 * 	if(currentFile < myCorpusFilenames.size() - 1){
 * 		currentFile++;
 * 		return true;
 * 	}
 * 	return false;
 * }
 * 
 * {@literal @}Override
 * public Progress[] getProgress(){
 * 	return new Progress[] { new ProgressImpl(currentFile, myCorpusFilenames.size(),
 * 			Progress.ENTITIES) }; //i.e., we're on number 6 out of 10 total
 * }
 * 	</code>
 * 	</pre>
 * 
 * </ul>
 * <p>
 * If your text pairs have multiple outcomes each, you should implement {@link TCReaderMultiLabel} 
 * instead of {@link TCReaderSingleLabel}.
 *
 * @author Nico Erbs
 * @author zesch
 * @author daxenberger
 * @author jamison
 *
 */
public abstract class AbstractPairReader
    extends JCasCollectionReader_ImplBase
{
    public static final String INITIAL_VIEW = CAS.NAME_DEFAULT_SOFA;
    public static final String PART_ONE = "PART_ONE";
    public static final String PART_TWO = "PART_TWO";

    /**
     * Generic corpus name.  
     * 
     * Example: "Reuters-21578"
     * 
     * @return corpus
     */
    protected abstract String getCollectionId();

    protected abstract String getLanguage();

    /**
     * This is for text common to both texts of the text pair being classified.  
     * 
     * Example: The original text that the pair texts are extracted from.
     * 
     * @return commonText
     * @throws IOException so user can read in text from a file
     */
    protected abstract String getInitialViewText() throws IOException;

    /**
     * This is for the ID of 
     * {@link #getInitialViewText()}.<p />
     * 
     * The docID, combined with the {@link #getBaseUri() BaseURI} should be unique for each instance.<p />
     * 
     * Note: If each of your data instances doesn't have its own source file,
     * then we recommend just using some instance-unique name here, <b>without</b>
     * any directories or special characters!  Otherwise, DKPro Lab may lose some
     * of your data without loudly notifying you.<p />
     * To check for lost data, look in "Evaluation[...]" file and add up the experiment's
     * "Correctly Classified Examples" and "Incorrectly Classified Examples".
     * 
     * @return iD
     */
    protected abstract String getInitialViewDocId();

    /**
     * This is a human-readable title of 
     * {@link #getInitialViewText()}.
     * 
     * @return title
     */
    protected abstract String getInitialViewTitle();
    
    /**
     * If the pair of texts come from one document, set this to the document's original pathname,
     * without the final filename.  It is used if the CAS is written to file.  The {@link pathname} does not
     * need to actually exist.<p />
     * 
     * The docID, combined with the {@link #getBaseUri() BaseURI} should be unique for each instance.<p />
     * 
     * Note: If each of your data instances doesn't have its own source file,
     * then we recommend using an empty string here, or a very simple directory name.
     * Otherwise, DKPro Lab may lose some of your data without loudly notifying you.<p />
     * To check for lost data, look in "Evaluation[...]" file and add up the experiment's
     * "Correctly Classified Examples" and "Incorrectly Classified Examples".
     * 
     * @return pathname
     */
    protected abstract String getBaseUri();

    /**
     * Sets the text for each of the two views (representing the pair of texts).  Intended to contain a 
     * case/part or if/else statement depending on which of the two views is the argument {@link part}, when overridden.
     * 
     * @param part Either {@link #PART_ONE} or {@link #PART_TWO}
     * @return text
     * @throws IOException
     */
    protected abstract String getText(String part) throws IOException;

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
