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
package org.dkpro.tc.groovyexamples.single.unit;

import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.TypeCapability;

import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.features.length.NrOfTokens;

/**
 * Extracts the number of tokens in the classification unit
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class NrOfTokensExternalResourceUFE
    extends NrOfTokens
    implements FeatureExtractor
{
    /**
     * A dummy resource which does not do anything.
     */
    public final static String PARAM_DUMMY_RESOURCE = "DummyResource";
    @ExternalResource(key = PARAM_DUMMY_RESOURCE)
    protected DummyResource dummy;
}
