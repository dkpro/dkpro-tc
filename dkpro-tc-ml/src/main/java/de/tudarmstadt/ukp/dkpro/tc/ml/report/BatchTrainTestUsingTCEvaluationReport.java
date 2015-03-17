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
package de.tudarmstadt.ukp.dkpro.tc.ml.report;

import java.util.Properties;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

/**
 * Collects the final evaluation results in a train/test setting.
 * 
 * @author daxenberger
 * @author Andriy Nadolskyy
 * 
 */
public class BatchTrainTestUsingTCEvaluationReport
    extends BatchReportBase
    implements Constants
{


    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();
        Properties outcomeIdProps = new Properties();

        // Map<>

        for (TaskContextMetadata subcontext : getSubtasks()) {
            // FIXME this is a bad hack
            if (subcontext.getType().contains("TestTask")) {
                // TODO: Id2Outcome einlesen, eindeutiges Mapping genieren; updaten unabhängig von
                // id

                outcomeIdProps.putAll(store.retrieveBinary(subcontext.getId(),
                        Constants.ID_OUTCOME_KEY, new PropertiesAdapter()).getMap());
            }
        }
        getContext().storeBinary(Constants.ID_OUTCOME_KEY, new PropertiesAdapter(outcomeIdProps));
    }
}