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
package org.dkpro.tc.ml.xgboost;

import java.util.Collection;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.ExecutableTaskBase;
import org.dkpro.lab.task.impl.FoldDimensionBundle;
import org.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import org.dkpro.tc.core.ml.TcShallowLearningAdapter;
import org.dkpro.tc.core.task.ModelSerializationTask;
import org.dkpro.tc.io.libsvm.LibsvmDataFormatWriter;
import org.dkpro.tc.io.libsvm.reports.LibsvmDataFormatBaselineMajorityClassIdReport;
import org.dkpro.tc.io.libsvm.reports.LibsvmDataFormatBaselineRandomIdReport;
import org.dkpro.tc.io.libsvm.reports.LibsvmDataFormatOutcomeIdReport;
import org.dkpro.tc.ml.xgboost.serialization.XgboostLoadModelConnector;
import org.dkpro.tc.ml.xgboost.serialization.XgboostSerializeModelConnector;

/**
 * <pre>
  General Parameters:
  	booster={gbtree, gblinear, dart}  (default booster=gbtree)
   	Choices:
 		- gbtree (Tree Booster)
 		- gblinear (Linear Booster)
 		- dart (Dart Booster)
 		gbtree and dart use tree based model while gblinear uses linear function.
 		
  	nthread={NUM} (defaults to all available threads) 
 		number of parallel threads used to run xgboost
 		
 	objective={} (default objective=reg:linear)
  		reg:linear 
  				linear regression
  		reg:logistic	
  				logistic regression
  		binary:logistic
  				logistic regression for binary classification, output probability
 		binary:logitraw
 				logistic regression for binary classification, output score before logistic transformation
 		count:poisson
 				poisson regression for count data, output mean of poisson distribution
 				max_delta_step is set to 0.7 by default in poisson regression (used to safeguard optimization)
 		survival:cox
 				Cox regression for right censored survival time data (negative values are considered right censored). Note that predictions are returned on the hazard ratio scale (i.e., as HR = exp(marginal_prediction) in the proportional hazard function h(t) = h0(t) * HR).
 		multi:softmax
 				set XGBoost to do multiclass classification using the softmax objective, you also need to set num_class(number of classes)
 		multi:softprob
 				same as softmax, but output a vector of ndata * nclass, which can be further reshaped to ndata, nclass matrix. The result contains predicted probability of each data point belonging to each class.
 		rank:pairwise
 				set XGBoost to do ranking task by minimizing the pairwise loss
 		reg:gamma
 				gamma regression with log-link. Output is a mean of gamma distribution. It might be useful, e.g., for modeling insurance claims severity, or for any outcome that might be gamma-distributed
 		reg:tweedie
 				Tweedie regression with log-link. It might be useful, e.g., for modeling total loss in insurance, or for any outcome that might be Tweedie-distributed.
 
 ****************************************************
 * Parameters for 'booster=gbtree' (Tree booster):  *
 **************************************************** 
  	eta [default=0.3, alias: learning_rate]
		    step size shrinkage used in update to prevents overfitting. After each boosting step, we can directly get the weights of new features. and eta actually shrinks the feature weights to make the boosting process more conservative 
		   range: [0,1]
	gamma [default=0, alias: min_split_loss]
			minimum loss reduction required to make a further partition on a leaf node of the tree. The larger, the more conservative the algorithm will be.
			range: [0,∞]
	max_depth [default=6]
			maximum depth of a tree, increase this value will make the model more complex / likely to be overfitting. 0 indicates no limit, limit is required for depth-wise grow policy.
			range: [0,∞]
	min_child_weight [default=1]
			minimum sum of instance weight (hessian) needed in a child. If the tree partition step results in a leaf node with the sum of instance weight less than min_child_weight, then the building process will give up further partitioning. In linear regression mode, this simply corresponds to minimum number of instances needed to be in each node. The larger, the more conservative the algorithm will be.
			range: [0,∞]
	max_delta_step [default=0]
			Maximum delta step we allow each tree's weight estimation to be. If the value is set to 0, it means there is no constraint. If it is set to a positive value, it can help making the update step more conservative. Usually this parameter is not needed, but it might help in logistic regression when class is extremely imbalanced. Set it to value of 1-10 might help control the update
			range: [0,∞]
	subsample [default=1]
			subsample ratio of the training instance. Setting it to 0.5 means that XGBoost randomly collected half of the data instances to grow trees and this will prevent overfitting.
			range: (0,1]
	colsample_bytree [default=1]
			subsample ratio of columns when constructing each tree.
			range: (0,1]
	colsample_bylevel [default=1]
			subsample ratio of columns for each split, in each level.
			range: (0,1]
	lambda [default=1, alias: reg_lambda]
			L2 regularization term on weights, increase this value will make model more conservative.
	alpha [default=0, alias: reg_alpha]
			L1 regularization term on weights, increase this value will make model more conservative.
	tree_method, string [default='auto']
			The tree construction algorithm used in XGBoost(see description in the reference paper)
			Distributed and external memory version only support approximate algorithm.
			Choices: {'auto', 'exact', 'approx', 'hist', 'gpu_exact', 'gpu_hist'}
				'auto': Use heuristic to choose faster one.
						For small to medium dataset, exact greedy will be used.
						For very large-dataset, approximate algorithm will be chosen.
						Because old behavior is always use exact greedy in single machine, user will get a message when approximate algorithm is chosen to notify this choice.
				'exact': Exact greedy algorithm.
				'approx': Approximate greedy algorithm using sketching and histogram.
				'hist': Fast histogram optimized approximate greedy algorithm. It uses some performance improvements such as bins caching.
				'gpu_exact': GPU implementation of exact algorithm.
				'gpu_hist': GPU implementation of hist algorithm.
							sketch_eps, [default=0.03]
							This is only used for approximate greedy algorithm.
							This roughly translated into O(1 / sketch_eps) number of bins. Compared to directly select number of bins, this comes with theoretical guarantee with sketch accuracy.
							Usually user does not have to tune this. but consider setting to a lower number for more accurate enumeration.
							range: (0, 1)
	scale_pos_weight, [default=1]
			Control the balance of positive and negative weights, useful for unbalanced classes. A typical value to consider: sum(negative cases) / sum(positive cases) See Parameters Tuning for more discussion. Also see Higgs Kaggle competition demo for examples: R, py1, py2, py3
			updater, [default='grow_colmaker,prune']
			A comma separated string defining the sequence of tree updaters to run, providing a modular way to construct and to modify the trees. This is an advanced parameter that is usually set automatically, depending on some other parameters. However, it could be also set explicitly by a user. The following updater plugins exist:
				'grow_colmaker': non-distributed column-based construction of trees.
				'distcol': distributed tree construction with column-based data splitting mode.
				'grow_histmaker': distributed tree construction with row-based data splitting based on global proposal of histogram counting.
				'grow_local_histmaker': based on local histogram counting.
				'grow_skmaker': uses the approximate sketching algorithm.
				'sync': synchronizes trees in all distributed nodes.
				'refresh': refreshes tree's statistics and/or leaf values based on the current data. Note that no random subsampling of data rows is performed.
				'prune': prunes the splits where loss &lt; min_split_loss (or gamma).
			In a distributed setting, the implicit updater sequence value would be adjusted as follows:
			'grow_histmaker,prune' when dsplit='row' (or default) and prob_buffer_row == 1 (or default); or when data has multiple sparse pages
			'grow_histmaker,refresh,prune' when dsplit='row' and prob_buffer_row &lt; 1
			'distcol' when dsplit='col'
			
	refresh_leaf, [default=1]
			This is a parameter of the 'refresh' updater plugin. When this flag is true, tree leafs as well as tree nodes' stats are updated. When it is false, only node stats are updated.
			process_type, [default='default']
			A type of boosting process to run.
			Choices: {'default', 'update'}
				'default': the normal boosting process which creates new trees.
				'update': starts from an existing model and only updates its trees. In each boosting iteration, a tree from the initial model is taken, a specified sequence of updater plugins is run for that tree, and a modified tree is added to the new model. The new model would have either the same or smaller number of trees, depending on the number of boosting iteratons performed. Currently, the following built-in updater plugins could be meaningfully used with this process type: 'refresh', 'prune'. With 'update', one cannot use updater plugins that create new trees.
	grow_policy, string [default='depthwise']
			Controls a way new nodes are added to the tree.
			Currently supported only if tree_method is set to 'hist'.
			Choices: {'depthwise', 'lossguide'}
				'depthwise': split at nodes closest to the root.
				'lossguide': split at nodes with highest loss change.
	max_leaves, [default=0]
			Maximum number of nodes to be added. Only relevant for the 'lossguide' grow policy.
			max_bin, [default=256]
			This is only used if 'hist' is specified as tree_method.
			Maximum number of discrete bins to bucket continuous features.
			Increasing this number improves the optimality of splits at the cost of higher computation time.
	predictor, [default='cpu_predictor']
			The type of predictor algorithm to use. Provides the same results but allows the use of GPU or CPU.
			'cpu_predictor': Multicore CPU prediction algorithm.
			'gpu_predictor': Prediction using GPU. Default for 'gpu_exact' and 'gpu_hist' tree method.
			
 ********************************************************
 * Parameters for 'booster=gblinear' (Linear booster):  *
 ********************************************************
 	lambda [default=0, alias: reg_lambda]
			L2 regularization term on weights, increase this value will make model more conservative. Normalised to number of training examples.
	alpha [default=0, alias: reg_alpha]
			L1 regularization term on weights, increase this value will make model more conservative. Normalised to number of training examples.
	updater [default='shotgun']
			Linear model algorithm
		'shotgun': Parallel coordinate descent algorithm based on shotgun algorithm. Uses 'hogwild' parallelism and therefore produces a nondeterministic solution on each run.
		'coord_descent': Ordinary coordinate descent algorithm. Also multithreaded but still produces a deterministic solution.
 
 **************************************************
 * Parameters for 'booster=dart' (Dart booster):  *
 **************************************************
 	sample_type [default="uniform"]
			type of sampling algorithm.
				"uniform": dropped trees are selected uniformly.
				"weighted": dropped trees are selected in proportion to weight.
	normalize_type [default="tree"]
			type of normalization algorithm.
				"tree": new trees have the same weight of each of dropped trees.
						weight of new trees are 1 / (k + learning_rate)
						dropped trees are scaled by a factor of k / (k + learning_rate)
				"forest": new trees have the same weight of sum of dropped trees (forest).
						weight of new trees are 1 / (1 + learning_rate)
						dropped trees are scaled by a factor of 1 / (1 + learning_rate)
						rate_drop [default=0.0]
	dropout rate (a fraction of previous trees to drop during the dropout).
			range: [0.0, 1.0]
	one_drop [default=0]
			when this flag is enabled, at least one tree is always dropped during the dropout (allows Binomial-plus-one or epsilon-dropout from the original DART paper).
	skip_drop [default=0.0]
			Probability of skipping the dropout procedure during a boosting iteration.
			If a dropout is skipped, new trees are added in the same manner as gbtree.
			Note that non-zero skip_drop has higher priority than rate_drop or one_drop.
			range: [0.0, 1.0]
 * </pre>
 */
public class XgboostAdapter
    implements TcShallowLearningAdapter
{

    public static TcShallowLearningAdapter getInstance()
    {
        return new XgboostAdapter();
    }

    public static String getOutcomeMappingFilename()
    {
        return "outcome-mapping.txt";
    }

    public static String getFeatureNameMappingFilename()
    {
        return "feature-name-mapping.txt";
    }

    public static String getFeatureNames()
    {
        return "featurenames.txt";
    }

    @Override
    public ExecutableTaskBase getTestTask()
    {
        return new XgboostTestTask();
    }

    @Override
    public Class<? extends ReportBase> getOutcomeIdReportClass()
    {
        return LibsvmDataFormatOutcomeIdReport.class;
    }

    @Override
    public Class<? extends ReportBase> getMajorityClassBaselineIdReportClass()
    {
        return LibsvmDataFormatBaselineMajorityClassIdReport.class;
    }

    @Override
    public Class<? extends ReportBase> getRandomBaselineIdReportClass()
    {
        return LibsvmDataFormatBaselineRandomIdReport.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DimensionBundle<Collection<String>> getFoldDimensionBundle(String[] files, int folds)
    {
        return new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
    }

    @Override
    public String getDataWriterClass()
    {
        return LibsvmDataFormatWriter.class.getName();
    }

    @Override
    public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass()
    {
        return XgboostLoadModelConnector.class;
    }

    @Override
    public ModelSerializationTask getSaveModelTask()
    {
        return new XgboostSerializeModelConnector();
    }

    @Override
    public boolean useSparseFeatures()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }
}
