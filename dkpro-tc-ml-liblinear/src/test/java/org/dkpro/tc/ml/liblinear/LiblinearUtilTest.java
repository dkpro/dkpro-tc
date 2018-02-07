/*******************************************************************************
 * Copyright 2018
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
        List<Object> param = Arrays.asList(new Object(), "-s", "0");
        assertEquals(SolverType.L2R_LR, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", "1");
        assertEquals(SolverType.L2R_L2LOSS_SVC_DUAL, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", "2");
        assertEquals(SolverType.L2R_L2LOSS_SVC, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", "3");
        assertEquals(SolverType.L2R_L1LOSS_SVC_DUAL, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", "4");
        assertEquals(SolverType.MCSVM_CS, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", "5");
        assertEquals(SolverType.L1R_L2LOSS_SVC, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", "6");
        assertEquals(SolverType.L1R_LR, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", "7");
        assertEquals(SolverType.L2R_LR_DUAL, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", "11");
        assertEquals(SolverType.L2R_L2LOSS_SVR, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", "12");
        assertEquals(SolverType.L2R_L2LOSS_SVR_DUAL, LiblinearUtils.getSolver(param));

        param = Arrays.asList("-s", "13");
        assertEquals(SolverType.L2R_L1LOSS_SVR_DUAL, LiblinearUtils.getSolver(param));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionSetSolver()
    {
        List<Object> param = Arrays.asList(new Object(), "-s");
        LiblinearUtils.getSolver(param);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionSetParamC()
    {
        List<Object> param = Arrays.asList(new Object(), "-c");
        LiblinearUtils.getParameterC(param);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionSetEpsilon()
    {
        List<Object> param = Arrays.asList(new Object(), "-e");
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
        List<Object> param = Arrays.asList(new Object(), "-c", "23.2");
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
        List<Object> param = Arrays.asList(new Object(), "-e", "2.1");
        assertEquals(2.1, LiblinearUtils.getParameterEpsilon(param), 0.0001);
    }
}
