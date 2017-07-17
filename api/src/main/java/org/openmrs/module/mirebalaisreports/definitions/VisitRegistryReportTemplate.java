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

import static org.openmrs.module.mirebalaisreports.definitions.BaseReportManager.Category.MONITORING;

@Component
public class VisitRegistryReportTemplate extends BasePihReportManager {

    private static final String EXCEL_TEMPLATE_NAME = "VisitRegistryTemplate";

    private static final String REPEATING_SECTION = "sheet:1,row:9,dataset:visitRegistry";

    @Override
    public BaseReportManager.Category getCategory() {
        return MONITORING;
    }

    @Override
    public List<ConfigDescriptor.Country> getCountries() {
        return Arrays.asList(ConfigDescriptor.Country.HAITI);
    }

    @Override
    public Integer getOrder() {
        return REPORTING_MONITORING_REPORTS_ORDER.indexOf(getUuid());
    }

    @Override
    public String getName() {
        return "visitRegistry";
    }

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.VISIT_REGISTRY_REPORTING_DEFINITION_UUID;
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
