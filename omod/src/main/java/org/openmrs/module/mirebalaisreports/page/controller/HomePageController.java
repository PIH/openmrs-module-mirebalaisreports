package org.openmrs.module.mirebalaisreports.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.definitions.AllPatientsWithIdsReportManager;
import org.openmrs.module.mirebalaisreports.definitions.BasicStatisticsReportManager;
import org.openmrs.module.mirebalaisreports.definitions.LqasDiagnosesReportManager;
import org.openmrs.module.mirebalaisreports.definitions.NonCodedDiagnosesReportManager;
import org.openmrs.module.mirebalaisreports.definitions.WeeklyDiagnosisSurveillanceReportManager;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

public class HomePageController {

	private final Log log = LogFactory.getLog(getClass());

    public void get(@SpringBean BasicStatisticsReportManager basicStatisticsReportManager,
					@SpringBean NonCodedDiagnosesReportManager nonCodedDiagnosesReportManager,
					@SpringBean WeeklyDiagnosisSurveillanceReportManager weeklyDiagnosisSurveillanceReportManager,
                    @SpringBean LqasDiagnosesReportManager lqasDiagnosesReportManager,
                    @SpringBean AllPatientsWithIdsReportManager allPatientsWithIdsReportManager,
                    @SpringBean MirebalaisReportsProperties mirebalaisReportsProperties,
                    PageModel pageModel) {

		// TODO: Move this all into the reports or some external configuration

		pageModel.addAttribute("basicStatisticsReport", basicStatisticsReportManager);
		pageModel.addAttribute("nonCodedDiagnosesReport", nonCodedDiagnosesReportManager);
		pageModel.addAttribute("weeklyDiagnosisSurveillanceReport", weeklyDiagnosisSurveillanceReportManager);
        pageModel.addAttribute("lqasDiagnosesReport", lqasDiagnosesReportManager);
        pageModel.addAttribute("allPatientsWithIdsReportManager", allPatientsWithIdsReportManager);
        pageModel.addAttribute("properties", mirebalaisReportsProperties);
    }
}
