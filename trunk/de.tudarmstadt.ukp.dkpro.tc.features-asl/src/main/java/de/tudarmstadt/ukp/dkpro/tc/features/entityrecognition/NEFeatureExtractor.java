package de.tudarmstadt.ukp.dkpro.tc.features.entityrecognition;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Person;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

public class NEFeatureExtractor
    extends FeatureExtractorResource_ImplBase
    implements DocumentFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas view)
        throws TextClassificationException
    {

        List<Feature> featList = new ArrayList<Feature>();

        int numOrgaNE = JCasUtil.select(view, Organization.class).size();
        int numPersonNE = JCasUtil.select(view, Person.class).size();
        int numLocNE = JCasUtil.select(view, Location.class).size();
        int numSentences = JCasUtil.select(view, Sentence.class).size();

        if (numSentences > 0) {
            featList.add(new Feature("NrOfOrganizationEntities", numOrgaNE));
            featList.add(new Feature("NrOfPersonEntities", numPersonNE));
            featList.add(new Feature("NrOfLocationEntities", numLocNE));

            featList.add(new Feature("NrOfOrganizationEntitiesPerSent", Math
                    .round(((float) numOrgaNE / numSentences) * 100f) / 100f));
            featList.add(new Feature("NrOfPersonEntitiesPerSent", Math
                    .round(((float) numPersonNE / numSentences) * 100f) / 100f));
            featList.add(new Feature("NrOfLocationEntitiesPerSent", Math
                    .round(((float) numLocNE / numSentences) * 100f) / 100f));
        }

        return featList;
    }

}
