package de.tudarmstadt.ukp.dkpro.tc.examples.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.ibm.icu.text.CharsetDetector;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class NERUnitDemoReader extends ResourceCollectionReaderBase implements TCReaderSequence {

	private ArrayList<String> tokens = new ArrayList<String>();
	private HashMap<String, String> spanToToken = new HashMap<String, String>();
	private ArrayList<String> firstLabels = new ArrayList<String>();
	private HashMap<String, String> spanToFirstLabel = new HashMap<String, String>();
	private int filesCounter = 0;
	

	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		
		String fileName = "";
		tokens.clear();
		spanToToken.clear();
		firstLabels.clear();
		spanToFirstLabel.clear();
		
		filesCounter++;
		
		ResourceCollectionReaderBase.Resource res = nextFile();
		initCas(aCAS, res);
		URI resourceUri = res.getResolvedUri();
		
		try {
			for (String line : FileUtils
					.readLines(new File(resourceUri))) {
				String[] parts = line.split("\t");
	
				if (parts.length < 2) {
					throw new IOException("Wrong file format in line: " + line);
				}
	
				if (parts[0].equals("#")) {
					fileName = parts[1];
				} else {
					tokens.add(parts[1]);
					firstLabels.add(parts[2]);
				}
			}
		} catch (IOException e) {
			throw new IOException(e);
		} 
		
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw new CollectionException();
		}
		
		String documentText = detokenize();
		jcas.setDocumentText(documentText);
		new Sentence(jcas, 0, documentText.length()).addToIndexes();

		//set meta data
		DocumentMetaData dmd = DocumentMetaData.get(jcas);
		dmd.setDocumentTitle(fileName);
		dmd.setDocumentUri(resourceUri.toASCIIString());
		dmd.setDocumentId(String.valueOf(filesCounter));
		
		for (String span : spanToToken.keySet()) {
			String[] beginAndEndPositions = span.split(" ");
			int begin = Integer.valueOf(beginAndEndPositions[0]);
			int end = Integer.valueOf(beginAndEndPositions[1]);
			new Token(jcas, begin, end).addToIndexes();
			
			TextClassificationUnit unit = new TextClassificationUnit(jcas, begin, end);
	        unit.addToIndexes();
	        
	        TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, begin, end);
	        outcome.setOutcome(getTextClassificationOutcome(jcas, unit));
	        outcome.addToIndexes();
		}
	}


	@Override
	public String getTextClassificationOutcome(JCas jcas, TextClassificationUnit unit)
			throws CollectionException {
		
		String key = String.valueOf(unit.getBegin()) + " " + String.valueOf(unit.getEnd());
        return spanToFirstLabel.get(key);
	}
	
	
	/*
	 * a relative simple rule-based detokenizer
	 */
	private String detokenize() {
		int begin = 0;
		int end = 0;
		int size = tokens.size();
		StringBuilder tokenizedText = new StringBuilder();		
		
		String regexToLeft = "\\?|!|,|:|\\.|\\)";
		String regexToRight = "\\(";
		boolean quoteBegun = false;
	
		for (int i=0; i<size - 1; i++) {
			String currentToken = tokens.get(i);
			String nextToken = tokens.get(i+1);
			
			tokenizedText.append(currentToken);
			end = begin + currentToken.length() - 1;
			String key = String.valueOf(begin) + " " + String.valueOf(end + 1);
			spanToToken.put(key, currentToken);
			spanToFirstLabel.put(key, firstLabels.get(i));
			begin += currentToken.length();
			
			if (currentToken.matches(regexToRight) || nextToken.matches(regexToLeft) 
					|| (currentToken.equals("\"") && quoteBegun)){
				//do nothing
			} else if (nextToken.equals("\"")){
				if (!quoteBegun){
					begin += 1;
					tokenizedText.append(" ");
					quoteBegun = true;	
				}else{
					quoteBegun = false;
				}				
			} else{
				begin += 1;
				tokenizedText.append(" ");
			}
		}
		String currentToken = tokens.get(size-1);
		tokenizedText.append(currentToken);
		end = begin + currentToken.length() - 1;
		String key = String.valueOf(begin) + " " + String.valueOf(end + 1);
		spanToToken.put(key, currentToken);
		spanToFirstLabel.put(key, firstLabels.get(size-1));

		return tokenizedText.toString();
	}
	
}



