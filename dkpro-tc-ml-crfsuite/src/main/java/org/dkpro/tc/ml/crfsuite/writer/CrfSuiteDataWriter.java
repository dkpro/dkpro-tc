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

package org.dkpro.tc.ml.crfsuite.writer;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.DataWriter;

import com.google.gson.Gson;

public class CrfSuiteDataWriter
    implements DataWriter
{
    protected CrfSuiteFeatureFormatExtractionIterator iterator;
    protected File outputDirectory;
    protected boolean useSparse;
    protected String learningMode;
    protected boolean applyWeigthing;
    protected BufferedWriter bw = null;
    protected Gson gson = new Gson();
    protected File classifierFormatOutputFile;
    protected String featureMode;

    @Override
    public void writeGenericFormat(List<Instance> instances)
        throws AnalysisEngineProcessException
    {
        try {
            initGeneric();

            // bulk-write - in sequence mode this keeps the instances together
            // that
            // belong to the same sequence!
            Instance[] array = instances.toArray(new Instance[0]);
            bw.write(gson.toJson(array) + "\n");

            bw.close();
            bw = null;
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void initGeneric() throws IOException
    {
        if (bw != null) {
            return;
        }
        bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(
                                new File(outputDirectory, Constants.GENERIC_FEATURE_FILE), true),
                        UTF_8));

    }

    @Override
    public void transformFromGeneric() throws Exception
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(outputDirectory, Constants.GENERIC_FEATURE_FILE)),
                UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(classifierFormatOutputFile), UTF_8))) {

            String line = null;
            while ((line = reader.readLine()) != null) {
                Instance[] instance = gson.fromJson(line, Instance[].class);
                List<Instance> ins = Arrays.asList(instance);

                Iterator<StringBuilder> sequenceIterator = new CrfSuiteFeatureFormatExtractionIterator(
                        ins);

                while (sequenceIterator.hasNext()) {
                    String features = sequenceIterator.next().toString();
                    writer.write(features);
                    writer.write("\n");
                }

            }

        }
    }

    @Override
    public void writeClassifierFormat(List<Instance> instances)
        throws AnalysisEngineProcessException
    {
        try {
            initClassifierFormat();

            Iterator<StringBuilder> sequenceIterator = new CrfSuiteFeatureFormatExtractionIterator(instances);

            while (sequenceIterator.hasNext()) {
                String features = sequenceIterator.next().toString();
                bw.write(features);
                bw.write("\n");
            }

            bw.close();
            bw = null;
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void initClassifierFormat() throws Exception
    {
        if (bw != null) {
            return;
        }

        bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(classifierFormatOutputFile, true), UTF_8));

    }

    @Override
    public void init(File outputDirectory, boolean useSparse, String learningMode,
            String featureMode, boolean applyWeighting, String[] outcomes)
        throws Exception
    {
        this.outputDirectory = outputDirectory;
        this.useSparse = useSparse;
        this.learningMode = learningMode;
        this.featureMode = featureMode;
        this.applyWeigthing = applyWeighting;

        classifierFormatOutputFile = new File(outputDirectory,
                Constants.FILENAME_DATA_IN_CLASSIFIER_FORMAT);

        // Caution: DKPro Lab imports (aka copies!) the data of the train task
        // as test task. We use
        // appending mode for streaming. We might errornously append the old
        // training file with
        // testing data!
        // Force delete the old training file to make sure we start with a
        // clean, empty file
        if (classifierFormatOutputFile.exists()) {
            FileUtils.forceDelete(classifierFormatOutputFile);
        }
        
        File genericOutputFile = new File(outputDirectory, getGenericFileName());
        if (genericOutputFile.exists()) {
            FileUtils.forceDelete(genericOutputFile);
        }
    }

    @Override
    public boolean canStream()
    {
        return true;
    }

    @Override
    public String getGenericFileName()
    {
        return Constants.GENERIC_FEATURE_FILE;
    }

    @Override
    public void close() throws Exception
    {

    }

}
