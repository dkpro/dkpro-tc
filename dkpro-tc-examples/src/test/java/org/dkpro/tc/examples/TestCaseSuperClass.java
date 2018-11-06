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
package org.dkpro.tc.examples;

import java.util.Locale;

import org.apache.commons.logging.LogFactory;
import org.junit.Before;

public class TestCaseSuperClass
{

    public final static String HOME = "target/results";

    @Before
    public void setup() throws Exception
    {
        System.setProperty("java.util.logging.config.file", "logging.properties");
        System.setProperty("DKPRO_HOME", HOME);
    }
    
    public static boolean filePathMatch(String expected, String actual) {
        if(actual.matches(expected)) {
            return true;
        }
        
        String diff = String.format(Locale.getDefault(), "%10s%s%n%10s%s","Expected: ", expected, "Actual: ", actual);
        System.err.println(diff);
        LogFactory.getLog(TestCaseSuperClass.class).warn(diff);
        return false;
    }
}
