package org.openmrs.module.mirebalaisreports.definitions;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Cohort;
import org.openmrs.module.mirebalaisreports.MirebalaisProperties;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.indicator.dimension.CohortIndicatorAndDimensionResult;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class BasicStatisticsReportManagerTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private BasicStatisticsReportManager basicStatisticsReportManager;

    @Autowired
    private MirebalaisProperties mirebalaisProperties;


    @Before
    public void setup() throws Exception {
        executeDataSet("visitReportTestDataset.xml");

        basicStatisticsReportManager.setMirebalaisProperties(mirebalaisProperties);
    }

    @Test
    public void shouldBuildAndRunReport() throws Exception {
        Date day = DateUtil.parseDate("2012-01-02", "yyyy-MM-dd");

        MapDataSet result = basicStatisticsReportManager.evaluate(day);

        CohortIndicatorAndDimensionResult startedVisitOnDay = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("startedVisitOnDay");
        CohortIndicatorAndDimensionResult startedVisitDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("startedVisitDayBefore");

        CohortIndicatorAndDimensionResult patientsCurrentlyInHospital = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("activeVisits");

        CohortIndicatorAndDimensionResult outpatientOnDay = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientOnDay");
        CohortIndicatorAndDimensionResult outpatientOnDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientOnDayBefore");

        CohortIndicatorAndDimensionResult womenOnDay = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("womenOnDay");
        CohortIndicatorAndDimensionResult womenOnDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("womenOnDayBefore");

        CohortIndicatorAndDimensionResult todayRegistrations = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("todayRegistrations");
        CohortIndicatorAndDimensionResult yesterdayRegistrations = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("yesterdayRegistrations");

        CohortIndicatorAndDimensionResult returningPatientsOnDay = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("returningPatientsOnDay");
        CohortIndicatorAndDimensionResult returningPatientsOnDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("returningPatientsOnDayBefore");

        CohortIndicatorAndDimensionResult outpatientsWithVitalsOnDay = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientWithVitalsOnDay");
        CohortIndicatorAndDimensionResult outpatientsWithVitalsOnDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientWithVitalsOnDayBefore");

        CohortIndicatorAndDimensionResult outpatientsWithDiagnosisOnDay = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientWithDiagnosisOnDay");
        CohortIndicatorAndDimensionResult outpatientsWithDiagnosisOnDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientWithDiagnosisOnDayBefore");

        CohortIndicatorAndDimensionResult womenWithVitalsOnDay = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("womenWithVitalsOnDay");
        CohortIndicatorAndDimensionResult womenWithVitalsOnDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("womenWithVitalsOnDayBefore");

        CohortIndicatorAndDimensionResult womenWithDiagnosisOnDay = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("womenWithDiagnosisOnDay");
        CohortIndicatorAndDimensionResult womenWithDiagnosisOnDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("womenWithDiagnosisOnDayBefore");

        assertThat(startedVisitOnDay.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 6, 7));
        assertThat(startedVisitDayBefore.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 7, 6));

        assertThat(patientsCurrentlyInHospital.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 6));

        assertThat(todayRegistrations.getCohortIndicatorAndDimensionCohort(), hasCohort(100));
        assertThat(yesterdayRegistrations.getCohortIndicatorAndDimensionCohort(), hasCohort(101, 9));

        assertThat(returningPatientsOnDay.getCohortIndicatorAndDimensionCohort(), hasCohort(7, 8, 9));
        assertThat(returningPatientsOnDayBefore.getCohortIndicatorAndDimensionCohort(), hasCohort(7));

        assertThat(outpatientOnDay.getCohortIndicatorAndDimensionCohort(), hasCohort(7, 9));
        assertThat(outpatientOnDayBefore.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 6, 7, 8));

        assertThat(womenOnDay.getCohortIndicatorAndDimensionCohort(), hasCohort(7, 8));
        assertThat(womenOnDayBefore.getCohortIndicatorAndDimensionCohort(), hasCohort(9));

        assertThat(outpatientsWithVitalsOnDay.getCohortIndicatorAndDimensionCohort(), hasCohort(7));
        assertThat(outpatientsWithVitalsOnDayBefore.getCohortIndicatorAndDimensionCohort(), hasCohort(2));

        assertThat(outpatientsWithDiagnosisOnDay.getCohortIndicatorAndDimensionCohort(), hasCohort(9));
        assertThat(outpatientsWithDiagnosisOnDayBefore.getCohortIndicatorAndDimensionCohort(), hasCohort(6, 7));

        assertThat(womenWithVitalsOnDay.getCohortIndicatorAndDimensionCohort(), hasCohort(7));
        assertThat(womenWithVitalsOnDayBefore.getCohortIndicatorAndDimensionCohort(), hasCohort(9));

        assertThat(womenWithDiagnosisOnDay.getCohortIndicatorAndDimensionCohort(), hasCohort(8));
        assertThat(womenWithDiagnosisOnDayBefore.getCohortIndicatorAndDimensionCohort(), hasCohort(9));
    }

    private Matcher<Cohort> hasCohort(final Integer... expectedMemberIds) {
        return new ArgumentMatcher<Cohort>() {
            @Override
            public boolean matches(Object argument) {
                Cohort actual = (Cohort) argument;
                return actual.size() == expectedMemberIds.length && containsInAnyOrder(expectedMemberIds).matches(actual.getMemberIds());
            }
        };
    }

}
