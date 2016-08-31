/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.core.util;

import static org.dkpro.tc.core.task.MetaInfoTask.META_KEY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.internal.ResourceManagerFactory;
import org.apache.uima.resource.CustomResourceSpecifier;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.features.meta.MetaDependent;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;

/**
 * Demo to show case how to train and save a model in document mode and multi-label classification
 * using Meka/Weka.
 */
public class SaveModelUtils
    implements Constants
{
    private static final String TCVERSION = "TcVersion";

    public static void writeModelParameters(TaskContext aContext, File aOutputFolder,
            List<TcFeature> featureSet)
                throws Exception
    {
        StringBuilder sb = new StringBuilder();
        for (TcFeature f : featureSet) {
            copyLuceneMetaResourcesAndGetOverrides(aContext, f, aOutputFolder);
            persistsFeatureClassObject(aContext, f, aOutputFolder);

            sb = copyParameters(aContext, f, sb, aOutputFolder);
        }
        writeFeatureParameters(sb, aOutputFolder);

    }

    private static void writeFeatureParameters(StringBuilder sb, File aOutputFolder)
        throws IOException
    {
        File file = new File(aOutputFolder, MODEL_FEATURE_EXTRACTOR_CONFIGURATION);
        FileUtils.writeStringToFile(file, sb.toString(), "utf-8");
    }

    private static StringBuilder copyParameters(TaskContext aContext, TcFeature f, StringBuilder sb,
            File aOutputFolder)
                throws IOException
    {
        sb.append(f.getFeatureName() + "\t");

        ExternalResourceDescription feDesc = f.getActualValue();
        Map<String, Object> parameterSettings = ConfigurationParameterFactory
                .getParameterSettings(feDesc.getResourceSpecifier());
        List<String> keySet = new ArrayList<>(parameterSettings.keySet());
        for (int i = 0; i < keySet.size(); i++) {

            String key = keySet.get(i);
            String value = parameterSettings.get(key).toString();

            if (valueExistAsFileOrFolderInTheFileSystem(value)) {
                String name = new File(value).getName();
                String destination = aOutputFolder + "/" + name;
                copyToTargetLocation(new File(value), new File(destination));
                sb = record(i, keySet, name, sb);
                continue;
            }
            sb = record(i, keySet, parameterSettings, sb);
        }

        sb.append("\n");

        return sb;
    }

    private static StringBuilder record(int i, List<String> keySet, String name, StringBuilder sb)
    {
        String key = keySet.get(i);
        sb.append(key + "=" + name);
        if (i + 1 < keySet.size()) {
            sb.append("\t");
        }
        return sb;
    }

    private static StringBuilder record(int i, List<String> keySet,
            Map<String, Object> parameterSettings, StringBuilder sb)
    {
        String key = keySet.get(i);
        sb.append(key + "=" + parameterSettings.get(key).toString());
        if (i + 1 < keySet.size()) {
            sb.append("\t");
        }
        return sb;
    }

    private static void persistsFeatureClassObject(TaskContext aContext, TcFeature f,
            File aOutputFolder)
                throws Exception
    {
        ExternalResourceDescription feDesc = f.getActualValue();

        String implName;
        if (feDesc.getResourceSpecifier() instanceof CustomResourceSpecifier) {
            implName = ((CustomResourceSpecifier) feDesc.getResourceSpecifier())
                    .getResourceClassName();
        }
        else {
            implName = feDesc.getImplementationName();
        }

        Class<?> feature = Class.forName(implName);

        InputStream inStream = feature.getResource("/" + implName.replace(".", "/") + ".class")
                .openStream();

        OutputStream outStream = buildOutputStream(aOutputFolder, implName);

        IOUtils.copy(inStream, outStream);
        outStream.close();
        inStream.close();
    }

    // copy the lucene index folder to the target location
    private static void copyLuceneMetaResourcesAndGetOverrides(TaskContext aContext, TcFeature f,
            File aOutputFolder)
                throws Exception
    {
        ExternalResourceDescription feDesc = f.getActualValue();
        Map<String, Object> parameterSettings = ConfigurationParameterFactory
                .getParameterSettings(feDesc.getResourceSpecifier());

        String implName;
        if (feDesc.getResourceSpecifier() instanceof CustomResourceSpecifier) {
            implName = ((CustomResourceSpecifier) feDesc.getResourceSpecifier())
                    .getResourceClassName();
        }
        else {
            implName = feDesc.getImplementationName();
        }

        Class<?> feClass = Class.forName(implName);

        // Skip feature extractors that are not dependent on meta collectors
        if (!MetaDependent.class.isAssignableFrom(feClass)) {
            return;
        }

        MetaDependent feInstance = (MetaDependent) feClass.newInstance();

        Map<String, Object> metaOverrides = new HashMap<>();
        Map<String, Object> extractorOverrides = new HashMap<>();

        // Tell the meta collectors where to store their data
        for (MetaCollectorConfiguration conf : feInstance
                .getMetaCollectorClasses(parameterSettings)) {
            Map<String, String> collectorOverrides = conf.collectorOverrides;
            metaOverrides.putAll(collectorOverrides);
            extractorOverrides.putAll(conf.extractorOverrides);

            for (Entry<String, String> entry : collectorOverrides.entrySet()) {
                File file = new File(aContext.getFolder(META_KEY, AccessMode.READWRITE),
                        entry.getValue().toString());

                String name = file.getName();
                String subFolder = aOutputFolder.getAbsoluteFile() + "/" + name;
                File targetFolder = new File(subFolder);
                copyToTargetLocation(file, targetFolder);
            }
        }
        writeOverrides(aOutputFolder, metaOverrides, META_COLLECTOR_OVERRIDE);
        writeOverrides(aOutputFolder, extractorOverrides, META_EXTRACTOR_OVERRIDE);
    }

    private static void writeOverrides(File aOutputFolder, Map<String, Object> override,
            String target)
                throws IOException
    {
        StringBuilder sb = new StringBuilder();
        for (String k : override.keySet()) {
            sb.append(k + "=" + override.get(k));
        }

        FileUtils.write(new File(aOutputFolder, target), sb.toString(), "utf-8");
    }

    private static boolean valueExistAsFileOrFolderInTheFileSystem(String aValue)
    {
        return new File(aValue).exists();
    }

    private static void copyToTargetLocation(File source, File destination)
        throws IOException
    {

        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdir();
            }

            for (String file : source.list()) {
                File src = new File(source, file);
                File dest = new File(destination, file);
                copyToTargetLocation(src, dest);
            }

        }
        else {
            copySingleFile(source, destination);
        }
    }

    private static void copySingleFile(File source, File destination)
        throws IOException
    {
        InputStream inputstream = new FileInputStream(source);
        OutputStream outputstream = new FileOutputStream(destination);
        IOUtils.copy(inputstream, outputstream);
        inputstream.close();
        outputstream.close();
    }

    public static void writeModelAdapterInformation(File aOutputFolder, String aModelMeta)
        throws Exception
    {
        // as a marker for the type, write the name of the ml adapter class
        // write feature extractors
        FileUtils.writeStringToFile(new File(aOutputFolder, MODEL_META), aModelMeta);
    }

    public static void writeFeatureClassFiles(File modelFolder, List<TcFeature> featureSet)
        throws Exception
    {
        for (TcFeature f : featureSet) {
            String featureString = f.getFeatureName();
            Class<?> feature = Class.forName(featureString);
            InputStream inStream = feature
                    .getResource("/" + featureString.replace(".", "/") + ".class").openStream();

            OutputStream outStream = buildOutputStream(modelFolder, featureString);

            IOUtils.copy(inStream, outStream);
            outStream.close();
            inStream.close();

        }

    }

    private static OutputStream buildOutputStream(File modelFolder, String featureString)
        throws Exception
    {

        String packagePath = featureString.substring(0, featureString.lastIndexOf("."))
                .replaceAll("\\.", "/");
        String featureClassName = featureString.substring(featureString.lastIndexOf(".") + 1)
                + ".class";

        String folderPath = modelFolder.getAbsolutePath() + "/" + MODEL_FEATURE_CLASS_FOLDER + "/"
                + packagePath + "/";
        new File(folderPath).mkdirs();
        return new FileOutputStream(new File(folderPath + featureClassName));
    }

    public static void writeCurrentVersionOfDKProTC(File outputFolder)
        throws Exception
    {
        String version = getCurrentTcVersionFromJar();
        if (version == null) {
            version = getCurrentTcVersionFromWorkspace();
        }
        if (version != null) {
            Properties properties = new Properties();
            properties.setProperty(TCVERSION, version);

            File file = new File(outputFolder + "/" + MODEL_TC_VERSION);
            FileOutputStream fileOut = new FileOutputStream(file);
            properties.store(fileOut, "Version of DKPro TC used to train this model");
            fileOut.close();
        }

    }

    private static String getCurrentTcVersionFromWorkspace()
        throws Exception
    {
        Class<?> contextClass = SaveModelUtils.class;

        // Try to determine the location of the POM file belonging to the context object
        URL url = contextClass.getResource(contextClass.getSimpleName() + ".class");
        String classPart = contextClass.getName().replace(".", "/") + ".class";
        String base = url.toString();
        base = base.substring(0, base.length() - classPart.length());
        base = base.substring(0, base.length() - "target/classes/".length());
        File pomFile = new File(new File(URI.create(base)), "pom.xml");

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        model = reader.read(new FileInputStream(pomFile));
        String version = model.getParent().getVersion();

        return version;
    }

    public static void verifyTcVersion(File tcModelLocation,
            Class<? extends ModelSerialization_ImplBase> class1)
                throws Exception
    {
        String loadedVersion = SaveModelUtils.loadTcVersionFromModel(tcModelLocation);
        String currentVersion = SaveModelUtils.getCurrentTcVersionFromJar();

        if (currentVersion == null) {
            currentVersion = SaveModelUtils.getCurrentTcVersionFromWorkspace();
        }

        if (loadedVersion.equals(currentVersion)) {
            return;
        }
        Logger.getLogger(class1).warn("The model was created under version [" + loadedVersion
                + "], you are using [" + currentVersion + "]");
    }

    private static String getCurrentTcVersionFromJar()
    {
        Class<?> contextClass = SaveModelUtils.class;

        InputStream resourceAsStream = contextClass
                .getResourceAsStream("/META-INF/maven/org.dkpro.tc/dkpro-tc-core/pom.xml");

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        try {
            model = reader.read(resourceAsStream);
        }
        catch (Exception e) {
            return null;
        }
        String version = model.getParent().getVersion();
        return version;
    }

    public static String loadTcVersionFromModel(File modelFolder)
        throws Exception
    {
        File file = new File(modelFolder, MODEL_TC_VERSION);
        Properties prop = new Properties();

        FileInputStream fos = new FileInputStream(file);
        prop.load(fos);
        fos.close();

        return prop.getProperty(TCVERSION);
    }

    public static void writeFeatureMode(File outputFolder, String featureMode)
        throws IOException
    {
        Properties properties = new Properties();
        properties.setProperty(DIM_FEATURE_MODE, featureMode);

        File file = new File(outputFolder + "/" + MODEL_FEATURE_MODE);
        FileOutputStream fileOut = new FileOutputStream(file);
        properties.store(fileOut, "Feature mode used to train this model");
        fileOut.close();

    }

    public static void writeLearningMode(File outputFolder, String learningMode)
        throws IOException
    {
        Properties properties = new Properties();
        properties.setProperty(DIM_LEARNING_MODE, learningMode);

        File file = new File(outputFolder + "/" + MODEL_LEARNING_MODE);
        FileOutputStream fileOut = new FileOutputStream(file);
        properties.store(fileOut, "Learning mode used to train this model");
        fileOut.close();
    }

    public static String initFeatureMode(File tcModelLocation)
        throws IOException
    {
        File file = new File(tcModelLocation, MODEL_FEATURE_MODE);
        Properties prop = new Properties();

        FileInputStream fis = new FileInputStream(file);
        prop.load(fis);
        fis.close();

        return prop.getProperty(DIM_FEATURE_MODE);
    }

    public static String initLearningMode(File tcModelLocation)
        throws IOException
    {
        File file = new File(tcModelLocation, MODEL_LEARNING_MODE);
        Properties prop = new Properties();

        FileInputStream fis = new FileInputStream(file);
        prop.load(fis);
        fis.close();

        return prop.getProperty(DIM_LEARNING_MODE);
    }

    public static TCMachineLearningAdapter initMachineLearningAdapter(File tcModelLocation)
        throws Exception
    {
        File modelMeta = new File(tcModelLocation, MODEL_META);
        String fileContent = FileUtils.readFileToString(modelMeta);
        Class<?> classObj = Class.forName(fileContent);
        return (TCMachineLearningAdapter) classObj.newInstance();
    }

    public static List<Object> initParameters(File tcModelLocation)
        throws IOException
    {
        List<Object> parameters = new ArrayList<>();
        Properties parametersProp = new Properties();

        FileInputStream fis = new FileInputStream(
                new File(tcModelLocation, MODEL_FEATURE_EXTRACTOR_CONFIGURATION));
        parametersProp.load(fis);
        fis.close();

        for (Object key : parametersProp.keySet()) {
            parameters.add((String) key);
            if (isExistingFilePath(tcModelLocation, (String) parametersProp.get(key))) {
                parameters.add(tcModelLocation + "/" + (String) parametersProp.get(key));
            }
            else {
                parameters.add((String) parametersProp.get(key));
            }
        }
        return parameters;
    }

    private static boolean isExistingFilePath(File tcModelLocation, String name)
    {
        return new File(tcModelLocation.getAbsolutePath() + "/" + name).exists();
    }

    static ExternalResourceDescription createExternalResource(Class<? extends Resource> resource,
            List<Object> convertedParameters)
    {
        return ExternalResourceFactory.createExternalResourceDescription(resource,
                convertedParameters.toArray());
    }

    public static List<Object> convertParameters(List<Object> parameters)
    {
        List<Object> convertedParameters = new ArrayList<Object>();
        if (parameters != null) {
            for (Object parameter : parameters) {
                convertedParameters.add(parameter.toString());
            }
        }
        else {
            parameters = new ArrayList<Object>();
        }
        return convertedParameters;
    }

    /*
     * Produces a resource manager that is used when creating the engine which is aware of the class
     * files located in the model folder
     */
    public static ResourceManager getModelFeatureAwareResourceManager(File tcModelLocation)
        throws ResourceInitializationException, MalformedURLException
    {
        // The features of a model are located in a subfolder where Java does
        // not look for them by default. This avoids that during model execution
        // several features with the same name are on the classpath which might
        // cause undefined behavior as it is not know which feature is first
        // found if several with same name exist. We create a new resource
        // manager here and point the manager explicitly to this subfolder where
        // the features to be used are located.
        ResourceManager resourceManager = ResourceManagerFactory.newResourceManager();
        String classpathOfModelFeatures = tcModelLocation.getAbsolutePath() + "/"
                + Constants.MODEL_FEATURE_CLASS_FOLDER;
        resourceManager.setExtensionClassPath(classpathOfModelFeatures, true);
        return resourceManager;
    }

    public static List<ExternalResourceDescription> loadExternalResourceDescriptionOfFeatures(
            File tcModelLocation, UimaContext aContext)
                throws Exception
    {
        List<ExternalResourceDescription> erd = new ArrayList<>();

        File classFile = new File(tcModelLocation + "/" + Constants.MODEL_FEATURE_CLASS_FOLDER);
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { classFile.toURI().toURL() });

        File file = new File(tcModelLocation, MODEL_FEATURE_EXTRACTOR_CONFIGURATION);
        for (String l : FileUtils.readLines(file)) {
            String[] split = l.split("\t");
            String name = split[0];
            Object[] parameters = getParameters(split);

            Class<? extends Resource> feClass = urlClassLoader.loadClass(name)
                    .asSubclass(Resource.class);

            List<Object> idRemovedParameters = filterId(parameters);
            String id = getId(parameters);

            idRemovedParameters = addModelPathAsPrefixIfParameterIsExistingFile(idRemovedParameters,
                    tcModelLocation.getAbsolutePath());

            TcFeature feature = TcFeatureFactory.create(id, feClass, idRemovedParameters.toArray());
            ExternalResourceDescription exRes = feature.getActualValue();

            // Skip feature extractors that are not dependent on meta collectors
            if (!MetaDependent.class.isAssignableFrom(feClass)) {
                erd.add(exRes);
                continue;
            }

            Map<String, String> overrides = loadOverrides(tcModelLocation, META_COLLECTOR_OVERRIDE);
            configureOverrides(tcModelLocation, exRes, overrides);
            overrides = loadOverrides(tcModelLocation, META_EXTRACTOR_OVERRIDE);
            configureOverrides(tcModelLocation, exRes, overrides);

            erd.add(exRes);
        }

        urlClassLoader.close();

        return erd;
    }

    private static List<Object> addModelPathAsPrefixIfParameterIsExistingFile(
            List<Object> idRemovedParameters, String modelPath)
    {
        List<Object> out = new ArrayList<>();

        for (int i = 0; i < idRemovedParameters.size(); i++) {
            if (i % 2 == 0) { // those are keys, keys are no surely no file paths
                out.add(idRemovedParameters.get(i));
                continue;
            }
            if (valueExistAsFileOrFolderInTheFileSystem(
                    modelPath + "/" + idRemovedParameters.get(i))) {
                out.add(modelPath + "/" + idRemovedParameters.get(i));
            }
            else {
                out.add(idRemovedParameters.get(i));
            }
        }

        return out;
    }

    private static void configureOverrides(File tcModelLocation, ExternalResourceDescription exRes,
            Map<String, String> overrides)
                throws IOException
    {
        // We assume for the moment that we only have primitive analysis engines for meta
        // collection, not aggregates. If there were aggregates, we'd have to do this
        // recursively
        ResourceSpecifier aDesc = exRes.getResourceSpecifier();
        if (aDesc instanceof AnalysisEngineDescription) {
            // Analysis engines are ok
            if (!((AnalysisEngineDescription) aDesc).isPrimitive()) {
                throw new IllegalArgumentException(
                        "Only primitive meta collectors currently supported.");
            }
        }
        else if (aDesc instanceof CustomResourceSpecifier_impl) {
            // Feature extractors are ok
        }
        else {
            throw new IllegalArgumentException(
                    "Descriptors of type " + aDesc.getClass() + " not supported.");
        }

        for (Entry<String, String> e : overrides.entrySet()) {
            // We generate a storage location from the feature extractor discriminator value
            // and the preferred value specified by the meta collector
            String parameterName = e.getKey();
            ConfigurationParameterFactory.setParameter(aDesc, parameterName,
                    new File(tcModelLocation, e.getValue()).getAbsolutePath());

        }
    }

    private static Map<String, String> loadOverrides(File tcModelLocation, String overrideFile)
        throws IOException
    {
        List<String> lines = FileUtils.readLines(new File(tcModelLocation, overrideFile), "utf-8");
        Map<String, String> overrides = new HashMap<>();

        for (String s : lines) {
            String[] split = s.split("=");
            overrides.put(split[0], split[1]);
        }

        return overrides;
    }

    private static String getId(Object[] parameters)
    {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].toString()
                    .equals(FeatureExtractorResource_ImplBase.PARAM_UNIQUE_EXTRACTOR_NAME)) {
                return parameters[i + 1].toString();
            }
        }
        return null;
    }

    private static List<Object> filterId(Object[] parameters)
    {
        List<Object> out = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].toString()
                    .equals(FeatureExtractorResource_ImplBase.PARAM_UNIQUE_EXTRACTOR_NAME)) {
                i++;
                continue;
            }
            out.add(parameters[i]);
        }

        return out;
    }

    private static Object[] getParameters(String[] split)
    {
        List<Object> p = new ArrayList<>();
        for (int i = 1; i < split.length; i++) {
            String string = split[i];
            int indexOf = string.indexOf("=");
            String paramName = string.substring(0, indexOf);
            String paramVal = string.substring(indexOf + 1);
            p.add(paramName);
            p.add(paramVal);
        }

        return p.toArray();
    }

}
