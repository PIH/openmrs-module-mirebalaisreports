/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.PihEmrConfigConstants;
import org.openmrs.module.pihcore.reporting.cohort.definition.AdmissionSoonAfterExitCohortDefinition;
import org.openmrs.module.pihcore.reporting.cohort.definition.DiedSoonAfterEncounterCohortDefinition;
import org.openmrs.module.pihcore.reporting.cohort.definition.LastDispositionBeforeExitCohortDefinition;
import org.openmrs.module.pihcore.reporting.library.PihCohortDefinitionLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class InpatientStatsDailyReportManager extends BasePihReportManager {

    public static final String EMERGENCY_DEPARTMENT_UUID = "f3a5586e-f06c-4dfb-96b0-6f3451a35e90";
    public static final String EMERGENCY_RECEPTION_UUID = "afa09010-43b6-4f19-89e0-58d09941bcbd";

    @Autowired
    EncounterService encounterService;

    @Autowired
    LocationService locationService;

    @Autowired
    private AllDefinitionLibraries libraries;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private AdtService adtService;

    @Override
    public Category getCategory() {
  //      return Category.DAILY;
        return null;  // right now we custom set up the link to this in the Custom App Loader Factory, so we don't set the category here
    }

    @Override
    public List<String> getSites() {
        return Arrays.asList("MIREBALAIS");
    }

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.INPATIENT_STATS_DAILY_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getName() {
        return "inpatientStatsDaily";
    }

    @Override
    public Integer getOrder() {
        return 4;
    }

    @Override
    public String getVersion() {
        return "1.2-SNAPSHOT";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("day", "mirebalaisreports.parameter.day", Date.class));
        return l;
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        log.info("Constructing " + getName());

        ReportDefinition rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");
        rd.setUuid(getUuid());
        rd.setParameters(getParameters());

        rd.addDataSetDefinition("cohorts", map(constructDataSetDefinition(), "startDate=${day},endDate=${day+1d-1ms}"));

        return rd;
    }

    public DataSetDefinition constructDataSetDefinition() {
        List<Location> inpatientLocations = adtService.getInpatientLocations();
        EncounterType admissionEncounterType = encounterService.getEncounterTypeByUuid(PihEmrConfigConstants.ENCOUNTERTYPE_ADMISSION_UUID);

        Concept dischargedDisposition = conceptService.getConceptByMapping("DISCHARGED", "PIH");
        Concept deathDisposition = conceptService.getConceptByMapping("DEATH", "PIH");
        Concept transferOutDisposition = conceptService.getConceptByMapping("Transfer out of hospital", "PIH");
        Concept leftWithoutCompletionOfTreatmentDisposition = conceptService.getConceptByMapping("Departed without medical discharge", "PIH");
        Concept leftWithoutSeeingClinicianDisposition = conceptService.getConceptByMapping("Left without seeing a clinician", "PIH");
        List<Concept> dispositionsToConsider = Arrays.asList(dischargedDisposition, deathDisposition, transferOutDisposition, leftWithoutCompletionOfTreatmentDisposition, leftWithoutSeeingClinicianDisposition);
        // Dispositions we're currently ignoring: "Transfer within hospital", "Admit to hospital", "Discharged", "Emergency Department observation", "Home"

        CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
        cohortDsd.addParameter(getStartDateParameter());
        cohortDsd.addParameter(getEndDateParameter());

        DiedSoonAfterEncounterCohortDefinition diedSoonAfterAdmission = new DiedSoonAfterEncounterCohortDefinition();
        diedSoonAfterAdmission.setEncounterType(admissionEncounterType);
        diedSoonAfterAdmission.addParameter(new Parameter("diedOnOrAfter", "Died on or after", Date.class));
        diedSoonAfterAdmission.addParameter(new Parameter("diedOnOrBefore", "Died on or before", Date.class));

        for (Location location : inpatientLocations) {
            String locationSuffix = ":" + location.getUuid();

            // census at start, census at end

            CohortDefinition censusCohortDef = libraries.cohortDefinition(PihCohortDefinitionLibrary.PREFIX + "inpatientAtLocationOnDate", "location", location);

            CohortIndicator censusStartInd = buildIndicator("Census at start: " + location.getName(), censusCohortDef, "date=${startDate}");
            CohortIndicator censusEndInd = buildIndicator("Census at end: " + location.getName(), censusCohortDef, "date=${endDate}");

            cohortDsd.addColumn("censusAtStart" + locationSuffix, "Census at start: " + location.getName(), map(censusStartInd, "startDate=${startDate}"), "");
            cohortDsd.addColumn("censusAtEnd" + locationSuffix, "Census at end: " + location.getName(), map(censusEndInd, "endDate=${endDate}"), "");

            // number of admissions

            CohortDefinition admissionDuring = libraries.cohortDefinition(PihCohortDefinitionLibrary.PREFIX + "admissionAtLocationDuringPeriod", "location", location);

            CohortIndicator admissionInd = buildIndicator("Admission: " + location.getName(), admissionDuring, "startDate=${startDate},endDate=${endDate}");
            cohortDsd.addColumn("admissions" + locationSuffix, "Admission: " + location.getName(), map(admissionInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number of transfer ins

            CohortDefinition transferInDuring = libraries.cohortDefinition(PihCohortDefinitionLibrary.PREFIX + "transferInToLocationDuringPeriod", "location", location);

            CohortIndicator transferInInd = buildIndicator("Transfer In: " + location.getName(), transferInDuring, "startDate=${startDate},endDate=${endDate}");
            cohortDsd.addColumn("transfersIn" + locationSuffix, "Transfer In: " + location.getName(), map(transferInInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number of transfer outs

            CohortDefinition transferOutDuring = libraries.cohortDefinition(PihCohortDefinitionLibrary.PREFIX + "transferOutOfLocationDuringPeriod", "location", location);

            CohortIndicator transferOutInd = buildIndicator("Transfer Out: " + location.getName(), transferOutDuring, "startDate=${startDate},endDate=${endDate}");
            cohortDsd.addColumn("transfersOut" + locationSuffix, "Transfer Out: " + location.getName(), map(transferOutInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number of discharges

            CohortDefinition discharged = libraries.cohortDefinition(PihCohortDefinitionLibrary.PREFIX + "dischargeExitFromLocationDuringPeriod", "location", location);

            CohortIndicator dischargedInd = buildIndicator("Discharged: " + location.getName(), discharged, "startDate=${startDate},endDate=${endDate}");
            cohortDsd.addColumn("discharged" + locationSuffix, "Discharged: " + location.getName(), map(dischargedInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number of deaths -> split deaths into within-48-hours and later

            CohortDefinition deathsEarly = libraries.cohortDefinition(PihCohortDefinitionLibrary.PREFIX + "diedExitFromLocationDuringPeriodSoonAfterAdmission", "location", location);
            CohortDefinition deathsLate = libraries.cohortDefinition(PihCohortDefinitionLibrary.PREFIX + "diedExitFromLocationDuringPeriodNotSoonAfterAdmission", "location", location);

            CohortIndicator deathsEarlyInd = buildIndicator("Deaths within 48h: " + location.getName(), deathsEarly, "startDate=${startDate},endDate=${endDate}");
            cohortDsd.addColumn("deathsWithin48" + locationSuffix, "Deaths within 48h: " + location.getName(), map(deathsEarlyInd, "startDate=${startDate},endDate=${endDate}"), "");

            CohortIndicator deathsLateInd = buildIndicator("Deaths after 48h: " + location.getName(), deathsLate, "startDate=${startDate},endDate=${endDate}");
            cohortDsd.addColumn("deathsAfter48" + locationSuffix, "Deaths after 48h: " + location.getName(), map(deathsLateInd, "startDate=${startDate},endDate=${endDate}"), "");


            // number transferred out of HUM

            CohortDefinition transfersOut = libraries.cohortDefinition(PihCohortDefinitionLibrary.PREFIX + "transferOutOfHumExitFromLocationDuringPeriod", "location", location);

            CohortIndicator transfersOutInd = buildIndicator("Transfer Outs: " + location.getName(), transfersOut, "startDate=${startDate},endDate=${endDate}");
            cohortDsd.addColumn("transfersOutOfHUM" + locationSuffix, "Transfer Outs: " + location.getName(), map(transfersOutInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number left without completing treatment

            LastDispositionBeforeExitCohortDefinition leftWithoutCompletingTreatment = new LastDispositionBeforeExitCohortDefinition();
            leftWithoutCompletingTreatment.addParameter(new Parameter("exitOnOrAfter", "Exit on or after", Date.class));
            leftWithoutCompletingTreatment.addParameter(new Parameter("exitOnOrBefore", "Exit on or before", Date.class));
            leftWithoutCompletingTreatment.setExitFromWard(location);
            leftWithoutCompletingTreatment.setDispositionsToConsider(dispositionsToConsider);
            leftWithoutCompletingTreatment.addDisposition(leftWithoutCompletionOfTreatmentDisposition);

            CohortIndicator leftWithoutCompletingTreatmentInd = buildIndicator("Left Without Completing Treatment: " + location.getName(), leftWithoutCompletingTreatment, "exitOnOrAfter=${startDate},exitOnOrBefore=${endDate}");
            cohortDsd.addColumn("leftWithoutCompletingTx" + locationSuffix, "Left Without Completing Treatment: " + location.getName(), map(leftWithoutCompletingTreatmentInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number left without completing treatment

            LastDispositionBeforeExitCohortDefinition leftWithoutSeeingClinician = new LastDispositionBeforeExitCohortDefinition();
            leftWithoutSeeingClinician.addParameter(new Parameter("exitOnOrAfter", "Exit on or after", Date.class));
            leftWithoutSeeingClinician.addParameter(new Parameter("exitOnOrBefore", "Exit on or before", Date.class));
            leftWithoutSeeingClinician.setExitFromWard(location);
            leftWithoutSeeingClinician.setDispositionsToConsider(dispositionsToConsider);
            leftWithoutSeeingClinician.addDisposition(leftWithoutSeeingClinicianDisposition);

            CohortIndicator leftWithoutSeeingClinicianInd = buildIndicator("Left Without Seeing Clinician: " + location.getName(), leftWithoutSeeingClinician, "exitOnOrAfter=${startDate},exitOnOrBefore=${endDate}");
            cohortDsd.addColumn("leftWithoutSeeingClinician" + locationSuffix, "Left Without Seeing Clinician: " + location.getName(), map(leftWithoutSeeingClinicianInd, "startDate=${startDate},endDate=${endDate}"), "");
        }

        // number of ED check-ins
        // TODO change this to count by visits or by encounters, instead of by patients
        EncounterCohortDefinition edCheckIn = new EncounterCohortDefinition();
        edCheckIn.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        edCheckIn.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
        edCheckIn.addEncounterType(encounterService.getEncounterTypeByUuid(PihEmrConfigConstants.ENCOUNTERTYPE_CONSULTATION_UUID));
        edCheckIn.addLocation(locationService.getLocationByUuid(EMERGENCY_DEPARTMENT_UUID));
        edCheckIn.addLocation(locationService.getLocationByUuid(EMERGENCY_RECEPTION_UUID));

        CohortIndicator edCheckInInd = buildIndicator("ED Check In", edCheckIn, "onOrAfter=${startDate},onOrBefore=${endDate}");
        cohortDsd.addColumn("edcheckin", "ED Check In", map(edCheckInInd, "startDate=${startDate},endDate=${endDate}"), "");

        // number of surgical op-notes entered
        EncounterCohortDefinition surgicalNotes = new EncounterCohortDefinition();
        surgicalNotes.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        surgicalNotes.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
        surgicalNotes.addEncounterType(encounterService.getEncounterTypeByUuid(PihEmrConfigConstants.ENCOUNTERTYPE_POST_OPERATIVE_NOTE_UUID));

        CohortIndicator surgicalNotesInd = buildIndicator("OR Volume", surgicalNotes, "onOrAfter=${startDate},onOrBefore=${endDate}");
        cohortDsd.addColumn("orvolume", "OR Volume", map(surgicalNotesInd, "startDate=${startDate},endDate=${endDate}"), "");

        // potential readmissions
        AdmissionSoonAfterExitCohortDefinition readmission = new AdmissionSoonAfterExitCohortDefinition();
        readmission.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        readmission.addParameter(new Parameter("onOrBefore", "On or before", Date.class));

        CohortIndicator readmissionInd = buildIndicator("Possible Readmission", readmission, "onOrAfter=${startDate},onOrBefore=${endDate}");
        cohortDsd.addColumn("possiblereadmission", "Possible Readmission", map(readmissionInd, "startDate=${startDate},endDate=${endDate}"), "");

        return cohortDsd;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        return new ArrayList<ReportDesign>();
    }
}
