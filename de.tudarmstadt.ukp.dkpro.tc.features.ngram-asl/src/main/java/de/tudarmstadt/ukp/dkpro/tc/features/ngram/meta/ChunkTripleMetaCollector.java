package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.NC;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.VC;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.ChunkTripleDFE;

public class ChunkTripleMetaCollector
    extends FreqDistBasedMetaCollector
{
    public static final String CHUNK_TRIPLE_FD_KEY = "chunkTriple.ser";

    @ConfigurationParameter(name = ChunkTripleDFE.PARAM_CHUNK_TRIPLE_FD_FILE, mandatory = true)
    private File chunkTripleFdFile;

    @ConfigurationParameter(name = ChunkTripleDFE.PARAM_CHUNK_TRIPLE_LOWER_CASE, mandatory = false, defaultValue = "true")
    private boolean chunkTripleLowerCase;

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        Set<String> triples = getTriples(jcas, chunkTripleLowerCase);
        fd.incAll(triples);
    }

    public static Set<String> getTriples(JCas jcas, boolean lowerCase)
    {
        Set<String> triples = new HashSet<String>();

        for (Chunk vc : JCasUtil.select(jcas, VC.class)) {
            String triple = getTriple(jcas, vc);
            if (lowerCase) {
                triple = triple.toLowerCase();
            }
            triples.add(triple);
        }

        return triples;
    }

    private static String getTriple(JCas jcas, Chunk vc)
    {
        List<NC> ncListLeft = JCasUtil.selectPreceding(jcas, NC.class, vc, 1);
        List<NC> ncListRight = JCasUtil.selectFollowing(jcas, NC.class, vc, 1);

        String ncStringLeft = getChunkString(jcas, ncListLeft);
        String ncStringRight = getChunkString(jcas, ncListRight);

        String vcString = getChunkString(jcas, Arrays.asList(vc));

        String tripleString;
        if (ncStringLeft.length() > 0 && ncStringRight.length() > 0) {
            // tripleString = ncStringLeft + "-" + ncStringRight;
            tripleString = ncStringLeft + "-" + vcString + "-" + ncStringRight;
        }
        else {
            tripleString = "";
        }

        return tripleString;
    }

    private static String getChunkString(JCas jcas, List<? extends Chunk> chunkList)
    {
        String chunkString = "";
        if (chunkList.size() > 0) {
            Chunk chunk = chunkList.get(0);

            // get rightmost lemma in chunk
            List<Lemma> lemmas = JCasUtil.selectCovered(jcas, Lemma.class, chunk);

            Set<String> lemmaStrings = new HashSet<String>();
            for (Lemma lemma : lemmas) {
                lemmaStrings.add(lemma.getValue());
            }
            chunkString = StringUtils.join(lemmaStrings, "_");

            // if (lemmas.size() > 0) {
            // chunkString = lemmas.get(lemmas.size()-1).getCoveredText();
            // }
        }
        return chunkString;
    }

    @Override
    public Map<String, String> getParameterKeyPairs()
    {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(ChunkTripleDFE.PARAM_CHUNK_TRIPLE_FD_FILE, CHUNK_TRIPLE_FD_KEY);
        return mapping;
    }

    @Override
    protected File getFreqDistFile()
    {
        return chunkTripleFdFile;
    }
}