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

import java.util.Arrays;
import java.util.List;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ADJP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ADVP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.FRAG;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.NP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.PP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.S;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.SBAR;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.SBARQ;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.SINV;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.SQ;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.VP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.WHNP;

public class ParsePatternUtils

{
    /**
     * The definitions of syntactic elements are based on Xiaofei Lu, 2010. Automatic analysis of
     * syntactic complexity in second language writing. In International Journal of Corpus
     * Linguistics}, vol.15, issue 4, pp.474--496
     * 
     * @author beinborn
     **/
    public static boolean isCoordinate(Constituent c)
    {
        if (c instanceof NP || c instanceof VP || c instanceof ADJP || c instanceof ADVP) {
            for (FeatureStructure child : c.getChildren().toArray()) {

                try {
                    String pos = ((Token) child).getPos().getPosValue();
                    if (pos.equals("CC")) {
                        return true;
                    }
                }
                catch (ClassCastException e) {
                    return false;
                }
                catch (NullPointerException e) {
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean isComplexTunit(Constituent c)
    {
        if (isTunit(c)) {
            List<SBAR> sbars = JCasUtil.selectCovered(SBAR.class, c);
            for (SBAR sbar : sbars) {
                if (isDependentClause(sbar)) {
                    return true;
                }

            }
        }
        return false;
    }

    public static boolean isDependentClause(Constituent c)
    {
        if (c instanceof SBAR) {

            for (FeatureStructure child : c.getChildren().toArray()) {
                try {
                    if (isClause((Constituent) child)) {
                        return true;
                    }
                }
                catch (ClassCastException e) {
                    // The child is a token, therefore not a clause
                }
            }
        }
        return false;
    }

    public static boolean isTunit(Constituent c)
    {
        if (c instanceof S || c instanceof SINV || c instanceof SQ || c instanceof SBARQ) {
            if (c.getParent() instanceof ROOT) {

                return true;
            }

            for (FeatureStructure leftSister : ((Constituent) c.getParent()).getChildren()
                    .toArray()) {

                // check the nodes left of the current node
                if (((Constituent) leftSister).equals(c)) {
                    break;
                }

                if (leftSister instanceof S || leftSister instanceof SINV
                        || leftSister instanceof SQ || leftSister instanceof SBARQ) {
                    for (Constituent parent : JCasUtil.selectCovering(Constituent.class,
                            ((Constituent) leftSister))) {
                        if (parent instanceof SBAR || parent instanceof VP) {
                            return false;
                        }
                    }
                    return true;

                }

            }
        }
        if (c instanceof FRAG && c.getParent() instanceof ROOT) {

            return true;
        }
        return false;
    }

    public static int getParseDepth(Sentence s)
    {

        Constituent root = JCasUtil.selectCovered(ROOT.class, s).get(0);
        return getDepth(root) - 1;
    }

    public static boolean isComplexNominal(Constituent c)
    {
        if (c instanceof NP) {
            if (!(c.getParent() instanceof NP)) {
                List<Constituent> coveredConstituents = JCasUtil
                        .selectCovered(Constituent.class, c);
                List<Token> coveredTokens = JCasUtil.selectCovered(Token.class, c);
                for (Constituent coveredC : coveredConstituents) {
                    if (coveredC instanceof PP || coveredC instanceof S) {
                        return true;
                    }
                    if (isAppositive(coveredC)) {
                        return true;
                    }
                }
                for (Token coveredT : coveredTokens) {
                    String[] accepted = new String[] { "VBG", "POS", "JJ" };
                    if (Arrays.asList(accepted).contains(coveredT.getPos().getPosValue())) {
                        return true;
                    }
                }
            }
        }
        else if (c instanceof SBAR) {
            Annotation parent = c.getParent();
            if (parent instanceof VP || isImmediateLeftSisterOfVP(c)) {
                int i = 0;
                for (Constituent child : JCasUtil.selectCovered(Constituent.class, c)) {
                    if (child instanceof WHNP) {
                        return true;
                    }
                    if (i == 0 && child instanceof S) {
                        return true;
                    }
                    i++;
                }
                Token firstDominatedToken = JCasUtil.selectCovered(Token.class, c).get(0);
                if (firstDominatedToken.getPos() instanceof PR
                        && (firstDominatedToken.getCoveredText().equals("for") || firstDominatedToken
                                .getCoveredText().equals("what"))) {
                    return true;
                }
            }

        }
        else if (c instanceof S) {
            boolean dominatesToOrGerundVP = false;
            for (VP vp : JCasUtil.selectCovered(VP.class, c)) {

                String headPos = JCasUtil.selectCovered(Token.class, vp).get(0).getPos()
                        .getPosValue();
                if (headPos.equals("VBG") || headPos.equals("TO")) {
                    dominatesToOrGerundVP = true;

                }
            }

            return (dominatesToOrGerundVP && isImmediateLeftSisterOfVP(c));
        }
        return false;
    }

    public static boolean isVerbPhrase(Constituent c)
    {
        if (c instanceof VP) {
            Annotation parent = c.getParent();
            if (parent instanceof S || parent instanceof SQ || parent instanceof SINV) {
                return true;
            }
        }
        return false;
    }

    private static boolean isImmediateLeftSisterOfVP(Constituent c)
    {
        try {
            VP followingVP = JCasUtil.selectFollowing(VP.class, c, 1).get(0);
            Annotation parent = c.getParent();
            // check if c is immediate left sister of followingVP

            if (parent.getEnd() >= followingVP.getEnd() && c.getEnd() + 1 == followingVP.getBegin()) {
                return true;
            }
        }
        catch (IndexOutOfBoundsException e) {
            // no vp follows, no complex nominal
        }
        return false;
    }

    public static boolean isAppositive(Constituent coveredC)
    {
        if (coveredC instanceof NP) {
            Annotation parent = coveredC.getParent();

            boolean hasRightNpSister = false;
            List<Constituent> siblings = JCasUtil.selectCovered(Constituent.class, parent);
            int indexOfConstituent = 100;
            for (int index = 0; index < siblings.size(); index++) {
                Constituent sibling = siblings.get(index);
                if (sibling.equals(coveredC)) {
                    indexOfConstituent = index;

                }
                if (index > indexOfConstituent) {
                    if (index == indexOfConstituent + 1) {
                        if (JCasUtil.selectCovered(Token.class, sibling).get(0).getPos()
                                .getPosValue().equals("CC")) {
                            return false;
                        }
                    }
                    if (sibling instanceof NP) {

                        hasRightNpSister = true;
                    }

                }

            }

            return hasRightNpSister;
        }
        return false;
    }

    public static int getDepth(FeatureStructure c)
    {

        if (c instanceof Token) {
            return 1;
        }
        else {
            int depth = 1;
            for (FeatureStructure child : ((Constituent) c).getChildren().toArray()) {
                int childDepth = getDepth(child);

                depth = Math.max(depth, childDepth);
            }
            return depth + 1;
        }
    }

    public static boolean isClause(Constituent c)
    {
        if (c instanceof S || c instanceof SINV || c instanceof SQ) {

            for (FeatureStructure child : c.getChildren().toArray()) {
                if (child instanceof VP) {
                    try {
                        Token grandchild = JCasUtil.selectCovered(Token.class, (Annotation) child)
                                .get(0);

                        String pos = grandchild.getPos().getPosValue();

                        String[] finiteVerbTags = { "MD", "VBD", "VBP", "VBZ" };
                        if (Arrays.asList(finiteVerbTags).contains(pos)) {
                            return true;
                        }
                    }
                    catch (IndexOutOfBoundsException e) {
                    }
                    catch (ClassCastException e1) {
                    }

                }
            }

        }
        if (c instanceof FRAG) {
            if (c.getParent() instanceof ROOT) {
                for (FeatureStructure child : c.getChildren().toArray()) {
                    if (child instanceof VP) {
                        return false;
                    }
                }
                System.out.println("Is a clause: " + c.getCoveredText());
                return true;
            }
        }

        return false;
    }
}
