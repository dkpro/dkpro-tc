/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.simple.builder;

import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.ml.builder.LearningMode;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.crfsuite.CrfSuiteAdapter;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.svmhmm.SvmHmmAdapter;
import org.dkpro.tc.ml.weka.MekaAdapter;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.xgboost.XgboostAdapter;

import meka.classifiers.multilabel.CCq;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SMOreg;

public abstract class SimpleBaseExperiment
{

    protected MLBackend getDefault(TcShallowLearningAdapter adapter, LearningMode lm)
    {
        if (isWeka(adapter)) {
            if (isRegression(lm)) {
                return new MLBackend(adapter, SMOreg.class.getName());
            }
            else if (isClassification(lm)) {
                return new MLBackend(adapter, SMO.class.getName());
            }
        }
        else if (isMeka(adapter)) {
            if (isMultiLabelClassification(lm)) {
                return new MLBackend(adapter, CCq.class.getName());
            }
        }
        else if (isLiblinear(adapter)) {
            if (isRegression(lm)) {
                return new MLBackend(adapter, "-s", "6", "-c", "100");
            }
            else if (isClassification(lm)) {
                return new MLBackend(adapter, "-s", "1", "-c", "100");
            }
        }
        else if (isLibsvm(adapter)) {
            if (isRegression(lm)) {
                return new MLBackend(adapter, "-s", "3", "-c", "100");
            }
            else if (isClassification(lm)) {
                return new MLBackend(adapter, "-s", "0", "-c", "100");
            }
        }
        else if (isXgboost(adapter)) {
            if (isRegression(lm)) {
                return new MLBackend(adapter);
            }
            else if (isClassification(lm)) {
                return new MLBackend(adapter, "multi:softmax");
            }
        }
        else if (isCrfsuite(adapter)) {
            if (isClassification(lm)) {
                return new MLBackend(adapter,
                        CrfSuiteAdapter.ALGORITHM_L2_STOCHASTIC_GRADIENT_DESCENT);
            }
        }
        else if (isSvmHmm(adapter)) {
            if (isClassification(lm)) {
                return new MLBackend(adapter,
                        CrfSuiteAdapter.ALGORITHM_L2_STOCHASTIC_GRADIENT_DESCENT);
            }
        }

        throw new IllegalStateException("No default combination found for [" + adapter.toString()
                + "] using [" + lm.toString() + "] as learning mode");
    }

    private boolean isSvmHmm(TcShallowLearningAdapter adapter)
    {
        return adapter instanceof SvmHmmAdapter;
    }

    private boolean isCrfsuite(TcShallowLearningAdapter adapter)
    {
        return adapter instanceof CrfSuiteAdapter;
    }

    private boolean isLiblinear(TcShallowLearningAdapter adapter)
    {
        return adapter instanceof LiblinearAdapter;
    }

    private boolean isLibsvm(TcShallowLearningAdapter adapter)
    {
        return adapter instanceof LibsvmAdapter;
    }

    private boolean isXgboost(TcShallowLearningAdapter adapter)
    {
        return adapter instanceof XgboostAdapter;
    }

    private boolean isMeka(TcShallowLearningAdapter adapter)
    {
        return adapter instanceof MekaAdapter;
    }

    private boolean isClassification(LearningMode lm)
    {
        return lm == LearningMode.SINGLE_LABEL;
    }

    private boolean isMultiLabelClassification(LearningMode lm)
    {
        return lm == LearningMode.MULTI_LABEL;
    }

    private boolean isRegression(LearningMode lm)
    {
        return lm == LearningMode.REGRESSION;
    }

    private boolean isWeka(TcShallowLearningAdapter adapter)
    {
        return adapter instanceof WekaAdapter;
    }
    
}
