/**
 * Copyright 2019
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
package org.dkpro.tc.tcAnnotator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

/**
 * This is a dummy annotator that takes the predicted classification outcomes and creates POS
 * annotations from the predicted values. This annotator is not intended for productive use but can
 * be adapted accordingly.
 */
public class ConversionAnnotator
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_SUFFIX = "pointlessParameterToTestParameterPassing";
    @ConfigurationParameter(name = PARAM_SUFFIX, mandatory = false)
    private String suffix;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {

        for (TextClassificationOutcome o : JCasUtil.select(aJCas,
                TextClassificationOutcome.class)) {
            POS p = new POS(aJCas, o.getBegin(), o.getEnd());

            String val = o.getOutcome();
            if (suffix != null && !suffix.isEmpty()) {
                val += suffix;
            }
            p.setPosValue(val);
            p.addToIndexes();
        }

    }

}
