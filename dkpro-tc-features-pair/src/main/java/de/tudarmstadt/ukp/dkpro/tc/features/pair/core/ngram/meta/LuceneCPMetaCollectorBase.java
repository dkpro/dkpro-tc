package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.LuceneNGramCPFE;

public abstract class LuceneCPMetaCollectorBase
    extends LucenePMetaCollectorBase
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
        for (String ngram1: view1NGrams.getKeys()){
            for (String ngram2: view2NGrams.getKeys()){

                int combinedSize = ngram1.split(NGramUtils.NGRAM_GLUE).length 
                        + ngram2.split(NGramUtils.NGRAM_GLUE).length;
                if (combinedSize <= getNgramMaxNCombo()
                        && combinedSize >= getNgramMinNCombo()) {
                    // set count = 1, for doc freq and not total term freq
                	long count = view1NGrams.getCount(ngram1) * view2NGrams.getCount(ngram2);
                	for(int i=0;i<count;i++){
	                    addField(jcas, 
	                            getFieldNameCombo(),
	                            ngram1 + ComboUtils.JOINT + ngram2);
                	}
                }
            }
        }
        
        try {
            writeToIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }   
    
    protected abstract int getNgramMinNCombo();
    protected abstract int getNgramMaxNCombo();
    protected abstract String getFieldNameCombo();

}
