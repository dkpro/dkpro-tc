/**
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.ml.weka.core;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.ml.base.TcPredictor;
import org.dkpro.tc.ml.base.TcTrainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import meka.classifiers.multilabel.incremental.PSUpdateable;

public class MekaBackendTest
{
    File data;
    File model;

    @Before
    public void setup() throws IOException
    {
        data = new File("src/test/resources/data/Yeast_meka.txt");
        model = FileUtil.createTempFile("mekaModel", ".model");
    }

    @After
    public void tearDown()
    {
        model.delete();
    }

    @Test
    public void testMeka() throws Exception
    {
        TcTrainer trainer = new MekaTrainer();
        
        List<String> parameters = new ArrayList<>();
        parameters.add(PSUpdateable.class.getName());
        parameters.add("-B");
        parameters.add("900");
        parameters.add("-S");
        parameters.add("9");
        
        trainer.train(data, model, parameters);
        
        TcPredictor predictor = new MekaPredictor();
        List<String> predict = predictor.predict(data, model);
        assertEquals(26, predict.size());

    }

}
