/*******************************************************************************
 * Copyright 2016
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
import org.dkpro.tc.api.type.TextClassificationFocus;
import org.dkpro.tc.api.type.TextClassificationUnit;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * This JCasMultiplier creates a new JCas for each {@link TextClassificationUnit} annotation in the
 * original JCas. The newly created JCas contains one {@link TextClassificationFocus} annotation
 * that shows with TextClassificationUnit should be classified. All annotations in the original JCas
 * are copied to the new one.
 */
public class FoldClassificationUnitCasMultiplier
    extends JCasMultiplier_ImplBase
{

    public static final String PARAM_REQUESTED_SPLITS = "numSplitsReq";
    @ConfigurationParameter(name = PARAM_REQUESTED_SPLITS, mandatory = true)
    private int numReqSplits;

    // For each TextClassificationUnit stored in this collection one corresponding JCas is created.
    private Collection<? extends AnnotationFS> annotations;
    private Iterator<? extends AnnotationFS> iterator;

    private JCas jCas;

    private int subCASCounter;
    private Integer unitCounter;

    private List<AnnotationFS> unitBuf = new ArrayList<AnnotationFS>();

    int totalNum = 0;
    int unitsPerCas = 0;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        jCas = aJCas;
        subCASCounter = 0;
        unitCounter = 0;

        annotations = JCasUtil.select(aJCas, TextClassificationUnit.class);

        Iterator<? extends AnnotationFS> all = annotations.iterator();
        while (all.hasNext()) {
            all.next();
            totalNum++;
        }
        unitsPerCas = (int) (totalNum / (double) numReqSplits);
        isUnitsGreaterZero();

        iterator = annotations.iterator();

        if (!iterator.hasNext())
            throw new AnalysisEngineProcessException(new RuntimeException(
                    "No annotations found in CAS for Units or Sequences."));
    }

    private void isUnitsGreaterZero()
    {
        if (unitsPerCas <= 0) {
            throw new IllegalStateException(
                    "The number of TextClassificationUnits per CAS would have been lower than 0 units - total number units found ["
                            + totalNum + "] number of folds requested [" + numReqSplits + "]");
        }
    }

    @Override
    public boolean hasNext()
        throws AnalysisEngineProcessException
    {
        unitBuf = new ArrayList<AnnotationFS>();

        for (int i = 0; i < unitsPerCas && iterator.hasNext(); i++) {
            AnnotationFS next = iterator.next();
            unitBuf.add(next);
        }

        return !unitBuf.isEmpty();
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

        // delete all text classification units
        for (TextClassificationUnit u : JCasUtil.select(copyJCas, TextClassificationUnit.class)) {
            u.removeFromIndexes();
        }

        for (AnnotationFS a : unitBuf) {
            TextClassificationUnit unit = new TextClassificationUnit(copyJCas, a.getBegin(),
                    a.getEnd());
            unit.addToIndexes();
            unit.setId(unitCounter);
            unitCounter++;
        }

        subCASCounter++;

        // issue #261
        DocumentMetaData.get(copyJCas).setIsLastSegment(subCASCounter == annotations.size());

        getLogger().debug("Creating CAS " + subCASCounter + " of " + annotations.size());

        return copyJCas;
    }
}