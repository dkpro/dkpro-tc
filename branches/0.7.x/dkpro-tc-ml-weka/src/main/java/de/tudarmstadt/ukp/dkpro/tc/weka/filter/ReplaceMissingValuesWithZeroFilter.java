/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.filter;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.SparseInstance;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

/**
 * Weka Classifiers treat missing values very differently. This class is used to force the treatment
 * of missing values as zero. This is especially useful if missing values actually encode something
 * you would normally encode as a distinct value.
 * 
 * @author Michael Kutschke
 */
public class ReplaceMissingValuesWithZeroFilter
    extends ReplaceMissingValues
{

    private static final long serialVersionUID = 3951067862412755578L;

    /**
     * Input an instance for filtering. Filter requires all training instances be read before
     * producing output.
     * 
     * @param instance
     *            the input instance
     * @return true if the filtered instance may now be collected with output().
     * @throws IllegalStateException
     *             if no input format has been set.
     */
    @Override
    public boolean input(Instance instance)
    {

        if (getInputFormat() == null) {
            throw new IllegalStateException("No input instance format defined");
        }
        if (m_NewBatch) {
            resetQueue();
            m_NewBatch = false;
        }
        convertInstance(instance);
        return true;
    }

    /**
     * Convert a single instance over. The converted instance is added to the end of the output
     * queue.
     * 
     * @param instance
     *            the instance to convert
     */
    private void convertInstance(Instance instance)
    {

        Instance inst = null;
        if (instance instanceof SparseInstance) {
            double[] vals = new double[instance.numValues()];
            int[] indices = new int[instance.numValues()];
            int num = 0;
            for (int j = 0; j < instance.numValues(); j++) {
                if (instance.isMissingSparse(j)
                        && (getInputFormat().classIndex() != instance.index(j))
                        && (instance.attributeSparse(j).isNominal() || instance.attributeSparse(j)
                                .isNumeric())) {
                }
                else {
                    vals[num] = instance.valueSparse(j);
                    indices[num] = instance.index(j);
                    num++;
                }
            }
            if (num == instance.numValues()) {
                inst = new SparseInstance(instance.weight(), vals, indices,
                        instance.numAttributes());
            }
            else {
                double[] tempVals = new double[num];
                int[] tempInd = new int[num];
                System.arraycopy(vals, 0, tempVals, 0, num);
                System.arraycopy(indices, 0, tempInd, 0, num);
                inst = new SparseInstance(instance.weight(), tempVals, tempInd,
                        instance.numAttributes());
            }
        }
        else {
            double[] vals = new double[getInputFormat().numAttributes()];
            for (int j = 0; j < instance.numAttributes(); j++) {
                if (instance.isMissing(j)
                        && (getInputFormat().classIndex() != j)
                        && (getInputFormat().attribute(j).isNominal() || getInputFormat()
                                .attribute(j).isNumeric())) {
                    vals[j] = 0.0d;
                }
                else {
                    vals[j] = instance.value(j);
                }
            }
            inst = new DenseInstance(instance.weight(), vals);
        }
        inst.setDataset(instance.dataset());
        push(inst);
    }

}
