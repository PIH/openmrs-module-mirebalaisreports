package org.openmrs.module.mirebalaisreports.definitions;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.deploy.bundle.core.program.HIVProgramBundle;
import org.openmrs.module.pihcore.deploy.bundle.core.program.ZikaProgramBundle;
import org.openmrs.module.pihcore.metadata.Metadata;
import org.openmrs.module.pihcore.metadata.core.Locations;
import org.openmrs.module.pihcore.metadata.core.program.HIVProgram;
import org.openmrs.module.pihcore.metadata.core.program.ZikaProgram;
import org.openmrs.module.pihcore.metadata.haiti.PihHaitiPatientIdentifierTypes;
import org.openmrs.module.pihcore.reporting.BaseReportTest;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.openmrs.module.reporting.common.ReportingMatchers.isCohortWithExactlyMembers;

@SkipBaseSetup
public class HIVProgramSummaryReportManagerTest extends BaseReportTest {

    @Autowired
    private ProgramWorkflowService programWorkflowService;

    @Autowired
    private MirebalaisReportsProperties mirebalaisReportsProperties;

    @Autowired
    private HIVProgramSummaryReportManager manager;

    @Autowired
    private HIVProgramBundle hivProgramBundle;

    @Autowired
    private ZikaProgramBundle zikaProgramBundle;

    @Autowired
    TestDataManager testData;

    private Patient p1, p2, p3;


    @Before
    public void before() throws Exception {
        executeDataSet("org/openmrs/module/mirebalaisreports/hivSummaryReportTestData.xml");
        deployService.installBundle(zikaProgramBundle);
        deployService.installBundle(hivProgramBundle);
    }

    @Test
    public void testReport() throws Exception {

        PatientIdentifierType zlemrId = Metadata.lookup(PihHaitiPatientIdentifierTypes.ZL_EMR_ID);
        Location unknown = Metadata.lookup(Locations.UNKNOWN);

        Program hivProgram = programWorkflowService.getProgramByUuid(HIVProgram.HIV.uuid());
        Program zikaProgram = programWorkflowService.getProgramByUuid(ZikaProgram.ZIKA.uuid());

        // create three test patients (stolen from Daily Clinical Encounters test)
        p1 = testData.patient().name("Mary", "Rodriguez").gender("F").birthdate("1946-05-26", false).dateCreated("2013-10-01").identifier(zlemrId, "Y2ARM5", unknown).save();
        p2 = testData.patient().name("Alice", "Smith").gender("F").birthdate("1975-01-02", false).dateCreated("2013-10-01").identifier(zlemrId, "Y2ATDN", unknown).save();
        p3 = testData.patient().name("Gamma", "Helm").gender("M").birthdate("1985-01-01", false).dateCreated("2013-01-01").identifier(zlemrId, "Y2AVWK", unknown).save();

        // first patient we enroll in HIV program during the date range--twice, but they should only be reported once
        testData.patientProgram().patient(p1).program(hivProgram).dateEnrolled(DateUtil.parseDate("2017-09-05", "yyyy-MM-dd")).save();
        testData.patientProgram().patient(p1).program(hivProgram).dateEnrolled(DateUtil.parseDate("2017-09-10", "yyyy-MM-dd")).save();

        // second patient we enroll in HIV program, but outside of the date range
        testData.patientProgram().patient(p2).program(hivProgram).dateEnrolled(DateUtil.parseDate("2017-06-05", "yyyy-MM-dd")).save();

        // third patient in range, but in Zika program
        testData.patientProgram().patient(p3).program(zikaProgram).dateEnrolled(DateUtil.parseDate("2017-09-05", "yyyy-MM-dd")).save();

        // first patient tests negative for HIV, second patient tests positive, 3rd patient tests positive but out of query range
        testData.obs().person(p1).concept(mirebalaisReportsProperties.getHivTestResultConcept()).value(mirebalaisReportsProperties.getNegativeConcept())
                .obsDatetime(DateUtil.parseDate("2017-09-05", "yyyy-MM-dd")).save();
        testData.obs().person(p2).concept(mirebalaisReportsProperties.getHivTestResultConcept()).value(mirebalaisReportsProperties.getPositiveConcept())
                .obsDatetime(DateUtil.parseDate("2017-09-05", "yyyy-MM-dd")).save();
        testData.obs().person(p3).concept(mirebalaisReportsProperties.getHivTestResultConcept()).value(mirebalaisReportsProperties.getPositiveConcept())
                .obsDatetime(DateUtil.parseDate("2017-08-05", "yyyy-MM-dd")).save();

        EvaluationContext context = new EvaluationContext();
        context.addParameterValue("startDate", DateUtil.parseDate("2017-09-01", "yyyy-MM-dd"));
        context.addParameterValue("endDate", DateUtil.parseDate("2017-10-01", "yyyy-MM-dd"));

        ReportDefinition reportDefinition = manager.constructReportDefinition();
        ReportData data = reportDefinitionService.evaluate(reportDefinition, context);
        DataSet dataSet = data.getDataSets().get("cohorts");

        Iterator<DataSetRow> i = dataSet.iterator();
        DataSetRow row = i.next();
        assertFalse(i.hasNext());
        assertThat((Cohort) row.getColumnValue("newEnrollmentsInHIV"), isCohortWithExactlyMembers(p1));
        assertThat((Cohort) row.getColumnValue("testedForHIV"), isCohortWithExactlyMembers(p1,p2));
        assertThat((Cohort) row.getColumnValue("testedPositiveForHIV"), isCohortWithExactlyMembers(p2));

    }

}
