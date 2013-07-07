package de.tudarmstadt.ukp.dkpro.tc.features.util;

import junit.framework.Assert;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class FeatureTestUtil
{
    public static void assertFeature(String expectedName, Object expectedValue, Feature actualFeature) {
        Assert.assertNotNull(actualFeature);
        Assert.assertEquals(expectedName, actualFeature.getName());
        Assert.assertEquals(expectedValue, actualFeature.getValue());
    }

    public static void assertFeature(String expectedName, double expectedValue, Feature actualFeature, double epsilon) {
        Assert.assertNotNull(actualFeature);
        Assert.assertEquals(expectedName, actualFeature.getName());
        Assert.assertEquals(expectedValue, (Double) actualFeature.getValue(), epsilon);
    }
}
