package org.openmrs.module.mirebalaisreports.definitions;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.TsvReportRenderer;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertThat;
import static org.openmrs.module.emr.test.ReportingMatchers.isCohortWithExactlyIds;

/**
 *
 */
public class DailyClinicalEncountersReportManagerTest extends BaseMirebalaisReportTest {

    @Autowired
    DailyClinicalEncountersReportManager manager;

    @Before
    public void setUp() throws Exception {
        executeDataSet("org/openmrs/module/mirebalaisreports/DailyStatsReportsTestDataset.xml");
    }

    @Test
    public void testReport() throws Exception {
        EvaluationContext context = new EvaluationContext();
        context.addParameterValue("day", DateUtil.parseDate("2013-10-01", "yyyy-MM-dd"));

        ReportDefinition reportDefinition = manager.constructReportDefinition();
        ReportData data = reportDefinitionService.evaluate(reportDefinition, context);
        new TsvReportRenderer().render(data, null, System.out);

        DataSet byLocation = data.getDataSets().get("byLocation");
        for (DataSetRow row : byLocation) {
            if (row.getColumnValue("rowLabel").equals("ui.i18n.Location.name.2c93919d-7fc6-406d-a057-c0b640104790")) {
                assertThat((Cohort) row.getColumnValue("vitals"), isCohortWithExactlyIds(1001, 1002));
                assertThat((Cohort) row.getColumnValue("consults"), isCohortWithExactlyIds(1001));
            }
            else {
                assertThat((Cohort) row.getColumnValue("vitals"), isCohortWithExactlyIds());
                assertThat((Cohort) row.getColumnValue("consults"), isCohortWithExactlyIds());
            }
        }
    }

}
