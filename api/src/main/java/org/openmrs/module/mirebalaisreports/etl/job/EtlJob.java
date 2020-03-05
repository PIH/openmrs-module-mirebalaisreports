package org.openmrs.module.mirebalaisreports.etl.job;

import org.openmrs.module.mirebalaisreports.etl.config.EtlJobConfig;

/**
 * Interface for a particular EtlJob
 */
public interface EtlJob {

    void execute(EtlJobConfig config) throws Exception;
}
