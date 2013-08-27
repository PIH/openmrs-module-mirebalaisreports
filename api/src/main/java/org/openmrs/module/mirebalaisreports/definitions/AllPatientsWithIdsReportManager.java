package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AllPatientsWithIdsReportManager extends  BaseMirebalaisReportManager{

    @Override
    protected String getMessageCodePrefix() {
        return "mirebalaisreports.allpatientswithids.";
    }

    @Override
    public List<RenderingMode> getRenderingModes() {
        List<RenderingMode> l = new ArrayList<RenderingMode>();
        {
            RenderingMode mode = new RenderingMode();
            mode.setLabel(translate("output.Excel"));
            mode.setRenderer(new XlsReportRenderer());
            mode.setSortWeight(50);
            mode.setArgument("");
            l.add(mode);
        }
        return l;
    }

    @Override
    public ReportDefinition constructReportDefinition(EvaluationContext context) {
        log.info("Constructing " + getName());

        ReportDefinition rd = new ReportDefinition();
        rd.setName(getName());
        rd.setDescription(getDescription());

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
}
