/**
 * Copyright 2014
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.core.Attribute;
import weka.core.Utils;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.MissingValue.MissingValueType;
import de.tudarmstadt.ukp.dkpro.tc.weka.AttributeStore;

/**
 * Converts the TC feature representation into the Weka representation.
 * 
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * @author Torsten Zesch
 * 
 */

public class WekaFeatureEncoder
{

    /**
     * @param instanceList
     * @return A Weka attribute store given a TC feature store
     * @throws TextClassificationException
     */
    public static AttributeStore getAttributeStore(FeatureStore instanceList)
        throws TextClassificationException
    {
        AttributeStore attributeStore = new AttributeStore();

        for (Instance instance : instanceList.getInstances()) {
            for (Feature feature : instance.getFeatures()) {
                if (!attributeStore.containsAttributeName(feature.getName())) {
                    Attribute attribute = featureToAttribute(feature);
                    attributeStore.addAttribute(feature.getName(), attribute);
                }
            }
        }

        return attributeStore;
    }

    /**
     * @param feature
     * @return An Weka attribute given a TC feature
     * @throws TextClassificationException
     */
    public static Attribute featureToAttribute(Feature feature)
        throws TextClassificationException
    {
        String name = Utils.quote(feature.getName());
        Object value = feature.getValue();
        Attribute attribute;
        // if value is a number then create a numeric attribute
        if (value instanceof Number) {
            attribute = new Attribute(name);
        }// if value is a boolean then create a numeric attribute
        else if (value instanceof Boolean) {
            attribute = new Attribute(name);
        }
        // if value is an Enum thene create a nominal attribute
        else if (value instanceof Enum) {
            Object[] enumConstants = value.getClass().getEnumConstants();
            ArrayList<String> attributeValues = new ArrayList<String>(enumConstants.length);
            for (Object enumConstant : enumConstants) {
                attributeValues.add(enumConstant.toString());
            }
            attribute = new Attribute(name, attributeValues);
        }
        // if the value has a missing value, determine its type according to the missing value type
        else if (value instanceof MissingValue) {
            switch (((MissingValue) value).getType()) {
            case NUMERIC:
                attribute = new Attribute(name);
                break;
            case NOMINAL:
                Object[] enumConstants = ((MissingValue) value).getNominalClass()
                        .getEnumConstants();
                ArrayList<String> attributeValues = new ArrayList<String>(enumConstants.length);
                for (Object enumConstant : enumConstants) {
                    attributeValues.add(enumConstant.toString());
                }
                attribute = new Attribute(name, attributeValues);
                break;
            case BOOLEAN:
                attribute = new Attribute(name);
                break;
            case STRING:
                attribute = new Attribute(null);
                break;
            default:
                throw new TextClassificationException("Type of missing value is unknown.");
            }
        }
        // if value is not a number, boolean, enum, or missing value then we will create a
        // string attribute
        else {
            attribute = new Attribute(name, (ArrayList<String>) null);
        }
        return attribute;
    }

    /**
     * A map returning a double value for each valid {@link MissingValueType}
     * 
     * @return a map with {@link MissingValueType} keys, and doubles as value
     */
    public static Map<MissingValueType, Double> getMissingValueConversionMap()
    {
        Map<MissingValueType, Double> map = new HashMap<MissingValueType, Double>();
        // Weka internal representation fopr missing values
        map.put(MissingValueType.BOOLEAN, Double.NaN);
        map.put(MissingValueType.NUMERIC, Double.NaN);
        map.put(MissingValueType.NOMINAL, Double.NaN);
        map.put(MissingValueType.STRING, Double.NaN);
        return map;
    }
}