/**
 * Copyright 2019
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
package org.dkpro.tc.examples.shallow.serialization.crfsuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class BrownFeat
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

    public Set<Feature> extract(JCas aJcas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        String unit = aTarget.getCoveredText().toLowerCase();
        Set<Feature> features = createFeatures(unit);

        someOperationsOnInnerClassUsingAnonymousClasses();

        return features;
    }

    /*
     * part of a test case that ensures that the inner and anonymous classes are loaded properly -
     * irrelevant for productive use
     */
    private void someOperationsOnInnerClassUsingAnonymousClasses()
    {
        List<AInner> l = new ArrayList<>();
        l.add(new AInner());
        l.add(new AInner());
        l.add(new AInner());

        // This will create an anonymous inner class xxxx$1
        Collections.sort(l,
                new Comparator<AInner>()
                {

                    @Override
                    public int compare(
                            AInner o1,
                            AInner o2)
                    {
                        if (o1.a > o2.a) {
                            return 1;
                        }
                        else if (o1.a < o2.a) {
                            return -1;
                        }

                        return 0;
                    }

                });

        // This will create an anonymous inner class xxxx$2
        Collections.sort(l,
                new Comparator<AInner>()
                {

                    @Override
                    public int compare(
                            AInner o1,
                            AInner o2)
                    {
                        if (o1.a < o2.a) {
                            return 1;
                        }
                        else if (o1.a > o2.a) {
                            return -1;
                        }

                        return 0;
                    }

                });

    }

    private Set<Feature> createFeatures(String unit) throws TextClassificationException
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

    private Feature getFeature(String bitCode, int i) throws TextClassificationException
    {
        if (bitCode == null || bitCode.isEmpty()) {
            return new Feature("brown_" + i, NOT_SET, true, FeatureType.STRING);
        }

        String value = bitCode.length() >= i ? bitCode.substring(0, i) : NOT_SET;
        return new Feature("brown_" + i, value, value.equals(NOT_SET), FeatureType.STRING);
    }

    private void init() throws TextClassificationException
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

    private BufferedReader openFile() throws Exception
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

    class AInner
    {
        int a;
        int few;
        int variables;
        private B x;

        AInner()
        {
            this.a = new Random().nextInt();
            this.few = 23;
            this.variables = 2;
            this.x = new B();

            List<B> o = new ArrayList<>();
            o.add(x);
            o.add(new B());
            o.add(new B());
            o.add(new B());

            Collections.sort(o, new Comparator<B>()
            {

                @Override
                public int compare(B o1, B o2)
                {
                    if (o1.y > o2.y) {
                        return -1;
                    }
                    else if (o1.y < o2.y) {
                        return 1;
                    }
                    return 0;
                }
            });

        }

        class B
        {
            int y = 0;

            public B()
            {
                y = new Random().nextInt();
            }
        }

    }

}
