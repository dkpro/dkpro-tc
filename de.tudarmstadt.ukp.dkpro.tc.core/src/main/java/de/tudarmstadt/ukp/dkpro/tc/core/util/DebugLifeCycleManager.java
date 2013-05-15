package de.tudarmstadt.ukp.dkpro.tc.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.lab.engine.LifeCycleException;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.engine.impl.DefaultLifeCycleManager;
import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.task.Task;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;

public class DebugLifeCycleManager
    extends DefaultLifeCycleManager
{

    @Override
    public void complete(TaskContext aContext, Task aConfiguration)
        throws LifeCycleException
    {
        aContext.getMetadata().setEnd(System.currentTimeMillis());
        aContext.message("Completing task ["+aConfiguration.getType()+"]");
        aContext.message("Running reports for task ["+aConfiguration.getType()+"]");
        List<Class<? extends Report>> reports = new ArrayList<Class<? extends Report>>(
                aConfiguration.getReports());
        Collections.sort(reports, new Comparator<Class<?>>()
        {
            @Override
            public int compare(Class<?> aO1, Class<?> aO2)
            {
                return aO1.getName().compareTo(aO2.getName());
            }
        });
        int i = 1;
        for (Class<? extends Report> reportClass : reports) {
            for (int g = 0; g < 3; g++) {
                System.gc();
            }
            try {
                aContext.message("Starting report [" + reportClass.getName() + "] (" + i + "/"
                        + reports.size() + ")");
                Report report = reportClass.newInstance();
                report.setContext(aContext);
                report.execute();
                aContext.message("Report complete [" + reportClass.getName() + "] (" + i + "/"
                        + reports.size() + ")");
            }
            catch (Exception e) {
//                aContext.message("Report failed [" + reportClass.getName() + "] (" + i + "/"
//                        + reports.size() + "): " + e.getMessage());
                throw new LifeCycleException(e);
            }
            finally {
                i++;
            }
        }
        aContext.storeBinary(TaskContextMetadata.METADATA_KEY, aContext.getMetadata());
        aContext.message("Completed task ["+aConfiguration.getType()+"]");
    }
}