package de.tudarmstadt.ukp.dkpro.tc.weka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import weka.core.Attribute;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;

/**
 * Data structure that stores a collection of Weka attributes.
 * 
 * @author zesch
 *
 */
public class AttributeStore
{

    private ArrayList<Attribute> attributes;
    private Map<String, Integer> nameOffsetMap;

    public AttributeStore()
    {
        attributes = new ArrayList<Attribute>();
        nameOffsetMap = new HashMap<String, Integer>();
    }

    public void addAttributeAtBegin(String name, Attribute attribute)
    {
        if (!nameOffsetMap.containsKey(name)) {
            shift(nameOffsetMap);
            attributes.add(0, attribute);
            nameOffsetMap.put(name, 0);
        }
    }

    private void shift(Map<String, Integer> nameOffsetMap)
    {
        for (Entry<String, Integer> entry : nameOffsetMap.entrySet()) {
            int i = entry.getValue() + 1;
            nameOffsetMap.put(entry.getKey(), i);
        }
    }

    public void addAttribute(String name, Attribute attribute)
        throws TextClassificationException
    {
        if (!nameOffsetMap.containsKey(name)) {
            attributes.add(attribute);
            nameOffsetMap.put(name, attributes.size() - 1);
        }
        else {
            // TODO do we need better error message?
            throw new TextClassificationException("Attribute with name " + name
                    + " already present in feature store. Duplicate feature ignored.");
        }
    }

    public boolean containsAttributeName(String name)
    {
        return nameOffsetMap.containsKey(name);
    }

    public int size()
    {
        return attributes.size();
    }

    public ArrayList<Attribute> getAttributes()
    {
        return attributes;
    }

    public Attribute getAttribute(String name)
    {
        return attributes.get(nameOffsetMap.get(name));
    }

    public int getAttributeOffset(String name)
    {
        if (!nameOffsetMap.containsKey(name)) {
            System.err.println("No entry for: " + name);
            return -1;
        }
        else {
            return nameOffsetMap.get(name);

        }
    }
}
