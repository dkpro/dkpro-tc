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
package de.tudarmstadt.ukp.dkpro.tc.api.features.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAFramework;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.impl.ResourceManager_impl;
import org.apache.uima.resource.metadata.ResourceManagerConfiguration;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

/**
 * Utils for feature extractors
 */
public class FeatureUtil
{
    /**
     * Escapes the names, as Weka does not seem to like special characters in attribute names.
     * @param name
     * @return
     */
    public static String escapeFeatureName(String name) {
        
        // TODO Issue 120: improve the escaping
        // the fix was necessary due to Issue 32
        // http://code.google.com/p/dkpro-tc/issues/detail?id=32
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<name.length(); i++) {
            String c = name.substring(i, i+1);
            if (StringUtils.isAlphanumeric(c) || c.equals("_")) {
                sb.append(c);
            }
            else {
                sb.append("u");
                sb.append(c.codePointAt(0));
            }
        }
        return sb.toString();
    }
    
    /**
     * @param inputFile Location of the file that contains the stopwords. One stopword per line.
     * @param toLowerCase Whether the stopwords should be converted to lowercase or not.
     * @return A set of stopwords.
     * @throws IOException
     */
    public static Set<String> getStopwords(String inputFile, boolean toLowerCase)
        throws IOException
    {
        Set<String> stopwords = new HashSet<String>();
        if (inputFile != null) {
            URL stopUrl = ResourceUtils.resolveLocation(inputFile, null);
            InputStream is = stopUrl.openStream();
            for (String stopword : IOUtils.readLines(is, "UTF-8")) {
                if (toLowerCase) {
                    stopwords.add(stopword.toLowerCase());
                }
                else {
                    stopwords.add(stopword);
                }
            }
        }
        
        return stopwords;
    }
    
    // TODO this will eventually also be included in uimaFit and can be removed then
	@SuppressWarnings("unchecked")
	public static <T extends Resource> T createResource(Class<T> aClass, Object... aParam)
	        throws ResourceInitializationException, ResourceAccessException  {
	  // Configure external resource
	  ExternalResourceDescription desc = ExternalResourceFactory.createExternalResourceDescription(
	          aClass, aParam);
	
	  // Configure resource manager
	  ResourceManagerConfiguration cfg = UIMAFramework.getResourceSpecifierFactory()
	          .createResourceManagerConfiguration();
	
	  ExternalResourceFactory.bindExternalResource(cfg, "rootResource", desc);
	
	  // Instantiate resource manager (internally instantiates resources)
	  ResourceManager manager = new ResourceManager_impl();
	  manager.initializeExternalResources(cfg, "", null);
	
	  // Get resource instance
	  return (T) manager.getResource("rootResource");
	}
}