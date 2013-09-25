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