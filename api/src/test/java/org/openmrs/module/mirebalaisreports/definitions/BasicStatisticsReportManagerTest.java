package org.openmrs.module.mirebalaisreports.definitions;

import org.junit.Test;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.indicator.dimension.CohortIndicatorAndDimensionResult;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BasicStatisticsReportManagerTest extends BaseModuleContextSensitiveTest {

    @Test
    public void shouldBuildAndRunReport() throws Exception {
        Date day = DateUtil.parseDate("2005-01-01", "yyyy-MM-dd");

        BasicStatisticsReportManager report = new BasicStatisticsReportManager();
        MapDataSet result = report.evaluate(day);
        CohortIndicatorAndDimensionResult startedVisitOnDay = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("startedVisitOnDay");
        CohortIndicatorAndDimensionResult startedVisitDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("startedVisitDayBefore");
        assertThat(startedVisitOnDay.getValue().intValue(), is(2));
        assertThat(startedVisitDayBefore.getValue().intValue(), is(0));
    }

}
