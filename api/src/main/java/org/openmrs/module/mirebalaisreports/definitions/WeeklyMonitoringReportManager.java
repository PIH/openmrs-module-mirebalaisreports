package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.config.ConfigDescriptor;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.openmrs.module.mirebalaisreports.definitions.BaseReportManager.Category.DATA_EXPORT;

@Component
public class WeeklyMonitoringReportManager extends BaseMirebalaisReportManager {

    private static final String EXCEL_TEMPLATE_NAME = "WeeklyMonitoringReportTemplate";

    private static final String REPEATING_SECTION = "sheet:1,row:20,dataset:weeklyMonitoring";

    @Override
    public Category getCategory() {
        return DATA_EXPORT;
    }

    @Override
    public String getName() {
        return "weeklyMonitoring";
    }

    @Override
    public List<ConfigDescriptor.Country> getCountries() {
        return Arrays.asList(ConfigDescriptor.Country.HAITI);
    }

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
        return getStartAndEndDateParameters();
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        return constructSqlReportDefinition(getName());
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) throws IOException {
        return Arrays.asList(xlsReportDesign(reportDefinition, EXCEL_TEMPLATE_NAME, REPEATING_SECTION));
    }

    @Override
    protected String getMessageCodePrefix() {
        return  "mirebalaisreports.weeklymonitoringdataexport.";
    }
}
