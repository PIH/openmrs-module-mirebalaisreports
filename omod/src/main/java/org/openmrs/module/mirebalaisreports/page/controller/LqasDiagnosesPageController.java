package org.openmrs.module.mirebalaisreports.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.cookie.DateParseException;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.mirebalaisreports.definitions.LqasDiagnosesReportManager;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.template.TemplateFactory;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.FileDownload;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class LqasDiagnosesPageController {

    private final Log log = LogFactory.getLog(getClass());

    public void get(@SpringBean LqasDiagnosesReportManager reportManager,
                    PageModel pageModel) throws EvaluationException, DateParseException {
        pageModel.addAttribute("reportManager", reportManager);
    }

    public Object post(@SpringBean LqasDiagnosesReportManager reportManager,
                       @SpringBean ReportDefinitionService reportDefinitionService,
                       @RequestParam("startDate") Date startDate,
                       @RequestParam("endDate") Date endDate,
                       @RequestParam("location") Location location) throws EvaluationException, IOException {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(reportManager.getStartDateParameter().getName(), startDate);
        parameters.put(reportManager.getEndDateParameter().getName(), endDate);
        parameters.put(reportManager.getLocationParameter().getName(), location);

        EvaluationContext context = reportManager.initializeContext(parameters);
        ReportDefinition reportDefinition = reportManager.constructReportDefinition();
        RenderingMode mode = reportManager.getRenderingModes().get(0);

        log.info("Evaluating " + reportManager.getName());
        ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);

        log.info("Rendering to " + mode.getDescriptor());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mode.getRenderer().render(reportData, mode.getArgument(), out);

        ReportRequest reportRequest = new ReportRequest(Mapped.noMappings(reportDefinition), null, mode, ReportRequest.Priority.NORMAL, null);

        TemplateFactory templateFactory = Context.getRegisteredComponents(TemplateFactory.class).get(0);
        Map templateModel = new HashMap();
        templateModel.put("parameters", parameters);
        templateModel.put("evalDate", new Date());

        String template = "mirebalais.lqasdiagnosesdataexport." +
                "{{ formatDate parameters.startDate \"yyyyMMdd\" }}." +
                "{{ formatDate parameters.endDate \"yyyyMMdd\" }}." +
                "{{ parameters.location.name }}." +
                "{{ formatDate evalDate \"yyyyMMdd\" }}." +
                "{{ formatDate evalDate \"HHmm\" }}.xls";

        String filename = templateFactory.evaluateHandlebarsTemplate(template, templateModel);
        String contentType = mode.getRenderer().getRenderedContentType(reportRequest);

        log.info("Rendering complete.  Outputting " + filename + " as contentType: " + contentType);
        return new FileDownload(filename, contentType, out.toByteArray());
    }

}
