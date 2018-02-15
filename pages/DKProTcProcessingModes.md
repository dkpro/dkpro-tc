---
layout: page-fullwidth
title: "DKPro TC Deep Learning (1.0.0 Release)"
permalink: "/DKProTcProcessingModes/"
---

DKPro TC supports the following processing modes that correspond to typical classification setups in Natural Language Processing, which are shown in the table below:

|                  | Single-Label  | Multi-Label   | Regression    | 
| -------------    | :-------------: | :-------------: | :-------------: |
| Document Mode    | x             |   x           |   x           |
| Unit Mode        |    x      | x | x |
| Sequence Mode    |  x  |  |  |

**Document Mode**: Classifes whole documents, classical use-case is for instance E-mail classification into `spam` or `no spam` (single-label)

**Unit Mode**: Sub-document classification, this is a special case of document mode where the classification focuses on two or more smaller portions of the text body, for instance classyfing the age-range on several user-comments under an article where both, the article and the user comments are hold together (single-label). 

**Sequence Mode**: Classification mode for sequentially dependend information where the prediction of the preceding element is relevant for the prediction of the next one, for instance in part-of-speech tagging where the prediction sequence of word-labels carries meaning for the next word (single-label).

Classification using *Multi-Label* are for instance prediction of movie categories where a movie might belong to more than just a single category and the different categories have a certain correlation to (not) occur together. *Regression* is a special form of single-label classification where instead of a fixed label a numerica value is predicted.
