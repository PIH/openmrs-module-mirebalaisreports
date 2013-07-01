package org.openmrs.module.mirebalaisreports.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.cookie.DateParseException;
import org.openmrs.module.mirebalaisreports.definitions.BasicStatisticsReportManager;
import org.openmrs.module.mirebalaisreports.definitions.FullDataExportReportManager;
import org.openmrs.module.mirebalaisreports.definitions.NonCodedDiagnosesReportManager;
import org.openmrs.module.mirebalaisreports.definitions.ReportManager;
import org.openmrs.module.mirebalaisreports.definitions.WeeklyDiagnosisSurveillanceReportManager;
import org.openmrs.module.reporting.common.ListMap;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.FileDownload;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePageController {

	private final Log log = LogFactory.getLog(getClass());

    public void get(@SpringBean BasicStatisticsReportManager basicStatisticsReportManager,
					@SpringBean NonCodedDiagnosesReportManager nonCodedDiagnosesReportManager,
					@SpringBean WeeklyDiagnosisSurveillanceReportManager weeklyDiagnosisSurveillanceReportManager,
					@SpringBean FullDataExportReportManager fullDataExportReportManager,
                    PageModel pageModel) {

		// TODO: Move this all into the reports or some external configuration

		pageModel.addAttribute("basicStatisticsReport", basicStatisticsReportManager);
		pageModel.addAttribute("nonCodedDiagnosesReport", nonCodedDiagnosesReportManager);
		pageModel.addAttribute("weeklyDiagnosisSurveillanceReport", weeklyDiagnosisSurveillanceReportManager);
		pageModel.addAttribute("fullDataExportReport", fullDataExportReportManager);
    }
}
