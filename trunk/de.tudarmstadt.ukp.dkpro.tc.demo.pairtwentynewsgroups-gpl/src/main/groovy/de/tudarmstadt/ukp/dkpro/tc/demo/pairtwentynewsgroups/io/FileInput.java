package de.tudarmstadt.ukp.dkpro.tc.demo.pairtwentynewsgroups.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FileInput
{
    public static List<List<String>> readFileToLists(String fileLocationString) throws IOException {
    	
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