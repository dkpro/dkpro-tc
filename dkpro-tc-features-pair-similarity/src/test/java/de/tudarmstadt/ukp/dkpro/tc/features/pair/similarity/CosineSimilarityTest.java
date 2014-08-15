package de.tudarmstadt.ukp.dkpro.tc.features.pair.similarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.similarity.PPipelineTestBase;

public class CosineSimilarityTest
    extends PPipelineTestBase
{
    /**
     * Tests if just View1 ngrams are being extracted as features.
     * 
     * @throws Exception
     */
    @Test
    public void testCosSimDefaultTfIdf()
        throws Exception
    {
    	CosineSimilarityTest test = new CosineSimilarityTest();
        test.initialize();
        test.parameters = new Object[] { 
        		CosineFeatureExtractor.PARAM_LUCENE_DIR, test.lucenePath };
        test.runPipeline();
        assertTrue(test.featureNames.first().equals("SimilarityCosineSimilarity"));
        assertEquals(test.featureNames.size(), 1);
        
        for(Feature feat: test.instanceList.get(0).getFeatures()){
        	assertEquals(feat.getValue(), 0.0);
        }
        
        
    }
    @Override
    protected void getFeatureExtractorCollector(List<Object> parameterList)
        throws ResourceInitializationException
    {
        featExtractorConnector = TaskUtils.getFeatureExtractorConnector(parameterList,
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_PAIR, false, false,
                CosineFeatureExtractor.class.getName());
    }
    @Override
	protected void getMetaCollector(List<Object> parameterList)
			throws ResourceInitializationException
		{
			metaCollector = AnalysisEngineFactory.createEngineDescription(
					IdfPairMetaCollector.class,
	                parameterList.toArray()
	        );
		}
}