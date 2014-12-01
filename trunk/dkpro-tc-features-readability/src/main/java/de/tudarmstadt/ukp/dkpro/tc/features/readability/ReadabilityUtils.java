package de.tudarmstadt.ukp.dkpro.tc.features.readability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ReadabilityUtils

{
    public static boolean isAuxiliaryVerbEn(String verb)
    {
        // following the list at http://en.wikipedia.org/wiki/Auxiliary_verb

        String[] auxVerbs = new String[] { "be", "am", "are", "is", "was", "were", "being", "been",
                "can", "could", "dare", "do", "does", "did", "have", "has", "had", "having", "may",
                "might", "must", "need", "ought", "shall", "should", "will", "would"

        };
        return Arrays.asList(auxVerbs).contains(verb);
    }

    public static boolean isModalVerbEn(String verb)
    { // following the list at http://en.wikipedia.org/wiki/English_modal_verbs
        String[] modalVerbs = new String[] { "can", "could", "might", "may", "must", "should",
                "will", "would", "shall" };
        return Arrays.asList(modalVerbs).contains(verb);
    }

    // This is the same check for words as in the readability measures but it also includes hyphens
    // when they occur in a word e.g. color-blind
    public static boolean isWord(String strWord)
    {
        for (int i = 0; i < strWord.length(); ++i) {
            char ch = strWord.charAt(i);
            if (!Character.isLetterOrDigit(ch)) {
                if (strWord.length() == 1) {
                    return false;
                }
                else {
                    if (ch != '-') {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }

    public static boolean isWord(Token tok)
    {
        return isWord(tok.getCoveredText());
    }

    public static boolean isLexicalWordEn(Token token)
    {
        POS p = token.getPos();
        boolean rightPos = (p instanceof N || p instanceof V || p instanceof ADJ || p instanceof ADV);
        return (rightPos && !isModalVerbEn(token.getCoveredText()) && !isAuxiliaryVerbEn(token
                .getCoveredText()));
    }

    public static boolean isLexicalWord(Token token)
    {
        POS p = token.getPos();
        return (p instanceof N || p instanceof V || p instanceof ADJ || p instanceof ADV);

    }

    // This code is copied from
    // src/main/java/de/tudarmstadt/ukp/similarity/algorithms/style/MTLDComparator.java
    // BETTER: make it public there
    public static double getMTLD(JCas jcas, boolean reverse, double mtldThreshold)
    {
        double factors = 0.0;

        DocumentAnnotation doc1 = new ArrayList<DocumentAnnotation>(JCasUtil.select(jcas,
                DocumentAnnotation.class)).get(0);
        List<Lemma> lemmas = new ArrayList<Lemma>(JCasUtil.selectCovered(jcas, Lemma.class, doc1));

        // Initialize tokens and types
        List<String> tokens = new ArrayList<String>();
        Set<String> types = new HashSet<String>();

        // Reverse lemmas if flag is set
        if (reverse) {
            Collections.reverse(lemmas);
        }

        for (int i = 0; i < lemmas.size(); i++) {
            Lemma lemma = lemmas.get(i);

            try {
                types.add(lemma.getValue().toLowerCase());
                tokens.add(lemma.getCoveredText().toLowerCase());
            }
            catch (NullPointerException e) {
                System.out.println("Couldn't add token: " + lemma.getCoveredText());
            }

            double ttr = new Integer(types.size()).doubleValue()
                    / new Integer(tokens.size()).doubleValue();

            if (ttr < mtldThreshold) {
                // Reset types and tokens
                tokens.clear();
                types.clear();

                // Increment full factor count
                factors++;
            }
            else if (i == lemmas.size() - 1) {
                // If the end of lemma list is reached, and no full factor is reached,
                // add a incomplete factor score

                double ifs = (1.0 - ttr) / (1.0 - mtldThreshold);
                factors += ifs;
            }
        }

        // mtld = number of tokens divided by factor count
        double mtld = (factors == 0) ? (0.0) : (new Integer(lemmas.size()).doubleValue() / factors);

        return mtld;
    }
}
