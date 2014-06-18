package de.tudarmstadt.ukp.dkpro.tc.mallet.writer;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue.MissingValueType;

public class MalletFeatureEncoder
{

    /**
     * A map returning a String value for each valid {@link MissingValueType}
     * 
     * @return a map with {@link MissingValueType} keys, and strings as values
     */
    public static Map<MissingValueType, String> getMissingValueConversionMap()
    {
        Map<MissingValueType, String> map = new HashMap<MissingValueType, String>();
        // for booelan attributes: false
        map.put(MissingValueType.BOOLEAN, "0");
        // for numeric attributes: zero
        map.put(MissingValueType.NUMERIC, "0");
        // TODO is this really what we want?
        // for nominal attributes: the first
        map.put(MissingValueType.NOMINAL, "0");
        // TODO is this really what we want?
        // for string attributes: the first
        map.put(MissingValueType.STRING, "0");
        return map;
    }
}
