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
package org.dkpro.tc.core.feature.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class JsonInstanceIterator
    implements Iterator<String>
{
    BufferedReader reader;
    String line = null;

    public JsonInstanceIterator(File file) throws Exception
    {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
    }

    @Override
    public boolean hasNext()
    {
        try {
            line = reader.readLine();
            if (line == null) {
                reader.close();
                return false;
            }
        }
        catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }

        return true;
    }

    @Override
    public String next()
    {
        if (line == null) {
            throw new NoSuchElementException();
        }

        return line;
    }

}
