---
layout: page-fullwidth
title: "Deep Learning"
permalink: "/DKProTcDeepLearning_1_0_0/"
---

### Deep learning experiments with DKPro TC
At the moment, three deep learning experiments are supported by DKPro TC:

* Deeplearning 4j (https://deeplearning4j.org)
* DyNet (https://github.com/clab/dynet)
* Keras (https://keras.io)

`DyNet` and `Keras` are used via Python, using these two frameworks in DKPro TC requires that:
* Python is locally installed
* the deep learning framework with all dependencies are locally installed

Deeplearning 4j is written in Java and requires no additional installation effort.


### Python-based Deep Learning Experiments in DKPro TC
Python-based frameworks are not as straight-forward to integrate as Java-based frameworks. We discuss subsequently how using Python-based frameworks in DKPro TC and how the interfacing between Java/Python works.
The code snipped below shows a setup to configure a Python-based DKPro TC deep learning experiment. The biggest difference to a `shallow learning` experiment is the wiring of the `ParameterSpace`, which uses a few more additional `dimensions`.

{% highlight groovy %} 
CollectionReaderDescription trainReader = createReaderTrain(...)

CollectionReaderDescription testReader = createReaderTest(...)		
	
Map<String, Object> dimReaders = new HashMap<String, Object>();
dimReaders.put(DIM_READER_TRAIN, trainReader);
dimReaders.put(DIM_READER_TEST, testReader);
		
ParameterSpace pSpace = new ParameterSpace(
//same as for shallow 
Dimension.createBundle("readers", dimReaders), 
Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE),
Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL), 
/*
Additional deep learning framework specific dimensions
*/
// absolute path to the python installation for which the deep learning framework is installed
Dimension.create(DIM_PYTHON_INSTALLATION, python3),
// seed value that is passed to the deep learning 
Dimension.create(DIM_SEED_VALUE, 12345),
// the working memory that shall be used, 
// i.e. requries that the Python-based framework supports memory limits
Dimension.create(DIM_RAM_WORKING_MEMORY, 5000), 
// file path to the word embeddings file that shall be used in the experiment        
Dimension.create(DIM_PRETRAINED_EMBEDDINGS, embedding),
// automatically translates all words in the input text into integer values and stores a mapping
Dimension.create(DIM_VECTORIZE_TO_INTEGER, true), 
// file path to a code snipped,
// which defines the actual Python-framework-specific deep learning code
Dimension.create(DIM_USER_CODE, dyNetUserCode)
);

/* Experiment instantiation, note that deep learning experiments use `DeepLearningExperimentTrainTest` 
   while shallow learning experiments use `ExperimentTrainTest` */
DeepLearningExperimentTrainTest experiment = new DeepLearningExperimentTrainTest("Experiment", DynetAdapter.class);
experiment.setParameterSpace(pSpace);
experiment.setPreprocessing(createEngineDescription(SequenceOutcomeAnnotator.class));
Lab.getInstance().run(experiment);
{% endhighlight %}

When the experiment is executed, the `vectorization` into integer is automatically performed on the training and testing data, the word embeddings are pruned to contain only occuring vocabulary, and are all passed to the code-snipped provided as file path in the dimension `DIM_USER_CODE`.

The receiving Python code has then eventually to take care of loading the provided data files into the data format the framework expects. 

### Results of an experiment
The results are written to the folder provided as `DKPRO_HOME` directory. The subfolder contain all output written by an experiment, and not just the final results. The folder with the results is the `Evaluation-*` folder. The other folders are probably not of importance for using DKPRo TC, but we explain their content yet briefly. For a train-test experiment, the following folders are created:

* InitTaskDeep-Train-ExperimentName-*
* InitTaskDeep-Test-ExperimentName-*
* EmbeddingTask-ExperimentName-*
* VectorizationTask-Train-ExperimentName-*
* VectorizationTask-Test-ExperimentName-*
* DKProTcShallowTestTask-ExperimentName-*
* \<MachineLearningAdapter>-ExperimentName-*
* Evaluation-ExperimentName-*

The `InitTaskDeep` folders contain the provided training and testing data converted into an internal data format. `EmbeddingTask` takes care of pruning the provied embedding (if one was provided) or initializes missing words with a random vector. This step does nothing if no embedding is provided. `VectorizationTask` transforms the training and testing data into a flat file format, which is provied in `<MachineLearningAdapter>` to the deep learning code. The results per instance and some more low-level information can be found in the `<MachineLearningAdapter>` folder.

