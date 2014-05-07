/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.writer;

import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.Utils;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
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
     */
    public static Attribute featureToAttribute(Feature feature)
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
        // if value is not a number, boolean, or enum, then we will create a
        // string attribute
        else {
            attribute = new Attribute(name, (ArrayList<String>) null);
        }
        return attribute;
    }
}