/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mirebalaisreports;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.mirebalaisreports.definitions.FullDataExportBuilder;
import org.openmrs.module.mirebalaisreports.definitions.ReportManager;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;

import java.util.List;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class MirebalaisHospitalReportingModuleActivator extends BaseModuleActivator {
	
	protected Log log = LogFactory.getLog(getClass());

    private ReportService reportService;
    private ReportDefinitionService reportDefinitionService;

    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }

    public void setReportDefinitionService(ReportDefinitionService reportDefinitionService) {
        this.reportDefinitionService = reportDefinitionService;
    }

    /**
	 * @see ModuleActivator#started()
	 */
	public void started() {
        reportService = Context.getService(ReportService.class);
        reportDefinitionService = Context.getService(ReportDefinitionService.class);

        setupFullDataExports();

        log.info("Mirebalais Hospital Reporting Module Module started");
	}

    private void setupFullDataExports() {
        FullDataExportBuilder fullDataExportBuilder = Context.getRegisteredComponents(FullDataExportBuilder.class).get(0);
        for (ReportManager manager : fullDataExportBuilder.getAllReportManagers()) {
            setupReport(manager);
        }
    }

    /**
     * This is only public for testing
     * @param manager
     */
    public void setupReport(ReportManager manager) {
        ReportDefinition reportDefinition = manager.constructReportDefinition();

        log.info("Saving new definition of " + reportDefinition.getName());

        ReportDefinition existing = reportDefinitionService.getDefinitionByUuid(reportDefinition.getUuid());
        if (existing != null) {
            // we need to overwrite the existing, rather than purge-and-recreate, to avoid deleting old ReportRequests
            log.debug("overwriting existing ReportDefinition");
            reportDefinition.setId(existing.getId());
            Context.evictFromSession(existing);
        }

        reportDefinitionService.saveDefinition(reportDefinition);

        // purging a ReportDesign doesn't trigger any extra logic, so we can just purge-and-recreate here
        List<ReportDesign> existingDesigns = reportService.getReportDesigns(reportDefinition, null, true);
        if (existingDesigns.size() > 0) {
            log.debug("Deleting " + existingDesigns.size() + " old designs for " + reportDefinition.getName());
            for (ReportDesign design : existingDesigns) {
                reportService.purgeReportDesign(design);
            }
        }

        List<ReportDesign> designs = manager.constructReportDesigns(reportDefinition);
        for (ReportDesign design : designs) {
            reportService.saveReportDesign(design);
        }

    }

    /**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("Mirebalais Hospital Reporting Module Module stopped");
	}
		
}
