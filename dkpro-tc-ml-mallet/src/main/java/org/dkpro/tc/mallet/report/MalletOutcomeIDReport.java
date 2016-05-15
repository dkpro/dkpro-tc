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
package org.dkpro.tc.mallet.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.mallet.MalletAdapter;
import org.dkpro.tc.ml.report.util.SortedKeyProperties;

public class MalletOutcomeIDReport
    extends ReportBase
    implements Constants
{

    private static final String SEPARATOR_CHAR = ";";
    private final String THRESHOLD_CONSTANTS = "-1";

    @Override
    public void execute()
        throws Exception
    {
        String prediction = MalletAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.predictionsFile);
        File filePredictions = getContext().getFile(prediction, AccessMode.READWRITE);

        Map<String, Integer> map = buildLabel2IdMap(filePredictions);
        String header = makeHeader(map);

        Properties p = new SortedKeyProperties();

        List<String> lines = FileUtils.readLines(filePredictions);
        int i = 0;
        for (String l : lines) {
            if (l.startsWith("#")) {
                continue;
            }
            if (l.isEmpty()) {
                continue;
            }
            String[] split = l.split(SEPARATOR_CHAR);
            String pred = split[0];
            String gold = split[1];
            p.put("" + i++, map.get(pred) + SEPARATOR_CHAR + map.get(gold) + SEPARATOR_CHAR
                    + THRESHOLD_CONSTANTS);
        }

        File file = getContext().getFile(ID_OUTCOME_KEY, AccessMode.READWRITE);
        OutputStreamWriter fos = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
        p.store(fos, header);
        fos.close();
    }

    private String makeHeader(Map<String, Integer> map)
    {
        List<String> keySet = new ArrayList<>(map.keySet());
        Collections.sort(keySet);

        StringBuilder sb = new StringBuilder();
        sb.append("ID=PREDICTION" + SEPARATOR_CHAR + "GOLDSTANDARD" + SEPARATOR_CHAR + "THRESHOLD");
        sb.append("\n");
        sb.append("#labels ");
        keySet.forEach(x -> sb.append(x + "=" + map.get(x) + " "));

        return sb.toString().trim();
    }

    private Map<String, Integer> buildLabel2IdMap(File filePredictions)
        throws Exception
    {
        Set<String> labels = new HashSet<>();
        List<String> lines = FileUtils.readLines(filePredictions);
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            if (line.isEmpty()) {
                continue;
            }
            String[] split = line.split(";");

            labels.add(split[0]);
            labels.add(split[1]);
        }

        Map<String, Integer> map = new HashMap<>();
        int id = 0;
        for (String l : labels) {
            map.put(l, id++);
        }

        return map;
    }

}