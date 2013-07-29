package de.tudarmstadt.ukp.dkpro.tc.experiments.usefulcomments.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;

public class CommentsReaderTest {

	private static double epsilon = .000001;

	@Test
	public void testGetComments() throws ResourceInitializationException, IOException {
		CommentsReader reader = new CommentsReader();
		Map<String, File> comments = reader.getComments("src/main/resources/data/corpus");

		assertNotNull(comments);
		assertEquals(34150, comments.size());
		assertTrue(comments.containsKey("47267889-5803890151-72157626776764029"));
		File testFile = comments.get("47267889-5803890151-72157626776764029");
		assertNotNull(testFile);
		assertEquals("AllComment", testFile.getParentFile().getName());
		assertEquals("IR-72157627314344362-person", testFile.getParentFile().getParentFile().getName());
	}

	@Test
	public void testGetAnnotations() throws ResourceInitializationException, IOException {
		CommentsReader reader = new CommentsReader();
		Map<String, Double> annotations = reader.getAnnotations("src/main/resources/data/Useful-NonUseful-Comments");

		assertNotNull(annotations);
		assertEquals(2525, annotations.size());
		assertTrue(annotations.containsKey("47267889-5803890151-72157626776764029"));
		assertEquals(0d, annotations.get("47267889-5803890151-72157626776764029"), epsilon);
		assertEquals(0.766666667d, annotations.get("8602872-2163451008-72157604147121763"), epsilon);
		assertEquals(0.3d, annotations.get("8602872-2162645323-72157616212517553"), epsilon);
		assertEquals(0d, annotations.get("8602872-3238476581-72157613551217678"), epsilon);

	}

	@Test
	@Ignore
	public void testCompleteness() throws ResourceInitializationException, IOException {

		CommentsReader reader = new CommentsReader();
		Map<String, File> comments = reader.getComments("src/main/resources/data/corpus");
		Map<String, Double> annotations = reader.getAnnotations("src/main/resources/data/Useful-NonUseful-Comments");

		//		int counter = 1;
		for(String annotation : annotations.keySet()){			
			//			if(!comments.containsKey(annotation)){
			//				System.out.println(counter++);
			//			}
			assertTrue(comments.containsKey(annotation));
		}

	}

	@Test
	public void getNextTest() throws UIMAException, IOException{
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				CommentsReader.class,
				CommentsReader.PARAM_ANNOTATIONS_PATH, "src/main/resources/data/Useful-NonUseful-Comments",
				CommentsReader.PARAM_CORPUS_PATH, "src/main/resources/data/corpus",
				CommentsReader.PARAM_USEFUL_COMMENT_THRESHOLD, 0.5f,
				CommentsReader.PARAM_NOT_USEFUL_COMMENT_THRESHOLD, 0.5f,
				CommentsReader.PARAM_LANGUAGE, "en");

		String expectedDocument = "I love the Pantheon of Rome :-)   ";

		int i = 0;
		for (JCas jcas : new JCasIterable(reader)) {
			if (i==0) {
				String document = jcas.getDocumentText();
				assertEquals(expectedDocument, document);
				assertEquals("useful", JCasUtil.selectSingle(jcas, TextClassificationOutcome.class).getOutcome());
			}
			i++;
		}
		assertEquals(2502,i);

	}


}
