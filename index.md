---
#
# Use the widgets beneath and the content will be
# inserted automagically in the webpage. To make
# this work, you have to use › layout: frontpage
#
layout: frontpage
title: "DKPro TC"
---

DKPro TC is a UIMA-based text classification framework built on top of [DKPro Core][DKPRO_CORE] and [DKPro Lab][DKPRO_LAB]. It is intended to alleviate supervised machine learning experiments with any kind of textual data. 

DKPro TC comes with

  * Getting-started example code for standard text collections, e.g. the Reuters-21578 Text Categorization corpus, in Java and Groovy
  * many generic feature extractors, e.g. n-grams, POS-tags etc.
  * convenient parameter optimization capabilities
  * comprehensive reporting with support for many standard performance measures
  * support for single- and multi-label classification, and regression in various frameworks, e.g. [Weka][WEKA] and [CRFsuite][CRFSUITE]

If you want to use the latest (snapshot) version of DKPro TC, please mind that the project is subject to constant change. 


### How to cite?

If you use DKPro TC in research, please cite the following paper:

> Johannes Daxenberger and Oliver Ferschke and Iryna Gurevych and Torsten Zesch (2014). DKPro TC: A Java-based Framework for Supervised Learning Experiments on Textual Data. In:  Proceedings of the 52nd Annual Meeting of the Association for Computational Linguistics.  System Demonstrations. [(pdf)][ACL_2014] [(bib)][ACL_2014_BIB]

### License

While most DKPro TC modules are available under the Apache Software License (ASL) version 2, there are a few modules that depend on external libraries and are thus licensed under the GPL. The license of each individual module is specified in its LICENSE file.

It must be pointed out that while the component's source code itself is licensed under the ASL or GPL, individual components might make use of third-party libraries or products that are not licensed under the ASL or GPL. Please make sure that you are aware of the third party licenses and respect them.

### About

This project was initiated under the auspices of Prof. Dr. Iryna Gurevych, [Ubiquitous Knowledge Processing Lab (UKP)](http://www.ukp.tu-darmstadt.de/), Technische Universität Darmstadt.

[DKPRO_CORE]: https://dkpro.github.io/dkpro-core
[DKPRO_LAB]: https://dkpro.github.io/dkpro-lab
[WEKA]: http://www.cs.waikato.ac.nz/ml/weka
[CRFSUITE]: https://github.com/chokkan/crfsuite
[ACL_2014]: http://anthology.aclweb.org//P/P14/P14-5011.pdf
[ACL_2014_BIB]: http://anthology.aclweb.org/P/P14/P14-5011.bib