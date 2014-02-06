package de.tudarmstadt.ukp.dkpro.tc.core.io;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.JCasIterator;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

/**
 * Tests for ClassificationUnitCasMultiplier
 * 
 * @author Artem Vovk
 * 
 */
public class ClassificationUnitCasMultiplierTest
{

    private static AnalysisEngine engine;

    @BeforeClass
    public static void setUp()
        throws ResourceInitializationException
    {
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription multiplier = createEngineDescription(ClassificationUnitCasMultiplier.class);
        AggregateBuilder ab = new AggregateBuilder();

        ab.add(segmenter);
        ab.add(multiplier);
        AnalysisEngineDescription desc = ab.createAggregateDescription();
        desc.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);

        engine = AnalysisEngineFactory.createEngine(desc);
        engine.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
    }

    @Test
    public void testEmpty()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        JCas jCas = createNewJCasWithText("Test String");
        JCasIterator it = engine.processAndOutputNewCASes(jCas);
        assertEquals("The JCasIterator should not have CASes", false, it.hasNext());
    }

    @Test
    public void testOneCas()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        createAndCheckNCases(1);
    }

    @Test
    public void testHundredCases()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        createAndCheckNCases(100);
    }

    private void createAndCheckNCases(int n)
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = StringUtils.leftPad("s", n, 'x');
        JCas jCas = createNewJCasWithText(text);

        for (int i = 0; i < n; i++) {
            createAnnotationForCas(jCas, i);
        }

        JCasIterator it = engine.processAndOutputNewCASes(jCas);
        int i = 0;
        while (it.hasNext()) {
            JCas a = it.next();
            Collection<TextClassificationOutcome> c = JCasUtil.select(a,
                    TextClassificationOutcome.class);
            assertEquals("The CAS should have only one TextClassificationOutcome annotation",
                    c.size(), 1);
            TextClassificationOutcome out = c.iterator().next();
            assertEquals("TextClassificationOutcome is wrong", String.valueOf(i), out.getOutcome());
            a.release();
            i++;
        }
        assertEquals("Expected amount of CASes is wrong", n, i);
    }

    private JCas createNewJCasWithText(String text)
        throws ResourceInitializationException
    {
        JCas jCas = engine.newJCas();
        DocumentMetaData d = new DocumentMetaData(jCas);
        d.setDocumentId("Test Doc");
        d.setDocumentUri("Fake URI");
        d.addToIndexes();
        jCas.setDocumentText(text);
        return jCas;
    }

    private void createAnnotationForCas(JCas jCas, int index)
    {
        TextClassificationUnit unit = new TextClassificationUnit(jCas);
        unit.setBegin(index);
        unit.setEnd(index + 1);
        unit.addToIndexes();
        TextClassificationOutcome out = new TextClassificationOutcome(jCas);
        out.setBegin(index);
        out.setEnd(index + 1);
        out.setOutcome(String.valueOf(index));
        out.addToIndexes();
    }
}
