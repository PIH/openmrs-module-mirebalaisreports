package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.pihcore.config.ConfigDescriptor;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.ReportDesignRenderer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static org.openmrs.module.mirebalaisreports.definitions.BaseReportManager.Category.DATA_EXPORT;

@Component
public class UsersAndProvidersReportManager extends BasePihReportManager {

    @Override
    public Category getCategory() {
        return DATA_EXPORT;
    }

    @Override
    public List<ConfigDescriptor.Country> getCountries() {
        return Arrays.asList(ConfigDescriptor.Country.HAITI, ConfigDescriptor.Country.LIBERIA);
    }

    @Override
    public Integer getOrder() {
        return REPORTING_DATA_EXPORT_REPORTS_ORDER.indexOf(getUuid()) + 1000;
    }

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.USERS_AND_PROVIDERS_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getName() {
        return "userAndProviders";
    }

    @Override
    public String getVersion() {
        return "1.4-SNAPSHOT"; // last change: standardized download filename
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

        String sql = MirebalaisReportsUtil.getStringFromResource(SQL_DIR + "allUsersAndProviders.sql");
        sqlDsd.setSqlQuery(sql);

        rd.addDataSetDefinition("usersAndProviders", sqlDsd, null);

        return rd;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        ReportDesign reportDesign = csvReportDesign(reportDefinition);
        reportDesign.addPropertyValue(ReportDesignRenderer.FILENAME_BASE_PROPERTY,
                "usersandprovidersdateexport." +
                "{{ formatDate request.evaluateStartDatetime \"yyyyMMdd\" }}." +
                "{{ formatDate request.evaluateStartDatetime \"HHmm\" }}");
        return Arrays.asList(reportDesign);
    }

}
