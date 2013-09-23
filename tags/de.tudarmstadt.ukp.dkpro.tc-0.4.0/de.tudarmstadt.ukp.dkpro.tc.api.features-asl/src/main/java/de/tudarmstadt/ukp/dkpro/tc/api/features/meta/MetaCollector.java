package de.tudarmstadt.ukp.dkpro.tc.api.features.meta;

import java.util.Map;

import org.apache.uima.fit.component.JCasAnnotator_ImplBase;

/**
 * Interface for meta collectors that collect document-level information for feature extractors.
 * 
 * @author zesch
 *
 */
public abstract class MetaCollector
    extends JCasAnnotator_ImplBase
{
    public abstract Map<String,String> getParameterKeyPairs();
}
