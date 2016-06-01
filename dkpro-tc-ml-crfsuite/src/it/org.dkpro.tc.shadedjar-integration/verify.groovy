/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.io.*;
import org.apache.commons.logging.LogFactory;

try {
    double eps = 0.00001;
    double osx_expected   = 0.4836734693877553;
    double linux_expected = 0.35714285714285715;

    def command = new ArrayList();
    command.add("java");
    command.add("-jar");
    command.add("dkpro-tc-ml-crfsuite/target/it/org.dkpro.tc.shadedjar-integration/target/org.dkpro.tc.shadedjar-integration-0.0.1-SNAPSHOT-standalone.jar");
    command.add("dkpro-tc-ml-crfsuite/target/it/org.dkpro.tc.shadedjar-integration/src/main/resources/a15.xml");
    command.add("dkpro-tc-ml-crfsuite/target/it/org.dkpro.tc.shadedjar-integration/src/main/resources/a17.xml");
    def probuilder = new ProcessBuilder(command);
    probuilder.inheritIO();
    def p = probuilder.start();
    p.waitFor();

    double actual = Double.valueOf(FileUtils.readFileToString(new File("target/accuracy.txt")));

//    throw new Exception(Math.abs(actual-osx_expected)  + " " +  Math.abs(actual-linux_expected) );    

    //The CRFsuite binary splits internally the training data this split dependes on a system dependend variable i.e. osx and linux will have different results for the same data sets
    if(Math.abs(actual-osx_expected) > eps || Math.abs(actual-linux_expected) > eps){
        throw new IllegalStateException(
        "Integration test failed - expected accuracy of [" + osx_expected + " or " + linux_expected +
        "] but was [" + actual + "]");
        return false;
    }
}
catch( Throwable t ) {
    t.printStackTrace();
    return false;
}

return true;
