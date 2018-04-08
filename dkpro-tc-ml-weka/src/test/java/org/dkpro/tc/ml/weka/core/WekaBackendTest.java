package org.dkpro.tc.ml.weka.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.ml.base.TcPredictor;
import org.dkpro.tc.ml.base.TcTrainer;
import org.dkpro.tc.ml.weka.core.WekaPredictor;
import org.dkpro.tc.ml.weka.core.WekaTrainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import weka.classifiers.functions.SMO;

public class WekaBackendTest
{
    File data;
    File model;

    @Before
    public void setup() throws IOException
    {
        data = new File("src/test/resources/data/featureFile.txt");
        model = FileUtil.createTempFile("wekaModel", ".model");
    }

    @After
    public void tearDown()
    {
        model.delete();
    }

    @Test
    public void testWeka() throws Exception
    {
        TcTrainer trainer = new WekaTrainer();

        List<String> parameters = new ArrayList<>();
        parameters.add(SMO.class.getName());
        trainer.train(data, model, parameters);

        TcPredictor predictor = new WekaPredictor();
        List<String> predictions = predictor.predict(data, model);

        assertTrue(predictions != null && !predictions.isEmpty());
        assertEquals(24, predictions.size());

        for (String s : predictions) {
            double d = Double.parseDouble(s);
            assertTrue(d >= 0.0 && d <= 2.0);
        }

    }

}
