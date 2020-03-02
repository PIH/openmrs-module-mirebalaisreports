package org.openmrs.module.mirebalaisreports.definitions;

import static org.openmrs.module.mirebalaisreports.definitions.BaseReportManager.Category.DATA_EXPORT;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.config.Components;
import org.openmrs.module.pihcore.config.ConfigDescriptor;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.ReportDesignRenderer;
import org.springframework.stereotype.Component;

@Component
public class J9CaseRegistrationReportManager extends BasePihReportManager {

    @Override
    public Category getCategory() {
        return DATA_EXPORT;
    }

    @Override
    public List<ConfigDescriptor.Country> getCountries() {
        return Arrays.asList(ConfigDescriptor.Country.HAITI);
    }

    @Override
    public String getComponent() { return Components.MCH_PROGRAM; }

    @Override
    public Integer getOrder() {
        return REPORTING_DATA_EXPORT_REPORTS_ORDER.indexOf(getUuid()) + 1000;
    }

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.MCH_J9_CASE_REGISTRATION_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getName() {
        return "j9CaseRegistration";
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        return constructSqlFileReportDefinition(getName(), new HashMap<String, Object>());
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        ReportDesign reportDesign = csvReportDesign(reportDefinition);
        reportDesign.addPropertyValue(ReportDesignRenderer.FILENAME_BASE_PROPERTY,
                "j9CaseRegistration." +
                        "{{ formatDate request.evaluateStartDatetime \"yyyyMMdd\" }}." +
                        "{{ formatDate request.evaluateStartDatetime \"HHmm\" }}");
        return Arrays.asList(reportDesign);
    }
}
