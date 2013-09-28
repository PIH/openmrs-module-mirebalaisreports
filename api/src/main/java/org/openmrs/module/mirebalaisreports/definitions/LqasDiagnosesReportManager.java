package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This shows a random list of 25 visits with diagnoses, data clerk, dossier, clinician. LQAS report is used for
 * concordance of diagnoses in the dossier with the paper record available in the HUM system.
 */
@Component
public class LqasDiagnosesReportManager extends BaseMirebalaisReportManager {

    public static final String TEMPLATE_DIR = "org/openmrs/module/mirebalaisreports/reportTemplates/";

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.LQAS_DIAGNOSES_REPORT_DEFINITION_UUID;
    }

    @Override
    protected String getMessageCodePrefix() {
        return "mirebalaisreports.lqasdiagnoses.";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(getStartDateParameter());
        l.add(getEndDateParameter());
        l.add(getLocationParameter());
        return l;
    }

    @Override
    public List<RenderingMode> getRenderingModes() {
        List<RenderingMode> l = new ArrayList<RenderingMode>();
        {
            RenderingMode mode = new RenderingMode();
            mode.setLabel(translate("output.Excel"));
            mode.setRenderer(new XlsReportRenderer());
            mode.setSortWeight(50);
            mode.setArgument("");
            l.add(mode);
        }
        return l;
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        log.info("Constructing " + getName());

        ReportDefinition rd = new ReportDefinition();
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setUuid(getUuid());
        rd.setParameters(getParameters());

        SqlDataSetDefinition dsd = new SqlDataSetDefinition();
        dsd.setName(getName());
        dsd.setDescription(getDescription());
        dsd.addParameter(getStartDateParameter());
        dsd.addParameter(getEndDateParameter());
        dsd.addParameter(getLocationParameter());

        String sql = MirebalaisReportsUtil.getStringFromResource(SQL_DIR + "lqas_diagnoses.sql");
        sql = applyMetadataReplacements(sql);
        dsd.setSqlQuery(sql);
        if (log.isTraceEnabled()) {
            log.trace("sql = " + sql);
        }

        Map<String, Object> mappings =  new HashMap<String, Object>();
        mappings.put("startDate","${startDate}");
        mappings.put("endDate", "${endDate}");
        mappings.put("location", "${location}");

        rd.addDataSetDefinition("dsd", dsd, mappings);
        return rd;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        return Arrays.asList(xlsReportDesign(reportDefinition));
    }

}
