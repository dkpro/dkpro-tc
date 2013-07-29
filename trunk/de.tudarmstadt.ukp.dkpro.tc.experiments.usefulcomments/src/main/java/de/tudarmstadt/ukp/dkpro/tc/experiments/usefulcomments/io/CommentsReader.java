package de.tudarmstadt.ukp.dkpro.tc.experiments.usefulcomments.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class CommentsReader
extends JCasCollectionReader_ImplBase
{

	/**
	 * The language of the corpus
	 */
	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
	protected String language;

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

	/**
	 * The lower threshold of scores for useful comments 
	 * Must be higher than or equal to the threshold for none useful comments
	 */
	public static final String PARAM_USEFUL_COMMENT_THRESHOLD = "UsefulCommentThreshold";
	@ConfigurationParameter(name = PARAM_USEFUL_COMMENT_THRESHOLD, mandatory = true)
	protected float usefulCommentThreshold;

	/**
	 * The upper threshold of scores for none useful comments
	 * Must be lower than or equal to the threshold for useful comments
	 */
	public static final String PARAM_NOT_USEFUL_COMMENT_THRESHOLD = "NotUsefulCommentThreshold";
	@ConfigurationParameter(name = PARAM_NOT_USEFUL_COMMENT_THRESHOLD, mandatory = true)
	protected float notUsefulCommentThreshold;

	private Map<String, Double> annotations;
	private Map<String, File> comments;
	private Iterator<String> annotationIterator;
	private int counter;

	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		try {
			annotations = getAnnotations(annotationsPath);
			comments = getComments(corpusPath);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}

		//Remove annotations without comments
		List<String> annotationsToRemove = new ArrayList<String>();
		for(String annotation : annotations.keySet()){
			if(!comments.containsKey(annotation)){
				annotationsToRemove.add(annotation);
			}
		}
		for(String toRemove : annotationsToRemove){
			annotations.remove(toRemove);
		}

		annotationIterator = annotations.keySet().iterator();
		counter = 0;

		//Are threshold set correctly?
		if(notUsefulCommentThreshold > usefulCommentThreshold){
			throw new ResourceInitializationException();
		}

	}

	Map<String, Double> getAnnotations(String annotationsPath) throws IOException{
		Map<String, Double> annotations = new HashMap<String, Double>();

		int duplicateCounter = 1;

		File annotationsFile = new File(annotationsPath);
		for(File file : annotationsFile.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if(file.getName().endsWith(".csv")){
					return true;
				}
				else{
					return false;
				}
			}
		})){
			String[] splitLine;
			for(String line : FileUtils.readLines(file)){
				if(!line.equals("annoID,judgeValue")){
					splitLine = line.split(",");

					if(annotations.containsKey(splitLine[0])){
						System.err.println("Duplicated id + (" + duplicateCounter++ + ")");
						if(Double.valueOf(splitLine[1]).doubleValue() != annotations.get(splitLine[0]).doubleValue()){
							System.err.println(Double.valueOf(splitLine[1]));
							System.err.println(annotations.get(splitLine[0]));
						}
						annotations.remove(splitLine[1]);

					}
					else{
						annotations.put(splitLine[0], Double.valueOf(splitLine[1]));						
					}
				}
			}

		}

		return annotations;
	}

	Map<String, File> getComments(String corpusPath) throws ResourceInitializationException, IOException {
		Map<String, File> comments = new HashMap<String, File>();
		//		int duplicatedId = 1;

		File corpus = new File(corpusPath);
		for(File file : corpus.listFiles()){
			if(file.isDirectory()){
				for(File subCorpusFile :file.listFiles(new FileFilter() {

					@Override
					public boolean accept(File file) {
						return file.getName().equals("AllComment");
					}
				})){
					for(File commentFile : subCorpusFile.listFiles()){

						if(comments.containsKey(getId(commentFile))){
							//							System.err.println("Duplicated comment id (" + duplicatedId++ + ")");
							if(!FileUtils.readFileToString(commentFile).equals(FileUtils.readFileToString(comments.get(getId(commentFile))))){
								throw new ResourceInitializationException();
								//								System.err.println(commentFile.getAbsolutePath());
								//								System.err.println(comments.get(getId(commentFile)).getAbsolutePath());
							}
						}
						comments.put(getId(commentFile), commentFile);
					}
				}
			}
		}

		return comments;
	}

	private String getId(File file){
		return file.getName().replace(".txt", "");
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[]{new ProgressImpl(counter, annotations.size(), "annotations")};
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return annotationIterator.hasNext();
	}

	@Override
	public void getNext(JCas jcas) throws IOException, CollectionException {

		String id = annotationIterator.next();
		counter++;
		File commentFile = comments.get(id);

		jcas.setDocumentLanguage(language);

		DocumentMetaData dmd = DocumentMetaData.create(jcas);
		dmd.setCollectionId(corpusPath);
		dmd.setDocumentBaseUri(commentFile.getParentFile().getAbsolutePath());
		dmd.setDocumentTitle(id);
		dmd.setDocumentUri(commentFile.getAbsolutePath());
		dmd.setDocumentId(commentFile.getName());
		dmd.setLanguage(language);
	
		jcas.setDocumentText(FileUtils.readFileToString(commentFile));

		//Set outcome whether the score is above the 
		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
		double score = annotations.get(id);
		if(score >= usefulCommentThreshold){
			outcome.setOutcome("useful");
		}
		else if(score <= notUsefulCommentThreshold){
			outcome.setOutcome("not_useful");
		}
		else{
			outcome.setOutcome("undefined");
		}
		outcome.addToIndexes();
	}

}