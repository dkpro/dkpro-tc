---
layout: page-fullwidth
title: "Demo Experiments (1.0.0 Release)"
permalink: "/DemoExperiment_1_0_0/"
---

### Anatomy of a DKPro TC experiment

Subsequently, we introduce the key concepts necessary for using DKPro TC by discussing a minimal setup and walk the reader to the building blocks of an experiment.

```java

// Defining the readers that read the data that we use in an experiment
CollectionReaderDescription readerTrain = CollectionReaderFactory.create..()
CollectionReaderDescription readerTest = CollectionReaderFactory.create..();
Map<String, Object> dimReaders = new HashMap<String, Object>();
dimReaders.put(DIM_READER_TRAIN, readerTrain);
dimReaders.put(DIM_READER_TEST, readerTest);
Dimension<Map<String, Object>> readersDimension = Dimension.createBundle("readers", dimReaders);

/* Defining the features that we extract for training a classifier model, 
   we use two features, the number of tokens and the 50 most frequent word ngrams */ 
 
/* Classification of documents (alternative would be sinlge words, 
either independelty or as sequence modelling task) */
Dimension<String> dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT); 
  
/* Classification is done by predicting a single label for each document 
 (alternative would be regression, i.e. no label, or multi-label) */
Dimension<String> dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);  
  
/* The feature set we use, we use two dummy features here: 
   Number of tokens per document and the 50 most frequent
   word ngrams over all documents */
Dimension<TcFeatureSet> dimFeatureSet = Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
				TcFeatureFactory.create(NrOfTokens.class),
				TcFeatureFactory.create(LuceneNGram.class, 
							LuceneNGram.PARAM_NGRAM_USE_TOP_K, 50)));

/* The classification arguments specify which classifier we want to use, one can specify several 
   classifiers or confirgurations of the same classifier; TC will automatically execute them all */
Dimension<List<Object>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
				  Arrays.asList(new LiblinearAdapter()));

// Wire everything in a parameter space
ParameterSpace pSpace = new ParameterSpace(
	dimLearningModem,
	dimFeatureMode,
        readersDimension,
	dimFeatureSet, 
        dimClassificationArgs
 );

// Pass this configuration to an experiment
ExperimentTrainTest exp = new ExperimentTrainTest("BrownPosDemoCV");
exp.setPreprocessing(createEngineDescription(OutcomeAnnotator.class);
exp.setParameterSpace(pSpace); 

// Run experiment
Lab.getInstance().run(exp);
```

### Dimensions and the parameter space
An experiment consists of (i) several dimensions that are combined in a (ii) parameter space and provided to an experiment. 
Regarding (i), dimensions are the basic building blocks of an experimental setup. Almost every parameter that is altered in an experiment is changed or set via a dimension. The dimensions in the code declare three building blocks: First, the readers that provide the data for the experiment, second, the feature set that is used in this experiment, and third, the classification arguments that specify the classifier which is to be used (Liblinear in this case).
Regarding (ii), the parameter space is main data structure which is used by DKPro TC in the background; it is important that all created dimension are added to the parameter space, otherwise they are not used. 

### Experiment
DKPro TC has two experimental modi, a train-test experiment (shown in the code snippet) in which a fixed train-test data split is provided by the user or cross-validation in which DKPro TC splits the data autonomously into the number of requested folds.
