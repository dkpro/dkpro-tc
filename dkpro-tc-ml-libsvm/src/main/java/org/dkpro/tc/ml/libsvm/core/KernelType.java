/*******************************************************************************
 * Copyright 2019
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.ml.libsvm.core;

public enum KernelType
{
    /**
     * Linear: u'*v
     */
    Linear("0"),
    
    /**
     * Polynomial: (gamma*u'*v + coef0)^degree 
     */
    Polynomial("1"),
    
    /**
     * radial basis function: exp(-gamma*|u-v|^2)
     */
    RadialBasis("2"),
    
    /**
     * sigmoid: tanh(gamma*u'*v + coef0)
     */
    Sigmoid("3");

    private String name;

    private KernelType(String n)
    {
        this.name = n;
    }

    @Override
    public String toString()
    {
        return name;
    }
    
    public static KernelType getByName(String name)
    {
        for (KernelType t : KernelType.values()) {
            if (t.toString().equals(name)) {
                return t;
            }
        }

        throw new IllegalArgumentException("Name [" + name + "] unknown");
    }
}
