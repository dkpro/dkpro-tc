package de.tudarmstadt.ukp.dkpro.tc.api.features.util;

import junit.framework.Assert;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

/**
 * Utils for testing feature extractors
 */
public class FeatureTestUtil
{
    /**
     * Shortcut for JUnit assert that test whether a feature has the correct name and value
     * 
     * @param expectedName
     * @param expectedValue
     * @param actualFeature
     */
    public static void assertFeature(String expectedName, Object expectedValue, Feature actualFeature) {
        Assert.assertNotNull(actualFeature);
        Assert.assertEquals(expectedName, actualFeature.getName());
        Assert.assertEquals(expectedValue, actualFeature.getValue());
    }

    /**
     * Shortcut for JUnit assert that test whether a feature has the correct name and value
     * 
     * @param expectedName
     * @param expectedValue
     * @param actualFeature
     */
    public static void assertFeature(String expectedName, double expectedValue, Feature actualFeature, double epsilon) {
        Assert.assertNotNull(actualFeature);
        Assert.assertEquals(expectedName, actualFeature.getName());
        Assert.assertEquals(expectedValue, (Double) actualFeature.getValue(), epsilon);
    }
}
