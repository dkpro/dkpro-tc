/**
 * Copyright 2017
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
package org.dkpro.tc.examples.single.sequence.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.feature.filter.FeatureFilter;

import com.google.gson.Gson;

/*
 *  Demonstrates how feature filtering might work. The filter removes all ''character ngrams'' from the feature set
 *   - intended as demonstration - in practical setups you would simply remove the character ngram feature 
 */
public class FilterLuceneCharacterNgramStartingWithLetter
    implements FeatureFilter
{

    @Override
    public void applyFilter(File inputFeatureFile)
        throws Exception
    {
        Gson gson = new Gson();

        // iterating over a stream is for large data more reasonable that bulk-read of all data
        List<String> outputLines = new ArrayList<>();

        List<String> inputLines = FileUtils.readLines(inputFeatureFile, "utf-8");
        for (String l : inputLines) {
            // de-serialize
            Instance inst = gson.fromJson(l, Instance.class);

            // collect features starting with a t-letter
            List<Feature> features = new ArrayList<>(inst.getFeatures());
            List<Feature> deletionTargets = new ArrayList<>();
            for (Feature f : features) {
                if (f.getName().startsWith("charngram")) {
                    deletionTargets.add(f);
                }
            }
            // remove those features
            for (Feature f : deletionTargets) {
                features.remove(f);
            }

            // update instances
            inst.setFeatures(features);

            // re-serialize
            outputLines.add(gson.toJson(inst));
        }

        // Write new file to temporary location
        File tmp = File.createTempFile("tmpFeatureFile", "tmp");
        FileUtils.writeLines(tmp, "utf-8", outputLines);

        // overwrite input file with new file
        FileUtils.copyFile(tmp, inputFeatureFile);
        tmp.delete();
    }

    @Override
    public boolean isApplicableForTraining()
    {
        return true;
    }

    @Override
    public boolean isApplicableForTesting()
    {
        return true;
    }

}
