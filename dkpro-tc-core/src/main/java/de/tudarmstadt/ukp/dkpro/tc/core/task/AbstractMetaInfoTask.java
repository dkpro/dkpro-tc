/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.tc.core.task;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.uima.task.impl.UimaTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;

/**
 * Abstract base class to provide common functionality for MetaInfoTasks
 *
 */
public abstract class AbstractMetaInfoTask extends UimaTaskBase {

	/**
	 * Public name of the task key
	 */
	public static final String META_KEY = "meta";
	protected List<String> operativeViews;
	@Discriminator
	protected List<Object> pipelineParameters;
	protected Set<Class<? extends MetaCollector>> metaCollectorClasses;

	public AbstractMetaInfoTask() {
		super();
	}

	protected AggregateBuilder getAnalysisEngineBuilder(List<Object> parameters)
			throws ResourceInitializationException
	{
		AggregateBuilder builder = new AggregateBuilder();
	
	    for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
	        if (operativeViews != null) {
	            for (String viewName : operativeViews) {
	                builder.add(createEngineDescription(metaCollectorClass, parameters.toArray()),
	                        CAS.NAME_DEFAULT_SOFA, viewName);
	            }
	        }
	        else {
	            builder.add(createEngineDescription(metaCollectorClass, parameters.toArray()));
	        }
	    }
		return builder;
	}

	protected List<Object> setParameters(TaskContext aContext)
			throws ResourceInitializationException
	{
		// collect parameter/key pairs that need to be set
	    Map<String, String> parameterKeyPairs = new HashMap<String, String>();
	    for (Class<? extends MetaCollector> metaCollectorClass : metaCollectorClasses) {
	        try {
	            parameterKeyPairs.putAll(metaCollectorClass.newInstance().getParameterKeyPairs());
	        }
	        catch (InstantiationException e) {
	            throw new ResourceInitializationException(e);
	        }
	        catch (IllegalAccessException e) {
	            throw new ResourceInitializationException(e);
	        }
	    }
	
	    List<Object> parameters = new ArrayList<Object>();
	    if (pipelineParameters != null) {
	        parameters.addAll(pipelineParameters);
	    }
	
	    // make sure that the meta key import can be resolved (even when no meta features have been
	    // extracted, as in the regression demo)
	    // TODO better way to do this?
	    if (parameterKeyPairs.size() == 0) {
	        File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READONLY)
	                .getPath());
	        file.mkdir();
	    }
	
	    for (Entry<String, String> entry : parameterKeyPairs.entrySet()) {
	        File file = new File(aContext.getStorageLocation(META_KEY, AccessMode.READONLY),
	                entry.getValue());
	        parameters.addAll(Arrays.asList(entry.getKey(), file.getAbsolutePath()));
	    }
		return parameters;
	}

}