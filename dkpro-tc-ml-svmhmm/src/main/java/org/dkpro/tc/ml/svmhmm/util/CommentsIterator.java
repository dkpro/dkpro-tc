/*
 * Copyright 2017
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
package org.dkpro.tc.ml.svmhmm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class CommentsIterator implements Iterator<List<String>> {
	private BufferedReader br =null;
	private String line = null;
	
	public CommentsIterator(File f) throws IOException{
		br = new BufferedReader(
				new InputStreamReader(new FileInputStream(f)));
	
	}
		@Override
		public boolean hasNext() {
			try {
				line = br.readLine();
			} catch (IOException e) {
			    throw new UnsupportedOperationException(new IOException(e));
			}

			boolean result = line != null;
			if (!result) {
				try {
					br.close();
				} catch (IOException e) {
				    throw new UnsupportedOperationException(new IOException(e));
				}
			}

			return result;
		}

		@Override
		public List<String> next() {

			String comment=null;
			try {
				comment = extractComment(line);
			} catch (Exception e1) {
			    throw new UnsupportedOperationException(e1);
			}

			List<String> list = new ArrayList<>();

			String[] tokens = comment.split("\\s+");
			// filter empty tokens
			for (String token : tokens) {
				String trim = token.trim();
				if (!trim.isEmpty()) {
					// decode from URL representation
					String s = null;
					try {
						s = URLDecoder.decode(trim, "utf-8");
					} catch (UnsupportedEncodingException e) {
					    throw new UnsupportedOperationException(e);
					}
					list.add(s);
				}
			}
			return list;
		}

		private String extractComment(String rawLine) throws Exception {
			// Ideally there is only one # unless the gold label is #
			// then we have something like [# qid:317 3:1 # # 317 %23]

			// Normal case
			int firstCandidate = rawLine.lastIndexOf("#");
			int countMatches = StringUtils.countMatches(rawLine, "#");
			if(countMatches==1){
				return rawLine.substring(firstCandidate+1);
			}
			
			// Occurrence of two # as in [O qid:5 2:7 # #IFTHEN O 5]
			int secondCandidate = rawLine.lastIndexOf("#", firstCandidate);
			if(countMatches==2){
				return rawLine.substring(secondCandidate);					
			}
			
			// Is there an # before the one we just found and the first
			// character in the line is #, too 
			if (countMatches==3 && rawLine.charAt(0) == '#') {
				return rawLine.substring(secondCandidate);
			}

			throw new Exception("Encountered an unexpected case when extracting the comments");

		}
}
