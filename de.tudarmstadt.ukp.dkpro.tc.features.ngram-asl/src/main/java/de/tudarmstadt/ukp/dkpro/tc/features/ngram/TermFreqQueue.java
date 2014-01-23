package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import org.apache.lucene.util.PriorityQueue;

/**
 * Priority queue for TermFreqTuples ordered by termFreq
 **/
public class TermFreqQueue
    extends PriorityQueue<TermFreqTuple>
{
    TermFreqQueue(int size)
    {
        super(size);
    }

    @Override
    protected boolean lessThan(TermFreqTuple a, TermFreqTuple b)
    {
        return a.getFreq() < b.getFreq();
    }
}