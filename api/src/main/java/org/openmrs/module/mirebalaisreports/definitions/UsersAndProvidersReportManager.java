package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class UsersAndProvidersReportManager extends BaseMirebalaisReportManager {

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.USERS_AND_PROVIDERS_REPORT_DEFINITION_UUID;
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
        return Arrays.asList(xlsReportDesign(reportDefinition, null));
    }

    @Override
    protected String getMessageCodePrefix() {
        return "mirebalaisreports.manager.userAndProviders.";
    }
}
