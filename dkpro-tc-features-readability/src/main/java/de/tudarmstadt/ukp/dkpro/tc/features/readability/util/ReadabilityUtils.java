/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.features.readability.util;

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

    public static String[] getAdjectiveEndings(String documentLanguage)
    {

        if (documentLanguage.equals("en")) {
            return new String[] { "able", "ible", "al", "ant", "ent", "ar", "ed", "ful", "ic",
                    "ical", "ive", "less", "ory", "ous" };

        }
        if (documentLanguage.equals("de")) {
            return new String[] { "abel", "al", "arm", "artig", "bar", "echt", "eigen", "ens",
                    "er", "fach", "fähig", "fern", "fest", "frei", "gemäß", "gerecht", "getreu",
                    "haft", "halber", "haltig", "ig", "isch", "leer", "lich", "los", "mal", "mals",
                    "maßen", "mäßig", "nah", "ös", "reich", "reif", "s", "sam", "schwer", "seits",
                    "tel", "trächtig", "tüchtig", "voll", "wärts", "weise", "wert", "würdig" };
        }
        if (documentLanguage.equals("fr")) {
            return new String[] { "ien", "ienne ", "éen", "éenne ", "in", "ine ", "ais", "aise ",
                    "ois", "oise ", "ain", "aine ", "an", "ane ", "ite ", "esque ", "ique",
                    "aïque ", "al", "ale ", "el", "elle ", "if", "ive ", "aire ", "eux", "euse ",
                    "ueux", "ueuse ", "ique ", "atique ", "ier", "ière ", "escent", "escente ",
                    "in", "ine" };
        }
        else {
            return new String[] {};
        }
    }

    public static String[] getInflectedHelpverbs(String documentLanguage)
    {
        if (documentLanguage.equals("en")) {
            return new String[] { "am", "are", "is", "was", "were", "be", "have", "has" };
        }
        if (documentLanguage.equals("de")) {
            return new String[] { "bin", "bist", "ist", "seid", "sind", "habe", "hast", "hat",
                    "haben", "habt" };
        }
        else {
            return new String[] {};
        }
    }

    public static String[] getHelpverbs(String documentLanguage)
    {
        if (documentLanguage.equals("en")) {
            return new String[] { "am", "are", "is", "was", "were", "be", "been", "have", "has",
                    "had", "can", "could", "may", "will", "would", "might", "give", "gave" };
        }

        if (documentLanguage.equals("de")) {
            return new String[] { "habe", "hast", "hat", "haben", "habt", "hatte", "hattest",
                    "hatten", "hattet", "gehabt", "bin", "war", "bist", "warst", "ist", "war",
                    "sind", "waren", "seid", "wart", "gewesen", "werde", "wurde", "wirst", "warst",
                    "wirt", "werden", "wurden", "werdet", "wurdet" };
        }
        if (documentLanguage.equals("fr")) {
            return new String[] { "ai", "as", "a", "avons", "avez", "ont", "avais", "avait",
                    "avions", "aviez", "avaient", "aurai", "auras", "aura", "aurons", "aurez",
                    "auront", "aurais", "aurait", "aurions", "auriez", "auraient", "eusse",
                    "eusses", "eût", "eussions", "eussiez", "eussent", "eus", "eut", "eûmes",
                    "eûtes", "eurent", "suis", "es", "est", "sommes", "êtes", "sont", " étais",
                    "était", "étions", "étiez", "étaient", "fus", "fus", "fut", "fûmes", "fûtes",
                    "furent", "serai", "seras", "sera", "serons", "serez", "seront", "sois",
                    "soit", "soyons", "soyez", "soient", "fusse", "fusses", "fût", "fussions",
                    "fussiez", "fussent", "serais", "serait", "serions", "seriez", "seraient" };
        }
        else {
            return new String[] {};
        }
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
