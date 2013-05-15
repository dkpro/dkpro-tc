package de.tudarmstadt.ukp.dkpro.tc.features.style;

import static org.uimafit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;

//Heylighen & Dewaele (2002): Variation in the contextuality of language
public class ContextualityMeasureFeatureExtractor
    implements SimpleFeatureExtractor
{
    public static final String CONTEXTUALITY_MEASURE_FN = "ContextualityMeasure";
    
    @Override
    public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
        throws CleartkExtractorException
    {
        List<Feature> featList = new ArrayList<Feature>();
        
        double total = JCasUtil.select(jcas, POS.class).size();
        double noun = select(jcas,   N.class).size() / total;
        double adj =  select(jcas, ADJ.class).size() / total;
        double prep = select(jcas,  PP.class).size() / total;
        double art =  select(jcas, ART.class).size() / total;// !includes determiners
        double pro =  select(jcas,  PR.class).size() / total;
        double verb = select(jcas,   V.class).size() / total;
        double adv =  select(jcas, ADV.class).size() / total;
        
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

        featList.addAll(Arrays.asList(new Feature("NounRate", noun)));
        featList.addAll(Arrays.asList(new Feature("AdjectiveRate", adj)));
        featList.addAll(Arrays.asList(new Feature("PrepositionRate", prep)));
        featList.addAll(Arrays.asList(new Feature("ArticleRate", art)));
        featList.addAll(Arrays.asList(new Feature("PronounRate", pro)));
        featList.addAll(Arrays.asList(new Feature("VerbRate", verb)));
        featList.addAll(Arrays.asList(new Feature("AdverbRate", adv)));
        featList.addAll(Arrays.asList(new Feature("InterjectionRate", interj)));
        featList.addAll(Arrays.asList(new Feature(CONTEXTUALITY_MEASURE_FN, contextualityMeasure)));
        
        return featList;
    }

}
