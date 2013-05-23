package de.tudarmstadt.ukp.dkpro.tc.features.NER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Person;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;


public class NEFeatureExtractor
    implements FeatureExtractor
{

	@Override
	public List<Feature> extract(JCas view, Annotation focusAnnotation)
			throws TextClassificationException {

		List<Feature> featList = new ArrayList<Feature>();

		int numOrgaNE = JCasUtil.select(view, Organization.class).size();
		int numPersonNE = JCasUtil.select(view, Person.class).size();
		int numLocNE = JCasUtil.select(view, Location.class).size();
		int numSentences = JCasUtil.select(view, Sentence.class).size();

		if(numSentences>0){
			featList.addAll(Arrays.asList(new Feature("NrOfOrganizationEntities",numOrgaNE)));
			featList.addAll(Arrays.asList(new Feature("NrOfPersonEntities",numPersonNE)));
			featList.addAll(Arrays.asList(new Feature("NrOfLocationEntities",numLocNE)));

			featList.addAll(Arrays.asList(new Feature("NrOfOrganizationEntitiesPerSent",Math.round(((float)numOrgaNE/numSentences)*100f)/100f)));
			featList.addAll(Arrays.asList(new Feature("NrOfPersonEntitiesPerSent",Math.round(((float)numPersonNE/numSentences)*100f)/100f)));
			featList.addAll(Arrays.asList(new Feature("NrOfLocationEntitiesPerSent",Math.round(((float)numLocNE/numSentences)*100f)/100f)));
		}

		return featList;
	}

}
