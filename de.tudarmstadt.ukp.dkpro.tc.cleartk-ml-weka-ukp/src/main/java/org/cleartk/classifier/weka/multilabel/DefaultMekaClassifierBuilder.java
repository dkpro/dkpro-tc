package org.cleartk.classifier.weka.multilabel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.outcome.StringArrayToStringArrayEncoder;
import org.cleartk.classifier.jar.ClassifierBuilder_ImplBase;
import org.cleartk.classifier.jar.JarStreams;
import org.cleartk.classifier.weka.WekaFeaturesEncoder;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class DefaultMekaClassifierBuilder
    extends
    ClassifierBuilder_ImplBase<DefaultMekaClassifier, Iterable<Feature>, String[], String[]>
{
    public DefaultMekaClassifierBuilder()
    {
        super();
        this.setFeaturesEncoder(new WekaFeaturesEncoder());
        this.setOutcomeEncoder(new StringArrayToStringArrayEncoder());
    }

    Classifier classifier;

    @Override
    public File getTrainingDataFile(File dir)
    {
        return new File(dir, "training-data.arff.gz");
    }

    @Override
    public void trainClassifier(File dir, String... args)
        throws Exception
    {
        File trainingData = getTrainingDataFile(dir);
        Reader fileReader = null;

        try {
            fileReader = new InputStreamReader(new BufferedInputStream(new GZIPInputStream(
                    new FileInputStream(trainingData), 65536), 1048576));
            Instances inst = new Instances(fileReader);

            // for Meka
            int numClassLabels = Integer.parseInt(inst.relationName()
                    .substring(inst.relationName().indexOf("C")).split(" ")[1]);
            inst.setClassIndex(numClassLabels);

            String classifierName = args[0];
            String[] arguments = Arrays.copyOfRange(args, 1, args.length);

            classifier = AbstractClassifier.forName(classifierName, arguments);
            classifier.buildClassifier(inst);
        }
        finally {
            IOUtils.closeQuietly(fileReader);
        }

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(new File(dir, "model.meka")));
            oos.writeObject(this.classifier);
        }
        finally {
            IOUtils.closeQuietly(oos);
        }

    }

    @Override
    protected DefaultMekaClassifier newClassifier()
    {
        return new DefaultMekaClassifier(this.classifier, this.featuresEncoder, this.outcomeEncoder);
    }

    @Override
    public void packageClassifier(File dir, JarOutputStream modelStream)
        throws IOException
    {
        super.packageClassifier(dir, modelStream);
        JarStreams.putNextJarEntry(modelStream, "model.meka", new File(dir, "model.meka"));
    }

    @Override
    public void unpackageClassifier(JarInputStream modelStream)
        throws IOException
    {
        super.unpackageClassifier(modelStream);
        JarStreams.getNextJarEntry(modelStream, "model.meka");
        ObjectInputStream objectStream = new ObjectInputStream(modelStream);
        try {
            this.classifier = ((Classifier) objectStream.readObject());
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}