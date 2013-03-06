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
        CohortIndicatorAndDimensionResult todayRegistrations = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("todayRegistrations");
        CohortIndicatorAndDimensionResult outpatientsDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientsDayBefore");
        CohortIndicatorAndDimensionResult outpatientsDayBeforeWithClinical = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientsDayBeforeWithClinical");
        CohortIndicatorAndDimensionResult outpatientsDayBeforeWithVitals = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientsDayBeforeWithVitals");
        CohortIndicatorAndDimensionResult outpatientsDayBeforeWithDiagnosis = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientsDayBeforeWithDiagnosis");

        assertThat(startedVisitOnDay.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 6, 7));
        assertThat(startedVisitDayBefore.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 7));
        assertThat(patientsCurrentlyInHospital.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 6));
        assertThat(todayRegistrations.getCohortIndicatorAndDimensionCohort(), hasCohort(7));

        Matcher<Cohort> isExpectedOutpatientEncounterCohort = hasCohort(2, 6, 7, 8);
        Matcher<Cohort> isExpectedOutpatientClinicalCohort = hasCohort(2, 6, 7);
        assertThat(outpatientsDayBefore.getCohortIndicatorAndDimensionCohort(), isExpectedOutpatientEncounterCohort);
        assertThat(outpatientsDayBeforeWithClinical.getCohortIndicatorAndDimensionCohort(), isExpectedOutpatientClinicalCohort);
        assertThat(outpatientsDayBeforeWithClinical.getCohortIndicatorAndDimensionDenominator(), isExpectedOutpatientEncounterCohort);
        assertThat(outpatientsDayBeforeWithVitals.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 7));
        assertThat(outpatientsDayBeforeWithVitals.getCohortIndicatorAndDimensionDenominator(), isExpectedOutpatientClinicalCohort);
        assertThat(outpatientsDayBeforeWithDiagnosis.getCohortIndicatorAndDimensionCohort(), hasCohort(7));
        assertThat(outpatientsDayBeforeWithDiagnosis.getCohortIndicatorAndDimensionDenominator(), isExpectedOutpatientClinicalCohort);
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
