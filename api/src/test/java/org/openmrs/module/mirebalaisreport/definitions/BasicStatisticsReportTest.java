package org.openmrs.module.mirebalaisreport.definitions;

import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BasicStatisticsReportTest extends BaseModuleContextSensitiveTest {

    @Test
    public void shouldBuildAndRunReport() throws Exception {
        Date today = new Date();
        Date yesterday = DateUtils.addDays(today, -1);
        BasicStatisticsReportManager report = new BasicStatisticsReportManager();
        org.openmrs.module.reporting.dataset.MapDataSet result = report.evaluate(yesterday, today);
        Object enrollments = result.getData().getColumnValue("enrollments");
        assertThat((Integer) enrollments, is(1));
    }

}
