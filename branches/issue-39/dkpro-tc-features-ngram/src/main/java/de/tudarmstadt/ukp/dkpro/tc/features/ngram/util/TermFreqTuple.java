/*******************************************************************************
 * Copyright 2014
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.features.ngram.util;

public class TermFreqTuple
    implements Comparable<TermFreqTuple>
{
    private String term;
    private long freq;
    
    public TermFreqTuple(String term, long freq)
    {
        super();
        this.term = term;
        this.freq = freq;
    }
    
    public String getTerm()
    {
        return term;
    }
    public void setTerm(String term)
    {
        this.term = term;
    }
    public long getFreq()
    {
        return freq;
    }
    public void setFreq(long freq)
    {
        this.freq = freq;
    }

    @Override
    public String toString()
    {
        return term + " - " + freq;
    }

    @Override
    public int compareTo(TermFreqTuple arg0)
    {
        if (this.freq < arg0.freq) {
            return 1;
        }
        else if (this.freq > arg0.freq) {
            return -1;    
        }
        else {
            return 0;
        }
    }   
}