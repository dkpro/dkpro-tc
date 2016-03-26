/**
 * Copyright 2016
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
package org.dkpro.tc.crfsuite.writer;

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
