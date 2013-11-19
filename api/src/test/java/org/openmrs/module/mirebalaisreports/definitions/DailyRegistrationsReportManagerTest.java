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
import org.openmrs.test.SkipBaseSetup;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.openmrs.module.emr.test.ReportingMatchers.isCohortWithExactlyIds;

/**
 *
 */
@SkipBaseSetup
public class DailyRegistrationsReportManagerTest extends BaseMirebalaisReportTest {

    @Autowired
    DailyRegistrationsReportManager manager;

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

        DataSet byLocation = data.getDataSets().get("byLocation");
        for (DataSetRow row : byLocation) {
            if (row.getColumnValue("rowLabel").equals("ui.i18n.Location.name.787a2422-a7a2-400e-bdbb-5c54b2691af5")) {
                assertThat((Cohort) row.getColumnValue("registrations"), isCohortWithExactlyIds(1001));
            } else {
                assertThat((Cohort) row.getColumnValue("registrations"), isCohortWithExactlyIds());
            }
        }

        DataSet overall = data.getDataSets().get("overall");
        assertThat(overall.getMetaData().getColumnCount(), is(1));
        DataSetRow overallRow = overall.iterator().next();
        assertThat((Cohort) overallRow.getColumnValue("mirebalaisreports.dailyRegistrations.overall"), isCohortWithExactlyIds(1001));
    }

}