package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.LuceneNGramCPFE;

public class LuceneNGramCPMetaCollectorTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void combinedNgramPairMetaCollectorTest()
        throws Exception
    {
        File tmpDir = folder.newFolder();

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestPairReader.class,
                TestPairReader.PARAM_INPUT_FILE, "src/test/resources/data/textpairs.txt"
                );

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AggregateBuilder builder = new AggregateBuilder();
        builder.add(segmenter, AbstractPairReader.INITIAL_VIEW, AbstractPairReader.PART_ONE);
        builder.add(segmenter, AbstractPairReader.INITIAL_VIEW, AbstractPairReader.PART_TWO);

        AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(
                LuceneNGramCPMetaCollector.class,
                LuceneNGramCPFE.PARAM_LUCENE_DIR, tmpDir
                );

        // test fails if for-loop removed
        for (@SuppressWarnings("unused")
        JCas jcas : new JCasIterable(reader, builder.createAggregateDescription(),
                metaCollector)) {
            // System.out.println(jcas.getDocumentText().length());
        }

        int i = 0;
        IndexReader index;
        try {
            index = DirectoryReader.open(FSDirectory.open(tmpDir));
            Fields fields = MultiFields.getFields(index);
            if (fields != null) {
                Terms terms = fields.terms(LuceneNGramCPFE.LUCENE_NGRAM_FIELDCOMBO);
                if (terms != null) {
                    TermsEnum termsEnum = terms.iterator(null);

                    BytesRef text = null;
                    while ((text = termsEnum.next()) != null) {
                        // System.out.println(text.utf8ToString() + " - " +
                        // termsEnum.totalTermFreq());
                        // System.out.println(termsEnum.docFreq());

                        // if there were multiple instances of the same ngram,
                        // then this would be relevant
                        if (text.utf8ToString().equals("mice_ANDcats_.")) {
                            assertEquals(1, termsEnum.docFreq());
                            assertEquals(1, termsEnum.totalTermFreq());
                        }
                        i++;
                    }
                }
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

        assertEquals(65, i);
    }
}
