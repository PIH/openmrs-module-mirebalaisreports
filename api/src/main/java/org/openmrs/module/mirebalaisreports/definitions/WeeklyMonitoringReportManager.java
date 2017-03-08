package org.openmrs.module.mirebalaisreports.definitions;

import org.apache.commons.io.IOUtils;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
public class WeeklyMonitoringReportManager extends BaseMirebalaisReportManager {

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.WEEKLY_MONITORING_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getVersion() {
        return "1.0.10";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(getStartDateParameter());
        l.add(getEndDateParameter());
        return l;
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");
        rd.setParameters(getParameters());
        rd.setUuid(getUuid());

        SqlDataSetDefinition sqlDsd = new SqlDataSetDefinition();
        sqlDsd.setName(MessageUtil.translate(getMessageCodePrefix() + "name"));
        sqlDsd.setDescription(MessageUtil.translate(getMessageCodePrefix() + "description"));

        String sql = MirebalaisReportsUtil.getStringFromResource(SQL_DIR + "weeklyMonitoring.sql");
        sqlDsd.setSqlQuery(sql);
        sqlDsd.addParameters(getParameters());

        Map<String, Object> mappings =  new HashMap<String, Object>();
        mappings.put("startDate","${startDate}");
        mappings.put("endDate", "${endDate}");

        rd.addDataSetDefinition("weeklyMonitoring", sqlDsd, mappings);

        return rd;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) throws IOException {

        InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream("org/openmrs/module/mirebalaisreports/reportTemplates/WeeklyMonitoringReportTemplate.xls");
        byte[] excelTemplate = IOUtils.toByteArray(is);

        Properties designProperties = new Properties();
        designProperties.put("repeatingSections", "sheet:1,row:20,dataset:weeklyMonitoring");

        return Arrays.asList(xlsReportDesign(reportDefinition, excelTemplate, designProperties));


/*        design.addPropertyValue(ReportDesignRenderer.FILENAME_BASE_PROPERTY, "weeklymonitoringdataexport." +
                "{{ formatDate request.reportDefinition.parameterMappings.startDate \"yyyyMMdd\" }}." +
                "{{ formatDate request.reportDefinition.parameterMappings.endDate \"yyyyMMdd\" }}." +
                "{{ formatDate request.evaluateStartDatetime \"yyyyMMdd\" }}." +
                "{{ formatDate request.evaluateStartDatetime \"HHmm\" }}");*/


    }

    @Override
    protected String getMessageCodePrefix() {
        return "mirebalaisreports.weeklymonitoringdataexport.";
    }
}
