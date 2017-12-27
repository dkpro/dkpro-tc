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
package org.dkpro.tc.ml.dynet;

public class DyNetConstants {

	/** The DyNet package  translates the global seed/memory values to these DyNet values */
	protected static final String DYNET_SEED = "--dynet-seed";
	
	/** Working memory. In case of GPU processing this values automatically refers to the GPU's working memory*/
	protected static final String DYNET_MEMORY = "--dynet-mem";
	
	/**
	 * Defines the number of GPUs that are used, which one are used is not
	 * specified. i.e. --dynet-gpus X with X being a number from 0 to N
	 * depending on the number of GPUs available
	 */
	public static final String DIM_DYNET_GPUS = "--dynet-gpus";
	
	/**
	 * Autobatching feature of DyNet. See DyNet documentation for details.
	 * Default value is 0 (off), providing 1 (on) turns this feature on.
	 */
	public static final String DIM_DYNET_AUTOBATCH = "--dynet-autobatch";
	
	/**
	 * Allows specifying exactly which hardware devices should be used by naming the physical devices
	 * directly i.e. GPU:0 is the first graphic card in the system, CPU:0 is the first CPU.
	 * Several devices are provided as list e.g. "GPU:0,GPU:3,CPU:0"
	 */
	public static final String DIM_DYNET_DEVICES = "--dynet-devices";

}
