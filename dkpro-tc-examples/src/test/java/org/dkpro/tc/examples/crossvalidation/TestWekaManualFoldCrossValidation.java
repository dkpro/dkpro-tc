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
package org.dkpro.tc.examples.crossvalidation;

import org.dkpro.lab.engine.ExecutionException;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.junit.Test;

public class TestWekaManualFoldCrossValidation extends TestCaseSuperClass
{
	
    /*
     * We request more folds than we have files (2) because we set 'manualMode' to true we expect an exception 
     */
    @Test(expected=ExecutionException.class)
    public void testManualFoldCrossValdiationException() throws Exception{
        WekaManualFoldCrossValidation javaExperiment = new WekaManualFoldCrossValidation();
        javaExperiment.runCrossValidation(WekaManualFoldCrossValidation.getParameterSpace(true), 3);
    }
    
    /*
     * We request more folds than we have files (2) without 'manualModel' thus the CAS should be split up and create sufficient many CAS to be distributed into the fold 
     */
    @Test 
    public void testManualFoldCrossValdiation() throws Exception{
        WekaManualFoldCrossValidation javaExperiment = new WekaManualFoldCrossValidation();
        javaExperiment.runCrossValidation(WekaManualFoldCrossValidation.getParameterSpace(false), 3);
    }

}
