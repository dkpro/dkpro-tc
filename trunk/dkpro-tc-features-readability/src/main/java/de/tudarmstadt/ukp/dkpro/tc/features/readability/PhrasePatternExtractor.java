/*******************************************************************************
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
 ******************************************************************************/

package de.tudarmstadt.ukp.dkpro.tc.features.readability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.ADJC;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.NC;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.PC;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.VC;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

public class PhrasePatternExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    /**
     * This Extractor is inspired by the ParsePatternExtractor but relies only on chunking
     * information.
     * 
     * @author beinborn
     */

    public static final String NCS_PER_SENTENCE = "NCsPerSentence";
    public static final String VCS_PER_SENTENCE = "VCsPerSentence";
    public static final String ADVCS_PER_SENTENCE = "ADVCsPerSentence";
    public static final String SBARS_PER_SENTENCE = "SBarsPerSentence";
    public static final String PCS_PER_SENTENCE = "PCsPerSentence";
    public static final String CHUNKS_PER_SENTENCE = "ChunksPerSentence";

    public List<Feature> extract(JCas jcas)

    {

        int chunkSum = 0;
        double nrOfSentences = 0.0;

        int ncSum = 0;
        int vcSum = 0;
        int advcSum = 0;
        int pcSum = 0;
        int sbarSum = 0;

        Collection<Sentence> sents = JCasUtil.select(jcas, Sentence.class);
        nrOfSentences = sents.size() * 1.0;
        List<Feature> featList = new ArrayList<Feature>();

        for (Sentence s : sents) {
            int nrOfNCs = 0;
            int nrOfVCs = 0;
            int nrOfADVCs = 0;
            int nrOfPCs = 0;
            int nrOfSBARs = 0;

            Collection<Chunk> chunks = JCasUtil.selectCovered(Chunk.class, s);
            for (Chunk c : chunks) {

                if (c instanceof NC) {
                    nrOfNCs++;
                }
                if (c instanceof VC) {
                    nrOfVCs++;
                }
                if (c instanceof PC) {

                    nrOfPCs++;
                }
                if (c instanceof ADJC) {
                    nrOfADVCs++;
                }
                // SBARS has chunkType 0, same as punctuation
                // we do not want to collect punctuation chunks
                if (c.getChunkValue().startsWith("SBAR")) {
                    nrOfSBARs++;
                }

            }

            ncSum += nrOfNCs;
            vcSum += nrOfVCs;
            pcSum += nrOfPCs;
            advcSum += nrOfADVCs;
            sbarSum += nrOfSBARs;
            chunkSum += chunks.size();
        }

        featList.addAll(Arrays.asList(new Feature(NCS_PER_SENTENCE, ncSum / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(VCS_PER_SENTENCE, vcSum / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(PCS_PER_SENTENCE, pcSum / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(ADVCS_PER_SENTENCE, advcSum / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(SBARS_PER_SENTENCE, sbarSum / nrOfSentences)));
        featList.addAll(Arrays.asList(new Feature(CHUNKS_PER_SENTENCE, chunkSum / nrOfSentences)));
        return featList;
    }
}