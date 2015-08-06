package de.tudarmstadt.ukp.dkpro.tc.examples.initializer;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.core.initializer.SingleLabelOutcomeAnnotator_ImplBase;

public class TwentyNewsgroupsOutcomeAnnotator 
	extends SingleLabelOutcomeAnnotator_ImplBase
{

	@Override
	public String getTextClassificationOutcome(JCas jcas)
			throws AnalysisEngineProcessException
	{
		try {
            String uriString = DocumentMetaData.get(jcas).getDocumentUri();
            return new File(new URI(uriString).getPath()).getParentFile().getName();
        }
        catch (URISyntaxException e) {
            throw new AnalysisEngineProcessException(e);
        }
	}
}