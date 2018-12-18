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
package org.dkpro.tc.ml.report;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

class LearningCurveRunIdentifier
{
    String md5;
    String configAsString;

    public LearningCurveRunIdentifier(Map<String,String> configMap)
        throws NoSuchAlgorithmException
    {
        StringBuilder sb = new StringBuilder();
        configMap.forEach((x,y)-> sb.append(x+"=" + y+", "));
        configAsString = sb.toString();

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest((configAsString).getBytes(UTF_8));
        BigInteger bigInt = new BigInteger(1, digest);
        String md5 = bigInt.toString(16);
        this.md5 = md5;
    }
    

    @Override
    public int hashCode()
    {
        int result = 17;
        result = 31 * result + md5.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof LearningCurveRunIdentifier))
            return false;
        LearningCurveRunIdentifier otherMyClass = (LearningCurveRunIdentifier) other;
        if (otherMyClass.md5.equals(md5)) {
            return true;
        }
        return false;
    }
}
