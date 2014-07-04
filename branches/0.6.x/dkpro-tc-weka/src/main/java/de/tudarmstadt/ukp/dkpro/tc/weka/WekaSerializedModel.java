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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

/**
 * A serializable model of Weka and Meka classifiers along with all necessary meta information to
 * use it as external resource in tasks which use this model to classify unseen data.
 * 
 * @author daxenberger
 * 
 */
public class WekaSerializedModel
    implements SharedResourceObject, Serializable
{

    private List<Attribute> attributes;
    private Classifier trainedClassifier;
    private String bipartitionThreshold;
    private List<String> editFeatureExtractors;
    private List<String> allClassLabels;
    private List<Object> pipelineParameters;
    private Map<String, FrequencyDistribution<String>> metaFiles;

    private static final long serialVersionUID = -6293683995416413736L;

    public WekaSerializedModel()
    {
        // default constructor
    }

    /**
     * Creates a serializable Weka/Meka model with all necessary meta data to reuse it as external
     * resource in tasks which use this model to classify unseen data.
     * 
     * @param attributes
     *            list of attributes used to train the classifier
     * @param trainedClassifier
     *            trained classifier model
     * @param bipartitionThreshold
     *            bipartition threshold (only in multi-label experiments)
     * @param editFeatureExtractors
     *            names of all feature extractors which have been used to create the classifier
     *            model
     * @param allLabels
     *            list of all class label names
     * @param pipelineParameters
     *            key - value pairs of pipeline parameters using for feature extraction
     * @param metaFiles
     *            key - frequency distribution pairs with all meta files
     */
    public WekaSerializedModel(List<Attribute> attributes, Classifier trainedClassifier,
            String bipartitionThreshold, List<String> editFeatureExtractors,
            List<String> allLabels, List<Object> pipelineParameters,
            Map<String, FrequencyDistribution<String>> metaFiles)
    {
        this.attributes = attributes;
        this.bipartitionThreshold = bipartitionThreshold;
        this.editFeatureExtractors = editFeatureExtractors;
        this.allClassLabels = allLabels;
        this.pipelineParameters = pipelineParameters;
        this.trainedClassifier = trainedClassifier;
        this.metaFiles = metaFiles;
    }

    @Override
    public void load(DataResource aData)
        throws ResourceInitializationException
    {
        ConfigurationParameterInitializer.initialize(this, aData);

        try {
            InputStream fileIn = aData.getInputStream();
            ObjectInputStream in = new ObjectInputStream(fileIn);
            WekaSerializedModel model = (WekaSerializedModel) in.readObject();
            in.close();
            fileIn.close();
            this.attributes = model.getAttributes();
            this.bipartitionThreshold = model.getBipartitionThreshold();
            this.editFeatureExtractors = model.getEditFeatureExtractors();
            this.allClassLabels = model.getAllClassLabels();
            this.pipelineParameters = model.getPipelineParameters();
            this.trainedClassifier = model.getTrainedClassifier();
            this.metaFiles = model.getMetaFiles();
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    public String getBipartitionThreshold()
    {
        return bipartitionThreshold;
    }

    public List<Attribute> getAttributes()
    {
        return attributes;
    }

    public List<String> getEditFeatureExtractors()
    {
        return editFeatureExtractors;
    }

    public List<String> getAllClassLabels()
    {
        return allClassLabels;
    }

    public List<Object> getPipelineParameters()
    {
        return pipelineParameters;
    }

    public Classifier getTrainedClassifier()
    {
        return trainedClassifier;
    }

    public Map<String, FrequencyDistribution<String>> getMetaFiles()
    {
        return metaFiles;
    }
}
