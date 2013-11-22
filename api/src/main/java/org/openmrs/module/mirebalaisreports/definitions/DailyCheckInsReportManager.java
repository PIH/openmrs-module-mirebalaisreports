package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.Location;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.definitions.helper.DailyIndicatorByLocationReportDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterWithCodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.MappedParametersCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortsWithVaryingParametersDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class DailyCheckInsReportManager extends DailyIndicatorByLocationReportDefinition {

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.DAILY_CHECK_INS_REPORT_DEFINITION_UUID;
    }

    @Override
    protected String getMessageCodePrefix() {
        return "mirebalaisreports.dailyCheckInEncounters.";
    }

    @Override
    public String getNameOfLocationParameterOnCohortDefinition() {
        return "location";
    }

    @Override
    public void addDataSetDefinitions(ReportDefinition reportDefinition) {
        EncounterCohortDefinition priorConsultAtLocation = new EncounterCohortDefinition();
        priorConsultAtLocation.addEncounterType(mirebalaisReportsProperties.getConsultEncounterType());
        priorConsultAtLocation.addParameter(new Parameter("locationList", "Location List", Location.class));
        priorConsultAtLocation.addParameter(new Parameter("onOrBefore", "On or before", Date.class));

        CohortsWithVaryingParametersDataSetDefinition byLocationDsd = new CohortsWithVaryingParametersDataSetDefinition();
        byLocationDsd.setName("byLocation");
        byLocationDsd.addParameter(getStartDateParameter());
        byLocationDsd.addParameter(getEndDateParameter());
        // byLocationDsd.addColumn(checkInWithReason("PIH:CLINICAL")); // TODO split this
        byLocationDsd.addColumn(checkInSplitByPriorConsultation("PIH:CLINICAL-new", checkInWithReason("PIH:CLINICAL"), priorConsultAtLocation, false));
        byLocationDsd.addColumn(checkInSplitByPriorConsultation("PIH:CLINICAL-return", checkInWithReason("PIH:CLINICAL"), priorConsultAtLocation, true));
        byLocationDsd.addColumn(checkInWithReason("PIH:Lab only"));
        byLocationDsd.addColumn(checkInWithReason("PIH:Pharmacy only"));
        byLocationDsd.addColumn(checkInWithReason("PIH:Procedure only"));
        byLocationDsd.addColumn(checkInWithReason("PIH:Social assistance and psychosocial support"));
        byLocationDsd.addColumn(checkInWithReason("PIH:Request scheduled appointment"));
        byLocationDsd.addColumn(checkInWithReason("PIH:ID card only"));
        byLocationDsd.addColumn(checkInWithOtherOrMissingReasons("PIH:CLINICAL", "PIH:Lab only", "PIH:Pharmacy only",
                "PIH:Procedure only", "PIH:Social assistance and psychosocial support", "PIH:Request scheduled appointment",
                "PIH:ID card only"));
        byLocationDsd.setVaryingParameters(getParameterOptions());
        byLocationDsd.setRowLabelTemplate("{{ message location.uuid prefix=\"ui.i18n.Location.name.\" }}");

        reportDefinition.addDataSetDefinition("byLocation", map(byLocationDsd, MAP_DAY_TO_START_AND_END_DATE));
    }

    private CohortDefinition checkInSplitByPriorConsultation(String columnName, CohortDefinition checkInWithReason, EncounterCohortDefinition priorConsult, boolean included) {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName(columnName);
        cd.addParameter(getStartDateParameter());
        cd.addParameter(getEndDateParameter());
        cd.addParameter(getLocationParameter());
        cd.addSearch("checkInWithReason", map(checkInWithReason, "startDate=${startDate},endDate=${endDate},location=${location}"));
        cd.addSearch("priorConsult", map((CohortDefinition) priorConsult, "onOrBefore=${startDate - 1d},locationList=${location}"));
        cd.setCompositionString("checkInWithReason " + (included ? "AND" : "AND NOT") + " priorConsult");
        return cd;
    }

    private CohortDefinition checkInWithReason(String valueCoded) {
        CodeAndSource value = new CodeAndSource(valueCoded);
        EncounterWithCodedObsCohortDefinition cd = new EncounterWithCodedObsCohortDefinition();
        cd.setName(value.getCode());
        cd.setDescription(value.getCode());
        cd.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
        cd.addParameter(new Parameter("locationList", "Locations", Location.class));
        cd.addEncounterType(mirebalaisReportsProperties.getCheckInEncounterType());
        cd.setConcept(conceptService.getConceptByMapping("Type of HUM visit", "PIH"));
        cd.addIncludeCodedValue(conceptService.getConceptByMapping(value.getCode(), value.getSource()));
        return renameParameters(cd);
    }

    private CohortDefinition checkInWithOtherOrMissingReasons(String... excludeValues) {
        EncounterWithCodedObsCohortDefinition cd = new EncounterWithCodedObsCohortDefinition();
        cd.setName("OTHER");
        cd.setDescription("OTHER");
        cd.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
        cd.addParameter(new Parameter("locationList", "Locations", Location.class));
        cd.addEncounterType(mirebalaisReportsProperties.getCheckInEncounterType());
        cd.setIncludeNoObsValue(true);
        for (String excludeValue : excludeValues) {
            CodeAndSource exclude = new CodeAndSource(excludeValue);
            cd.addExcludeCodedValue(conceptService.getConceptByMapping(exclude.getCode(), exclude.getSource()));
        }
        return renameParameters(cd);
    }

    private CohortDefinition renameParameters(CohortDefinition in) {
        Map<String, String> rename = new HashMap<String, String>();
        rename.put("locationList", "location");
        rename.put("onOrAfter", "startDate");
        rename.put("onOrBefore", "endDate");

        MappedParametersCohortDefinition renamed = new MappedParametersCohortDefinition(in, rename);
        renamed.setName(in.getName());
        renamed.setDescription(in.getDescription());
        return renamed;
    }

    private class CodeAndSource {
        private String source;
        private String code;
        public CodeAndSource(String sourceToCode) {
            String[] split = sourceToCode.split(":");
            source = split[0];
            code = split[1];
        }

        private String getSource() {
            return source;
        }

        private String getCode() {
            return code;
        }
    }
}
