package de.tudarmstadt.ukp.dkpro.tc.testing;

import java.io.File;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;

/**
 * A test setup for native Weka access
 */
public class WekaArffTest
{

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args)
        throws Exception
    {
        File train = new File("src/main/resources/arff/manyInstances/train.arff.gz");
        File test = new File("src/main/resources/arff/manyInstances/test.arff.gz");

        Instances trainData = TaskUtils.getInstances(train, false);
        Instances testData = TaskUtils.getInstances(test, false);

        Classifier cl = new NaiveBayes();
        
        // no problems until here
        Evaluation eval = new Evaluation(trainData);
        eval.evaluateModel(cl, testData);
    }
}
