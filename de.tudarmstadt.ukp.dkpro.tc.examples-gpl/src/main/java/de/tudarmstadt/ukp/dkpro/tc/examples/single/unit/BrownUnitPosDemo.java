package de.tudarmstadt.ukp.dkpro.tc.examples.single.unit;

import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.examples.single.sequence.BrownPosDemo;

/**
 * This is an example for POS tagging as unit classification.
 * Each POS is treated as a classification unit, but unlike sequence tagging the decision for each POS is taken independently.
 * This will usually give worse results, so this is only to showcase the concept.
 * 
 * We are re-using the sequence tagging setup.
 */
public class BrownUnitPosDemo
    extends BrownPosDemo
{

    public static void main(String[] args)
        throws Exception
    {
        ParameterSpace pSpace = getParameterSpace(Constants.FM_UNIT, Constants.LM_SINGLE_LABEL);

        BrownUnitPosDemo experiment = new BrownUnitPosDemo();
        experiment.runCrossValidation(pSpace);
    }
}
