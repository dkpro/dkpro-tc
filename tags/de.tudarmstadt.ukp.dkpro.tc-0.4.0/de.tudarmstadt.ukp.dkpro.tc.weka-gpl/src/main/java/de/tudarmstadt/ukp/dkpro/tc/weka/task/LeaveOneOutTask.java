package de.tudarmstadt.ukp.dkpro.tc.weka.task;

import weka.core.Instances;

@Deprecated
public class LeaveOneOutTask
    extends CrossValidationTask
{

    @Override
    protected int getFolds(Instances data)
    {
        return data.numInstances();
    }
}
