/**
 * Copyright 2019
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
package org.dkpro.tc.examples.raw;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.dkpro.tc.examples.TestCaseSuperClass;
import org.dkpro.tc.examples.shallow.raw.WekaRawDemoUIMAonly;
import org.junit.Test;

/**
 * This test just ensures that the experiment runs without throwing any exception.
 */
public class RawUimaDemo
    extends TestCaseSuperClass
{

    @Test
    public void testJavaTrainTest() throws Exception
    {
        WekaRawDemoUIMAonly uima = new WekaRawDemoUIMAonly();
        List<String> run = uima.run();
        assertTrue(run != null);
        assertEquals(8, run.size());
    }
}
