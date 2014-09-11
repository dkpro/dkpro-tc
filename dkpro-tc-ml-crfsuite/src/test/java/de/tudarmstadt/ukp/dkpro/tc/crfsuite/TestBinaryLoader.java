/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.tc.crfsuite;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;

public class TestBinaryLoader
{
    String platform = PlatformDetector.OS_OSX + "-" + PlatformDetector.ARCH_X86_64;

    @Test
    public void testLoadingBinary()
        throws Exception
    {
        BinaryLoader bl = new BinaryLoader();
        File binaryLocation = bl.loadCRFBinary();
        assertNotNull(binaryLocation);
        assertNotSame("", binaryLocation.getAbsoluteFile());
    }

}
