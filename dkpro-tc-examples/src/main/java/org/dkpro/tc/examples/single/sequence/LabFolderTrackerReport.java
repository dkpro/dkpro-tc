/**
 * Copyright 2017
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
package org.dkpro.tc.examples.single.sequence;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.task.deep.EmbeddingTask;
import org.dkpro.tc.core.task.deep.PreparationTask;
import org.dkpro.tc.core.task.deep.VectorizationTask;

/**
 * This is a slightly ugly solution for recording the DKPro Lab output folder of an experiment to
 * read result files in JUnit tests
 */
public class LabFolderTrackerReport
    extends BatchReportBase
{
 
    public String preparationTask=null;
    public String vectorizationTaskTrain = null;
    public String vectorizationTaskTest = null;
    public String embeddingTask=null;
    

    @Override
    public void execute()
        throws Exception
    {
        
        StorageService ss = getContext().getStorageService();
        for (TaskContextMetadata subcontext : getSubtasks()) {
            
            String type = subcontext.getType();
            if(type.contains(VectorizationTask.class.getName()) && type.contains("-Train-")){
                vectorizationTaskTrain = ss.locateKey(subcontext.getId(), "").getAbsolutePath(); 
            }
            if(type.contains(VectorizationTask.class.getName()) && type.contains("-Test-")){
                vectorizationTaskTest = ss.locateKey(subcontext.getId(), "").getAbsolutePath(); 
            }
            if(type.contains(PreparationTask.class.getName())){
                preparationTask = ss.locateKey(subcontext.getId(), "").getAbsolutePath(); 
            }
            if(type.contains(EmbeddingTask.class.getName())){
                embeddingTask = ss.locateKey(subcontext.getId(), "").getAbsolutePath(); 
            }
        }
    }
}
