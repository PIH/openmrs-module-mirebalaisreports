package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.config.ConfigDescriptor;
import org.openmrs.module.pihcore.reporting.cohort.definition.InpatientLocationCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class InpatientListReportManager extends BaseMirebalaisReportManager {

    @Override
    public List<ConfigDescriptor.Site> getSites() {
        return Arrays.asList(ConfigDescriptor.Site.MIREBALAIS);
    }

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.INPATIENT_LIST_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getVersion() {
        return "1.1";
    }

    @Override
    protected String getMessageCodePrefix() {
        return "mirebalaisreports.inpatientList.";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        log.info("Constructing " + getName());


        ReportDefinition rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");
        rd.setUuid(getUuid());
        rd.setParameters(getParameters());

        CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
        cohortDsd.addParameter(getStartDateParameter());
        cohortDsd.addParameter(getEndDateParameter());

        InpatientLocationCohortDefinition censusCohortDef = new InpatientLocationCohortDefinition();
        censusCohortDef.addParameter(getEffectiveDateParameter());
        censusCohortDef.setWard(null);

        CohortIndicator censusStartInd = buildIndicator("Census at start: ", censusCohortDef, "effectiveDate=${startDate}");
        cohortDsd.addColumn("censusAtStart" , "Census at start: ", map(censusStartInd, "startDate=${startDate}"), "");

        rd.addDataSetDefinition("cohorts", map(cohortDsd, "startDate=${day},endDate=${day+1d-1ms}"));

        return rd;
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("day", "mirebalaisreports.parameter.day", Date.class));
        return parameters;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        //return Arrays.asList(xlsReportDesign(reportDefinition, null, null));
        return Collections.EMPTY_LIST;

    }

    private Parameter getEffectiveDateParameter() {
        return new Parameter("effectiveDate", "mirebalaisreports.parameter.effectiveDate", Date.class);
    }
}
