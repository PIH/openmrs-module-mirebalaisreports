package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.Location;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.definitions.helper.DailyIndicatorByLocationReportDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.MappedParametersCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
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
public class DailyRegistrationsReportManager extends DailyIndicatorByLocationReportDefinition {

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.DAILY_REGISTRATIONS_REPORT_DEFINITION_UUID;
    }

    @Override
    protected String getMessageCodePrefix() {
        return "mirebalaisreports.dailyRegistrations.";
    }

    @Override
    public String getNameOfLocationParameterOnCohortDefinition() {
        return "location";
    }

    @Override
    public CohortDefinition getByLocationCohortDefinition() {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.addEncounterType(mirebalaisReportsProperties.getRegistrationEncounterType());
        cd.addParameter(new Parameter("locationList", "Location", Location.class));
        cd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));

        Map<String, String> rename = new HashMap<String, String>();
        rename.put("locationList", "location");
        rename.put("onOrAfter", "startDate");
        rename.put("onOrBefore", "endDate");
        MappedParametersCohortDefinition renamed = new MappedParametersCohortDefinition(cd, rename);
        renamed.setName("Location-{{ location.uuid }}");
        renamed.setDescription("ui.i18n.Location.name.{{ location.uuid }}");
        return renamed;
    }

    @Override
    public void addAdditionalDataSetDefinitions(ReportDefinition reportDefinition) {
        CohortCrossTabDataSetDefinition dsd = new CohortCrossTabDataSetDefinition();
        dsd.addParameter(getStartDateParameter());
        dsd.addParameter(getEndDateParameter());

        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.addEncounterType(mirebalaisReportsProperties.getRegistrationEncounterType());
        cd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));

        dsd.addColumn(getMessageCodePrefix() + "overall", map(cd, "onOrAfter=${startDate},onOrBefore=${endDate}"));

        reportDefinition.addDataSetDefinition("overall", map(dsd, "startDate=${day},endDate=${day+1d-1s}"));
    }

}
