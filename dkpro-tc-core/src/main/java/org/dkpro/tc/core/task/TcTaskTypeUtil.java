/*******************************************************************************
 * Copyright 2017
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
package org.dkpro.tc.core.task;

import java.io.IOException;
import java.util.Map;

import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.core.Constants;

public class TcTaskTypeUtil
{
    public static boolean isMachineLearningAdapterTask(StorageService store, String id)
        throws IOException
    {
        return isTask(TcTaskType.MACHINE_LEARNING_ADAPTER, store, id);
    }

    public static boolean isCrossValidationTask(StorageService store, String id)
        throws IOException
    {
        return isTask(TcTaskType.CROSS_VALIDATION, store, id);
    }

    public static boolean isInitTrainTask(StorageService store, String id)
        throws IOException
    {
        return isTask(TcTaskType.INIT_TRAIN, store, id);
    }

    public static boolean isInitTestTask(StorageService store, String id)
        throws IOException
    {
        return isTask(TcTaskType.INIT_TEST, store, id);
    }

    public static boolean isFeatureExtractionTrainTask(StorageService store, String id)
        throws IOException
    {
        return isTask(TcTaskType.FEATURE_EXTRACTION_TRAIN, store, id);
    }

    public static boolean isFeatureExtractionTestTask(StorageService store, String id)
        throws IOException
    {
        return isTask(TcTaskType.FEATURE_EXTRACTION_TEST, store, id);
    }

    public static boolean isMetaTask(StorageService store, String id)
        throws IOException
    {
        return isTask(TcTaskType.META, store, id);
    }

    private static boolean isTask(TcTaskType target, StorageService store, String id)
        throws IOException
    {
        Map<String, String> loadAttributes = loadAttributes(store, id);
        TcTaskType taskType = TcTaskTypeUtil.getTaskType(loadAttributes);
        return taskType == target;
    }

    protected static TcTaskType getTaskType(Map<String, String> attributes)
    {
        String string = attributes.get(Constants.TC_TASK_TYPE);
        if (string == null) {
            return TcTaskType.NO_TYPE;
        }

        return TcTaskType.valueOf(string);
    }

    private static Map<String, String> loadAttributes(StorageService store, String contextId)
        throws IOException
    {
        return store.retrieveBinary(contextId, TaskBase.ATTRIBUTES_KEY, new PropertiesAdapter())
                .getMap();
    }

}
