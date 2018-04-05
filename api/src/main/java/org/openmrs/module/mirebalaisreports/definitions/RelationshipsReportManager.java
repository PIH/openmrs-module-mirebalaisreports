package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.config.ConfigDescriptor;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.ReportDesignRenderer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static org.openmrs.module.mirebalaisreports.definitions.BaseReportManager.Category.DATA_EXPORT;

@Component
public class RelationshipsReportManager extends BasePihReportManager {

    @Override
    public BaseReportManager.Category getCategory() {
        return DATA_EXPORT;
    }

    @Override
    public List<ConfigDescriptor.Country> getCountries() {
        return Arrays.asList(ConfigDescriptor.Country.HAITI);
    }

    @Override
    public Integer getOrder() {
        return REPORTING_DATA_EXPORT_REPORTS_ORDER.indexOf(getUuid()) + 1000;
    }

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.RELATIONSHIPS_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getName() {
        return "relationships";
    }

    @Override
    public String getVersion() {
        return "1.3-SNAPSHOT";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
      return constructSqlReportDefinition(getName());
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        ReportDesign reportDesign = csvReportDesign(reportDefinition);
        reportDesign.addPropertyValue(ReportDesignRenderer.FILENAME_BASE_PROPERTY,
                "relationshipsdataexport." +
                        "{{ formatDate request.evaluateStartDatetime \"yyyyMMdd\" }}." +
                        "{{ formatDate request.evaluateStartDatetime \"HHmm\" }}");
        return Arrays.asList(reportDesign);
    }

}

