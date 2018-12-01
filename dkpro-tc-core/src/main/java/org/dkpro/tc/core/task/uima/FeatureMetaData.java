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
package org.dkpro.tc.core.task.uima;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import static java.nio.charset.StandardCharsets.UTF_8;
public class FeatureMetaData
    implements Constants
{

    private TreeSet<String> featureNames;
    private Map<String, FeatureType> featDesc = new HashMap<>();
    private Map<String, String> enumFeatureName = new HashMap<>();
    private boolean didCollect;

    public FeatureMetaData()
    {
        featureNames = new TreeSet<>();
    }

    public boolean didCollect()
    {
        return this.didCollect;
    }

    public void collectMetaData(List<Instance> instances)
    {
        featureNames = new TreeSet<>();
        for (Feature f : instances.get(0).getFeatures()) {
            featureNames.add(f.getName());

            if (!featDesc.containsKey(f.getName())) {
                featDesc.put(f.getName(), f.getType());
            }

            if (f.getType() == FeatureType.NOMINAL) {
                enumFeatureName.put(f.getName(), f.getValue().getClass().getName());
            }
        }

        didCollect = true;
    }

    private String typeDescriptionToString()
    {
        StringBuilder sb = new StringBuilder();
        List<String> keyList = new ArrayList<String>(featDesc.keySet());
        Collections.sort(keyList);
        for (String k : keyList) {
            FeatureType type = featDesc.get(k);
            sb.append(k + "\t" + type.toString());

            if (type == FeatureType.NOMINAL) {
                sb.append("\t" + enumFeatureName.get(k));
            }

            sb.append("\n");
        }
        return sb.toString();
    }

    public void writeMetaData(File outputDirectory) throws IOException
    {

        FileUtils.writeLines(new File(outputDirectory, FILENAME_FEATURES), UTF_8.toString(),
                featureNames);
        FileUtils.writeStringToFile(
                new File(outputDirectory, FILENAME_FEATURES_DESCRIPTION),
                typeDescriptionToString(), UTF_8);
    }

    public void setFeatureNames(TreeSet<String> featureNames)
    {
        this.featureNames = featureNames;
    }

    public TreeSet<String> getFeatureNames()
    {
        return new TreeSet<>(featureNames);
    }

}