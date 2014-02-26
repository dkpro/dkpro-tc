package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

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