package org.openmrs.module.mirebalaisreports.cohort.definition.evaluator;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.module.mirebalaisreports.cohort.definition.AdmissionSoonAfterExitCohortDefinition;
import org.openmrs.module.mirebalaisreports.definitions.BaseInpatientReportTest;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertThat;
import static org.openmrs.module.emr.test.ReportingMatchers.isCohortWithExactlyIds;

/**
 *
 */
@Ignore("The underlying query uses a MySQL-specific date function")
public class AdmissionSoonAfterExitCohortDefinitionEvaluatorTest extends BaseInpatientReportTest {

    // TODO when we re-enable this test, if it still fails, note that we have refactoring the test data set, so there could be an issue there

    @Autowired
    CohortDefinitionService cohortDefinitionService;

    @Test
    public void testEvaluate() throws Exception {
        Date startDate = DateUtil.parseDate("2013-10-03 00:00:00", "yyyy-MM-dd HH:mm:ss");
        Date endDate = DateUtil.parseDate("2013-10-03 23:59:59", "yyyy-MM-dd HH:mm:ss");

        AdmissionSoonAfterExitCohortDefinition definition = new AdmissionSoonAfterExitCohortDefinition();
        definition.setOnOrAfter(startDate);
        definition.setOnOrBefore(endDate);

        EvaluatedCohort result = cohortDefinitionService.evaluate(definition, new EvaluationContext());
        assertThat(result, isCohortWithExactlyIds(1002));
    }

}
