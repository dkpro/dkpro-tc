package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ne;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.PairFeatureExtractor;

public class SharedNEsFeatureExtractor
    implements PairFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view1, JCas view2)
        throws TextClassificationException
    {
        return Arrays.asList(
                new Feature("SharedNEs",
                        Collections.disjoint(getNEs(view1),
                                getNEs(view2))
                ));

    }

    private Collection<String> getNEs(JCas view)
    {
        // if(JCasUtil.select(view, NamedEntity.class).size() > 0){
        // System.out.println("Named entites exist!");
        // }
        List<String> entities = new ArrayList<String>();
        for (NamedEntity entity : JCasUtil.select(view, NamedEntity.class)) {
            entities.add(entity.getCoveredText());
        }
        return entities;
    }
}
