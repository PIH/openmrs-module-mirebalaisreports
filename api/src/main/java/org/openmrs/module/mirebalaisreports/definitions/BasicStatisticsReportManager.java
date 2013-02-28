package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.emr.EmrProperties;
import org.openmrs.module.mirebalaisreports.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.patientregistration.PatientRegistrationGlobalProperties;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;

public class BasicStatisticsReportManager {

    public BasicStatisticsReportManager() {
    }

    public MapDataSet evaluate(Date day) throws EvaluationException {
        day = DateUtil.getStartOfDay(day);

        // set up underlying queries
        VisitCohortDefinition visitsStartedOnDayQuery = new VisitCohortDefinition();
        visitsStartedOnDayQuery.addParameter(new Parameter("startedOnOrAfter", "Start of Day", Date.class));
        visitsStartedOnDayQuery.addParameter(new Parameter("startedOnOrBefore", "End of Day", Date.class));

        // set up indicators
        CohortIndicator visitsStartedOnDay = new CohortIndicator();
        visitsStartedOnDay.addParameter(new Parameter("day", "Day", Date.class));
        visitsStartedOnDay.setCohortDefinition(visitsStartedOnDayQuery, "startedOnOrAfter=${day},startedOnOrBefore=${day}");

        VisitCohortDefinition activeVisitsQuery = new VisitCohortDefinition();
        activeVisitsQuery.setActive(true);
        CohortIndicator activeVisits = new CohortIndicator();
        activeVisits.setCohortDefinition(activeVisitsQuery, "");

        EncounterType encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PATIENT_REGISTRATION_ENCOUNTER_TYPE();
        EncounterCohortDefinition todayRegistrationsQuery = new EncounterCohortDefinition();
        todayRegistrationsQuery.setEncounterTypeList(Arrays.asList(encounterType));
        todayRegistrationsQuery.setOnOrAfter(day);
        todayRegistrationsQuery.setOnOrBefore(DateUtil.getEndOfDay(day));
        CohortIndicator todayRegistrations = new CohortIndicator();
        todayRegistrations.setCohortDefinition(todayRegistrationsQuery, "");

        // set up a dataset with the indicators
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.addParameter(new Parameter("reportDay", "Report Day", Date.class));
        dsd.addColumn("startedVisitOnDay", "Started Visit On Day", map(visitsStartedOnDay, "day=${reportDay}"), "");
        dsd.addColumn("startedVisitDayBefore", "Started Visit On Day Before", map(visitsStartedOnDay, "day=${reportDay-1d}"), "");
        dsd.addColumn("activeVisits", "Current Active Visits", map(activeVisits, ""), "");
        dsd.addColumn("todayRegistrations", "Registrations made today", map(todayRegistrations, ""), "");

        EvaluationContext evaluationContext = new EvaluationContext();
        evaluationContext.addParameterValue("reportDay", day);

        DataSet evaluated = Context.getService(DataSetDefinitionService.class).evaluate(dsd, evaluationContext);
        return (MapDataSet) evaluated;
    }

    private <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
        if (parameterizable == null) {
            throw new NullPointerException("Programming error: missing parameterizable");
        }
        if (mappings == null) {
            mappings = ""; // probably not necessary, just to be safe
        }
        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
    }

}
