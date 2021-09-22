package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.module.pihcore.PihEmrConfigConstants;
import org.openmrs.module.pihcore.config.ConfigDescriptor;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.MappedParametersCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortsWithVaryingParametersDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Component
public class DailyRegistrationsReportManager extends DailyIndicatorByLocationReportManager {

    public static final String DAILY_REGISTRATIONS_REPORT_DEFINITION_UUID = "2e91bd04-4c7a-11e3-9325-f3ae8db9f6a7";

    @Autowired
    EncounterService encounterService;

    @Override
    public Category getCategory() {
        return Category.DAILY;
    }

    @Override
    public List<ConfigDescriptor.Country> getCountries() {
        return Arrays.asList(ConfigDescriptor.Country.HAITI);
    }

    @Override
    public String getUuid() {
        return DAILY_REGISTRATIONS_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getName() {
        return "dailyRegistrations";
    }

    @Override
    public String getVersion() {
        return "1.4-SNAPSHOT";
    }

    @Override
    public Integer getOrder() {
        return 1;
    }

    @Override
    public String getNameOfLocationParameterOnCohortDefinition() {
        return "location";
    }

    @Override
    public void addDataSetDefinitions(ReportDefinition reportDefinition) {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.addEncounterType(encounterService.getEncounterTypeByUuid(PihEmrConfigConstants.ENCOUNTERTYPE_PATIENT_REGISTRATION_UUID));
        cd.addParameter(new Parameter("locationList", "Location", Location.class));
        cd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));

        Map<String, String> rename = new HashMap<String, String>();
        rename.put("locationList", "location");
        rename.put("onOrAfter", "startDate");
        rename.put("onOrBefore", "endDate");
        MappedParametersCohortDefinition regByLocation = new MappedParametersCohortDefinition(cd, rename);
        regByLocation.setName("registrations");
        regByLocation.setDescription("ui.i18n.EncounterType.name.873f968a-73a8-4f9c-ac78-9f4778b751b6");

        CohortsWithVaryingParametersDataSetDefinition byLocationDsd = new CohortsWithVaryingParametersDataSetDefinition();
        byLocationDsd.setName("byLocation");
        byLocationDsd.addParameter(getStartDateParameter());
        byLocationDsd.addParameter(getEndDateParameter());
        byLocationDsd.addColumn(regByLocation);
        byLocationDsd.setVaryingParameters(getParameterOptions());
        byLocationDsd.setRowLabelTemplate("{{ location.name }}");

        CohortCrossTabDataSetDefinition overallDsd = new CohortCrossTabDataSetDefinition();
        overallDsd.setName("overall");
        overallDsd.addParameter(getStartDateParameter());
        overallDsd.addParameter(getEndDateParameter());

        EncounterCohortDefinition overall = new EncounterCohortDefinition();
        overall.addEncounterType(encounterService.getEncounterTypeByUuid(PihEmrConfigConstants.ENCOUNTERTYPE_PATIENT_REGISTRATION_UUID));
        overall.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
        overall.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));

        overallDsd.addColumn(getMessageCodePrefix() + "overall", map(overall, "onOrAfter=${startDate},onOrBefore=${endDate}"));

        reportDefinition.addDataSetDefinition("overall", map(overallDsd, MAP_DAY_TO_START_AND_END_DATE));
        reportDefinition.addDataSetDefinition("byLocation", map(byLocationDsd, MAP_DAY_TO_START_AND_END_DATE));
    }

}
