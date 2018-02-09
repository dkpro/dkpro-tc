---
layout: page-fullwidth
title: "Demo Experiments (1.0.0 Release)"
permalink: "/DemoExperiment_1_0_0/"
---

### Anatomy of a DKPro TC experiment

Experiments in DKPro TC follow the following structure, which we briefly introduce as they are the key concepts of DKPro TC.

```java

CollectionReaderDescription readerTrain = CollectionReaderFactory.create..()
CollectionReaderDescription readerTest = CollectionReaderFactory.create..();
Map<String, Object> dimReaders = new HashMap<String, Object>();
dimReaders.put(DIM_READER_TRAIN, readerTrain);
dimReaders.put(DIM_READER_TEST, readerTest);
Dimension<Map<String, Object>> readersDimensions = Dimension.createBundle("readers", dimReaders);

Dimension<TcFeatureSet> dimFeatureSet = Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
				TcFeatureFactory.create(NrOfTokens.class),
				TcFeatureFactory.create(LuceneCharacterNGram.class, LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 50)));

Dimension<List<Object>> dimClassificationArgs = Dimension.create(Constants.DIM_CLASSIFICATION_ARGS,
				  Arrays.asList(new LiblinearAdapter()));

ParameterSpace pSpace = new ParameterSpace(
				Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL), 
        Dimension.create(DIM_FEATURE_MODE, FM_UNIT),
        readersDimensions,
				dimFeatureSet, 
        dimClassificationArgs
 );
 
ExperimentTrainTest batch = new ExperimentTrainTest("BrownPosDemoCV");
batch.setPreprocessing(createEngineDescription(OutcomeAnnotator.class);
batch.setParameterSpace(pSpace); 

```

### Dimensions and the parameter space
An experiment consists of (i) several dimensions that are combined in a (ii) parameter space and provided to an experiment. 
Regarding (i), dimensions are the basic building blocks of an experimental setup. Almost every parameter that is altered in an experiment is changed or set via a dimension. Regarding (ii), the parameter space is main data structure which is used by DKPro TC in the background; it is important that all created dimension are added to the parameter space, otherwise they are not used. 

### Experiment
DKPro TC has two experimental modi, a train-test experiment (shown in the code snippet) in which a fixed train-test data split is provided by the user or cross-validation in which DKPro TC splits the data autonomously into the number of requested folds.
