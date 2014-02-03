package org.openmrs.module.mirebalaisreports.page.controller;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.mirebalaisreports.dataset.definition.AwaitingAdmissionDataSetDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

import java.util.Collections;
import java.util.List;

public class AwaitingAdmissionPageController  {
    private final Log log = LogFactory.getLog(getClass());

    public void get(PageModel model,
                    @SpringBean AllDefinitionLibraries libraries,
                    @SpringBean DataSetDefinitionService dsdService,
                    @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService) throws EvaluationException {

        EvaluationContext context = new EvaluationContext();
        List<Extension> admissionActions = appFrameworkService.getExtensionsById("patientDashboard.visitActions", "mirebalais.admit");
        Collections.sort(admissionActions);
        model.addAttribute("admissionActions", admissionActions);

        AwaitingAdmissionDataSetDefinition awaitingAdmissionDataSetDefinition = new AwaitingAdmissionDataSetDefinition();
        DataSet dataSet = dsdService.evaluate(awaitingAdmissionDataSetDefinition, context);

        model.addAttribute("awaitingAdmissionList", MirebalaisReportsUtil.simplify(dataSet));
    }


}
