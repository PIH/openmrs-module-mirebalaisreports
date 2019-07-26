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
public class MexicoSisVisitsReportManager extends BasePihReportManager {

    private static final String EXCEL_TEMPLATE_NAME = "MexicoSisVisitsTemplate";

    private static final String REPEATING_SECTION = "sheet:1,row:2,dataset:mexicoSisVisits";

    @Override
    public Category getCategory() {
        return DATA_EXPORT;
    }

    @Override
    public List<ConfigDescriptor.Country> getCountries() {
        return Arrays.asList(ConfigDescriptor.Country.MEXICO);
    }

    @Override
    public Integer getOrder() {
        return REPORTING_DATA_EXPORT_REPORTS_ORDER.indexOf(getUuid());
    }

    @Override
    public String getName() {
        return "mexicoSisVisits";
    }

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.MEXICO_SIS_VISITS_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public List<Parameter> getParameters() {
        return getStartAndEndDateParameters();
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        return constructSqlReportDefinition(getName(), getStartAndEndDateMappings());
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) throws IOException {
        return Arrays.asList(xlsReportDesign(reportDefinition, EXCEL_TEMPLATE_NAME, REPEATING_SECTION));
    }

}
