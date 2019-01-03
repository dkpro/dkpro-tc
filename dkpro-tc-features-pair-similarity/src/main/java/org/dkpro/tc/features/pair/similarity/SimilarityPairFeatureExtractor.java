/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.features.pair.similarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.PairFeatureExtractor;
import org.dkpro.similarity.algorithms.api.JCasTextSimilarityMeasure;
import org.dkpro.similarity.algorithms.api.SimilarityException;
import org.dkpro.similarity.uima.resource.TextSimilarityResourceBase;

public class SimilarityPairFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements PairFeatureExtractor
{
    /**
     * Feature path specifying the document segments (tokens, lemmas, sentences) that the measure
     * should use for computing similarity
     */
    public static final String PARAM_SEGMENT_FEATURE_PATH = "segmentFeaturePath";
    @ConfigurationParameter(name = PARAM_SEGMENT_FEATURE_PATH, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private String segmentFeaturePath;

    /**
     * Text similarity measure to be used.
     */
    public static final String PARAM_TEXT_SIMILARITY_RESOURCE = "textSimilarityResource";
    @ExternalResource(key = PARAM_TEXT_SIMILARITY_RESOURCE, mandatory = true)
    private TextSimilarityResourceBase textSimilarityResource;

    @Override
    public Set<Feature> extract(JCas view1, JCas view2) throws TextClassificationException
    {
        try {
            double similarity;
            switch (textSimilarityResource.getMode()) {
            case text:
                similarity = textSimilarityResource.getSimilarity(view1.getDocumentText(),
                        view2.getDocumentText());
                break;
            case jcas:
                similarity = ((JCasTextSimilarityMeasure) textSimilarityResource)
                        .getSimilarity(view1, view2);
                break;
            default:
                List<String> f1 = getItems(view1);
                List<String> f2 = getItems(view2);

                // Remove "_" tokens
                for (int i = f1.size() - 1; i >= 0; i--) {
                    if (f1.get(i) == null || f1.get(i).equals("_")) {
                        f1.remove(i);
                    }
                }
                for (int i = f2.size() - 1; i >= 0; i--) {
                    if (f2.get(i) == null || f2.get(i).equals("_")) {
                        f2.remove(i);
                    }
                }

                similarity = textSimilarityResource.getSimilarity(f1, f2);
            }

            return new Feature("Similarity" + textSimilarityResource.getName(), similarity,
                    FeatureType.NUMERIC).asSet();
        }
        catch (FeaturePathException e) {
            throw new TextClassificationException(e);
        }
        catch (SimilarityException e) {
            throw new TextClassificationException(e);
        }

    }

    private List<String> getItems(JCas view) throws FeaturePathException
    {
        List<String> items = new ArrayList<String>();

        for (Map.Entry<AnnotationFS, String> entry : FeaturePathFactory.select(view.getCas(),
                segmentFeaturePath)) {
            items.add(entry.getValue());
        }

        return items;
    }
}