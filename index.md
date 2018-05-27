---
#
# Use the widgets beneath and the content will be
# inserted automagically in the webpage. To make
# this work, you have to use › layout: frontpage
#
layout: frontpage
title: "Welcome"
---

DKPro TC is a UIMA-based text classification framework built on top of [DKPro Core][DKPRO_CORE] and [DKPro Lab][DKPRO_LAB]. It is intended to facilitate supervised machine learning experiments with any kind of textual data. 

DKPro TC comes with

  * Getting-started example code for standard text collections, e.g. the Reuters-21578 Text Categorization corpus, in Java and Groovy
  * many generic feature extractors, e.g. n-grams, POS-tags etc.
  * convenient parameter optimization capabilities
  * comprehensive reporting with support for many standard performance measures
  * support for single- and multi-label classification, and regression in various frameworks, e.g. [CRFsuite][CRFSUITE], [DyNet][DYNET], [DeepLearning4j][DL4J], [LibLinear][LIBLINEAR], [LibSvm][LIBSVM], [Keras][KERAS], [SvmHmm][SVMHMM], [Weka][WEKA] and [XGBoost][XGBOOST], 

If you want to use the latest (snapshot) version of DKPro TC, please mind that the project is subject to constant change. 


### How to cite?

If you use DKPro TC in research, please cite the following paper:

> Johannes Daxenberger, Oliver Ferschke, Iryna Gurevych, and Torsten Zesch (2014). DKPro TC: A Java-based Framework for Supervised Learning Experiments on Textual Data. In:  *Proceedings of the 52nd Annual Meeting of the Association for Computational Linguistics (System Demonstrations)*, pp. 61-66, Baltimore, Maryland, USA. [(pdf)][ACL_2014] [(bib)][ACL_2014_BIB]

> Tobias Horsmann and Torsten Zesch (2018). DeepTC - An Extension of DKPro Text Classification for Fostering Reproducibility of Deep Learning Experiments. In: *Proceedings of the International Conference on Language Resources and Evaluation (LREC)*, pp. 2539-2545, Miyazaki, Japan. [(pdf)](http://www.lrec-conf.org/proceedings/lrec2018/pdf/45.pdf) [(bib)](http://www.lrec-conf.org/proceedings/lrec2018/summaries/45.html)

### License

While most DKPro TC modules are available under the Apache Software License (ASL) version 2, there are a few modules that depend on external libraries and are thus licensed under the GPL. The license of each individual module is specified in its LICENSE file.

It must be pointed out that while the component's source code itself is licensed under the ASL or GPL, individual components might make use of third-party libraries or products that are not licensed under the ASL or GPL. Please make sure that you are aware of the third party licenses and respect them.

### About

This project was initiated under the auspices of Prof. Iryna Gurevych, [Ubiquitous Knowledge Processing Lab (UKP)](https://www.ukp.tu-darmstadt.de/), Technische Universität Darmstadt. It is now jointly developed by UKP Lab (Technische Universität Darmstadt), [Language Technology Lab](http://ltl.uni-due.de/) (Universität Duisburg-Essen), and other [contributors](https://github.com/dkpro/dkpro-tc/blob/master/CONTRIBUTORS.txt).

[CRFSUITE]: https://github.com/chokkan/crfsuite
[DKPRO_CORE]: https://dkpro.github.io/dkpro-core
[DKPRO_LAB]: https://dkpro.github.io/dkpro-lab
[DYNET]: https://github.com/clab/dynet
[DL4J]: https://deeplearning4j.org
[KERAS]: https://keras.io
[LIBLINEAR]: https://www.csie.ntu.edu.tw/~cjlin/liblinear/
[LIBSVM]: https://www.csie.ntu.edu.tw/~cjlin/libsvm/
[SVMHMM]: https://www.cs.cornell.edu/people/tj/svm_light/svm_hmm.html
[WEKA]: http://www.cs.waikato.ac.nz/ml/weka
[XGBOOST]: https://github.com/dmlc/xgboost
[ACL_2014]: http://anthology.aclweb.org//P/P14/P14-5011.pdf
[ACL_2014_BIB]: http://anthology.aclweb.org/P/P14/P14-5011.bib
