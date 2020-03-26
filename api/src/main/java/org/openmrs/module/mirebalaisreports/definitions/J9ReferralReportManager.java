package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.config.Components;
import org.openmrs.module.pihcore.config.ConfigDescriptor;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.ReportDesignRenderer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class J9ReferralReportManager extends BasePihReportManager {

    @Override
    public Category getCategory() {
        return null;
    }

    @Override
    public List<ConfigDescriptor.Country> getCountries() {
        return Arrays.asList(ConfigDescriptor.Country.HAITI);
    }

    @Override
    public String getComponent() { return Components.MCH_PROGRAM; }

    @Override
    public Integer getOrder() {
        return null;
    }

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.MCH_J9_REFERRALS_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getName() {
        return "j9Referrals";
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public List<Parameter> getParameters() {

        List<Parameter> parameters = getStartAndEndDateParameters();

        Parameter patient = new Parameter("patient", null, String.class);
        patient.setRequired(false);
        parameters.add(patient);

        Parameter locale = new Parameter("locale", null, String.class);
        locale.setRequired(false);
        parameters.add(locale);

        return parameters;
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        Map<String,Object> mappings = getStartAndEndDateMappings();
        mappings.put("patient","${patient}");
        mappings.put("locale", "${locale}");
        return constructSqlFileReportDefinition(getName(), mappings);
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        ReportDesign reportDesign = csvReportDesign(reportDefinition);
        reportDesign.addPropertyValue(ReportDesignRenderer.FILENAME_BASE_PROPERTY,
                "j9Referrals." +
                        "{{ formatDate request.evaluateStartDatetime \"yyyyMMdd\" }}." +
                        "{{ formatDate request.evaluateStartDatetime \"HHmm\" }}");
        return Arrays.asList(reportDesign);
    }

}
