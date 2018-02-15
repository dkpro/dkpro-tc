---
layout: page-fullwidth
title: "DKPro TC Setup"
permalink: "/SettingUpDKPro/"
---

We discuss here briefly how to setup DKPro TC. The following programs are required for this tutorial.

* Java JDK v8+
* Eclipse 
* Maven 3
* Git

## Setting up Maven
To make sure that all DKPro Snapshot dependencies are found, you have to configure Maven to look in the DKPro repositories for the `Snapshot` versions. This is done by adding entries into the `settings.xml` of Maven.

Go to your home directory and enter the folder `.m2`. This folder is by default hidden, depending the settings of your operating system, the folder might not be visible. If you have not used Maven before, this folder might not exist yet, in this case create a `.m2` folder. In this folder, a `settings.xml` might already exist. If it does not exist yet, create one and copy [this](https://dkpro.github.io/dkpro-core/pages/setup-maven/) content into the `settings.xml` and activate this out-commented line `<!--activeProfile>ukp-oss-snapshots</activeProfile-->`. The content of the linked `settings.xml` has to be merged into an existing file accordingly. 

### Import DKPro TC a cloned repository
It is most easiest to import DKPro by cloning the repository on the command line. Open a command line prompt and change to a directory where you want to store DKPro TC.
If you work via command line change directory to your workspace folder and clone DKPro TC: `git clone https://github.com/dkpro/dkpro-tc.git`. In Eclipse, choose via the menu `Import` -> `Import as Maven Project` and choose the folder to which DKPro TC was cloned in the previous step and follow the further instructions.

### Initial Import
Once the import of DKPro TC into the Eclipse workspace was started, it will take a short while until DKPro TC is ready to be used. Maven will download all necessary dependencies from the Internet on your local computer that are used by DKPro TC.  Depending on the speed of your computer and the bandwidth of your Internet connection, this might take some time. It its reasonable to expect 30 minutes for the initial import. 
