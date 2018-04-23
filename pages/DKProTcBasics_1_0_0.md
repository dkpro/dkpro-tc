---
layout: page-fullwidth
title: "DKPro TC Basics Experiments"
permalink: "/DKProTcBasics_1_0_0/"
---

### Anatomy of a DKPro TC experiment

Subsequently, we introduce how to configure a DKPro TC experiement by discussing a minimal setup.

{% highlight java %}
// Defining the readers that read the data that we use in an experiment
public CollectionReaderDescription getReaderTrain() { ... }
public CollectionReaderDescription getReaderTest() { ... }

public AnalysisEngineDescription getPreprocessing() { ... }

TcFeatureSet featureSet = new TcFeatureSet(
				TcFeatureFactory.create(WordNGram.class, 
							WordNGram.PARAM_NGRAM_USE_TOP_K, 50));

// The experiment builder lets the user wire an experiment with few steps
 ExperimentBuilder builder = new ExperimentBuilder();
 
 // Setup - providing all parameters and machine learning adapters that are used for an experiment
        builder.experiment(ExperimentType.TRAIN_TEST, "trainTest")
                .dataReaderTrain(getReaderTrain())
                .dataReaderTest(getReaderTest())
                .preprocessing(getPreprocessing())
                .featureSets(featureSet)
                .learningMode(LearningMode.SINGLE_LABEL)
                .featureMode(FeatureMode.DOCUMENT)
                .machineLearningBackend(
                        //an example how to configure an execution of four classifiers in a setup.
                        // DKPro TC will run the feature extraction and use each classifier
                        new MLBackend(new XgboostAdapter(), "objective=multi:softmax"),
                        new MLBackend(new WekaAdapter(), SMO.class.getName(), "-C", "1.0", "-K",
                                PolyKernel.class.getName() + " " + "-C -1 -E 2"),
                        new MLBackend(new LiblinearAdapter(), "-s", "4", "-c", "100"),
                        new MLBackend(new LibsvmAdapter(), "-s", "1", "-c", "1000", "-t", "3"))
			
	// Execute the experiment		
        builder.run();

{% endhighlight java %}

### Results of an experiment
The results are written to the folder provided as `DKPRO_HOME` directory. The subfolder contain all output written by an experiment, and not just the final results. The folder with the results is the `Evaluation-*` folder. The other folders are probably not of importance for using DKPRo TC, but we explain their content yet briefly. For a train-test experiment, the following folders are created:

* InitTask-Train-ExperimentName-*
* InitTask-Test-ExperimentName-*
* OutcomeCollectionTask-ExperimentName-*
* MetaInfoTask-ExperimentName-*
* ExtractFeaturesTask-Train-ExperimentName-*
* ExtractFeaturesTask-Test-ExperimentName-*
* DKProTcShallowTestTask-ExperimentName-*
* \<MachineLearningAdapter>-ExperimentName-*
* Evaluation-ExperimentName-*

The `InitTask` folders contain the provided training and testing data converted into an internal data format. `OutcomeCollectionTask` collects all occurring labels in the training and testing data (or nothing if its regression). `MetaInfoTask` prepares the usage of features that use a frequency cut-off, i.e. the word-ngram feature that is used in the experimental setup. `ExtractFeatureTask` contain the extracted features in the data format the respective classifier expects. `DKProTcShallowTestTask` and `<MachineLearningAdapter>` execute the actual classifier with the feature data extracted before. The results per instance and some more low-level information can be found in the `<MachineLearningAdapter>` folder.
