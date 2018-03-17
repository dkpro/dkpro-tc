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
package org.dkpro.tc.features.ngram.base;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;

/**
 * Ratio of the number of tokens in a document with respect to the longest document in the training
 * data
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public abstract class MaximumNormalizationExtractorBase
    extends LuceneFeatureExtractorBase
    implements FeatureExtractor
{

    protected double getRatio(double actual, long maximum) throws TextClassificationException
    {

        double value = actual / maximum;

        if (value > 1.0) {
            // a larger value that during training was encountered; cap to 1.0
            value = 1.0;
        }

        if (value < 0) {
            throw new TextClassificationException("Negative sentence length encountered");
        }

        return value;
    }

    protected long getMax() throws TextClassificationException
    {

        String string = "-1";
        try {
            string = getTopNgrams().getSampleWithMaxFreq().split("_")[0];
        }
        catch (ResourceInitializationException e) {
            throw new TextClassificationException(e);
        }
        return Long.parseLong(string);
    }

    @Override
    protected int getTopN()
    {
        return 1;
    }

    @Override
    protected void logSelectionProcess(long N)
    {
        // no log message for this feature
    }
}
