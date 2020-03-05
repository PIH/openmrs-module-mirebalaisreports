package org.openmrs.module.mirebalaisreports.etl;

import org.openmrs.module.mirebalaisreports.etl.config.ConfigLoader;
import org.openmrs.module.mirebalaisreports.etl.config.EtlJobConfig;
import org.openmrs.module.mirebalaisreports.etl.job.LoadSqlServerJob;
import org.openmrs.module.mirebalaisreports.etl.job.RunMultipleJob;

/**
 * Encapsulates a runnable pipeline
 */
public class EtlJobRunner {

    /**
     * Run the given job specified at the configured path
     */
    public static void run(String jobPath) {
        EtlJobConfig etlJobConfig = ConfigLoader.getEtlJobConfigFromFile(jobPath);
        run(etlJobConfig);
    }

    /**
     * Run the given job given the specified configuration
     */
    public static void run(EtlJobConfig etlJobConfig) {
        if ("run-pipeline".equals(etlJobConfig.getType())) {
            RunMultipleJob job = new RunMultipleJob();
            job.execute(etlJobConfig);
        }
        else if ("load-sqlserver".equals(etlJobConfig.getType())) {
            LoadSqlServerJob job = new LoadSqlServerJob();
            job.execute(etlJobConfig);
        }
    }
}
