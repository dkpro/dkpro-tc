/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.ml.liblinear;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.pear.util.FileUtil;
import org.dkpro.tc.ml.liblinear.util.LiblinearUtils;
import org.junit.Test;

import de.bwaldvogel.liblinear.SolverType;

public class LiblinearUtilTest
{
    @Test
    public void testDefaultGetSolver()
    {
        assertEquals(SolverType.L2R_LR, LiblinearUtils.getSolver(null));
    }

    @Test
    public void testSetSolver()
    {
        List<String> param = Arrays.asList("-s", LiblinearTestTask.SOLVER_L2R_L1LOSS_SVC_DUAL);
        assertEquals(SolverType.L2R_L1LOSS_SVC_DUAL, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", LiblinearTestTask.SOLVER_L1R_L2LOSS_SVC);
        assertEquals(SolverType.L1R_L2LOSS_SVC, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", LiblinearTestTask.SOLVER_L1R_LR);
        assertEquals(SolverType.L1R_LR, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", LiblinearTestTask.SOLVER_L2R_L1LOSS_SVR_DUAL);
        assertEquals(SolverType.L2R_L1LOSS_SVR_DUAL, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", LiblinearTestTask.SOLVER_L2R_L2LOSS_SVC);
        assertEquals(SolverType.L2R_L2LOSS_SVC, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", LiblinearTestTask.SOLVER_L2R_L2LOSS_SVC_DUAL);
        assertEquals(SolverType.L2R_L2LOSS_SVC_DUAL, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", LiblinearTestTask.SOLVER_L2R_L2LOSS_SVR);
        assertEquals(SolverType.L2R_L2LOSS_SVR, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", LiblinearTestTask.SOLVER_L2R_L2LOSS_SVR_DUAL);
        assertEquals(SolverType.L2R_L2LOSS_SVR_DUAL, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", LiblinearTestTask.SOLVER_L2R_LR);
        assertEquals(SolverType.L2R_LR, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", LiblinearTestTask.SOLVER_L2R_LR_DUAL);
        assertEquals(SolverType.L2R_LR_DUAL, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", LiblinearTestTask.SOLVER_MCSVM_CS);
        assertEquals(SolverType.MCSVM_CS, LiblinearUtils.getSolver(param));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionSetSolver()
    {
        List<String> param = Arrays.asList("-s");
        LiblinearUtils.getSolver(param);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testExceptionSetParamC()
    {
        List<String> param = Arrays.asList("-c");
        LiblinearUtils.getParameterC(param);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testExceptionSetEpsilon()
    {
        List<String> param = Arrays.asList("-e");
        LiblinearUtils.getParameterEpsilon(param);
    }

    @Test
    public void testDefaultParamC()
    {
        assertEquals(1.0, LiblinearUtils.getParameterC(null), 0.0);
    }

    @Test
    public void testSetParamC()
    {
        List<String> param = Arrays.asList("-c", "23.2");
        assertEquals(23.2, LiblinearUtils.getParameterC(param), 0.0001);
    }

    @Test
    public void testDefaultEpsilon()
    {
        assertEquals(0.01, LiblinearUtils.getParameterEpsilon(null), 0.0001);
    }

    @Test
    public void testSetEpsilon()
    {
        List<String> param = Arrays.asList("-e", "2.1");
        assertEquals(2.1, LiblinearUtils.getParameterEpsilon(param), 0.0001);
    }
    
    @Test
    public void testMapToString(){
        Map<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        String outcomeMap2String = LiblinearUtils.outcomeMap2String(m);
        assertEquals(outcomeMap2String, "a\t1\nb\t2\n");
    }
    
    @Test
    public void testCreateMapping() throws IOException{
        String dummyData = "A\t1:1.0\t2:1.0\nB\t1:1.0\nC\t2:1.0";
        File tmpFile = FileUtil.createTempFile("junitTest", ".tmp");
        FileUtils.write(tmpFile, dummyData);
        Map<String, Integer> map = LiblinearUtils.createMapping(tmpFile);
        
        assertEquals(3,map.size());
        assertEquals(new Integer(0), map.get("A"));
        assertEquals(new Integer(1), map.get("B"));
        assertEquals(new Integer(2), map.get("C"));
        
        File integerReplacedFile = LiblinearUtils.replaceOutcomeByIntegerValue(tmpFile, map);
        assertEquals("0\t1:1.0\t2:1.0\n1\t1:1.0\n2\t2:1.0\n",FileUtils.readFileToString(integerReplacedFile));
    }
}
