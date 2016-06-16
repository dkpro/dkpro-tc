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
package org.dkpro.tc.ml.liblinear;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.Discriminator;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.liblinear.util.LiblinearUtils;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LiblinearTestTask
    extends ExecutableTaskBase
    implements Constants
{
    @Discriminator(name = DIM_CLASSIFICATION_ARGS)
    private List<String> classificationArguments;
    @Discriminator(name = DIM_FEATURE_MODE)
    private String featureMode;
    @Discriminator(name = DIM_LEARNING_MODE)
    private String learningMode;

    public static String SEPARATOR_CHAR = ";";
    public static final double EPISILON_DEFAULT = 0.01;
    public static final double PARAM_C_DEFAULT = 1.0;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        if (multiLabel) {
            throw new TextClassificationException(
                    "Multi-label requested, but LIBLINEAR only supports single label setups.");
        }

        File trainFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY);
        String trainFileName = LiblinearAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTrain = new File(trainFolder, trainFileName);

        File testFolder = aContext.getFolder(TEST_TASK_INPUT_KEY_TEST_DATA, AccessMode.READONLY);
        String testFileName = LiblinearAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        File fileTest = new File(testFolder, testFileName);

        // default for bias is -1, documentation says to set it to 1 in order to get results closer
        // to libsvm
        // writer adds bias, so if we de-activate that here for some reason, we need to also
        // deactivate it there
        Problem train = Problem.readFromFile(fileTrain, 1.0);

        SolverType solver = LiblinearUtils.getSolver(classificationArguments);
        double C = LiblinearUtils.getParameterC(classificationArguments);
        double eps = LiblinearUtils.getParameterEpsilon(classificationArguments);

        Linear.setDebugOutput(null);

        Parameter parameter = new Parameter(solver, C, eps);
        Model model = Linear.train(train, parameter);

        Problem test = Problem.readFromFile(fileTest, 1.0);

        predict(aContext, model, test);
    }

    private void predict(TaskContext aContext, Model model, Problem test)
        throws Exception
    {
        File predFolder = aContext.getFolder("", AccessMode.READWRITE);
        String predFileName = LiblinearAdapter.getInstance()
                .getFrameworkFilename(AdapterNameEntries.predictionsFile);
        File predictionsFile = new File(predFolder, predFileName);

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(predictionsFile), "utf-8"));
        writer.append("#PREDICTION;GOLD" + "\n");

        Feature[][] testInstances = test.x;
        for (int i = 0; i < testInstances.length; i++) {
            Feature[] instance = testInstances[i];
            Double prediction = Linear.predict(model, instance);

            writer.write(prediction.intValue() + SEPARATOR_CHAR + new Double(test.y[i]).intValue());
            writer.write("\n");
        }

        writer.close();
    }

    /**
     * Parameter {@code "-c <C>"}: Typical SVM parameter C trading-off slack vs. magnitude of the
     * weight-vector. NOTE: The default value for this parameter is unlikely to work well for your
     * particular problem. A good value for C must be selected via cross-validation, ideally
     * exploring values over several orders of magnitude.
     */
    public static final String PARAM_C = "-c";

    /**
     * Parameter {@code "-e <EPSILON>"}: This specifies the tolerance of termination criterion.
     */
    public static final String PARAM_EPSILON = "-e";

    /**
     * Parameter {@code "-s <SOLVER>"}: This specifies the solver type - available solver are
     * defined as constant values beginning with SOLVER* in this class
     */
    public static final String PARAM_SOLVER_TYPE = "-s";

    /**
     * L2-regularized logistic regression (primal)
     *
     * (fka L2_LR)
     */
    public static final String SOLVER_L2R_LR = "L2R_LR";

    /**
     * L2-regularized L2-loss support vector classification (dual)
     *
     * (fka L2LOSS_SVM_DUAL)
     */
    public static final String SOLVER_L2R_L2LOSS_SVC_DUAL = "L2R_L2LOSS_SVC_DUAL";

    /**
     * L2-regularized L2-loss support vector classification (primal)
     *
     * (fka L2LOSS_SVM)
     */
    public static final String SOLVER_L2R_L2LOSS_SVC = "L2R_L2LOSS_SVC";

    /**
     * L2-regularized L1-loss support vector classification (dual)
     *
     * (fka L1LOSS_SVM_DUAL)
     */
    public static final String SOLVER_L2R_L1LOSS_SVC_DUAL = "L2R_L1LOSS_SVC_DUAL";

    /**
     * multi-class support vector classification by Crammer and Singer
     */
    public static final String SOLVER_MCSVM_CS = "MCSVM_CS";

    /**
     * L1-regularized L2-loss support vector classification
     */
    public static final String SOLVER_L1R_L2LOSS_SVC = "L1R_L2LOSS_SVC";

    /**
     * L1-regularized logistic regression
     */
    public static final String SOLVER_L1R_LR = "L1R_LR";

    /**
     * L2-regularized logistic regression (dual)
     */
    public static final String SOLVER_L2R_LR_DUAL = "L2R_LR_DUAL";

    /**
     * L2-regularized L2-loss support vector regression (dual)
     */
    public static final String SOLVER_L2R_L2LOSS_SVR = "L2R_L2LOSS_SVR";

    /**
     * L2-regularized L1-loss support vector regression (dual)
     */
    public static final String SOLVER_L2R_L2LOSS_SVR_DUAL = "L2R_L2LOSS_SVR_DUAL";

    /**
     * L2-regularized L2-loss support vector regression (primal)
     */
    public static final String SOLVER_L2R_L1LOSS_SVR_DUAL = "L2R_L1LOSS_SVR_DUAL";

}