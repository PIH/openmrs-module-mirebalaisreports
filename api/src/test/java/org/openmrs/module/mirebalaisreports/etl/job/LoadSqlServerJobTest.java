package org.openmrs.module.mirebalaisreports.etl.job;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.mirebalaisreports.etl.config.ConfigLoader;
import org.openmrs.module.mirebalaisreports.etl.config.EtlJobConfig;

/**
 *
 */
public class LoadSqlServerJobTest {

    @Before
    public void setConfigurationDirectory() {
        String path = "src/test/resources/org/openmrs/module/mirebalaisreports/etl";
        File configDir = new File(path);
        ConfigLoader.setConfigDirectory(configDir);
    }

    @Test
    public void testLoadingEncounterTypes() {
        EtlJobConfig etlJobConfig = ConfigLoader.getEtlJobConfigFromFile("jobs/encountertypetest/job.yml");
        LoadSqlServerJob job = new LoadSqlServerJob();
        job.execute(etlJobConfig);
    }

    @Test
    public void testLoadingVaccinationsAnc() {
        EtlJobConfig etlJobConfig = ConfigLoader.getEtlJobConfigFromFile("jobs/vaccinations_anc/job.yml");
        LoadSqlServerJob job = new LoadSqlServerJob();
        job.execute(etlJobConfig);
    }
}
