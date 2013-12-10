package org.openmrs.module.mirebalaisreports.page.controller;

import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 */
public class DailyReportPageController {

    public void get(@SpringBean ReportDefinitionService reportDefinitionService,
                    @RequestParam("reportDefinition") String reportDefinitionUuid,
                    PageModel model) {

        ReportDefinition reportDefinition = reportDefinitionService.getDefinitionByUuid(reportDefinitionUuid);
        if (reportDefinition == null) {
            throw new IllegalArgumentException("No reportDefinition with the given uuid");
        }

        model.addAttribute("reportDefinition", reportDefinition);
    }

}
