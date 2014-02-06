package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

public class TermFreqTuple
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
}