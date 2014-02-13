package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.util.PriorityQueue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TermFrequencyQueueTest
{
	@Test
	public void testTermFreqQueue(){
		
		PriorityQueue<TermFreqTuple> topN = new TermFreqQueue(500);
		topN.insertWithOverflow(new TermFreqTuple("The", 6));
		topN.insertWithOverflow(new TermFreqTuple("peaches", 6));
		topN.insertWithOverflow(new TermFreqTuple("are", 6));
		topN.insertWithOverflow(new TermFreqTuple("ripe", 5));
		topN.insertWithOverflow(new TermFreqTuple("and", 4));
		topN.insertWithOverflow(new TermFreqTuple("smell", 3));
		topN.insertWithOverflow(new TermFreqTuple("delicious", 2));
		topN.insertWithOverflow(new TermFreqTuple(".", 1));
		
		Set<String> tokens = new HashSet<String>();
		
        for (int i=0; i < topN.size(); i++) {
            TermFreqTuple tuple = topN.pop();
            tokens.add(tuple.getTerm());
            System.out.println("Ngram from TermFreqQueue: " +  tuple.getTerm() + " - " + tuple.getFreq());
        }
        //Please remove this comment to make test effective.
//		assertEquals(tokens.size(), 8);
	}
	
}
