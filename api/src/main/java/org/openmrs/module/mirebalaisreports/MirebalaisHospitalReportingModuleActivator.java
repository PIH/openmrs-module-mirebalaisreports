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
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.SerializedObject;
import org.openmrs.api.db.SerializedObjectDAO;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.mirebalaisreports.definitions.AllPatientsWithIdsReportManager;
import org.openmrs.module.mirebalaisreports.definitions.AppointmentsReportManager;
import org.openmrs.module.mirebalaisreports.definitions.FullDataExportBuilder;
import org.openmrs.module.mirebalaisreports.definitions.InpatientListReportManager;
import org.openmrs.module.mirebalaisreports.definitions.InpatientStatsDailyReportManager;
import org.openmrs.module.mirebalaisreports.definitions.InpatientStatsMonthlyReportManager;
import org.openmrs.module.mirebalaisreports.definitions.ReportManager;
import org.openmrs.module.mirebalaisreports.definitions.UsersAndProvidersReportManager;
import org.openmrs.module.mirebalaisreports.definitions.helper.DailyIndicatorByLocationReportDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;

import java.io.IOException;
import java.util.List;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class MirebalaisHospitalReportingModuleActivator extends BaseModuleActivator {
	
	protected Log log = LogFactory.getLog(getClass());

    private ReportService reportService;
    private ReportDefinitionService reportDefinitionService;
    private SerializedObjectDAO serializedObjectDAO;
    private AdministrationService administrationService;

    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }

    public void setReportDefinitionService(ReportDefinitionService reportDefinitionService) {
        this.reportDefinitionService = reportDefinitionService;
    }

    public void setSerializedObjectDAO(SerializedObjectDAO serializedObjectDAO) {
        this.serializedObjectDAO = serializedObjectDAO;
    }

    public void setAdministrationService(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    /**
	 * @see ModuleActivator#started()
	 */
	public void started() {
        reportService = Context.getService(ReportService.class);
        reportDefinitionService = Context.getService(ReportDefinitionService.class);
        serializedObjectDAO = Context.getRegisteredComponents(SerializedObjectDAO.class).get(0);
        administrationService = Context.getAdministrationService();

        setupFullDataExports();
        setupOtherReports();
        scheduleReports();

        log.info("Mirebalais Hospital Reporting Module Module started");
	}

    /**
     * Currently we require these to be white-listed, until we've gone through all ReportManagers, and ensured they are
     * ready to be included here
     */
    private void setupOtherReports() {
        setupReport(Context.getRegisteredComponents(AllPatientsWithIdsReportManager.class).get(0));
        setupReport(Context.getRegisteredComponents(InpatientStatsDailyReportManager.class).get(0));
        setupReport(Context.getRegisteredComponents(InpatientStatsMonthlyReportManager.class).get(0));
        setupReport(Context.getRegisteredComponents(InpatientListReportManager.class).get(0));
        setupReport(Context.getRegisteredComponents(UsersAndProvidersReportManager.class).get(0));
        setupReport(Context.getRegisteredComponents(AppointmentsReportManager.class).get(0));
        for (DailyIndicatorByLocationReportDefinition manager : Context.getRegisteredComponents(DailyIndicatorByLocationReportDefinition.class)) {
            setupReport(manager);
        }
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
        if (alreadyAtLatestVersion(manager)) {
            return;
        }

        ReportDefinition reportDefinition = manager.constructReportDefinition();

        log.info("Saving new definition of " + reportDefinition.getName());

        ReportDefinition existing = reportDefinitionService.getDefinitionByUuid(reportDefinition.getUuid());
        if (existing != null) {
            // we need to overwrite the existing, rather than purge-and-recreate, to avoid deleting old ReportRequests
            log.debug("overwriting existing ReportDefinition");
            reportDefinition.setId(existing.getId());
            Context.evictFromSession(existing);
        }
        else {
            // incompatible class changes for a serialized object could mean that getting the definition return null
            // and some serialization error gets logged. In that case we want to overwrite the invalid serialized definition
            SerializedObject invalidSerializedObject = serializedObjectDAO.getSerializedObjectByUuid(reportDefinition.getUuid());
            if (invalidSerializedObject != null) {
                reportDefinition.setId(invalidSerializedObject.getId());
                Context.evictFromSession(invalidSerializedObject);
            }
//            serializedObjectDAO.purgeObject(invalidSerializedObject.getId());
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

        try {
            List<ReportDesign> designs = manager.constructReportDesigns(reportDefinition);
            for (ReportDesign design : designs) {
                reportService.saveReportDesign(design);
            }
            administrationService.setGlobalProperty(globalPropertyFor(manager), manager.getVersion());
        }
        catch (IOException ex) {
            log.error("Error constructing report design for " + reportDefinition.getName(), ex);
        }
    }

    private void scheduleReports() {

        // TODO: do we need to make sure this does not run on the reporting server?
        // TODO: how do we want to copy this to a remote server? plus a cron job to clean old reports up?

        // schedule the all patients report to run at midnight and noon everyday
        ReportRequest allPatientsScheduledReportRequest = reportService.getReportRequestByUuid(MirebalaisReportsProperties.ALL_PATIENTS_SCHEDULED_REPORT_REQUEST_UUID);
        if (allPatientsScheduledReportRequest == null) {
            allPatientsScheduledReportRequest = new ReportRequest();
        }
        ReportDefinition allPatientsReportDefinition = reportDefinitionService.getDefinitionByUuid(MirebalaisReportsProperties.ALL_PATIENTS_WITH_IDS_REPORT_DEFINITION_UUID);
        allPatientsScheduledReportRequest.setUuid(MirebalaisReportsProperties.ALL_PATIENTS_SCHEDULED_REPORT_REQUEST_UUID);
        allPatientsScheduledReportRequest.setReportDefinition(Mapped.noMappings(allPatientsReportDefinition));
        allPatientsScheduledReportRequest.setRenderingMode(getCsvReportRenderer(allPatientsReportDefinition));
        allPatientsScheduledReportRequest.setSchedule("0 0 */12 * * ?");
        reportService.queueReport(allPatientsScheduledReportRequest);

        // schedule the appointments report to run at midnight and noon everyday, retrieving all appointments for the next seven days
        ReportRequest appointmentsScheduledReportRequest = reportService.getReportRequestByUuid(MirebalaisReportsProperties.APPOINTMENTS_SCHEDULED_REPORT_REQUEST_UUID);
        if (appointmentsScheduledReportRequest == null) {
            appointmentsScheduledReportRequest = new ReportRequest();
        }
        ReportDefinition appointmentsReportDefinition = reportDefinitionService.getDefinitionByUuid(MirebalaisReportsProperties.APPOINTMENTS_REPORT_DEFINITION_UUID);
        appointmentsScheduledReportRequest.setUuid(MirebalaisReportsProperties.APPOINTMENTS_SCHEDULED_REPORT_REQUEST_UUID);
        appointmentsScheduledReportRequest.setReportDefinition(Mapped.map(appointmentsReportDefinition, "startDate=${start_of_today},endDate=${start_of_today + 7d}"));
        appointmentsScheduledReportRequest.setRenderingMode(getCsvReportRenderer(appointmentsReportDefinition));
        appointmentsScheduledReportRequest.setSchedule("0 0 */12 * * ?");
        reportService.queueReport(appointmentsScheduledReportRequest);

        // schedule the appointments report to run at midnight and noon everyday, retrieving all check-ins for the past seven days
        ReportRequest checkInsDataExportScheduledReportRequest = reportService.getReportRequestByUuid(MirebalaisReportsProperties.CHECKINS_DATA_EXPORT_SCHEDULED_REPORT_REQUEST_UUID);
        if (checkInsDataExportScheduledReportRequest == null) {
            checkInsDataExportScheduledReportRequest = new ReportRequest();
        }
        ReportDefinition checkInsDataExportReportDefinition = reportDefinitionService.getDefinitionByUuid(MirebalaisReportsProperties.CHECKINS_DATA_EXPORT_REPORT_DEFINITION_UUID);
        checkInsDataExportScheduledReportRequest.setUuid(MirebalaisReportsProperties.CHECKINS_DATA_EXPORT_SCHEDULED_REPORT_REQUEST_UUID);
        checkInsDataExportScheduledReportRequest.setReportDefinition(Mapped.map(checkInsDataExportReportDefinition, "startDate=${start_of_today - 7d},endDate=${now}"));
        checkInsDataExportScheduledReportRequest.setRenderingMode(getCsvReportRenderer(checkInsDataExportReportDefinition));
        checkInsDataExportScheduledReportRequest.setSchedule("0 0 */12 * * ?");
        reportService.queueReport(checkInsDataExportScheduledReportRequest);

    }

    private boolean alreadyAtLatestVersion(ReportManager manager) {
        String newVersion = manager.getVersion();
        String existingVersion = administrationService.getGlobalProperty(globalPropertyFor(manager));
        return existingVersion != null &&
                existingVersion.equals(newVersion) &&
                !newVersion.contains("-SNAPSHOT");
    }

    private String globalPropertyFor(ReportManager manager) {
        return "mirebalaisreports." + manager.getUuid() + ".version";
    }

    private RenderingMode getCsvReportRenderer(ReportDefinition reportDefinition) {

        for (RenderingMode candidate : reportService.getRenderingModes(reportDefinition)) {
            if (candidate.getDescriptor().startsWith("org.openmrs.module.reporting.report.renderer.CsvReportRenderer")) {
                return candidate;
            }
        }
        return null;
    }

    /**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("Mirebalais Hospital Reporting Module Module stopped");
	}

}
