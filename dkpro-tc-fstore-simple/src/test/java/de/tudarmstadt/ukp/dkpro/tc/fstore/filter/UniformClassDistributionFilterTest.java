package de.tudarmstadt.ukp.dkpro.tc.fstore.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;

public class UniformClassDistributionFilterTest {

	@Test
	public void uniformFilterTest() 
		throws Exception
	{
		FeatureStore fs = new SimpleFeatureStore();
		
		Feature f1 = new Feature("feature1", "value1");
		Feature f2 = new Feature("feature2", "value2");
		List<Feature> features = new ArrayList<>();
		features.add(f1);
		features.add(f2);

		fs.addInstance( new Instance(features, "outcome1"));
		fs.addInstance( new Instance(features, "outcome1"));
		fs.addInstance( new Instance(features, "outcome1"));
		fs.addInstance( new Instance(features, "outcome2"));
		fs.addInstance( new Instance(features, "outcome2"));
		
		new UniformClassDistributionFilter().applyFilter(fs);
		
		assertEquals(4, fs.getNumberOfInstances());
	}
}
