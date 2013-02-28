package org.openmrs.module.mirebalaisreports.definitions;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emr.EmrConstants;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.indicator.dimension.CohortIndicatorAndDimensionResult;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BasicStatisticsReportManagerTest extends BaseModuleContextSensitiveTest {

    @Before
    public void setupDatabase() throws Exception {
        executeDataSet("visitReportTestDataset.xml");
    }

    @Test
    public void shouldBuildAndRunReport() throws Exception {
        Date day = DateUtil.parseDate("2012-01-02", "yyyy-MM-dd");

        BasicStatisticsReportManager report = new BasicStatisticsReportManager();

        MapDataSet result = report.evaluate(day);

        CohortIndicatorAndDimensionResult startedVisitOnDay = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("startedVisitOnDay");
        CohortIndicatorAndDimensionResult startedVisitDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("startedVisitDayBefore");
        CohortIndicatorAndDimensionResult patientsCurrentlyInHospital = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("activeVisits");
        CohortIndicatorAndDimensionResult todayRegistrations = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("todayRegistrations");

        assertThat(startedVisitOnDay.getValue().intValue(), is(3));
        assertThat(startedVisitDayBefore.getValue().intValue(), is(2));
        assertThat(patientsCurrentlyInHospital.getValue().intValue(), is(2));
        assertThat(todayRegistrations.getValue().intValue(), is(1));
    }

}
