/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.features.ngram.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

/**
 * Creates a skip-ngram iterable from a list of tokens. It does not detect any sentence boundaries.
 * Thus, one should make sure to only add lists that reflect a sentence or a phrase.
 * 
 * Definition of skip-ngrams follows: David Guthrie, Ben Allison, W. Liu, Louise Guthrie, and Yorick
 * Wilks. A closer look at skip-gram modelling. In Proceedings of the Fifth international Conference
 * on Language Resources and Evaluation (LREC) pages 1222–1225, 2006
 */
public class SkipNgramStringListIterable
    implements Iterable<List<String>>
{
    Set<List<String>> nGramSet;

    public SkipNgramStringListIterable(Iterable<String> tokens, int minN, int maxN, int skipN)
    {
        this.nGramSet = createSkipNgramSet(tokens, minN, maxN, skipN);
    }

    public SkipNgramStringListIterable(String[] tokens, int minN, int maxN, int skipN)
    {
        this.nGramSet = createSkipNgramSet(Arrays.asList(tokens), minN, maxN, skipN);
    }

    @Override
    public Iterator<List<String>> iterator()
    {
        return nGramSet.iterator();
    }

    private Set<List<String>> createSkipNgramSet(Iterable<String> tokens, int minN, int maxN,
            int skipN)
    {
        if (minN > maxN) {
            throw new IllegalArgumentException("minN needs to be smaller or equal than maxN.");
        }

        if (minN < 2) {
            throw new IllegalArgumentException(
                    "minN needs to be greater than 1. Not much to skip in unigrams :)");
        }

        if (skipN < 1) {
            throw new IllegalArgumentException(
                    "skipN needs to be greater than 0. Would be identical to normal grams.");
        }

        Set<List<String>> nGrams = new HashSet<List<String>>();

        // fill token list
        List<String> tokenList = new ArrayList<String>();
        for (String t : tokens) {
            tokenList.add(t);
        }

        // add ngrams for each requested ngram size
        for (int k = minN; k <= maxN; k++) {
            // if the number of tokens is less than k => break
            if (tokenList.size() < k) {
                break;
            }
            nGrams.addAll(getSkipNgrams(tokenList, k, skipN));
        }

        return nGrams;
    }

    private Set<List<String>> getSkipNgrams(List<String> tokenList, int n, int skipN)
    {
        Set<List<String>> nGrams = new HashSet<List<String>>();

        // iterate over each position in the tokenlist where skip ngrams can be generated
        int size = tokenList.size();
        for (int start = 0; start < size - n; start++) {
            int end = start + n + skipN;

            if (end > size) {
                end = size;
            }
            List<String> sublist = tokenList.subList(start, end);

            // generate all permutations between start & end
            ICombinatoricsVector<String> initialSet = Factory.createVector(sublist);
            Generator<String> gen = Factory.createSubSetGenerator(initialSet);
            for (ICombinatoricsVector<String> subSet : gen) {
                if (subSet.getSize() == n) {
                    nGrams.add(subSet.getVector());
                }
            }
        }

        return nGrams;
    }
}