---
layout: page-fullwidth
title: "DKPro TC Deep Learning (1.0.0 Release)"
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
The code snipped below shows a setup to configure a Python-based DKPro TC deep learning experiment. The biggest difference to a `shall learning` experiment is the wiring of the `ParameterSpace`, which uses a few more additional `dimensions`.

```java
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
        // the working memory that shall be used, i.e. requries that the Python-based framework supports memory limits
				Dimension.create(DIM_RAM_WORKING_MEMORY, 5000), 
        // file path to the word embeddings file that shall be used in the experiment        
				Dimension.create(DIM_PRETRAINED_EMBEDDINGS, embedding),
        // automatically translates all words in the input text into integer values and stores a mapping
				Dimension.create(DIM_VECTORIZE_TO_INTEGER, true), 
        // file path to a code snipped which defines the actual Python-framework-specific deep learning code
				Dimension.create(DIM_USER_CODE, dyNetUserCode));

    /* Experiment instantiation, note that deep learning experiments use `DeepLearningExperimentTrainTest` 
    while shallow learning experiments use `ExperimentTrainTest` */
		DeepLearningExperimentTrainTest experiment = new DeepLearningExperimentTrainTest("Experiment", DynetAdapter.class);
		experiment.setParameterSpace(pSpace);
		experiment.setPreprocessing(createEngineDescription(SequenceOutcomeAnnotator.class));

		Lab.getInstance().run(experiment);
```
