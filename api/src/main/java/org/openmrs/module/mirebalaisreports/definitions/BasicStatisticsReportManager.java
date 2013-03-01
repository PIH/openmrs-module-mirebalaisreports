package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emr.EmrProperties;
import org.openmrs.module.mirebalaisreports.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.patientregistration.PatientRegistrationGlobalProperties;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
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
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BasicStatisticsReportManager {

    @Autowired
    EmrProperties emrProperties;

    public MapDataSet evaluate(Date day) throws EvaluationException {
        day = DateUtil.getStartOfDay(day);

        // metadata we're (hard)coding against

        EncounterType registrationEncounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PATIENT_REGISTRATION_ENCOUNTER_TYPE();

        EncounterType vitalsEncounterType = Context.getEncounterService().getEncounterTypeByUuid("4fb47712-34a6-40d2-8ed3-e153abbd25b7");
        if (vitalsEncounterType == null) {
            throw new IllegalStateException("Cannot find Vitals encounter type by uuid 4fb47712-34a6-40d2-8ed3-e153abbd25b7");
        }

        List<EncounterType> nonRegistrationEncounterTypes = Context.getEncounterService().getAllEncounterTypes(false);
        nonRegistrationEncounterTypes.remove(registrationEncounterType);

        Location outpatientClinic = Context.getLocationService().getLocationByUuid("199e7d87-92a0-4398-a0f8-11d012178164");
        if (outpatientClinic == null) {
            throw new IllegalStateException("Cannot find outpatient clinic by uuid 199e7d87-92a0-4398-a0f8-11d012178164");
        }

        // set up underlying queries
        VisitCohortDefinition visitsStartedOnDayQuery = new VisitCohortDefinition();
        visitsStartedOnDayQuery.addParameter(new Parameter("startedOnOrAfter", "Start of Day", Date.class));
        visitsStartedOnDayQuery.addParameter(new Parameter("startedOnOrBefore", "End of Day", Date.class));

        VisitCohortDefinition activeVisitsQuery = new VisitCohortDefinition();
        activeVisitsQuery.setActive(true);

        EncounterCohortDefinition todayRegistrationsQuery = new EncounterCohortDefinition();
        todayRegistrationsQuery.setEncounterTypeList(Arrays.asList(registrationEncounterType));
        todayRegistrationsQuery.setOnOrAfter(day);
        todayRegistrationsQuery.setOnOrBefore(DateUtil.getEndOfDay(day));

        EncounterCohortDefinition outpatientEncountersOnDayQuery = new EncounterCohortDefinition();
        outpatientEncountersOnDayQuery.setLocationList(Arrays.asList(outpatientClinic));
        outpatientEncountersOnDayQuery.setEncounterTypeList(nonRegistrationEncounterTypes);
        outpatientEncountersOnDayQuery.addParameter(new Parameter("onOrAfter", "Start of day", Date.class));
        outpatientEncountersOnDayQuery.addParameter(new Parameter("onOrBefore", "End of day", Date.class));

        EncounterCohortDefinition encountersOfTypeInPeriodQuery = new EncounterCohortDefinition();
        encountersOfTypeInPeriodQuery.addParameter(new Parameter("encounterTypeList", "Encounter Types", EncounterType.class));
        encountersOfTypeInPeriodQuery.addParameter(new Parameter("onOrAfter", "Start of day", Date.class));
        encountersOfTypeInPeriodQuery.addParameter(new Parameter("onOrBefore", "End of day", Date.class));

        // not really a coded obs, but there's no specific ObsCohortDefinition for searching for groups, and this will work if we don't set a value constraint
        CodedObsCohortDefinition diagnosisInPeriodQuery = new CodedObsCohortDefinition();
        diagnosisInPeriodQuery.addParameter(new Parameter("onOrAfter", "Start of day", Date.class));
        diagnosisInPeriodQuery.addParameter(new Parameter("onOrBefore", "End of day", Date.class));
        diagnosisInPeriodQuery.setTimeModifier(PatientSetService.TimeModifier.ANY);
        diagnosisInPeriodQuery.setQuestion(emrProperties.getDiagnosisMetadata().getDiagnosisSetConcept());

        CompositionCohortDefinition diagnosisAndVitalsOnDayQuery = new CompositionCohortDefinition();
        diagnosisAndVitalsOnDayQuery.addParameter(new Parameter("day", "Day", Date.class));
        diagnosisAndVitalsOnDayQuery.addSearch("vitals", encountersOfTypeInPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "encounterTypeList", vitalsEncounterType));
        diagnosisAndVitalsOnDayQuery.addSearch("diagnosis", map((CohortDefinition) diagnosisInPeriodQuery, "onOrAfter=${day},onOrBefore=${day}"));
        diagnosisAndVitalsOnDayQuery.setCompositionString("vitals AND diagnosis");

        // set up indicators
        CohortIndicator visitsStartedOnDay = new CohortIndicator("Visits Started on Day");
        visitsStartedOnDay.addParameter(new Parameter("day", "Day", Date.class));
        visitsStartedOnDay.setCohortDefinition(visitsStartedOnDayQuery, "startedOnOrAfter=${day},startedOnOrBefore=${day}");

        CohortIndicator activeVisits = new CohortIndicator("Active Visits");
        activeVisits.setCohortDefinition(activeVisitsQuery, "");

        CohortIndicator todayRegistrations = new CohortIndicator("Registrations Today");
        todayRegistrations.setCohortDefinition(todayRegistrationsQuery, "");

        CohortIndicator outpatientEncountersOnDay = new CohortIndicator("Outpatients on Day");
        outpatientEncountersOnDay.addParameter(new Parameter("day", "Day", Date.class));
        outpatientEncountersOnDay.setCohortDefinition(outpatientEncountersOnDayQuery, "onOrAfter=${day},onOrBefore=${day}");

        CohortIndicator outpatientEncountersOnDayWithVitals = new CohortIndicator("Outpatients on Day - % with vitals");
        outpatientEncountersOnDayWithVitals.setType(CohortIndicator.IndicatorType.FRACTION);
        outpatientEncountersOnDayWithVitals.addParameter(new Parameter("day", "Day", Date.class));
        outpatientEncountersOnDayWithVitals.setCohortDefinition(encountersOfTypeInPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "encounterTypeList", vitalsEncounterType));
        outpatientEncountersOnDayWithVitals.setDenominator(outpatientEncountersOnDayQuery, "onOrAfter=${day},onOrBefore=${day}");

        CohortIndicator outpatientEncountersOnDayWithDiagnosis = new CohortIndicator("Outpatients on Day - % with diagnosis");
        outpatientEncountersOnDayWithDiagnosis.setType(CohortIndicator.IndicatorType.FRACTION);
        outpatientEncountersOnDayWithDiagnosis.addParameter(new Parameter("day", "Day", Date.class));
        outpatientEncountersOnDayWithDiagnosis.setCohortDefinition(diagnosisInPeriodQuery, "onOrAfter=${day},onOrBefore=${day}");
        outpatientEncountersOnDayWithDiagnosis.setDenominator(outpatientEncountersOnDayQuery, "onOrAfter=${day},onOrBefore=${day}");

        CohortIndicator outpatientEncountersOnDayWithVitalsAndDiagnosis = new CohortIndicator("Outpatients on Day - $ with vitals and diagnosis");
        outpatientEncountersOnDayWithVitalsAndDiagnosis.setType(CohortIndicator.IndicatorType.FRACTION);
        outpatientEncountersOnDayWithVitalsAndDiagnosis.addParameter(new Parameter("day", "Day", Date.class));
        outpatientEncountersOnDayWithVitalsAndDiagnosis.setCohortDefinition(diagnosisAndVitalsOnDayQuery, "day=${day}");
        outpatientEncountersOnDayWithVitalsAndDiagnosis.setDenominator(outpatientEncountersOnDayQuery, "onOrAfter=${day},onOrBefore=${day}");

        // set up a dataset with the indicators
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.addParameter(new Parameter("reportDay", "Report Day", Date.class));
        dsd.addColumn("startedVisitOnDay", "Started Visit On Day", map(visitsStartedOnDay, "day=${reportDay}"), "");
        dsd.addColumn("startedVisitDayBefore", "Started Visit On Day Before", map(visitsStartedOnDay, "day=${reportDay-1d}"), "");
        dsd.addColumn("activeVisits", "Current Active Visits", map(activeVisits, ""), "");
        dsd.addColumn("todayRegistrations", "Registrations made today", map(todayRegistrations, ""), "");
        dsd.addColumn("outpatientsDayBefore", "Yesterday's Outpatients", map(outpatientEncountersOnDay, "day=${reportDay-1d}"), "");
        dsd.addColumn("outpatientsDayBeforeWithVitals", "Yesterday's Outpatients with vitals", map(outpatientEncountersOnDayWithVitals, "day=${reportDay-1d}"), "");
        dsd.addColumn("outpatientsDayBeforeWithDiagnosis", "Yesterday's Outpatients with diagnosis", map(outpatientEncountersOnDayWithDiagnosis, "day=${reportDay-1d}"), "");
        dsd.addColumn("outpatientsDayBeforeWithVitalsAndDiagnosis", "Yesterday's Outpatients with diagnosis and vitals", map(outpatientEncountersOnDayWithVitalsAndDiagnosis, "day=${reportDay-1d}"), "");

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

    public void setEmrProperties(EmrProperties emrProperties) {
        this.emrProperties = emrProperties;
    }
}
