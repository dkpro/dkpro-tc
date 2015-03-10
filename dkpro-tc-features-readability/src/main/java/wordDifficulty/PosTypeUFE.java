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

package wordDifficulty;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class PosTypeUFE

    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    /**
     * @author beinborn
     * 
     *         This feature extractor checks the pos tag of the word. As not all ml learning
     *         algorithms can use enumerations, we create a boolean feature for each pos type.
     */
    public List<Feature> extract(JCas jcas, TextClassificationUnit unit)

    {

        List<Feature> featList = new ArrayList<Feature>();
        String pos = JCasUtil.selectCovered(jcas, POS.class, unit).get(0).getType().getShortName();

        // main word classes
        String[] postags = { "ADJ", "ADV", "ART", "CONJ", "NN", "NP", "PP", "PR", "V" };

        for (String candidatePos : postags) {
            // boolean feature for each pos type
            String featureName = "Is" + candidatePos;
            featList.add(new Feature(featureName, pos.equals(candidatePos)));
        }

        return featList;
    }
}