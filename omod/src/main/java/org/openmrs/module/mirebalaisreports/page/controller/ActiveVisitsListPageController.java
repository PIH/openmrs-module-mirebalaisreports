package org.openmrs.module.mirebalaisreports.page.controller;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.pihcore.reporting.cohort.definition.ActiveVisitsCohortDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

import java.util.Date;

public class ActiveVisitsListPageController {

    private final Log log = LogFactory.getLog(getClass());

    public void get(PageModel model,
                    @SpringBean CoreAppsProperties coreAppsProperties,
                    @SpringBean AllDefinitionLibraries libraries,
                    @SpringBean DataSetDefinitionService dsdService) throws EvaluationException {

        EvaluationContext context = new EvaluationContext();
        Date today = new Date();
        context.addParameterValue("day", today);

        DataSet result = null ;
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        ActiveVisitsCohortDefinition activeVisitsCohortDefinition = new ActiveVisitsCohortDefinition();
        dsd.addRowFilter(activeVisitsCohortDefinition, null);
        dsd.addColumn("patientId", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.patientId"), "");
        dsd.addColumn("familyName", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.familyName"), "");
        dsd.addColumn("givenName", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.givenName"), "");
        dsd.addColumn("zlEmrId", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.preferredZlEmrId.identifier"), "");
        dsd.addColumn("firstCheckinLocation", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.checkin.location"), "");
        dsd.addColumn("checkinDateTime", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.checkin.encounterDatetime"), "");
        dsd.addColumn("lastEncounterType", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.lastEncounter.type"), "");
        dsd.addColumn("lastEncounterLocation", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.lastEncounter.location"), "");
        dsd.addColumn("lastEncounterDateTime", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.lastEncounter.encounterDatetime"), "");

        result = dsdService.evaluate(dsd, context);

        model.addAttribute("activeVisitsList", MirebalaisReportsUtil.simplify(result));
        model.addAttribute("dashboardUrl", coreAppsProperties.getDashboardUrl());
        model.put("privilegePatientDashboard", MirebalaisReportsProperties.PRIVILEGE_PATIENT_DASHBOARD);  // used to determine if we display links to patient dashboard)
    }
}
