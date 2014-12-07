/*
 * Copyright 2014
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
 */
package de.tudarmstadt.ukp.dkpro.tc.svmhmm;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.util.List;

/**
 * Reads the Brown corpus and sets POS tags for each tokens as classification outcome.
 */
public class BrownCorpusReader
    extends TeiReader
    implements TCReaderSequence
{
    
    /**
     * Whether coarse-grained or fine-grained POS tags should be used.
     */
    public static final String PARAM_USE_COARSE_GRAINED = "useCoarseGrained";
    @ConfigurationParameter(name = PARAM_USE_COARSE_GRAINED, mandatory = true, defaultValue="false")
    protected boolean useCoarseGrained;

    @Override
    public void getNext(CAS cas)
        throws IOException, CollectionException
    {
        super.getNext(cas);
        
        JCas jcas;
        try {
            jcas = cas.getJCas();
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }
        
        for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
            TextClassificationSequence sequence = new TextClassificationSequence(jcas, sentence.getBegin(), sentence.getEnd());
            sequence.addToIndexes();
            
            for (Token token : JCasUtil.selectCovered(jcas, Token.class, sentence)) {
                TextClassificationUnit unit = new TextClassificationUnit(jcas, token.getBegin(), token.getEnd());
                unit.addToIndexes();
                
                TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, token.getBegin(), token.getEnd());
                outcome.setOutcome(getTextClassificationOutcome(jcas, unit));
                outcome.addToIndexes();
            }
        }
    }
    
    @Override
    public String getTextClassificationOutcome(JCas jcas, TextClassificationUnit unit)
        throws CollectionException
    {
        List<POS> posList = JCasUtil.selectCovered(jcas, POS.class, unit);
        if (posList.size() != 1) {
            throw new CollectionException(new Throwable("Could not get unique POS annotation to be used as TC outome."));
        }
        
        String outcome;
        if (useCoarseGrained) {
            outcome = posList.get(0).getClass().getSimpleName();
        }
        else {
            outcome = posList.get(0).getPosValue();
        }
        return outcome;
    }
}