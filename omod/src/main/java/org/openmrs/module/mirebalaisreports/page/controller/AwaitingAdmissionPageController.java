package org.openmrs.module.mirebalaisreports.page.controller;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.mirebalaisreports.cohort.definition.AwaitingAdmissionCohortDefinition;
import org.openmrs.module.mirebalaisreports.cohort.definition.InpatientLocationCohortDefinition;
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

public class AwaitingAdmissionPageController  {
    private final Log log = LogFactory.getLog(getClass());

    public void get(PageModel model,
                    @SpringBean AllDefinitionLibraries libraries,
                    @SpringBean DataSetDefinitionService dsdService) throws EvaluationException {
        EvaluationContext context = new EvaluationContext();

        DataSet result = null ;
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        AwaitingAdmissionCohortDefinition cd = new AwaitingAdmissionCohortDefinition();

        dsd.addRowFilter(cd ,null);
        dsd.addColumn("patientId", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.patientId"), "");
        dsd.addColumn("familyName", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.familyName"), "");
        dsd.addColumn("givenName", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.givenName"), "");
        dsd.addColumn("zlEmrId", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.mostRecentZlEmrId.identifier"), "");
        dsd.addColumn("dossierNumber", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.mostRecentDossierNumber.identifier"), "");
        dsd.addColumn("firstAdmittedLocation", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.admission.location"), "");
        dsd.addColumn("admissionDateTime", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.admission.encounterDatetime"), "");
        dsd.addColumn("inpatientLocation", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.inpatient.location"), "");
        dsd.addColumn("inpatientDateTime", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.inpatient.encounterDatetime"), "");

        result = dsdService.evaluate(dsd, context);

        model.addAttribute("awaitingAdmissionList", MirebalaisReportsUtil.simplify(result));
    }


}
