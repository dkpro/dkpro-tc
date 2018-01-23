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

package org.dkpro.tc.ml.crfsuite.writer;

public class LabelSubstitutor
{
    /**
     * There is a bug in CRFsuite with certain characters as labels. These labels are not written
     * correctly to the stdout by CRFsuite resulting in blank labels. This cause problems for
     * downstream tasks in particular for the TC evaluation which would have to deal with blank
     * labels. These methods here provide a substitution of known problematic labels to values which
     * do work. The provided methods perform a substitution of the known problem-labels and
     * substitute them back after crfsuite ran. (TH 2015-01-26)
     */

    private final static String COLON = ":";
    private final static String COLON_SUBSTITUTE = "XXCol";

    public static String labelReplacement(String label)
    {
        switch (label) {
        case COLON:
            return COLON_SUBSTITUTE;
        }

        return label;
    }

    public static String undoLabelReplacement(String label)
    {
        switch(label){
        case COLON_SUBSTITUTE:
            return COLON;
        }
        return label;
    }

}
