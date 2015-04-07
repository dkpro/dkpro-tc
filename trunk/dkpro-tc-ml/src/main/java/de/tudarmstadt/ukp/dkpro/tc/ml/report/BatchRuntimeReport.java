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

import java.util.Map;
import java.util.Properties;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.MetaInfoTask;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;

/**
 * Collects the final runtime results in a train/test setting.
 * 
 * @author zesch
 * 
 */
public class BatchRuntimeReport
    extends BatchReportBase
    implements Constants
{

    /**
     * Name of the output file where the report stores the runtime results
     */
    public static final String RUNTIME_KEY = "RUNTIME.txt";
    
    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

        Properties props = new Properties();

        long preprocessingTime = 0;
        long metaTime = 0;
        long featureExtractionTime = 0;
        long testingTime = 0;
        
        for (TaskContextMetadata subcontext : getSubtasks()) {
            Map<String, String> metaMap = store.retrieveBinary(subcontext.getId(),
                    TaskContextMetadata.METADATA_KEY, new PropertiesAdapter()).getMap();
            
            long begin = 0;
            long end = 0;
            if (metaMap.containsKey("begin")) {
                begin = Long.parseLong(metaMap.get("begin"));
            }
            if (metaMap.containsKey("end")) {
                end = Long.parseLong(metaMap.get("end"));
            }
            long difference = end - begin;
            
            if (subcontext.getType().startsWith(PreprocessTask.class.getName())) {
                preprocessingTime += difference;
            }
            else if (subcontext.getType().startsWith(MetaInfoTask.class.getName())) {
                metaTime += difference;
            }
            else if (subcontext.getType().startsWith(ExtractFeaturesTask.class.getName())) {
                featureExtractionTime += difference;
            }
            // FIXME this is a bad hack
            else if (subcontext.getType().contains("TestTask")) {
                testingTime += difference;
            }
        }

        String preprocessingTimeString = convertTime(preprocessingTime);
        String metaTimeString = convertTime(metaTime);
        String featureExtractionTimeString = convertTime(featureExtractionTime);
        String testingTimeString = convertTime(testingTime);

        System.out.println("--- DETAILED RUNTIME REPORT ---");
        System.out.println("Preprocessing: " + preprocessingTimeString);
        System.out.println("Meta Extraction: " +  metaTimeString);
        System.out.println("Feature Extraction: " + featureExtractionTimeString);
        System.out.println("Testing: " + testingTimeString);
        System.out.println("-------------------------------");

        props.setProperty("preprocessing", preprocessingTimeString);
        props.setProperty("meta", metaTimeString);
        props.setProperty("featureextraction", featureExtractionTimeString);
        props.setProperty("testing", testingTimeString);
        
        getContext().storeBinary(RUNTIME_KEY, new PropertiesAdapter(props));
    }
    
    private String convertTime(long time){
        long millis = time % 1000;
        long second = (time / 1000) % 60;
        long minute = (time / (1000 * 60)) % 60;
        long hour = (time / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
    }
}
