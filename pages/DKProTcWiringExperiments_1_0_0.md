---
layout: page-fullwidth
title: "DKPro TC Wiring Experiments"
permalink: "/DKProTcWiringExperiments_1_0_0/"
---

### Anatomy of a DKPro TC experiment

Using the `ExperimentBuilder` as described in [Basics](https://github.com/dkpro/dkpro-tc/blob/gh-pages/pages/DKProTcBasics_1_0_0) wires an experiment and creates all necessary information. We briefly discuss here the necessary steps without using the `ExperimentBuilder` to create an experiment: All information is provided in a DKPro Lab data type called a `Dimension`:

{% highlight java %}
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
				TcFeatureFactory.create(AvgTokenRatioPerDocument.class),
				TcFeatureFactory.create(WordNGram.class, 
							WordNGram.PARAM_NGRAM_USE_TOP_K, 50)));
/* The configuration specifies which classifier we want to use, one can specify several 
   classifiers or confirgurations of the same classifier; TC will automatically execute them all */
Map<String, Object> libsvmConfig = new HashMap<String, Object>();
libsvmConfig.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new LibsvmAdapter(), "-s", "0", "-c", "100" });
libsvmConfig.put(DIM_DATA_WRITER, new LibsvmAdapter().getDataWriterClass());
libsvmConfig.put(DIM_FEATURE_USE_SPARSE, new LibsvmAdapter().useSparseFeatures());
	
Map<String, Object> liblinearConfig = new HashMap<String, Object>();
liblinearConfig.put(DIM_CLASSIFICATION_ARGS,
                new Object[] { new LiblinearAdapter(), "-s", "1"});
liblinearConfig.put(DIM_DATA_WRITER, new LiblinearAdapter().getDataWriterClass());
liblinearConfig.put(DIM_FEATURE_USE_SPARSE, new LiblinearAdapter().useSparseFeatures());	
 
Dimension<Map<String, Object>> configs = Dimension.createBundle("config", libsvmConfig, liblinearConfig);
	
// Wire everything in a parameter space
ParameterSpace pSpace = new ParameterSpace(
	dimLearningModem,
	dimFeatureMode,
        readersDimension,
	dimFeatureSet, 
        configs
 );
/* Sets the output-folder to which all data is written that is created by DKPro TC, 
   this includes the results of the experiments. 
   This environmental variable has to be set before the experiment runs, temporarily or permantely */
System.setProperty("DKPRO_HOME", System.getProperty("user.home")+"/Desktop/");
// Pass this configuration to an experiment
ExperimentTrainTest exp = new ExperimentTrainTest("ExperimentName");
exp.setPreprocessing(createEngineDescription(OutcomeAnnotator.class);
exp.addReport(new BatchTrainTestReport());
exp.setParameterSpace(pSpace); 
// Run experiment
Lab.getInstance().run(exp);
{% endhighlight java %}

### Dimensions and the parameter space
An experiment consists of (i) several dimensions that are combined in a (ii) parameter space and provided to an experiment. 
Regarding (i), dimensions are the basic building blocks of an experimental setup. Almost every parameter that is altered in an experiment is changed or set via a dimension. The dimensions in the code declare three building blocks: First, the readers that provide the data for the experiment, second, the feature set that is used in this experiment, and third, the classification arguments that specify the classifier which is to be used (Liblinear in this case).
Regarding (ii), the parameter space is main data structure which is used by DKPro TC in the background; it is important that all created dimension are added to the parameter space, otherwise they are not used. 
