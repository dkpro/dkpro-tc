package de.tudarmstadt.ukp.dkpro.tc.core.meta;

import java.util.Map;

import org.apache.uima.fit.component.JCasAnnotator_ImplBase;

public abstract class MetaCollector
    extends JCasAnnotator_ImplBase
{
    public abstract Map<String,String> getParameterKeyPairs();
}
