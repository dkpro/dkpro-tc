package de.tudarmstadt.ukp.dkpro.tc.weka;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import weka.classifiers.Classifier;
import weka.core.Attribute;

/**
 * A serializable model of Weka and Meka classifiers along with all necessary meta information to
 * use it as external resource in tasks which use this model to classify unseen data.
 * 
 * @author daxenberger
 * 
 */
public class WekaSerializedModel
    extends Resource_ImplBase
    implements Serializable
{

    public static final String PARAM_WEKA_SERIALIZED_MODEL_PATH = "wekaSerializedModelPath";
    @ConfigurationParameter(name = PARAM_WEKA_SERIALIZED_MODEL_PATH, mandatory = true)
    private String wekaSerializedModelPath;

    private List<Attribute> attributes;
    private Classifier trainedClassifier;
    private String bipartitionThreshold;
    private List<String> editFeatureExtractors;
    private List<String> allClassLabels;
    private List<Object> pipelineParameters;
    private static final long serialVersionUID = -6293683995416413736L;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            FileInputStream fileIn = new FileInputStream(wekaSerializedModelPath);
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
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    public WekaSerializedModel()
    {
        // nothing to do here
    }

    public WekaSerializedModel(List<Attribute> attributes, Classifier trainedClassifier,
            String bipartitionThreshold, List<String> editFeatureExtractors,
            List<String> allLabels, List<Object> pipelineParameters)
    {
        this.attributes = attributes;
        this.bipartitionThreshold = bipartitionThreshold;
        this.editFeatureExtractors = editFeatureExtractors;
        this.allClassLabels = allLabels;
        this.pipelineParameters = pipelineParameters;
        this.trainedClassifier = trainedClassifier;
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

}
