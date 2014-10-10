/*
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
 */

package de.tudarmstadt.ukp.dkpro.tc.svmhmm.util;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SVMHMMUtilsTest
{
    @Test
    public void testExtractComments()
            throws Exception
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.write("O qid:5 2:7 # #IFTHEN O 5", outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        List<List<String>> comments = SVMHMMUtils.extractComments(inputStream, 3);

        assertEquals(1, comments.size());
        assertEquals("#IFTHEN", comments.get(0).get(0));
        assertEquals("O", comments.get(0).get(1));
        assertEquals("5", comments.get(0).get(2));
    }
}