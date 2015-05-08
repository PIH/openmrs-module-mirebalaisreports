package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.definitions.helper.DailyIndicatorByLocationReportDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.MappedParametersCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortsWithVaryingParametersDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Component
public class DailyClinicalEncountersReportManager extends DailyIndicatorByLocationReportDefinition {

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.DAILY_CLINICAL_ENCOUNTERS_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getVersion() {
        return "1.2";
    }

    @Override
    protected String getMessageCodePrefix() {
        return "mirebalaisreports.dailyClinicalEncounters.";
    }

    @Override
    public String getNameOfLocationParameterOnCohortDefinition() {
        return "location";
    }

    @Override
    public void addDataSetDefinitions(ReportDefinition reportDefinition) {
        EncounterType vitalsEncounterType = mirebalaisReportsProperties.getVitalsEncounterType();
        EncounterType consultEncounterType = mirebalaisReportsProperties.getConsultEncounterType();

        CohortDefinition clinicalCheckIns = definitionLibraries.getDefinition(CohortDefinition.class, "mirebalais.cohortDefinition.clinicalCheckInAtLocation");
        clinicalCheckIns.setName("clinicalCheckIns");
        clinicalCheckIns.setDescription(getMessageCodePrefix() + "clinicalCheckIns");

        EncounterCohortDefinition vitals = new EncounterCohortDefinition();
        vitals.setName("vitals");
        vitals.setDescription("ui.i18n.EncounterType.name." + vitalsEncounterType.getUuid());
        vitals.addParameter(new Parameter("locationList", "Location", Location.class));
        vitals.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
        vitals.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
        vitals.addEncounterType(vitalsEncounterType);

        EncounterCohortDefinition consults = new EncounterCohortDefinition();
        consults.setName("consults");
        consults.setDescription("ui.i18n.EncounterType.name." + consultEncounterType.getUuid());
        consults.addParameter(new Parameter("locationList", "Location", Location.class));
        consults.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
        consults.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
        consults.addEncounterType(consultEncounterType);

        CompositionCohortDefinition consultWithoutVitals = new CompositionCohortDefinition();
        consultWithoutVitals.setName("consultWithoutVitals");
        consultWithoutVitals.setDescription(getMessageCodePrefix() + "consultWithoutVitals");
        consultWithoutVitals.addParameter(getStartDateParameter());
        consultWithoutVitals.addParameter(getEndDateParameter());
        consultWithoutVitals.addParameter(new Parameter("location", "Location", Location.class));
        consultWithoutVitals.addSearch("consult", Mapped.map(consults, "onOrAfter=${startDate},onOrBefore=${endDate},locationList=${location}"));
        consultWithoutVitals.addSearch("vitals", Mapped.map(vitals, "onOrAfter=${startDate},onOrBefore=${endDate},locationList=${location}"));
        consultWithoutVitals.setCompositionString("consult AND NOT vitals");

        CohortsWithVaryingParametersDataSetDefinition byLocationDsd = new CohortsWithVaryingParametersDataSetDefinition();
        byLocationDsd.setName("byLocation");
        byLocationDsd.addParameter(getStartDateParameter());
        byLocationDsd.addParameter(getEndDateParameter());
        byLocationDsd.addColumn(clinicalCheckIns);
        byLocationDsd.addColumn(renameParameters(vitals));
        byLocationDsd.addColumn(renameParameters(consults));
        byLocationDsd.addColumn(consultWithoutVitals);
        byLocationDsd.setVaryingParameters(getParameterOptions());
        byLocationDsd.setRowLabelTemplate("{{ message location.uuid prefix=\"ui.i18n.Location.name.\" }}");

        reportDefinition.addDataSetDefinition("byLocation", map(byLocationDsd, MAP_DAY_TO_START_AND_END_DATE));
    }

    private CohortDefinition renameParameters(EncounterCohortDefinition in) {
        Map<String, String> rename = new HashMap<String, String>();
        rename.put("locationList", "location");
        rename.put("onOrAfter", "startDate");
        rename.put("onOrBefore", "endDate");

        MappedParametersCohortDefinition renamed = new MappedParametersCohortDefinition(in, rename);
        renamed.setName(in.getName());
        renamed.setDescription(in.getDescription());
        return renamed;
    }

}
