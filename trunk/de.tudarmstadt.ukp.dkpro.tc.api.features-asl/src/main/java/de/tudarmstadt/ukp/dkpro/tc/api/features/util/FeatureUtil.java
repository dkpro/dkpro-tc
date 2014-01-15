package de.tudarmstadt.ukp.dkpro.tc.api.features.util;

import org.apache.commons.lang.StringUtils;

public class FeatureUtil
{
    /**
     * Escapes the names, as Weka does not seem to like special characters in attribute names.
     * @param name
     * @return
     */
    public static String escapeFeatureName(String name) {
        
        // TODO improve the escaping
        // the fix was necessary due to Issue 32
        // http://code.google.com/p/dkpro-tc/issues/detail?id=32
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<name.length(); i++) {
            String c = name.substring(i, i+1);
            if (StringUtils.isAlphanumeric(c) || c.equals("_")) {
                sb.append(c);
            }
            else {
                sb.append("u");
                sb.append(c.codePointAt(0));
            }
        }
        return sb.toString();
    }
}
