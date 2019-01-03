/*******************************************************************************
 * Copyright 2019
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
package org.dkpro.tc.api.features;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.util.FeatureUtil;

/**
 * Escapes features to ensure that they do not contain non-standard characters.
 * This class is thread-safe if used as static instance.
 */
public class FeatureNameEscaper {

	private Map<String, String> mapping = new HashMap<>();
	private Semaphore createLock = new Semaphore(1);

	/**
	 * Escapes feature names. The substitution is synchronized by
	 * a @link{java.util.concurrent.Semaphore} if used by multiple threads.
	 * 
	 * @param rawName
	 *            the unescaped name of the feature
	 * @return the escaped name of the feature, will be identical to the input
	 *         if no characters are found that require escaping
	 * @throws TextClassificationException
	 *             in case of an error
	 */
	public String escape(String rawName) throws TextClassificationException {

		if (mapping.containsKey(rawName)) {
			return mapping.get(rawName);
		}

		String escaped = rawName;
		try {
			createLock.acquire();
			escaped = FeatureUtil.escapeFeatureName(rawName);
			mapping.put(rawName, escaped);
			createLock.release();
		} catch (InterruptedException e) {
			throw new TextClassificationException(e);
		}

		return escaped;
	}
}
