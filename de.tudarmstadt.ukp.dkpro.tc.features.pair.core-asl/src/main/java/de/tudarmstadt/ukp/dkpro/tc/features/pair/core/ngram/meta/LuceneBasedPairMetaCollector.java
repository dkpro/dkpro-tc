package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneBasedMetaCollector;

public abstract class LuceneBasedPairMetaCollector
    extends LuceneBasedMetaCollector
{
    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        JCas view1;
        JCas view2;
        try{
            view1 = jcas.getView(AbstractPairReader.PART_ONE);
            view2 = jcas.getView(AbstractPairReader.PART_TWO);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        initializeDocument(jcas);
        
        List<JCas> jcases = new ArrayList<JCas>();
        jcases.add(view1);
        jcases.add(view2);

        FrequencyDistribution<String> view1NGrams;
        FrequencyDistribution<String> view2NGrams;
        FrequencyDistribution<String> documentNGrams;
        try{
            view1NGrams = getNgramsFDView1(view1);
            view2NGrams = getNgramsFDView2(view2);
            documentNGrams = getNgramsFD(jcases);
        }catch (TextClassificationException e){
            throw new AnalysisEngineProcessException(e);
        }


        for (String ngram : documentNGrams.getKeys()) {
            for (int i=0;i<documentNGrams.getCount(ngram);i++){
                addField(jcas, getFieldName(), ngram); 
            }
        }
        for (String ngram : view1NGrams.getKeys()) {
            for (int i=0;i<view1NGrams.getCount(ngram);i++){
                addField(jcas, getFieldNameView1(), ngram); 
            }
        }
        for (String ngram : view2NGrams.getKeys()) {
            for (int i=0;i<view2NGrams.getCount(ngram);i++){
                addField(jcas, getFieldNameView2(), ngram); 
            }
        }
        
        try {
            writeToIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }   
    protected abstract FrequencyDistribution<String> getNgramsFD(List<JCas> jcases)
            throws TextClassificationException;

    protected abstract FrequencyDistribution<String> getNgramsFDView1(JCas view1)
        throws TextClassificationException;

    protected abstract FrequencyDistribution<String> getNgramsFDView2(JCas view2)
        throws TextClassificationException;

    protected abstract String getFieldNameView1();
    protected abstract String getFieldNameView2();

}
