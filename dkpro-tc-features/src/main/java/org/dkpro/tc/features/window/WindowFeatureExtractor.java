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
package org.dkpro.tc.features.window;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;


public abstract class WindowFeatureExtractor<T extends Annotation>
        extends FeatureExtractorResource_ImplBase
        implements FeatureExtractor
{

       
	public static final String PARAM_NUM_PRECEEDING = "numPreceeding";
	@ConfigurationParameter(name = PARAM_NUM_PRECEEDING, mandatory = false, defaultValue = "3")
    private int numPreceeding;
	
	public static final String PARAM_NUM_FOLLOWING = "numFollowing";
	@ConfigurationParameter(name = PARAM_NUM_FOLLOWING, mandatory = false, defaultValue = "3")
    private int numFollowing;
    
	
	
    private Class<T> targetType;
    private String featureName;
    
    
    
    public WindowFeatureExtractor() {
    	this.featureName = getFeatureName();
    	this.targetType = getTargetType();    	
    }
    
    

    
    protected abstract Class<T> getTargetType();
	protected abstract String getFeatureName();
	protected abstract String getFeatureValue(T a); 


//    protected String getTokenText(Annotation a){
//    	String f = a.getType().getFeatureByBaseName("stem").toString();
//    	return f;
//    }



	@Override 
    public Set<Feature> extract(JCas jCas, TextClassificationTarget textClassificationUnit)
            throws TextClassificationException
    {
//    	List<Feature> features = new LinkedList<Feature>();
    	LinkedHashSet<Feature> features = new LinkedHashSet<Feature>();
    	
    	//Current 
    	for(T t : JCasUtil.selectCovered(targetType, textClassificationUnit))  {
    		String featureValue = getFeatureValue(t);
    		if(featureValue == null) {
    			featureValue = "PADDING";
    		}
    		
    		features.add(new Feature(featureName+"[0]".intern(), featureValue.intern()));    		
    		break;
    	}
    	
    	//Preceeding
    	List<T> preceedings = JCasUtil.selectPreceding(targetType, textClassificationUnit, numPreceeding);
    	int counter = -preceedings.size();
    	
    	for(int i=counter-1;i >= -numPreceeding; i--) {
    		features.add(new Feature(featureName+"["+i+"]", "PADDING"));
    	}
    	
    	for(T t : preceedings)  {   		
    		features.add(new Feature(featureName+"["+counter+"]".intern(), getFeatureValue(t).intern()));
    		counter++;
    	}
    	
    	
    	
    	//Following
    	counter = 1;
    	for(T t : JCasUtil.selectFollowing(targetType, textClassificationUnit, numFollowing))  {
    		features.add(new Feature(featureName+"["+counter+"]".intern(), getFeatureValue(t).intern()));
    		counter++;
    	}    
    	
    	for(;counter <= numFollowing; counter++) {
    		features.add(new Feature(featureName+"["+counter+"]".intern(), "PADDING"));
    	}
    	
        return features;
    }
    
    
	
}