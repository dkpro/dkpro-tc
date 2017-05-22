/**
 * Copyright 2017
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
package org.dkpro.tc.ml.weka.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.core.Attribute;
import weka.core.Utils;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.api.features.MissingValue;
import org.dkpro.tc.api.features.MissingValue.MissingValueType;
import org.dkpro.tc.core.task.uima.FeatureType;
import org.dkpro.tc.ml.weka.util.AttributeStore;

/*
 * Converts the TC feature representation into the Weka representation.
 * 
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class WekaFeatureEncoder
{

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

    public static Attribute featureToAttributeUsingFeatureDescription(String featureName,
            FeatureType value, String enumType)
                throws TextClassificationException
    {
        String name = Utils.quote(featureName);
        Attribute attribute;
        // if value is a number then create a numeric attribute
        if (value.equals(FeatureType.NUM) || value.equals(FeatureType.NUM_FLOATING_POINT)
                || value.equals(FeatureType.NUM_INTEGER) || value.equals(FeatureType.BOOLEAN)|| value.equals(FeatureType.STRING)) {
            attribute = new Attribute(name);
        }
        // if value is an Enum thene create a nominal attribute
        else if (value.equals(FeatureType.ENUM)) {
            Class<?> forName=null;
            try {
                forName = Class.forName(enumType);
            }
            catch (ClassNotFoundException e) {
                throw new TextClassificationException(e);
            }
            Object[] enumConstants = forName.getEnumConstants();
            ArrayList<String> attributeValues = new ArrayList<String>(enumConstants.length);
            for (Object enumConstant : enumConstants) {
                attributeValues.add(enumConstant.toString());
            }
            attribute = new Attribute(name, attributeValues);
        }
        else {
            attribute = new Attribute(name, (ArrayList<String>) null);
        }
        return attribute;
    }

    public static Attribute featureToAttribute(Feature feature)
        throws TextClassificationException
    {
        String name = Utils.quote(feature.getName());
        Object value = feature.getValue();
        Attribute attribute;
        // if value is a number then create a numeric attribute
        if (value instanceof Number) {
            attribute = new Attribute(name);
        } // if value is a boolean then create a numeric attribute
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
     * A map returning a double value for each valid
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