package de.tudarmstadt.ukp.dkpro.tc.experiments.usefulcomments.io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class CommentsReader
    extends JCasCollectionReader_ImplBase
{
	
	/**
	 * The path of the corpus
	 */
	public static final String PARAM_CORPUS_PATH = "CorpusPath";
	@ConfigurationParameter(name = PARAM_CORPUS_PATH, mandatory = true)
	protected String corpusPath;

	/**
	 * The path of the annotations
	 */
	public static final String PARAM_ANNOTATIONS_PATH = "AnnotationsPath";
	@ConfigurationParameter(name = PARAM_ANNOTATIONS_PATH, mandatory = true)
	protected String annotationsPath;

	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
            super.initialize();
            
            File usefulCommentsFile = new File(annotationsPath, "trained-Useful.csv");
            File nonUsefulCommentsFile = new File(annotationsPath, "trained-Non-Useful.csv");
            
            Map<String, File> comments = getComments(corpusPath);
            
            
            
            
    }

	Map<String, File> getComments(String corpusPath2) {
		Map<String, File> comments = new HashMap<String, File>();
		
        File corpus = new File(corpusPath);
        for(File file : corpus.listFiles()){
        	if(file.isDirectory()){
        		for(File commentFile :file.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File file, String arg1) {
						return file.getName().endsWith("AllComment");
					}
				})){
        			comments.put(commentFile.getName().replace(".txt", ""), commentFile);
        		}
        	}
        }
        
		return comments;
	}

	@Override
	public Progress[] getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void getNext(JCas jcas) throws IOException, CollectionException {
        DocumentMetaData dmd = DocumentMetaData.get(jcas);
        File parentFile;
        try {
            parentFile = new File(new URI(dmd.getDocumentUri()).getPath()).getParentFile();
            
            TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
            outcome.setOutcome(parentFile.getName());
            outcome.addToIndexes();
        }
        catch (URISyntaxException e) {
            throw new CollectionException(e);
        }
    }

}