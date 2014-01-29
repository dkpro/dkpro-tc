package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

public class ComboUtils
{
    public static String combo(String prefix, String ngram1, String ngram2){
        return prefix + ngram1 + "_" + ngram2;
    }
}
