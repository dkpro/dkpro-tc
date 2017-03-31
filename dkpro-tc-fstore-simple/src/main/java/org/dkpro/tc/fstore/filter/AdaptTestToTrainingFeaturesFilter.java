/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.fstore.filter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;

import com.google.gson.Gson;

public class AdaptTestToTrainingFeaturesFilter
    implements FeatureFilter
{

    private TreeSet<String> trainingFeatureNames;

    public void setFeatureNames(TreeSet<String> trainingFeatureNames)
    {
        this.trainingFeatureNames = trainingFeatureNames;
    }

    @Override
    public boolean isApplicableForTraining()
    {
        return false;
    }

    @Override
    public boolean isApplicableForTesting()
    {
        return true;
    }

    @Override
    public void applyFilter(File f)
        throws Exception
    {
        Iterator<String> iterator = new JsonInstanceIterator(f);

        File tmpOut = new File(f.getParentFile(), "json_filtered.txt");
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tmpOut), "utf-8"));

        Gson gson = new Gson();
        while (iterator.hasNext()) {
            String next = iterator.next();
            Instance i = gson.fromJson(next, Instance.class);
            List<Feature> newFeatures = new ArrayList<>();
            for (Feature feat : i.getFeatures()) {
                if (!trainingFeatureNames.contains(feat.getName())) {
                    LogFactory.getLog(getClass()).debug("Feature name [" + feat.getName()
                            + "] did not occur during training and will be dropped");
                    continue;
                }
                newFeatures.add(feat);
            }
            i.setFeatures(newFeatures);
            writer.write(gson.toJson(i) + System.lineSeparator());
        }

        writer.close();
        FileUtils.copyFile(tmpOut, f);
        FileUtils.deleteQuietly(tmpOut);
    }
}