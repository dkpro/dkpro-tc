/**
 * Copyright 2016
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
package org.dkpro.tc.weka.writer;

import java.io.File;

import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.weka.WekaClassificationAdapter;
import org.dkpro.tc.weka.util.WekaUtils;

/**
 * {@link DataWriter} for the Meka machine learning tool.
 */
public class MekaDataWriter
    implements DataWriter, Constants
{

    @Override
    public void write(File outputDirectory, FeatureStore featureStore, boolean useDenseInstances,
            String learningMode, boolean applyWeighting)
        throws Exception
    {
        WekaUtils.instanceListToArffFileMultiLabel(new File(outputDirectory,
        		WekaClassificationAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.featureVectorsFile)),
                featureStore, useDenseInstances, applyWeighting);
    }
}
