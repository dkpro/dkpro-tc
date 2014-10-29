package de.tudarmstadt.ukp.dkpro.tc.core.feature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class UnitContextMetaCollector 
	extends MetaCollector
{
	
    public static final String PARAM_UNIT_CONTEXT_FILE = "unitContextFile";
    @ConfigurationParameter(name = PARAM_UNIT_CONTEXT_FILE, mandatory = true)
    private File unitContextFile;
    
	public static final String UNIT_CONTEXT_KEY = "unitContext.txt";
	
	public static final int CONTEXT_WIDTH = 30;
	
	public static final String CONTEXT_SEPARATOR = "###";
	
	private StringBuilder sb;

	@Override
	public Map<String, String> getParameterKeyPairs() {	    
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(PARAM_UNIT_CONTEXT_FILE, UNIT_CONTEXT_KEY);
        return mapping;
	}

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException
	{
		super.initialize(context);
		
		sb = new StringBuilder();
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
  
        for (TextClassificationUnit unit : JCasUtil.select(jcas, TextClassificationUnit.class)) {
            String[] context = new String[4];
            context[0] = DocumentMetaData.get(jcas).getDocumentId();
        	context[1] = getLeftContext(jcas, unit);
        	context[2] = unit.getCoveredText();
        	context[3] = getRightContext(jcas, unit);
            sb.append(StringUtils.join(context, CONTEXT_SEPARATOR));
            sb.append("\n");
        }
	}

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException
	{
		super.collectionProcessComplete();
		
		try {
			FileUtils.writeStringToFile(unitContextFile, sb.toString());
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
	
	private String getLeftContext(JCas jcas, TextClassificationUnit unit) {
		int leftOffset = unit.getBegin() - CONTEXT_WIDTH;
		
		if (leftOffset < 0) {
			leftOffset = 0;
		}
		
		return jcas.getDocumentText().substring(leftOffset, unit.getBegin());
	}
	
	private String getRightContext(JCas jcas, TextClassificationUnit unit) {
		int rightOffset = unit.getEnd() + CONTEXT_WIDTH;
		
		if (rightOffset > jcas.getDocumentText().length()) {
			rightOffset = jcas.getDocumentText().length();
		}
		
		return jcas.getDocumentText().substring(unit.getEnd(), rightOffset);
	}
}