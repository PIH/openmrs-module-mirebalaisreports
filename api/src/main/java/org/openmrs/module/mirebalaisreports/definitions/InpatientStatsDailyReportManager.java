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
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.cohort.definition.AdmissionSoonAfterExitCohortDefinition;
import org.openmrs.module.mirebalaisreports.cohort.definition.InpatientLocationCohortDefinition;
import org.openmrs.module.mirebalaisreports.cohort.definition.InpatientTransferCohortDefinition;
import org.openmrs.module.mirebalaisreports.cohort.definition.LastDispositionBeforeExitCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
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

/**
 *
 */
@Component
public class InpatientStatsDailyReportManager extends BaseMirebalaisReportManager {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private AdtService adtService;

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.INPATIENT_STATS_DAILY_REPORT_DEFINITION_UUID;
    }

    @Override
    protected String getMessageCodePrefix() {
        return "mirebalaisreports.inpatientStatsDaily.";
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

        List<Location> inpatientLocations = adtService.getInpatientLocations();
        EncounterType admissionEncounterType = emrApiProperties.getAdmissionEncounterType();
        EncounterType transferWithinHospitalEncounterType = emrApiProperties.getTransferWithinHospitalEncounterType();
        EncounterType exitEncounterType = emrApiProperties.getExitFromInpatientEncounterType();

        Concept dischargedDisposition = conceptService.getConceptByMapping("DISCHARGED", "PIH");
        Concept deathDisposition = conceptService.getConceptByMapping("DEATH", "PIH");
        Concept transferOutDisposition = conceptService.getConceptByMapping("Transfer out of hospital", "PIH");
        Concept leftWithoutCompletionOfTreatmentDisposition = conceptService.getConceptByMapping("Departed without medical discharge", "PIH");
        Concept leftWithoutSeeingClinicianDisposition = conceptService.getConceptByMapping("Left without seeing a clinician", "PIH");
        List<Concept> dispositionsToConsider = Arrays.asList(dischargedDisposition, deathDisposition, transferOutDisposition, leftWithoutCompletionOfTreatmentDisposition, leftWithoutSeeingClinicianDisposition);
        // Dispositions we're currently ignoring: "Transfer within hospital", "Admit to hospital", "Discharged", "Emergency Department observation", "Home"

        ReportDefinition rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");
        rd.setUuid(getUuid());
        rd.setParameters(getParameters());

        CohortIndicatorDataSetDefinition cohortDsd = new CohortIndicatorDataSetDefinition();
        cohortDsd.addParameter(getStartDateParameter());
        cohortDsd.addParameter(getEndDateParameter());

        for (Location location : inpatientLocations) {

            // census at start, census at end

            InpatientLocationCohortDefinition censusCohortDef = new InpatientLocationCohortDefinition();
            censusCohortDef.addParameter(getEffectiveDateParameter());
            censusCohortDef.setWard(location);

            CohortIndicator censusStartInd = buildIndicator("Census at start: " + location.getName(), censusCohortDef, "effectiveDate=${startDate}");
            CohortIndicator censusEndInd = buildIndicator("Census at end: " + location.getName(), censusCohortDef, "effectiveDate=${endDate}");

            cohortDsd.addColumn("censusAtStart:" + location.getUuid(), "Census at start: " + location.getName(), map(censusStartInd, "startDate=${startDate}"), "");
            cohortDsd.addColumn("censusAtEnd:" + location.getUuid(), "Census at end: " + location.getName(), map(censusEndInd, "endDate=${endDate}"), "");

            // number of admissions

            EncounterCohortDefinition admissionDuring = new EncounterCohortDefinition();
            admissionDuring.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
            admissionDuring.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
            admissionDuring.addLocation(location);
            admissionDuring.addEncounterType(admissionEncounterType);

            CohortIndicator admissionInd = buildIndicator("Admission: " + location.getName(), admissionDuring, "onOrAfter=${startDate},onOrBefore=${endDate}");
            cohortDsd.addColumn("admissions:" + location.getUuid(), "Admission: " + location.getName(), map(admissionInd, "startDate=${startDate},endDate=${endDate}"), "");
            
            // number of transfer ins

            InpatientTransferCohortDefinition transferInDuring = new InpatientTransferCohortDefinition();
            transferInDuring.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
            transferInDuring.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
            transferInDuring.setInToWard(location);

            CohortIndicator transferInInd = buildIndicator("Transfer In: " + location.getName(), transferInDuring, "onOrAfter=${startDate},onOrBefore=${endDate}");
            cohortDsd.addColumn("transfersIn:" + location.getUuid(), "Transfer In: " + location.getName(), map(transferInInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number of transfer outs

            InpatientTransferCohortDefinition transferOutDuring = new InpatientTransferCohortDefinition();
            transferOutDuring.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
            transferOutDuring.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
            transferOutDuring.setOutOfWard(location);

            CohortIndicator transferOutInd = buildIndicator("Transfer Out: " + location.getName(), transferOutDuring, "onOrAfter=${startDate},onOrBefore=${endDate}");
            cohortDsd.addColumn("transfersOut:" + location.getUuid(), "Transfer Out: " + location.getName(), map(transferOutInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number of discharges

            LastDispositionBeforeExitCohortDefinition discharged = new LastDispositionBeforeExitCohortDefinition();
            discharged.addParameter(new Parameter("exitOnOrAfter", "Exit on or after", Date.class));
            discharged.addParameter(new Parameter("exitOnOrBefore", "Exit on or before", Date.class));
            discharged.setExitFromWard(location);
            discharged.setDispositionsToConsider(dispositionsToConsider);
            discharged.addDisposition(dischargedDisposition);

            CohortIndicator dischargedInd = buildIndicator("Discharged: " + location.getName(), discharged, "exitOnOrAfter=${startDate},exitOnOrBefore=${endDate}");
            cohortDsd.addColumn("discharged:" + location.getUuid(), "Discharged: " + location.getName(), map(dischargedInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number of deaths

            LastDispositionBeforeExitCohortDefinition deaths = new LastDispositionBeforeExitCohortDefinition();
            deaths.addParameter(new Parameter("exitOnOrAfter", "Exit on or after", Date.class));
            deaths.addParameter(new Parameter("exitOnOrBefore", "Exit on or before", Date.class));
            deaths.setExitFromWard(location);
            deaths.setDispositionsToConsider(dispositionsToConsider);
            deaths.addDisposition(deathDisposition);

            CohortIndicator deathsInd = buildIndicator("Deaths: " + location.getName(), deaths, "exitOnOrAfter=${startDate},exitOnOrBefore=${endDate}");
            cohortDsd.addColumn("deaths:" + location.getUuid(), "Deaths: " + location.getName(), map(deathsInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number transferred out of HUM

            LastDispositionBeforeExitCohortDefinition transfersOut = new LastDispositionBeforeExitCohortDefinition();
            transfersOut.addParameter(new Parameter("exitOnOrAfter", "Exit on or after", Date.class));
            transfersOut.addParameter(new Parameter("exitOnOrBefore", "Exit on or before", Date.class));
            transfersOut.setExitFromWard(location);
            transfersOut.setDispositionsToConsider(dispositionsToConsider);
            transfersOut.addDisposition(transferOutDisposition);

            CohortIndicator transfersOutInd = buildIndicator("Transfer Outs: " + location.getName(), transfersOut, "exitOnOrAfter=${startDate},exitOnOrBefore=${endDate}");
            cohortDsd.addColumn("transfersOutOfHUM:" + location.getUuid(), "Transfer Outs: " + location.getName(), map(transfersOutInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number left without completing treatment

            LastDispositionBeforeExitCohortDefinition leftWithoutCompletingTreatment = new LastDispositionBeforeExitCohortDefinition();
            leftWithoutCompletingTreatment.addParameter(new Parameter("exitOnOrAfter", "Exit on or after", Date.class));
            leftWithoutCompletingTreatment.addParameter(new Parameter("exitOnOrBefore", "Exit on or before", Date.class));
            leftWithoutCompletingTreatment.setExitFromWard(location);
            leftWithoutCompletingTreatment.setDispositionsToConsider(dispositionsToConsider);
            leftWithoutCompletingTreatment.addDisposition(leftWithoutCompletionOfTreatmentDisposition);

            CohortIndicator leftWithoutCompletingTreatmentInd = buildIndicator("Left Without Completing Treatment: " + location.getName(), leftWithoutCompletingTreatment, "exitOnOrAfter=${startDate},exitOnOrBefore=${endDate}");
            cohortDsd.addColumn("leftWithoutCompletingTx:" + location.getUuid(), "Left Without Completing Treatment: " + location.getName(), map(leftWithoutCompletingTreatmentInd, "startDate=${startDate},endDate=${endDate}"), "");

            // number left without completing treatment

            LastDispositionBeforeExitCohortDefinition leftWithoutSeeingClinician = new LastDispositionBeforeExitCohortDefinition();
            leftWithoutSeeingClinician.addParameter(new Parameter("exitOnOrAfter", "Exit on or after", Date.class));
            leftWithoutSeeingClinician.addParameter(new Parameter("exitOnOrBefore", "Exit on or before", Date.class));
            leftWithoutSeeingClinician.setExitFromWard(location);
            leftWithoutSeeingClinician.setDispositionsToConsider(dispositionsToConsider);
            leftWithoutSeeingClinician.addDisposition(leftWithoutSeeingClinicianDisposition);

            CohortIndicator leftWithoutSeeingClinicianInd = buildIndicator("Left Without Seeing Clinician: " + location.getName(), leftWithoutSeeingClinician, "exitOnOrAfter=${startDate},exitOnOrBefore=${endDate}");
            cohortDsd.addColumn("leftWithoutSeeingClinician:" + location.getUuid(), "Left Without Seeing Clinician: " + location.getName(), map(leftWithoutSeeingClinicianInd, "startDate=${startDate},endDate=${endDate}"), "");

            // length of stay of patients who exited from inpatient (by ward, and in the ER)

            // admissions within 48 hours of previous exit
        }

        // number of ED check-ins
        // TODO change this to count by visits or by encounters, instead of by patients
        EncounterCohortDefinition edCheckIn = new EncounterCohortDefinition();
        edCheckIn.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        edCheckIn.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
        edCheckIn.addEncounterType(mirebalaisReportsProperties.getCheckInEncounterType());
        edCheckIn.addLocation(mirebalaisReportsProperties.getEmergencyLocation());
        edCheckIn.addLocation(mirebalaisReportsProperties.getEmergencyReceptionLocation());

        CohortIndicator edCheckInInd = buildIndicator("ED Check In", edCheckIn, "onOrAfter=${startDate},onOrBefore=${endDate}");
        cohortDsd.addColumn("edcheckin", "ED Check In", map(edCheckInInd, "startDate=${startDate},endDate=${endDate}"), "");

        // number of surgical op-notes entered
        EncounterCohortDefinition surgicalNotes = new EncounterCohortDefinition();
        surgicalNotes.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        surgicalNotes.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
        surgicalNotes.addEncounterType(mirebalaisReportsProperties.getPostOpNoteEncounterType());

        CohortIndicator surgicalNotesInd = buildIndicator("OR Volume", surgicalNotes, "onOrAfter=${startDate},onOrBefore=${endDate}");
        cohortDsd.addColumn("orvolume", "OR Volume", map(surgicalNotesInd, "startDate=${startDate},endDate=${endDate}"), "");

        // potential readmissions
        AdmissionSoonAfterExitCohortDefinition readmission = new AdmissionSoonAfterExitCohortDefinition();
        readmission.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        readmission.addParameter(new Parameter("onOrBefore", "On or before", Date.class));

        CohortIndicator readmissionInd = buildIndicator("Possible Readmission", readmission, "onOrAfter=${startDate},onOrBefore=${endDate}");
        cohortDsd.addColumn("possiblereadmission", "Possible Readmission", map(readmissionInd, "startDate=${startDate},endDate=${endDate}"), "");

        rd.addDataSetDefinition("cohorts", map(cohortDsd, "startDate=${day},endDate=${day+1d-1s}"));

        return rd;
    }

    private Parameter getEffectiveDateParameter() {
        return new Parameter("effectiveDate", "mirebalaisreports.parameter.effectiveDate", Date.class);
    }

    private CohortIndicator buildIndicator(String name, CohortDefinition cd, String mappings) {
        CohortIndicator indicator = new CohortIndicator(name);
        indicator.addParameter(getStartDateParameter());
        indicator.addParameter(getEndDateParameter());
        indicator.addParameter(getLocationParameter());
        indicator.setCohortDefinition(map(cd, mappings));
        return indicator;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        // At present we aren't maintaining an excel template for this, because we don't intend for it to be downloaded.
        // If we commit to that decision, then remove this commented out code, as well as the xls file.
        //try {
        //    return Arrays.asList(xlsReportDesign(reportDefinition, getBytesForResource("org/openmrs/module/mirebalaisreports/reportTemplates/InpatientStatsDailyReport.xls")));
        //} catch (IOException e) {
        //    throw new IllegalStateException("Unable to load excel template", e);
        //}
        return Arrays.asList(xlsReportDesign(reportDefinition, null));
    }

}
