package org.openmrs.module.mirebalaisreports.definitions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.pihcore.config.Config;
import org.openmrs.module.radiologyapp.RadiologyProperties;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SqlFileDataSetDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Includes helpful methods for dealing with PIH Metadata (this class exists so that someday we might consider
 * moving BaseReportManager into a shared refapp module)
 */
public abstract class BasePihReportManager extends BaseReportManager {

    public static final String SQL_DIR = "org/openmrs/module/mirebalaisreports/sql/";

    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
	protected MirebalaisReportsProperties mirebalaisReportsProperties;

    @Autowired
    protected DispositionService dispositionService;

    @Autowired
    protected EmrApiProperties emrApiProperties;

    @Autowired
    protected RadiologyProperties radiologyProperties;

    @Autowired
    protected Config config;

    public abstract String getUuid();

    public void setMirebalaisReportsProperties(MirebalaisReportsProperties mirebalaisReportsProperties) {
        this.mirebalaisReportsProperties = mirebalaisReportsProperties;
    }

    protected ReportDefinition constructSqlFileReportDefinition(String sqlFileResourceName, Map<String, Object> mappings) {
        ReportDefinition rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");
        rd.setParameters(getParameters());
        rd.setUuid(getUuid());

        SqlFileDataSetDefinition sqlDsd = new SqlFileDataSetDefinition();
        sqlDsd.setName(MessageUtil.translate(getMessageCodePrefix() + "name"));
        sqlDsd.setDescription(MessageUtil.translate(getMessageCodePrefix() + "description"));
        sqlDsd.addParameters(getParameters());
        sqlDsd.setSqlResource(SQL_DIR + sqlFileResourceName + ".sql");

        rd.addDataSetDefinition(sqlFileResourceName, sqlDsd, mappings);

        return rd;
    }

    protected ReportDefinition constructSqlReportDefinition(String sqlFileName) {
        return constructSqlReportDefinition(sqlFileName, null);
    }

    protected ReportDefinition constructSqlReportDefinition(String sqlFileName, Map<String,Object> mappings) {
        ReportDefinition rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");
        rd.setParameters(getParameters());
        rd.setUuid(getUuid());

        SqlDataSetDefinition sqlDsd = new SqlDataSetDefinition();
        sqlDsd.setName(MessageUtil.translate(getMessageCodePrefix() + "name"));
        sqlDsd.setDescription(MessageUtil.translate(getMessageCodePrefix() + "description"));

        String rawSql = MirebalaisReportsUtil.getStringFromResource(SQL_DIR + sqlFileName + ".sql");
        String preparedSql = MirebalaisReportsUtil.applyMetadataReplacements(rawSql);
        log.warn("Updating report SQL query for " + sqlFileName + ". New query:\n" + preparedSql);
        sqlDsd.setSqlQuery(preparedSql);
        sqlDsd.addParameters(getParameters());

        rd.addDataSetDefinition(sqlFileName, sqlDsd, mappings);

        return rd;
    }


}
