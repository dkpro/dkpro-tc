package de.tudarmstadt.ukp.dkpro.tc.core.extractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.CasCopier;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

/**
 * This JCasMultiplier creates a new JCas for each TextClassificationUnit annotation in the original
 * JCas. The newly created JCas contains one TextClassificationUnit annotation from the original
 * JCas, all TextClassificationOutcomes covered by this TextClassificationUnit and all other
 * annotations types from the original JCas.
 * 
 * @author a_vovk
 * 
 */
public class ClassificationUnitCasMultiplier
    extends JCasMultiplier_ImplBase
{

    private final static String UNIT_ID_PREFIX = "_unit_";

    // For each TextClassificationUnit stored in this collection one corresponding JCas is created.
    private Collection<TextClassificationUnit> annotations;
    private Iterator<TextClassificationUnit> iterator;

    private JCas jCas;

    private int counter;

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
        TextClassificationUnit classificationUnit = this.iterator.next();
        counter++;
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

        // Set new ids for copied cases.
        DocumentMetaData.get(copyJCas).setDocumentId(
                DocumentMetaData.get(jCas).getDocumentId() + UNIT_ID_PREFIX + counter);

        // Remove other TextClassificationUnit annotations except current (since one CAS should have
        // only have one classification unit).
        removeAnnotationsExceptGiven(copyJCas,
                JCasUtil.selectCovered(copyJCas, TextClassificationUnit.class, classificationUnit));
        Collection<TextClassificationOutcome> outcomes = JCasUtil.selectCovered(copyJCas,
                TextClassificationOutcome.class, classificationUnit);
        // Remove TextClassificationOutcomes which are not covered by current
        // TextClassificationUnit.
        removeAnnotationsExceptGiven(copyJCas, outcomes);
        getLogger().info("Creating CAS " + counter + " from " + annotations.size());

        return copyJCas;
    }

    /**
     * Remove all annotations of the type provided in the Collection except the annotations stored
     * in the collection.
     * 
     * @param jCas
     *            for annotation removal.
     * @param annotations
     *            which should be kept in the JCas.
     */
    private void removeAnnotationsExceptGiven(JCas jCas,
            Collection<? extends Annotation> annotations)
    {
        if (annotations.isEmpty()) {
            throw new IllegalArgumentException("Annotation list should not be empty!");
        }

        Collection<? extends Annotation> foundAnnotations = JCasUtil.select(jCas, annotations
                .iterator().next().getClass());
        List<Annotation> toDelete = new ArrayList<Annotation>();
        for (Annotation annotation : foundAnnotations) {
            if (!annotations.contains(annotation)) {
                toDelete.add(annotation);
            }
        }
        for (Annotation a : toDelete) {
            a.removeFromIndexes();
        }
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        this.jCas = aJCas;
        this.counter = 0;
        this.annotations = JCasUtil.select(aJCas, TextClassificationUnit.class);
        this.iterator = annotations.iterator();
    }
}
