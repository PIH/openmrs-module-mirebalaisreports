package org.openmrs.module.mirebalaisreports.etl.job;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mirebalaisreports.etl.EtlJobRunner;
import org.openmrs.module.mirebalaisreports.etl.config.EtlJobConfig;

/**
 * Encapsulates a particular ETL job configuration
 */
public class RunMultipleJob implements EtlJob {

    private static Log log = LogFactory.getLog(RunMultipleJob.class);
    private static boolean refreshInProgress = false;

    //***** CONSTRUCTORS *****

    public RunMultipleJob() {}

    /**
     * @see EtlJob
     */
    @Override
    public void execute(EtlJobConfig config) {
        if (!refreshInProgress) {
            refreshInProgress = true;
            try {
                // Pull options off of configuration
                List<String> jobs = config.getStringList("jobs");
                boolean parallelExecution = config.getBoolean("parallelExecution");

                for (String job : jobs) {
                    // TODO: Handle the ability to utilize the parallel execution and run jobs in parallel to each other
                    EtlJobRunner.run(job);
                }
            }
            finally {
                refreshInProgress = false;
            }
        }
    }
}
