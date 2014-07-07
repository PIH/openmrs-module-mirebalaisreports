package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.ReportDesignRenderer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AllPatientsWithIdsReportManager extends BaseMirebalaisReportManager{

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.ALL_PATIENTS_WITH_IDS_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getVersion() {
        return "1.3"; // standardized download filename
    }

    @Override
    protected String getMessageCodePrefix() {
        return "mirebalaisreports.allpatientswithids.";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        log.info("Constructing " + getName());

        ReportDefinition rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");
        rd.setUuid(getUuid());

        SqlDataSetDefinition dsd = new SqlDataSetDefinition();
        dsd.setName(getName());
        dsd.setDescription(getDescription());

        String sql = MirebalaisReportsUtil.getStringFromResource(SQL_DIR + "allPatients_withIds.sql");
        sql = applyMetadataReplacements(sql);
        dsd.setSqlQuery(sql);
        if (log.isTraceEnabled()) {
            log.trace("sql = " + sql);
        }

        rd.addDataSetDefinition("dsd", dsd, null);
        return rd;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        ReportDesign reportDesign = csvReportDesign(reportDefinition);
        reportDesign.addPropertyValue(ReportDesignRenderer.FILENAME_BASE_PROPERTY,
                "mirebalais.allpatientslistdataexport." +
                "{{ formatDate request.evaluateStartDatetime \"yyyyMMdd\" }}." +
                "{{ formatDate request.evaluateStartDatetime \"HHmm\" }}");
        return Arrays.asList(reportDesign);
    }

}
