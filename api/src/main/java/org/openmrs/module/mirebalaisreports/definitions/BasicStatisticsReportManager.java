package org.openmrs.module.mirebalaisreports.definitions;

import org.apache.http.impl.cookie.DateParseException;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.api.MirebalaisReportsService;
import org.openmrs.module.mirebalaisreports.cohort.definition.PersonAuditInfoCohortDefinition;
import org.openmrs.module.mirebalaisreports.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.mirebalaisreports.library.BasicCohortDefinitionLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BasicStatisticsReportManager extends BaseReportManager {

	//***** PROPERTIES *****

    @Autowired
	MirebalaisReportsProperties mirebalaisReportsProperties;

    @Autowired
    EmrApiProperties emrApiProperties;

    @Autowired
    EncounterService encounterService;

    @Autowired
	MirebalaisReportsService reportsService;

	//***** INSTANCE METHODS *****

	@Override
	protected String getMessageCodePrefix() {
		return "mirebalaisreports.basicStatistics.";
	}

	@Override
	public List<Parameter> getParameters() {
		List<Parameter> ret = new ArrayList<Parameter>();
		ret.add(new Parameter("reportDay", "Report Day", Date.class));
		return ret;
	}

	@Override
	public ReportDefinition constructReportDefinition(EvaluationContext context) {
		// set up underlying queries
		VisitCohortDefinition visitsStartedOnDayQuery = new VisitCohortDefinition();
		visitsStartedOnDayQuery.addParameter(new Parameter("startedOnOrAfter", "Start of Day", Date.class));
		visitsStartedOnDayQuery.addParameter(new Parameter("startedOnOrBefore", "End of Day", Date.class));

		//active visits
		VisitCohortDefinition activeVisitsQuery = new VisitCohortDefinition();
		activeVisitsQuery.setActive(true);

		//registration
		PersonAuditInfoCohortDefinition registrationOnDayQuery = new PersonAuditInfoCohortDefinition();
		registrationOnDayQuery.addParameter(new Parameter("createdOnOrAfter", "Start of day", Date.class));
		registrationOnDayQuery.addParameter(new Parameter("createdOnOrBefore", "End of day", Date.class));

		//encounter of types
		EncounterCohortDefinition encountersOfTypesInPeriodQuery = new EncounterCohortDefinition();
		encountersOfTypesInPeriodQuery.addParameter(new Parameter("encounterTypeList", "Encounter Types", EncounterType.class, List.class, null));
		encountersOfTypesInPeriodQuery.addParameter(new Parameter("onOrAfter", "Start of day", Date.class));
		encountersOfTypesInPeriodQuery.addParameter(new Parameter("onOrBefore", "End of day", Date.class));

		//encounters of location
		EncounterCohortDefinition encountersOfLocationPeriodQuery = new EncounterCohortDefinition();
		encountersOfLocationPeriodQuery.addParameter(new Parameter("locationList", "Location List", Location.class, List.class, null));
		encountersOfLocationPeriodQuery.addParameter(new Parameter("encounterTypeList", "Encounter Types", EncounterType.class, List.class, null));
		encountersOfLocationPeriodQuery.addParameter(new Parameter("onOrAfter", "Start of day", Date.class));
		encountersOfLocationPeriodQuery.addParameter(new Parameter("onOrBefore", "End of day", Date.class));

		//consult and vitals
		CompositionCohortDefinition consultAndVitalsOnDayQuery = new CompositionCohortDefinition();
		consultAndVitalsOnDayQuery.addParameter(new Parameter("day", "Day", Date.class));
		consultAndVitalsOnDayQuery.addSearch("vitals", encountersOfTypesInPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "encounterTypeList", mirebalaisReportsProperties.getVitalsEncounterType()));
		consultAndVitalsOnDayQuery.addSearch("consult", encountersOfTypesInPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "encounterTypeList", emrApiProperties.getConsultEncounterType()));
		consultAndVitalsOnDayQuery.setCompositionString("vitals OR consult");

		//returning patients
		CompositionCohortDefinition returningPatientsOnDayQuery = new CompositionCohortDefinition();
		returningPatientsOnDayQuery.addParameter(new Parameter("day", "Day", Date.class));
		returningPatientsOnDayQuery.addSearch("returning", encountersOfTypesInPeriodQuery, SimpleObject.create("onOrAfter", DateUtil.getDateTime(1900, 1, 1), "onOrBefore", "${day-1d}", "encounterTypeList", mirebalaisReportsProperties.getVisitEncounterTypes()));
		returningPatientsOnDayQuery.addSearch("visit", encountersOfTypesInPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "encounterTypeList", mirebalaisReportsProperties.getVisitEncounterTypes()));
		returningPatientsOnDayQuery.setCompositionString("returning AND visit");

		CohortDefinition excludeTestPatientsCohortDefinition = reportsService.getCohortDefinition(BasicCohortDefinitionLibrary.PREFIX + "exclude test patients");

		// set up indicators

		//visits on day
		CohortIndicator visitsStartedOnDay = new CohortIndicator("Visits Started on Day");
		visitsStartedOnDay.addParameter(new Parameter("day", "Day", Date.class));
		visitsStartedOnDay.setCohortDefinition(visitsStartedOnDayQuery, "startedOnOrAfter=${day},startedOnOrBefore=${day}");

		//active visits
		CohortIndicator activeVisits = new CohortIndicator("Active Visits");
		activeVisits.setCohortDefinition(activeVisitsQuery, "");

		//registration on day
		CohortIndicator registrationsOnDay = new CohortIndicator("Registrations on Day");
		registrationsOnDay.addParameter(new Parameter("day", "Day", Date.class));
		registrationsOnDay.setCohortDefinition(registrationOnDayQuery, "createdOnOrAfter=${day},createdOnOrBefore=${day}");

		//returning patients on day
		CohortIndicator returningPatientsOnDay = new CohortIndicator("Returning patients on day");
		returningPatientsOnDay.addParameter(new Parameter("day", "Day", Date.class));
		returningPatientsOnDay.setCohortDefinition(returningPatientsOnDayQuery, "day=${day}");

		//outpatient on day
		CohortIndicator outpatientOnDay = new CohortIndicator("Outpatient on Day");
		outpatientOnDay.addParameter(new Parameter("day", "Day", Date.class));
		outpatientOnDay.setCohortDefinition(encountersOfLocationPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "locationList", mirebalaisReportsProperties.getOutpatientLocation()));

		//women on day
		CohortIndicator womenClinicOnDay = new CohortIndicator("Women on Day");
		womenClinicOnDay.addParameter(new Parameter("day", "Day", Date.class));
		womenClinicOnDay.setCohortDefinition(encountersOfLocationPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "locationList", mirebalaisReportsProperties.getWomenLocation()));

		//set up maps

		//outpatient map
		Mapped<CohortDefinition> outpatientOnDayMCD = new Mapped<CohortDefinition>(encountersOfLocationPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "locationList", mirebalaisReportsProperties.getOutpatientLocation()));
		//women map
		Mapped<CohortDefinition> womenOnDayMCD = new Mapped<CohortDefinition>(encountersOfLocationPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "locationList", mirebalaisReportsProperties.getWomenLocation()));
		//outpatient vitals map
		Mapped<CohortDefinition> outpatientVitalsOnDayMCD = new Mapped<CohortDefinition>(encountersOfLocationPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "locationList", mirebalaisReportsProperties.getOutpatientLocation(), "encounterTypeList", mirebalaisReportsProperties.getVitalsEncounterType()));
		//outpatient diagnosis map
		Mapped<CohortDefinition> outpatientDiagnosisOnDayMCD = new Mapped<CohortDefinition>(encountersOfLocationPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "locationList", mirebalaisReportsProperties.getOutpatientLocation(), "encounterTypeList", mirebalaisReportsProperties.getConsultEncounterType()));
		//women vitals map
		Mapped<CohortDefinition> womenVitalsOnDayMCD = new Mapped<CohortDefinition>(encountersOfLocationPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "locationList", mirebalaisReportsProperties.getWomenLocation(), "encounterTypeList", mirebalaisReportsProperties.getVitalsEncounterType()));
		//women diagnosis map
		Mapped<CohortDefinition> womenDiagnosisOnDayMCD = new Mapped<CohortDefinition>(encountersOfLocationPeriodQuery, SimpleObject.create("onOrAfter", "${day}", "onOrBefore", "${day}", "locationList", mirebalaisReportsProperties.getWomenLocation(), "encounterTypeList", mirebalaisReportsProperties.getConsultEncounterType()));

		//outpatient with vitals
		CohortIndicator outpatientWithVitalsOnDay = new CohortIndicator("Outpatients on Day - % with vitals");
		outpatientWithVitalsOnDay.setType(CohortIndicator.IndicatorType.FRACTION);
		outpatientWithVitalsOnDay.addParameter(new Parameter("day", "Day", Date.class));
		outpatientWithVitalsOnDay.setCohortDefinition(outpatientVitalsOnDayMCD);
		outpatientWithVitalsOnDay.setDenominator(outpatientOnDayMCD);

		//outpatient with diagnosis
		CohortIndicator outpatientWithDiagnosisOnDay = new CohortIndicator("Outpatients on Day - % with diagnosis");
		outpatientWithDiagnosisOnDay.setType(CohortIndicator.IndicatorType.FRACTION);
		outpatientWithDiagnosisOnDay.addParameter(new Parameter("day", "Day", Date.class));
		outpatientWithDiagnosisOnDay.setCohortDefinition(outpatientDiagnosisOnDayMCD);
		outpatientWithDiagnosisOnDay.setDenominator(outpatientOnDayMCD);

		//women with vitals
		CohortIndicator womenWithVitalsOnDay = new CohortIndicator("Women on Day - % with vitals");
		womenWithVitalsOnDay.setType(CohortIndicator.IndicatorType.FRACTION);
		womenWithVitalsOnDay.addParameter(new Parameter("day", "Day", Date.class));
		womenWithVitalsOnDay.setCohortDefinition(womenVitalsOnDayMCD);
		womenWithVitalsOnDay.setDenominator(womenOnDayMCD);

		//women with diagnosis
		CohortIndicator womenWithDiagnosisOnDay = new CohortIndicator("Women on Day - % with diagnosis");
		womenWithDiagnosisOnDay.setType(CohortIndicator.IndicatorType.FRACTION);
		womenWithDiagnosisOnDay.addParameter(new Parameter("day", "Day", Date.class));
		womenWithDiagnosisOnDay.setCohortDefinition(womenDiagnosisOnDayMCD);
		womenWithDiagnosisOnDay.setDenominator(womenOnDayMCD);

		// set up a dataset with the indicators
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.addParameter(new Parameter("reportDay", "Report Day", Date.class));

		dsd.addColumn("startedVisitOnDay", "Started Visit On Day", map(visitsStartedOnDay, "day=${reportDay}"), "");
		dsd.addColumn("startedVisitDayBefore", "Started Visit On Day Before", map(visitsStartedOnDay, "day=${reportDay-1d}"), "");

		dsd.addColumn("activeVisits", "Current Active Visits", map(activeVisits, ""), "");

		dsd.addColumn("outpatientOnDay", "Outpatient on Day", map(outpatientOnDay, "day=${reportDay}"), "");
		dsd.addColumn("outpatientOnDayBefore", "Outpatient on Day Before", map(outpatientOnDay, "day=${reportDay-1d}"), "");

		dsd.addColumn("womenOnDay", "Women Clinic on Day", map(womenClinicOnDay, "day=${reportDay}"), "");
		dsd.addColumn("womenOnDayBefore", "Women Clinic on Day Before", map(womenClinicOnDay, "day=${reportDay-1d}"), "");

		dsd.addColumn("outpatientWithVitalsOnDay", "Outpatient with vitals on Day", map(outpatientWithVitalsOnDay, "day=${reportDay}"), "");
		dsd.addColumn("outpatientWithVitalsOnDayBefore", "Outpatient with vitals on Day Before", map(outpatientWithVitalsOnDay, "day=${reportDay-1d}"), "");

		dsd.addColumn("outpatientWithDiagnosisOnDay", "Outpatient with diagnosis on Day", map(outpatientWithDiagnosisOnDay, "day=${reportDay}"), "");
		dsd.addColumn("outpatientWithDiagnosisOnDayBefore", "Outpatient with diagnosis on Day Before", map(outpatientWithDiagnosisOnDay, "day=${reportDay-1d}"), "");

		dsd.addColumn("womenWithVitalsOnDay", "Women Clinic with vitals on Day", map(womenWithVitalsOnDay, "day=${reportDay}"), "");
		dsd.addColumn("womenWithVitalsOnDayBefore", "Women Clinic with vitals on Day Before", map(womenWithVitalsOnDay, "day=${reportDay-1d}"), "");

		dsd.addColumn("womenWithDiagnosisOnDay", "Women Clinic with diagnosis on Day", map(womenWithDiagnosisOnDay, "day=${reportDay}"), "");
		dsd.addColumn("womenWithDiagnosisOnDayBefore", "Women Clinic with Diagnosis on Day Before", map(womenWithDiagnosisOnDay, "day=${reportDay-1d}"), "");

		dsd.addColumn("returningPatientsOnDay", "Returning Patients On Day", map(returningPatientsOnDay, "day=${reportDay}"), "");
		dsd.addColumn("returningPatientsOnDayBefore", "Returning Patients On Day Before", map(returningPatientsOnDay, "day=${reportDay-1d}"), "");

		dsd.addColumn("todayRegistrations", "Registrations made today", map(registrationsOnDay, "day=${reportDay}"), "");
		dsd.addColumn("yesterdayRegistrations", "Registrations made yesterday", map(registrationsOnDay, "day=${reportDay-1d}"), "");

		ReportDefinition rd = new ReportDefinition();
		rd.setBaseCohortDefinition(map((CohortDefinition) excludeTestPatientsCohortDefinition, ""));
		rd.addParameters(getParameters());
		rd.addDataSetDefinition("dsd", map(dsd, "reportDay=${reportDay}"));

		return rd;
	}

	public MapDataSet evaluate(Date day) throws EvaluationException, DateParseException {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("reportDay", DateUtil.getStartOfDay(day));

		EvaluationContext context = initializeContext(parameters);
		ReportDefinition rd = constructReportDefinition(context);

        ReportData evaluatedReport = Context.getService(ReportDefinitionService.class).evaluate(rd, context);
        DataSet evaluated = evaluatedReport.getDataSets().get("dsd");

        return (MapDataSet) evaluated;
    }

    private <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
        if (parameterizable == null) {
            throw new NullPointerException("Programming error: missing parameterizable");
        }
        if (mappings == null) {
            mappings = "";
        }
        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
    }

    public void setMirebalaisReportsProperties(MirebalaisReportsProperties mirebalaisReportsProperties) {
        this.mirebalaisReportsProperties = mirebalaisReportsProperties;
    }

}
