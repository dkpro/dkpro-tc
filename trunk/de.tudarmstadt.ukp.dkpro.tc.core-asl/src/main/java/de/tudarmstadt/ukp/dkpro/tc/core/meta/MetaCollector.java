package de.tudarmstadt.ukp.dkpro.tc.core.meta;

import java.util.Map;

import org.uimafit.component.JCasAnnotator_ImplBase;

public abstract class MetaCollector
    extends JCasAnnotator_ImplBase
{
    public abstract Map<String,String> getParameterKeyPairs();
}
