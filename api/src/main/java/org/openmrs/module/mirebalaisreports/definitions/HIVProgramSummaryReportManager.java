package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.metadata.core.program.HIVProgram;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// TODO not currently used and may be abandoned

@Component
public class HIVProgramSummaryReportManager extends BasePihReportManager {

    @Autowired
    private ProgramWorkflowService programWorkflowService;

    @Autowired
    private MirebalaisReportsProperties mirebalaisReportsProperties;

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.HIV_SUMMARY_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getName() {
        return "hivSummaryReport";
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public List<Parameter> getParameters() {
        return getStartAndEndDateParameters();
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        log.info("Constructing " + getName());

        ReportDefinition rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");
        rd.setUuid(getUuid());
        rd.setParameters(getParameters());
        rd.addDataSetDefinition("cohorts", constructDataSetDefinition(), getStartAndEndDateMappings());

        return rd;
    }

    public DataSetDefinition constructDataSetDefinition() {
        CohortCrossTabDataSetDefinition cohortDsd = new CohortCrossTabDataSetDefinition();
        cohortDsd.setParameters(getStartAndEndDateParameters());

        // Patients tested for HIV
        CodedObsCohortDefinition testedForHIV = new CodedObsCohortDefinition();
        testedForHIV.setQuestion(mirebalaisReportsProperties.getHivTestResultConcept());
        testedForHIV.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        testedForHIV.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
        cohortDsd.addColumn("testedForHIV", map(testedForHIV, "onOrAfter=${startDate},onOrBefore=${endDate}"));

        // Patients tested positive for HIV
        CodedObsCohortDefinition testedPositiveForHIV = new CodedObsCohortDefinition();
        testedPositiveForHIV.setQuestion(mirebalaisReportsProperties.getHivTestResultConcept());
        testedPositiveForHIV.setValueList(Collections.singletonList(mirebalaisReportsProperties.getPositiveConcept()));
        testedPositiveForHIV.setOperator(SetComparator.IN);
        testedPositiveForHIV.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        testedPositiveForHIV.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
        cohortDsd.addColumn("testedPositiveForHIV", map(testedPositiveForHIV, "onOrAfter=${startDate},onOrBefore=${endDate}"));

        // New Enrollments in HIV program in interval
        ProgramEnrollmentCohortDefinition enrolledInHIV = new ProgramEnrollmentCohortDefinition();
        enrolledInHIV.setPrograms(Collections.singletonList(programWorkflowService.getProgramByUuid(HIVProgram.HIV.uuid())));
        enrolledInHIV.addParameter(new Parameter("enrolledOnOrAfter", "On or after", Date.class));
        enrolledInHIV.addParameter(new Parameter("enrolledOnOrBefore", "On or before", Date.class));
        cohortDsd.addColumn("newEnrollmentsInHIV", map(enrolledInHIV, "enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));

        return cohortDsd;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) throws IOException {
        return Collections.EMPTY_LIST;
    }
}
