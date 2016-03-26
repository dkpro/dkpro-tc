/*
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dkpro.tc.svmhmm.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.svmhmm.util.SVMHMMUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SVMHMMUtilsTest
{
	File normalCase=null;
	File trickyCase=null;
	File evenMoreTrickyCase=null;
	
	@Before
	public void setup() throws IOException{
		normalCase = new File(System.currentTimeMillis()+"normalCase.txt");
		FileUtils.writeStringToFile(normalCase, "VBD qid:318 3:6 127:1 134:1 135:1 235:1 336:1 337:1 338:1 497:1 696:1 703:1 # VBD 318 signed");
		
		trickyCase =  new File(System.currentTimeMillis()+"trickyCase.txt");
		FileUtils.writeStringToFile(trickyCase, "O qid:5 2:7 # #IFTHEN O 5");
		
		//example with several # that occurred when processing the WSJ
		evenMoreTrickyCase= new File(System.currentTimeMillis()+"evenMoreTrickyCase.txt");
		FileUtils.writeStringToFile(evenMoreTrickyCase, "# qid:317 3:1 # # 317 %23");
	}
	
	@After
	public void tearDown() {
		trickyCase.delete();
		evenMoreTrickyCase.delete();
		normalCase.delete();
	}
	
	  @Test
	    public void testNormalCaseExtractComments()
	            throws Exception
	    {
	        Iterator<List<String>> comments = SVMHMMUtils.extractComments(normalCase);

	        int countAll=0;
	        List<String> first=null;
	        while(comments.hasNext()){
	        	List<String> next = comments.next();
	        	if(countAll==0){
	        		first=next;
	        	}
	        	countAll++;
	        }
	        
	        assertEquals(1, countAll);
	        assertEquals("VBD", first.get(0));
	        assertEquals("318", first.get(1));
	        assertEquals("signed", first.get(2));
	    }
	
    @Test
    public void testTrickyCaseExtractComments()
            throws Exception
    {
        Iterator<List<String>> comments = SVMHMMUtils.extractComments(trickyCase);

        int countAll=0;
        List<String> first=null;
        while(comments.hasNext()){
        	List<String> next = comments.next();
        	if(countAll==0){
        		first=next;
        	}
        	countAll++;
        }
        
        assertEquals(1, countAll);
        assertEquals("#IFTHEN", first.get(0));
        assertEquals("O", first.get(1));
        assertEquals("5", first.get(2));
    }
    
    @Test
    public void testEvenMoreTrickyCaseExtractComments()
            throws Exception
    {
        Iterator<List<String>> comments = SVMHMMUtils.extractComments(evenMoreTrickyCase);

        int countAll=0;
        List<String> first=null;
        while(comments.hasNext()){
        	List<String> next = comments.next();
        	if(countAll==0){
        		first=next;
        	}
        	countAll++;
        }
        
        assertEquals(1, countAll);
        assertEquals("#", first.get(0));
        assertEquals("317", first.get(1));
        assertEquals("#", first.get(2)); //URL encoded version of [%23]
    }
}