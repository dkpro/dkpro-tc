package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasCopier;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationFocus;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

/**
 * This JCasMultiplier creates a new JCas for each {@link TextClassificationUnit} annotation in the original JCas.
 * The newly created JCas contains one {@link TextClassificationFocus} annotation that shows with TextClassificationUnit should be classified.
 * All annotations in the original JCas are copied to the new one.
 * 
 * @author Artem Vovk
 * @author zesch
 * 
 */
public class ClassificationUnitCasMultiplier
    extends JCasMultiplier_ImplBase
{

    public static final String PARAM_USE_SEQUENCES = "useSequences";
    @ConfigurationParameter(name = PARAM_USE_SEQUENCES, mandatory = true, defaultValue="false")
    private boolean useSequences;
    
    private final static String UNIT_ID_PREFIX = "_unit_";

    // For each TextClassificationUnit stored in this collection one corresponding JCas is created.
    private Collection<? extends AnnotationFS> annotations;
    private Iterator<? extends AnnotationFS> iterator;

    private JCas jCas;

    private int counter;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        this.jCas = aJCas;
        this.counter = 0;
        
        if (useSequences) {
            this.annotations = JCasUtil.select(aJCas, TextClassificationSequence.class);
        }
        else {
            this.annotations = JCasUtil.select(aJCas, TextClassificationUnit.class);
        }
        this.iterator = annotations.iterator();
    }
    
    @Override
    public boolean hasNext()
        throws AnalysisEngineProcessException
    {
        if (!iterator.hasNext()) {
            this.jCas = null;
            this.iterator = null;
            this.annotations = null;
            return false;
        }
        return true;
    }

    @Override
    public AbstractCas next()
        throws AnalysisEngineProcessException
    {        
        // Create an empty CAS as a destination for a copy.
        JCas emptyJCas = this.getEmptyJCas();
        DocumentMetaData.create(emptyJCas);
        emptyJCas.setDocumentText(this.jCas.getDocumentText());
        CAS emptyCas = emptyJCas.getCas();

        // Copy current CAS to the empty CAS.
        CasCopier.copyCas(jCas.getCas(), emptyCas, false);
        JCas copyJCas;
        try {
            copyJCas = emptyCas.getJCas();
        }
        catch (CASException e) {
            throw new AnalysisEngineProcessException("Exception while creating JCas", null, e);
        }

        // Set new ids and URIs for copied cases.
        DocumentMetaData.get(copyJCas).setDocumentId(
                DocumentMetaData.get(jCas).getDocumentId() + UNIT_ID_PREFIX + counter);
        DocumentMetaData.get(copyJCas).setDocumentUri(
                DocumentMetaData.get(jCas).getDocumentUri() + UNIT_ID_PREFIX + counter);

        // set the focus annotation
        AnnotationFS focusUnit = this.iterator.next();
        TextClassificationFocus focus = new TextClassificationFocus(copyJCas, focusUnit.getBegin(), focusUnit.getEnd());  
        focus.addToIndexes();    
        
        counter++;
        getLogger().debug("Creating CAS " + counter + " of " + annotations.size());

        return copyJCas;
    }
}