package de.tudarmstadt.ukp.dkpro.tc.features.style;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeature;

/*
 * Heylighen & Dewaele (2002): Variation in the contextuality of language
 * The contextuality measure can reach values 0-100
 * The higher value, the more formal (male) style the text is,
 * i.e. contains many nouns, verbs, determiners.
 * The lower value, the more contextual (female) style the text is,
 * i.e. contains many adverbs, pronouns and such.
 * 
 * 
 * Extracts also values for each pos class, as they are calculated anyway
 * 
 */

public class ContextualityMeasureFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{
    public static final String CONTEXTUALITY_MEASURE_FN = "ContextualityMeasure";

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
        List<Feature> featList = new ArrayList<Feature>();

        double total = JCasUtil.select(jcas, POS.class).size();
        double noun = select(jcas, N.class).size() / total;
        double adj = select(jcas, ADJ.class).size() / total;
        double prep = select(jcas, PP.class).size() / total;
        double art = select(jcas, ART.class).size() / total;// !includes determiners
        double pro = select(jcas, PR.class).size() / total;
        double verb = select(jcas, V.class).size() / total;
        double adv = select(jcas, ADV.class).size() / total;

        int interjCount = 0;
        for (POS tag : JCasUtil.select(jcas, O.class)) {
            // FIXME this is tagset specific
            if (tag.getPosValue().contains("UH")) {
                interjCount++;
            }
        }
        double interj = interjCount / total;

        // noun freq + adj.freq. + prepositions freq. + article freq. - pronoun freq. - verb f. -
        // adverb - interjection + 100
        double contextualityMeasure = 0.5 * (noun + adj + prep + art - pro - verb - adv - interj + 100);

        featList.add(new SimpleFeature("NounRate", noun));
        featList.add(new SimpleFeature("AdjectiveRate", adj));
        featList.add(new SimpleFeature("PrepositionRate", prep));
        featList.add(new SimpleFeature("ArticleRate", art));
        featList.add(new SimpleFeature("PronounRate", pro));
        featList.add(new SimpleFeature("VerbRate", verb));
        featList.add(new SimpleFeature("AdverbRate", adv));
        featList.add(new SimpleFeature("InterjectionRate", interj));
        featList.add(new SimpleFeature(CONTEXTUALITY_MEASURE_FN, contextualityMeasure));

        return featList;
    }

}
