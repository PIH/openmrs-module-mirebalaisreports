package org.openmrs.module.mirebalaisreports.page.controller;


import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.mirebalaisreports.cohort.definition.AwaitingAdmissionCohortDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

public class AwaitingAdmissionPageController  {
    private final Log log = LogFactory.getLog(getClass());

    public void get(PageModel model,
                    @SpringBean AllDefinitionLibraries libraries,
                    @SpringBean DataSetDefinitionService dsdService,
                    @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService) throws EvaluationException {

        EvaluationContext context = new EvaluationContext();
        List<Extension> admissionActions = appFrameworkService.getAllEnabledExtensions("mirebalaisreports.awaitingAdmissionActions");
        Collections.sort(admissionActions);
        model.addAttribute("admissionActions", admissionActions);

        DataSet dataSet = null;

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        AwaitingAdmissionCohortDefinition cohortDefinition = new AwaitingAdmissionCohortDefinition();
        dsd.addRowFilter(cohortDefinition, null);

        dsd.addColumn("patientId", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.patientId"), "");
        dsd.addColumn("patientLastName", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.familyName"), "");
        dsd.addColumn("patientFirstName", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.givenName"), "");
        dsd.addColumn("primaryIdentifier", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.mostRecentZlEmrId.identifier"), "");
        dsd.addColumn("dossierNumber", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.mostRecentDossierNumber.identifier"), "");

        dsd.addColumn("visitId", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.visit.id"), "" );
        dsd.addColumn("requestedAdmissionFromLocation", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.requestedAdmissionFromLocation"), "" );
        dsd.addColumn("requestedAdmissionDateTime", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.requestedAdmissionDateTime"), "" );
        dsd.addColumn("requestedAdmissionProvider", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.requestedAdmissionProvider"), "" );
        dsd.addColumn("requestedAdmissionDiagnosis", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.requestedAdmissionDiagnosis"), "" );

        dsd.addColumn("requestedAdmissionToLocation", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.requestedAdmissionToLocation"), "" );
        dataSet = dsdService.evaluate(dsd, context);

        model.addAttribute("awaitingAdmissionList", MirebalaisReportsUtil.simplify(dataSet));
    }


}
