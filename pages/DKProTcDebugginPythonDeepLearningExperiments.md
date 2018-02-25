---
layout: page-fullwidth
title: "Debugging Pyhon-based Deep Learning Experiments"
permalink: "/DKProTcDebugginPythonDeepLearningExperiments/"
---

The initial configuration of a DKPro TC deep learning experiment with a Keras/DyNet code-snipped might take some time. To avoid a frequent re-execution of the DKPro TC pipeline, which might take some time, a debug-help is provided that allows running the provided deep learning code snipped with the transformed data and embeddings.
At every execution of DKPro TC, in the folder named after the deep learning framework adapter, e.g. `KerasTestTask-..`, you find a `cmdDebug.txt` which contains the command-line command to execute the code snipped using the vectorized data and pruned embedding to directly execute the user-code.
This is meant as debug help only and could turn out useful if some debugging issues occur in the user-code that are only traceable when running the code.

By copy/pasting this command into your command line prompt you can run the user-code directly, without having to execute DKPro TC again.
