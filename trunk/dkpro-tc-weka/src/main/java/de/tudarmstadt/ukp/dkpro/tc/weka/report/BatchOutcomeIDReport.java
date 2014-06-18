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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka.report;

import java.util.Properties;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;


/**
 * Collects all files written by the {@link OutcomeIDReport} and writes a merged file.
 * 
 * @author zesch
 *
 */
public class BatchOutcomeIDReport
	extends BatchReportBase
{
    
    @Override
    public void execute()
        throws Exception
    {       
        StorageService store = getContext().getStorageService();
        
        Properties props = new Properties();

        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().startsWith(TestTask.class.getName())) {
                props.putAll(store.retrieveBinary(subcontext.getId(), OutcomeIDReport.ID_OUTCOME_KEY, new PropertiesAdapter()).getMap());
            }
        }

        getContext().storeBinary(OutcomeIDReport.ID_OUTCOME_KEY, new PropertiesAdapter(props));
    }
}