package de.tudarmstadt.ukp.dkpro.tc.demo.pairtwentynewsgroups.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.io.TCReaderSingleLabel;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;


public class PairTwentyNewsgroupsReader
    extends AbstractPairReader
    implements TCReaderSingleLabel
{
	
    public static final String PARAM_LISTFILE = "inputFileName";
    @ConfigurationParameter(name = PARAM_LISTFILE, mandatory = true)
    protected String inputListName;

    public static final String PARAM_LANGUAGE2 = "Language2";
    @ConfigurationParameter(name = PARAM_LANGUAGE2, mandatory = true)
    protected String language2;
	
	private File doc1; // to avoid repetition
	private File doc2;
	
	public List<List<String>> listOfFiles;
	protected int currentParsedFilePointer;
	
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        try{
        	listOfFiles = readFileToLists(inputListName);
        } catch (Exception e){
        	throw new ResourceInitializationException(e);
        }
        currentParsedFilePointer = 0;
    }
	
    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
    	return currentParsedFilePointer < listOfFiles.size();
    }
    
    @Override
    public Progress[] getProgress()
    {
        return new Progress[] { new ProgressImpl(currentParsedFilePointer, listOfFiles.size(),
                Progress.ENTITIES) }; // i.e., we're on number 6 out of 10 total
    }

    @Override
    protected String getCollectionId()
    {
        return doc1.getParentFile().getParentFile().getParentFile().getName();
    }

    @Override
    protected String getLanguage()
    {
        return language2;
    }

    @Override
    protected String getInitialViewText()
    {
        return null;
    }

    @Override
    protected String getInitialViewDocId()
    {
        return doc1.getParentFile().getName() + "/" + doc1.getName() + "_" + doc2.getParentFile().getName() + "/"
                + doc2.getName();
    }

    @Override
    protected String getInitialViewTitle()
    {

        return doc1.getParent() + "/" + doc1.getName() + "_" + doc2.getParent() + "/"
                + doc2.getName();
    }

    @Override
    protected String getBaseUri()
    {

        return doc1.getParent() + "_" + doc2.getParent();
    }

    @Override
    protected String getText(String part)
        throws IOException
    {
        if (part.equals("PART_ONE")) {
            return FileUtils.readFileToString(doc1);
        }
        else if (part.equals("PART_TWO")) {
            return FileUtils.readFileToString(doc2);
        }
        return null;
    }

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
		doc1 = new File(listOfFiles.get(currentParsedFilePointer).get(0));
		doc2 = new File(listOfFiles.get(currentParsedFilePointer).get(1));
		
        super.getNext(jcas);

        TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
        String outcomeString = getTextClassificationOutcome(jcas);
        outcome.setOutcome(outcomeString);
        outcome.addToIndexes();

        currentParsedFilePointer++;
    }

    @Override
    public String getTextClassificationOutcome(JCas jcas)
        throws CollectionException
    {
        return listOfFiles.get(currentParsedFilePointer).get(2);
    }
    
   private static List<List<String>> readFileToLists(String fileLocationString) throws IOException {
        
        File fileLocation = new File(fileLocationString);
        List<List<String>> returnList = new ArrayList<List<String>>();
        for(String line : FileUtils.readLines(fileLocation)) {
            line = line.replace("\n", "");
            List<String> lineList = new ArrayList<String>();
            for(String word: line.split("\t")){
                lineList.add(word);
            }
            if(lineList.size() > 1){
                returnList.add(lineList);
            }
        }
        
        return returnList;
    }
}
