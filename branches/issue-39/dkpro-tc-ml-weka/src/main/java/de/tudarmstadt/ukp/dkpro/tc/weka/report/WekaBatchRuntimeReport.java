/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.report;

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
import de.tudarmstadt.ukp.dkpro.tc.weka.task.WekaTestTask;

/**
 * Collects the final evaluation results in a train/test setting.
 * 
 * @author zesch
 * 
 */
public class WekaBatchRuntimeReport
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
            else if (subcontext.getType().startsWith(WekaTestTask.class.getName())) {
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
