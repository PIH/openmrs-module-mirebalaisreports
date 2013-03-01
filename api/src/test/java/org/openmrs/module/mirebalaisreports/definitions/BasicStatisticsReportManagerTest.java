package org.openmrs.module.mirebalaisreports.definitions;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.emr.EmrProperties;
import org.openmrs.module.emr.consult.DiagnosisMetadata;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.indicator.dimension.CohortIndicatorAndDimensionResult;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Date;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicStatisticsReportManagerTest extends BaseModuleContextSensitiveTest {

    @Before
    public void setupDatabase() throws Exception {
        executeDataSet("visitReportTestDataset.xml");
    }

    @Test
    public void shouldBuildAndRunReport() throws Exception {
        Date day = DateUtil.parseDate("2012-01-02", "yyyy-MM-dd");

        DiagnosisMetadata dmd = new DiagnosisMetadata();
        dmd.setDiagnosisSetConcept(Context.getConceptService().getConcept(23));

        EmrProperties emrProperties = mock(EmrProperties.class);
        when(emrProperties.getDiagnosisMetadata()).thenReturn(dmd);

        BasicStatisticsReportManager report = new BasicStatisticsReportManager(emrProperties);

        MapDataSet result = report.evaluate(day);

        CohortIndicatorAndDimensionResult startedVisitOnDay = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("startedVisitOnDay");
        CohortIndicatorAndDimensionResult startedVisitDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("startedVisitDayBefore");
        CohortIndicatorAndDimensionResult patientsCurrentlyInHospital = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("activeVisits");
        CohortIndicatorAndDimensionResult todayRegistrations = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("todayRegistrations");
        CohortIndicatorAndDimensionResult outpatientsDayBefore = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientsDayBefore");
        CohortIndicatorAndDimensionResult outpatientsDayBeforeWithVitals = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientsDayBeforeWithVitals");
        CohortIndicatorAndDimensionResult outpatientsDayBeforeWithDiagnosis = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientsDayBeforeWithDiagnosis");
        CohortIndicatorAndDimensionResult outpatientsDayBeforeWithVitalsAndDiagnosis = (CohortIndicatorAndDimensionResult) result.getData().getColumnValue("outpatientsDayBeforeWithVitalsAndDiagnosis");

        assertThat(startedVisitOnDay.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 6, 7));
        assertThat(startedVisitDayBefore.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 7));
        assertThat(patientsCurrentlyInHospital.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 6));
        assertThat(todayRegistrations.getCohortIndicatorAndDimensionCohort(), hasCohort(7));

        Matcher<Cohort> isExpectedDenominatorCohort = hasCohort(2, 6, 7);
        assertThat(outpatientsDayBefore.getCohortIndicatorAndDimensionCohort(), isExpectedDenominatorCohort);
        assertThat(outpatientsDayBeforeWithVitals.getCohortIndicatorAndDimensionCohort(), hasCohort(2, 7));
        assertThat(outpatientsDayBeforeWithVitals.getCohortIndicatorAndDimensionDenominator(), isExpectedDenominatorCohort);
        assertThat(outpatientsDayBeforeWithDiagnosis.getCohortIndicatorAndDimensionCohort(), hasCohort(7));
        assertThat(outpatientsDayBeforeWithDiagnosis.getCohortIndicatorAndDimensionDenominator(), isExpectedDenominatorCohort);
        assertThat(outpatientsDayBeforeWithVitalsAndDiagnosis.getCohortIndicatorAndDimensionCohort(), hasCohort(7));
        assertThat(outpatientsDayBeforeWithVitalsAndDiagnosis.getCohortIndicatorAndDimensionDenominator(), isExpectedDenominatorCohort);
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
