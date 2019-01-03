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
package org.dkpro.tc.testing;

import java.util.Set;

import org.dkpro.tc.api.features.Feature;
import org.junit.Assert;

/**
 * Utils for testing feature extractors
 */
public class FeatureTestUtil
{

    /**
     * Shortcut for JUnit assert that test whether a feature has the correct name and value
     * 
     * @param expectedName
     *            expected
     * @param expectedValue
     *            actual
     * @param actualFeature
     *            feature
     */
    public static void assertFeature(String expectedName, Object expectedValue,
            Feature actualFeature)
    {
        Assert.assertNotNull(actualFeature);
        Assert.assertEquals(expectedName, actualFeature.getName());
        Assert.assertEquals(expectedValue, actualFeature.getValue());
    }

    /**
     * Shortcut for JUnit assert that test whether a feature has the correct name and double value
     * (compared using the epsilon)
     * 
     * @param expectedName
     *            expected
     * @param expectedValue
     *            actual
     * @param actualFeature
     *            feature
     * @param epsilon
     *            epsilon
     */
    public static void assertFeature(String expectedName, double expectedValue,
            Feature actualFeature, double epsilon)
    {
        Assert.assertNotNull(actualFeature);
        Assert.assertEquals(expectedName, actualFeature.getName());
        Assert.assertEquals(expectedValue, (Double) actualFeature.getValue(), epsilon);
    }

    /**
     * 
     * @param expectedName
     *            expected
     * @param expectedValue
     *            actual
     * @param features
     *            feature
     * @param epsilon
     *            epsilon
     */
    public static void assertFeatures(String expectedName, double expectedValue,
            Set<Feature> features, double epsilon)
    {
        Assert.assertNotNull(features);
        boolean found = false;
        for (Feature f : features) {
            if (f.getName().equals(expectedName)) {
                found = true;
                Assert.assertEquals(expectedValue, (Double) f.getValue(), epsilon);
            }
        }
        Assert.assertTrue(found);
    }

    /**
     * 
     * @param expectedName
     *            expected
     * @param expectedValue
     *            actual
     * @param features
     *            features
     */
    public static void assertFeatures(String expectedName, int expectedValue, Set<Feature> features)
    {
        Assert.assertNotNull(features);
        boolean found = false;
        for (Feature f : features) {
            if (f.getName().equals(expectedName)) {
                found = true;
                Assert.assertEquals(expectedValue, (int) f.getValue());
            }
        }
        Assert.assertTrue(found);
    }
}
