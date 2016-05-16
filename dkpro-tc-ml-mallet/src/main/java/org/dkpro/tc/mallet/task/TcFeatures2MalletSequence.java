/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.mallet.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

@SuppressWarnings("serial")
public class TcFeatures2MalletSequence
    extends Pipe
{
    Map<String, Double> value2double = new HashMap<>();
    Double nextFreeDouble = new Double(1);

    public TcFeatures2MalletSequence()
    {
        super(null, new LabelAlphabet());
    }

    public Instance pipe(Instance carrier)
    {
        Object inputData = carrier.getData();
        // Alphabet features = getDataAlphabet();
        LabelAlphabet labels;
        LabelSequence target = null;
        TokenSequence ts = new TokenSequence();

        List<List<String>> rawTextFeatures = splitData((String) inputData);

        labels = (LabelAlphabet) getTargetAlphabet();
        target = new LabelSequence(labels, rawTextFeatures.size());

        for (List<String> sequence : rawTextFeatures) {
            String alphabet = sequence.get(sequence.size() - 1);
            target.add(alphabet);
            // last entry in list is the gold label which is no feature
            Token tok = new Token(getTokenText(sequence.get(0)));
            for (int i = 0; i < sequence.size() - 1; i++) {
                String featureEntry = sequence.get(i);
                int indexOf = featureEntry.indexOf("=");
                String featureName = featureEntry.substring(0, indexOf);
                String featureValue = featureEntry.substring(indexOf + 1);
                tok.setFeatureValue(featureName, mapValue2double(featureValue));
            }
            ts.add(tok);
        }

        carrier.setData(ts);
        carrier.setTarget(target);
        return carrier;
    }

    private double mapValue2double(String featureValue)
    {
        Double double1 = value2double.get(featureValue);
        if (double1 == null) {
            double1 = nextFreeDouble++;
            value2double.put(featureValue, double1);
        }
        return 1.0;
    }

    private String getTokenText(String string)
    {
        int lastIndexOf = string.lastIndexOf("_");
        String tokenText = string.substring(lastIndexOf + 1);
        return tokenText;
    }

    private List<List<String>> splitData(String inputData)
    {
        List<List<String>> data = new ArrayList<>();
        String[] split = inputData.split("\n");
        for (String seqFeat : split) {
            List<String> singleSeq = new ArrayList<>();
            String[] features = seqFeat.split(" ");
            for (String f : features) {
                singleSeq.add(f);
            }
            data.add(singleSeq);
        }

        return data;
    }
}
