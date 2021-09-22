package org.openmrs.module.mirebalaisreports.page.controller;

import org.openmrs.module.pihcore.PihEmrConfigConstants;
import org.openmrs.ui.framework.page.PageModel;

/**
 * Quickly implemented report for Liberia that returns the number of registrations for particular age ranges,
 * and allows for drilling down into these patients to see the details
 */
public class CheckInsByAgePageController {

    public void get(PageModel model) throws Exception {
        model.addAttribute("privilegePatientDashboard", PihEmrConfigConstants.PRIVILEGE_APP_COREAPPS_PATIENT_DASHBOARD);
    }
}
