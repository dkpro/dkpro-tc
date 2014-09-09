package de.tudarmstadt.ukp.dkpro.tc.ml.liblinear;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.bwaldvogel.liblinear.Problem;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.ml.TCMachineLearningAdapter.AdapterNameEntries;

public class LiblinearDataWriterTest {
	 
	@Rule
    public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void dataWriterTest() throws Exception {
		
		FeatureStore fs = new SimpleFeatureStore();
		
		List<Feature> features1 = new ArrayList<>();
		features1.add(new Feature("feature1", 1.0));
		features1.add(new Feature("feature2", 0.0));
		
		List<Feature> features2 = new ArrayList<>();
		features2.add(new Feature("feature2", 0.5));
		features2.add(new Feature("feature1", 0.5));
		
		Instance instance1 = new Instance(features1, "1");
		Instance instance2 = new Instance(features2, "2");

		fs.addInstance(instance1);
		fs.addInstance(instance2);
		
		File outputDirectory = folder.newFolder();
		File outputFile = new File(outputDirectory, LiblinearAdapter.getInstance().getFrameworkFilename(AdapterNameEntries.trainingFile));
		LiblinearDataWriter writer = new LiblinearDataWriter();
		writer.write(outputDirectory, fs, false, Constants.LM_SINGLE_LABEL);
		
		Problem problem = Problem.readFromFile(outputFile, 1.0);
		assertEquals(2, problem.l);
		assertEquals(4, problem.n);
		assertEquals(1.0, problem.y[0], 0.00001);
	}
}
