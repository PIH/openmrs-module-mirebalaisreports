package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emr.reporting.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.mirebalaisreports.MirebalaisProperties;
import org.openmrs.module.mirebalaisreports.cohort.definition.PersonAuditInfoCohortDefinition;
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

import java.util.Date;
import java.util.List;

public class BasicStatisticsReportManager {

    @Autowired
    MirebalaisProperties mirebalaisProperties;

    @Autowired
    EncounterService encounterService;

    public MapDataSet evaluate(Date day) throws EvaluationException {
        day = DateUtil.getStartOfDay(day);

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

        PersonAuditInfoCohortDefinition todayRegistrationsQuery = new PersonAuditInfoCohortDefinition();
        todayRegistrationsQuery.addParameter(new Parameter("createdOnOrAfter", "Start of day", Date.class));
        todayRegistrationsQuery.addParameter(new Parameter("createdOnOrBefore", "End of day", Date.class));

        EncounterCohortDefinition encountersOfTypesInPeriodQuery = new EncounterCohortDefinition();
        encountersOfTypesInPeriodQuery.addParameter(new Parameter("encounterTypeList", "Encounter Types", EncounterType.class, List.class, null));
        encountersOfTypesInPeriodQuery.addParameter(new Parameter("onOrAfter", "Start of day", Date.class));
        encountersOfTypesInPeriodQuery.addParameter(new Parameter("onOrBefore", "End of day", Date.class));

        CompositionCohortDefinition consultAndVitalsOnDayQuery = new CompositionCohortDefinition();
        consultAndVitalsOnDayQuery.addParameter(new Parameter("day", "Day", Date.class));
        consultAndVitalsOnDayQuery.addSearch("vitals", encountersOfTypesInPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "encounterTypeList", mirebalaisProperties.getVitalsEncounterType()));
        consultAndVitalsOnDayQuery.addSearch("consult", encountersOfTypesInPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "encounterTypeList", mirebalaisProperties.getConsultEncounterType()));
        consultAndVitalsOnDayQuery.setCompositionString("vitals OR consult");

        // set up indicators
        CohortIndicator visitsStartedOnDay = new CohortIndicator("Visits Started on Day");
        visitsStartedOnDay.addParameter(new Parameter("day", "Day", Date.class));
        visitsStartedOnDay.setCohortDefinition(visitsStartedOnDayQuery, "startedOnOrAfter=${day},startedOnOrBefore=${day}");

        CohortIndicator activeVisits = new CohortIndicator("Active Visits");
        activeVisits.setCohortDefinition(activeVisitsQuery, "");

        CohortIndicator todayRegistrations = new CohortIndicator("Registrations Today");
        todayRegistrations.addParameter(new Parameter("day", "Day", Date.class));
        todayRegistrations.setCohortDefinition(todayRegistrationsQuery, "createdOnOrAfter=${day},createdOnOrBefore=${day}");

        Mapped<CohortDefinition> outpatientOnDayMCD = new Mapped<CohortDefinition>(encountersOfTypesInPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "encounterTypeList", mirebalaisProperties.getVisitEncounterTypes()));
        Mapped<CohortDefinition> clinicalOnDayMCD = new Mapped<CohortDefinition>(encountersOfTypesInPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "encounterTypeList", mirebalaisProperties.getClinicalEncounterTypes()));

        CohortIndicator outpatientEncountersOnDay = new CohortIndicator("Outpatients on Day (any encounter)");
        outpatientEncountersOnDay.addParameter(new Parameter("day", "Day", Date.class));
        outpatientEncountersOnDay.setCohortDefinition(outpatientOnDayMCD);

        CohortIndicator outpatientClinicalEncountersOnDay = new CohortIndicator("Outpatients on Day (clinical encounters)");
        outpatientClinicalEncountersOnDay.setType(CohortIndicator.IndicatorType.FRACTION);
        outpatientClinicalEncountersOnDay.addParameter(new Parameter("day", "Day", Date.class));
        outpatientClinicalEncountersOnDay.setCohortDefinition(clinicalOnDayMCD);
        outpatientClinicalEncountersOnDay.setDenominator(outpatientOnDayMCD);

        CohortIndicator outpatientClinicalEncountersOnDayWithVitals = new CohortIndicator("Outpatients on Day (clinical encounters) - % with vitals");
        outpatientClinicalEncountersOnDayWithVitals.setType(CohortIndicator.IndicatorType.FRACTION);
        outpatientClinicalEncountersOnDayWithVitals.addParameter(new Parameter("day", "Day", Date.class));
        outpatientClinicalEncountersOnDayWithVitals.setCohortDefinition(encountersOfTypesInPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "encounterTypeList", mirebalaisProperties.getVitalsEncounterType()));
        outpatientClinicalEncountersOnDayWithVitals.setDenominator(clinicalOnDayMCD);

        CohortIndicator outpatientClinicalEncountersOnDayWithConsult = new CohortIndicator("Outpatients on Day (clinical encounters) - % with consult");
        outpatientClinicalEncountersOnDayWithConsult.setType(CohortIndicator.IndicatorType.FRACTION);
        outpatientClinicalEncountersOnDayWithConsult.addParameter(new Parameter("day", "Day", Date.class));
        outpatientClinicalEncountersOnDayWithConsult.setCohortDefinition(encountersOfTypesInPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "encounterTypeList", mirebalaisProperties.getConsultEncounterType()));
        outpatientClinicalEncountersOnDayWithConsult.setDenominator(clinicalOnDayMCD);

        // set up a dataset with the indicators
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.addParameter(new Parameter("reportDay", "Report Day", Date.class));
        dsd.addColumn("startedVisitOnDay", "Started Visit On Day", map(visitsStartedOnDay, "day=${reportDay}"), "");
        dsd.addColumn("startedVisitDayBefore", "Started Visit On Day Before", map(visitsStartedOnDay, "day=${reportDay-1d}"), "");
        dsd.addColumn("activeVisits", "Current Active Visits", map(activeVisits, ""), "");
        dsd.addColumn("todayRegistrations", "Registrations made today", map(todayRegistrations, "day=${reportDay}"), "");
        dsd.addColumn("outpatientsDayBefore", "Yesterday's Outpatients", map(outpatientEncountersOnDay, "day=${reportDay-1d}"), "");
        dsd.addColumn("outpatientsDayBeforeWithClinical", "Yesterday's Outpatients with any clinical encounter", map(outpatientClinicalEncountersOnDay, "day=${reportDay-1d}"), "");
        dsd.addColumn("outpatientsDayBeforeWithVitals", "Yesterday's Outpatients with vitals", map(outpatientClinicalEncountersOnDayWithVitals, "day=${reportDay-1d}"), "");
        dsd.addColumn("outpatientsDayBeforeWithDiagnosis", "Yesterday's Outpatients with diagnosis", map(outpatientClinicalEncountersOnDayWithConsult, "day=${reportDay-1d}"), "");

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

    public void setMirebalaisProperties(MirebalaisProperties mirebalaisProperties) {
        this.mirebalaisProperties = mirebalaisProperties;
    }

}
