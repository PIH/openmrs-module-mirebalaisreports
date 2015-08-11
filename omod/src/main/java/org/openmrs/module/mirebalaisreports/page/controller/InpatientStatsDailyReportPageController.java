package org.openmrs.module.mirebalaisreports.page.controller;

import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

public class InpatientStatsDailyReportPageController {

    public void controller(PageModel model,
                           @SpringBean CoreAppsProperties coreAppsProperties) {
        model.put("dashboardUrlWithoutQueryParams", coreAppsProperties.getDashboardUrlWithoutQueryParams());
        model.put("privilegePatientDashboard", MirebalaisReportsProperties.PRIVILEGE_PATIENT_DASHBOARD);  // used to determine if we display links to patient dashboard)
    }

}
