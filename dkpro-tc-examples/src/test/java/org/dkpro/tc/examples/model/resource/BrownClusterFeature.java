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
package org.dkpro.tc.examples.model.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class BrownClusterFeature
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    private static final String NOT_SET = "*";

    public static final String PARAM_BROWN_CLUSTERS_LOCATION = "brownClusterLocations";
    @ConfigurationParameter(name = PARAM_BROWN_CLUSTERS_LOCATION, mandatory = true)
    private File inputFile;

    private HashMap<String, String> map = null;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            init();
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    public Set<Feature> extract(JCas aJcas, TextClassificationTarget aClassificationUnit)
        throws TextClassificationException
    {
        String unit = aClassificationUnit.getCoveredText().toLowerCase();
        Set<Feature> features = createFeatures(unit);

        return features;
    }

    private Set<Feature> createFeatures(String unit)
    {
        Set<Feature> features = new HashSet<Feature>();

        String bitCode = map.get(unit);

        features.add(getFeature(bitCode, 16));
        features.add(getFeature(bitCode, 14));
        features.add(getFeature(bitCode, 12));
        features.add(getFeature(bitCode, 10));
        features.add(getFeature(bitCode, 8));
        features.add(getFeature(bitCode, 6));
        features.add(getFeature(bitCode, 4));
        features.add(getFeature(bitCode, 2));

        return features;
    }

    private Feature getFeature(String bitCode, int i)
    {
        if(bitCode == null || bitCode.isEmpty()){
            return new Feature("brown_" + i , NOT_SET, true);
        }
        
        String value = bitCode.length() >= i ? bitCode.substring(0, i) : NOT_SET; 
        return new Feature("brown_" + i , value, value.equals(NOT_SET));
    }

    private void init()
        throws TextClassificationException
    {

        if (map != null) {
            return;
        }
        map = new HashMap<String, String>();

        try {

            BufferedReader bf = openFile();
            String line = null;
            while ((line = bf.readLine()) != null) {
                String[] split = line.split("\t");
                map.put(split[1], split[0]);
            }

        }
        catch (Exception e) {
            throw new TextClassificationException(e);
        }
    }

    private BufferedReader openFile()
        throws Exception
    {
        InputStreamReader isr = null;
        if (inputFile.getAbsolutePath().endsWith(".gz")) {

            isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile)),
                    "UTF-8");
        }
        else {
            isr = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
        }
        return new BufferedReader(isr);
    }

}
