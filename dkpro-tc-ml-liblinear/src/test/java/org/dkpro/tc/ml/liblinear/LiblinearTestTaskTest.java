/*******************************************************************************
 * Copyright 2019
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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.bwaldvogel.liblinear.SolverType;

public class LiblinearTestTaskTest
{
    @Test
    public void testDefaultGetSolver()
    {
        assertEquals(SolverType.L2R_LR, LiblinearTestTask.getSolver(null));
    }

    @Test
    public void testSetSolver()
    {
        List<Object> param = Arrays.asList(new Object(), "-s", "0");
        assertEquals(SolverType.L2R_LR, LiblinearTestTask.getSolver(param));

        param = Arrays.asList(new Object(),"-s", "1");
        assertEquals(SolverType.L2R_L2LOSS_SVC_DUAL, LiblinearTestTask.getSolver(param));

        param = Arrays.asList(new Object(),"-s", "2");
        assertEquals(SolverType.L2R_L2LOSS_SVC, LiblinearTestTask.getSolver(param));

        param = Arrays.asList(new Object(),"-s", "3");
        assertEquals(SolverType.L2R_L1LOSS_SVC_DUAL, LiblinearTestTask.getSolver(param));

        param = Arrays.asList(new Object(),"-s", "4");
        assertEquals(SolverType.MCSVM_CS, LiblinearTestTask.getSolver(param));

        param = Arrays.asList(new Object(),"-s", "5");
        assertEquals(SolverType.L1R_L2LOSS_SVC, LiblinearTestTask.getSolver(param));

        param = Arrays.asList(new Object(),"-s", "6");
        assertEquals(SolverType.L1R_LR, LiblinearTestTask.getSolver(param));

        param = Arrays.asList(new Object(),"-s", "7");
        assertEquals(SolverType.L2R_LR_DUAL, LiblinearTestTask.getSolver(param));

        param = Arrays.asList(new Object(),"-s", "11");
        assertEquals(SolverType.L2R_L2LOSS_SVR, LiblinearTestTask.getSolver(param));

        param = Arrays.asList(new Object(),"-s", "12");
        assertEquals(SolverType.L2R_L2LOSS_SVR_DUAL, LiblinearTestTask.getSolver(param));

        param = Arrays.asList(new Object(),"-s", "13");
        assertEquals(SolverType.L2R_L1LOSS_SVR_DUAL, LiblinearTestTask.getSolver(param));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionSetSolver()
    {
        List<Object> param = Arrays.asList(new Object(), "-s");
        LiblinearTestTask.getSolver(param);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionSetParamC()
    {
        List<Object> param = Arrays.asList(new Object(), "-c");
        LiblinearTestTask.getParameterC(param);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionSetEpsilon()
    {
        List<Object> param = Arrays.asList(new Object(), "-e");
        LiblinearTestTask.getParameterEpsilon(param);
    }

    @Test
    public void testDefaultParamC()
    {
        assertEquals(1.0, LiblinearTestTask.getParameterC(null), 0.0);
    }

    @Test
    public void testSetParamC()
    {
        List<Object> param = Arrays.asList(new Object(), "-c", "23.2");
        assertEquals(23.2, LiblinearTestTask.getParameterC(param), 0.0001);
    }

    @Test
    public void testDefaultEpsilon()
    {
        assertEquals(0.01, LiblinearTestTask.getParameterEpsilon(null), 0.0001);
    }

    @Test
    public void testSetEpsilon()
    {
        List<Object> param = Arrays.asList(new Object(), "-e", "2.1");
        assertEquals(2.1, LiblinearTestTask.getParameterEpsilon(param), 0.0001);
    }
}
