/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.ml.crfsuite.writer;

import java.util.Iterator;
import java.util.List;

import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.Instance;

/**
 * Takes instances and returns the string representation of a sequence that can be provided as-is to
 * CrfSuite
 */
public class CrfSuiteFeatureFormatExtractionIterator
    implements Iterator<StringBuilder>
{
    protected static final String idInitVal = "ü+Ü**'?=?=)(ÖÄ:";
    protected int insIdx;
    protected List<Instance> instances;

    public CrfSuiteFeatureFormatExtractionIterator(List<Instance> instances)
    {
        this.instances = instances;
    }

    @Override
    public boolean hasNext()
    {
        return insIdx < instances.size();
    }

    @Override
    public StringBuilder next()
    {
        StringBuilder sb = new StringBuilder();

        try {

            String lastSeenSeqId = idInitVal;
            boolean seqIdChanged = false;
            for (; insIdx < instances.size(); insIdx++) {
                Instance i = instances.get(insIdx);
                String id = getId(i);

                if (!lastSeenSeqId.equals(id)) {
                    seqIdChanged = true;
                    lastSeenSeqId = getId(i);
                }

                sb.append(LabelSubstitutor.labelReplacement(i.getOutcome()));
                sb.append("\t");

                int idx = 0;
                for (Feature f : i.getFeatures()) {
                    sb.append(f.getName() + "=" + f.getValue());
                    if (idx + 1 < i.getFeatures().size()) {
                        sb.append("\t");
                    }
                    idx++;
                }

                // Mark first line of new sequence with an additional __BOS__
                if (seqIdChanged) {
                    sb.append("\t");
                    sb.append("__BOS__");
                    seqIdChanged = false;
                }

                // Peak ahead - seqEnd reached?
                if (insIdx + 1 < instances.size()) {
                    Instance next = instances.get(insIdx + 1);
                    String nextId = getId(next);
                    if (!lastSeenSeqId.equals(nextId)) {
                        appendEOS(sb);
                        insIdx++;
                        break;
                    }
                }
                else if (insIdx + 1 == instances.size()) {
                    appendEOS(sb);
                    insIdx++;
                    break;// We're done
                }
                sb.append("\n");
            }

        }
        catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }

        return sb;
    }

    private String getId(Instance i)
    {
        int jcasId = i.getJcasId();
        int sequenceId = i.getSequenceId();

        return "" + jcasId + "_" + sequenceId;
    }

    private void appendEOS(StringBuilder sb) throws Exception
    {
        sb.append("\t");
        sb.append("__EOS__");
        sb.append("\n");
    }
}
