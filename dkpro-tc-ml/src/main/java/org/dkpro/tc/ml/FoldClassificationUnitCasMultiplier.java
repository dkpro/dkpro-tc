/*******************************************************************************
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.ml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
import org.dkpro.tc.api.type.JCasId;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/*
 * This JCasMultiplier creates a new JCas for each TextClassificationUnit or
 * TextClassificationSequence annotation in the original JCas. All other
 * TextClassificationUnit and TextClassificationUnitSequence are removed.
 */
public class FoldClassificationUnitCasMultiplier
    extends JCasMultiplier_ImplBase
{

    public static final String PARAM_USE_SEQUENCES = "useSequences";
    @ConfigurationParameter(name = PARAM_USE_SEQUENCES, mandatory = true, defaultValue = "false")
    private boolean useSequences;

    public static final String PARAM_REQUESTED_SPLITS = "numSplitsReq";
    @ConfigurationParameter(name = PARAM_REQUESTED_SPLITS, mandatory = true)
    private int numReqSplits;

    // For each TextClassificationUnit stored in this collection one corresponding JCas is created.
    private Collection<? extends AnnotationFS> annotations;
    private Iterator<? extends AnnotationFS> iterator;

    private JCas jCas;

    private int subCASCounter;
    private Integer unitCounter;
    private Integer seqCounter;

    private List<AnnotationFS> buf = new ArrayList<AnnotationFS>();
    private List<TextClassificationTarget> seqModeUnitsCoveredBySequenceAnno = new ArrayList<>();

    int totalNum = 0;
    int annosPerCas = 0;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        jCas = aJCas;
//        subCASCounter = 0; //do not reinitialize
        unitCounter = 0;
        seqCounter = 0;
        totalNum = 0;
        seqModeUnitsCoveredBySequenceAnno = new ArrayList<>();

        if (useSequences) {
            annotations = JCasUtil.select(aJCas, TextClassificationSequence.class);
        }
        else {
            annotations = JCasUtil.select(aJCas, TextClassificationTarget.class);
        }

        Iterator<? extends AnnotationFS> all = annotations.iterator();
        while (all.hasNext()) {
            all.next();
            totalNum++;
        }
        annosPerCas = (int) (totalNum / (double) numReqSplits);
        if (annosPerCas <= 0) {
            annosPerCas = totalNum;
        }
        isUnitsGreaterZero();

        iterator = annotations.iterator();

        if (!iterator.hasNext())
            throw new AnalysisEngineProcessException(new RuntimeException(
                    "No annotations found in CAS for Units or Sequences."));
    }

    private void isUnitsGreaterZero()
    {
        String anno = TextClassificationTarget.class.getSimpleName();
        if (useSequences) {
            anno = TextClassificationSequence.class.getSimpleName();
        }

        if (annosPerCas <= 0) {
            throw new IllegalStateException("The number of " + anno
                    + " per CAS would have been lower than 0 units - total number units found ["
                    + totalNum + "] number of folds requested [" + numReqSplits + "]");
        }
    }

    @Override
    public boolean hasNext()
        throws AnalysisEngineProcessException
    {
        buf = new ArrayList<AnnotationFS>();

        for (int i = 0; i < annosPerCas && iterator.hasNext(); i++) {
            AnnotationFS next = iterator.next();
            buf.add(next);
        }

        return !buf.isEmpty();
    }

    @Override
    public AbstractCas next()
        throws AnalysisEngineProcessException
    {
        // Create an empty CAS as a destination for a copy.
        JCas emptyJCas = this.getEmptyJCas();
        DocumentMetaData.create(emptyJCas);
        emptyJCas.setDocumentText(jCas.getDocumentText());
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

        // Check for multiple DocumentMetaData annotations (issue #266)
        Collection<DocumentMetaData> metaDataAnnotations = JCasUtil.select(copyJCas,
                DocumentMetaData.class);
        List<DocumentMetaData> metaDataAnnotationsToDelete = new ArrayList<>();

        if (metaDataAnnotations.size() > 1)
            for (DocumentMetaData metaDataAnnotation : metaDataAnnotations)
                if ("x-unspecified".equals(metaDataAnnotation.getLanguage())
                        && metaDataAnnotation.getDocumentTitle() == null
                        && metaDataAnnotation.getDocumentId() == null
                        && metaDataAnnotation.getDocumentUri() == null
                        && metaDataAnnotation.getDocumentBaseUri() == null
                        && metaDataAnnotation.getCollectionId() == null)
                    metaDataAnnotationsToDelete.add(metaDataAnnotation);

        for (DocumentMetaData metaDataAnnotation : metaDataAnnotationsToDelete)
            copyJCas.removeFsFromIndexes(metaDataAnnotation);

        // Set new ids and URIs for copied cases.
        // The counting variable keeps track of how many new CAS objects are created from the
        // original CAS, a CAS relative counter.
        // NOTE: As it may cause confusion: If in sequence classification several or all CAS
        // contains only a single sequence this counter would be zero in all cases - this is not a
        // bug, but a cosmetic flaw
        String currentDocId = DocumentMetaData.get(jCas).getDocumentId();
        DocumentMetaData.get(copyJCas).setDocumentId(currentDocId + "_" + subCASCounter);
        String currentDocUri = DocumentMetaData.get(jCas).getDocumentUri() + "_" + subCASCounter;
        DocumentMetaData.get(copyJCas).setDocumentUri(currentDocUri);

        deleteAllTextClassificationAnnotation(copyJCas);
        setTargetAnnotation(copyJCas);

        assignNewId(copyJCas);
        
        subCASCounter++;

        // issue #261
        DocumentMetaData.get(copyJCas).setIsLastSegment(subCASCounter == annotations.size());

        getLogger().debug("Creating CAS " + subCASCounter + " of " + annotations.size());

        return copyJCas;
    }

    private void assignNewId(JCas copyJCas)
    {
        JCasId jcasId = JCasUtil.selectSingle(copyJCas, JCasId.class);
        jcasId.setId(subCASCounter);
    }

    private void setTargetAnnotation(JCas copyJCas)
    {
        if (useSequences) {
            for (AnnotationFS s : buf) {
                TextClassificationSequence seq = new TextClassificationSequence(copyJCas,
                        s.getBegin(), s.getEnd());
                seq.addToIndexes();
                seq.setId(seqCounter++);

                // re-add the units that are covered by those sequences
                for (TextClassificationTarget u : seqModeUnitsCoveredBySequenceAnno) {
                    u.addToIndexes();
                }
                seqModeUnitsCoveredBySequenceAnno = new ArrayList<>();
            }
        }
        else {
            for (AnnotationFS u : buf) {
                TextClassificationTarget unit = new TextClassificationTarget(copyJCas, u.getBegin(),
                        u.getEnd());
                unit.addToIndexes();
                unit.setId(unitCounter);
                unitCounter++;
            }
        }
    }

    private void deleteAllTextClassificationAnnotation(JCas copyJCas)
    {
        if (useSequences) {
            // record units covered by sequence
            for (AnnotationFS seq : buf) {
                seqModeUnitsCoveredBySequenceAnno.addAll(JCasUtil.selectCovered(copyJCas,
                        TextClassificationTarget.class, seq.getBegin(), seq.getEnd()));
            }
            for (TextClassificationSequence s : JCasUtil.select(copyJCas,
                    TextClassificationSequence.class)) {
                s.removeFromIndexes();
            }
            for (TextClassificationTarget u : JCasUtil.select(copyJCas, TextClassificationTarget.class)) {
                u.removeFromIndexes();
            }
        }
        else {
            for (TextClassificationTarget u : JCasUtil.select(copyJCas, TextClassificationTarget.class)) {
                u.removeFromIndexes();
            }
        }
    }
}