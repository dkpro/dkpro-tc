/**
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.examples.shallow.feature;

import java.util.Collection;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureCollection;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Extracts the number of sentences in this classification unit
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class LengthFeatureNominal
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    public static final String FEATURE_NAME = "NominalLengthFeature";

    @Override
    public FeatureCollection extract(JCas jcas, TextClassificationTarget classificationUnit)
        throws TextClassificationException
    {
    		FeatureCollection featureSet = new FeatureCollection();

		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		if (tokens.size() > 150) {
			featureSet.add(new Feature(FEATURE_NAME, LengthEnum.LONG, FeatureType.NOMINAL));
		} else if (tokens.size() > 100) {
			featureSet.add(new Feature(FEATURE_NAME, LengthEnum.MIDDLE, FeatureType.NOMINAL));
		} else {
			featureSet.add(new Feature(FEATURE_NAME, LengthEnum.SHORT, FeatureType.NOMINAL));
		}
        
        return featureSet;
    }
}
