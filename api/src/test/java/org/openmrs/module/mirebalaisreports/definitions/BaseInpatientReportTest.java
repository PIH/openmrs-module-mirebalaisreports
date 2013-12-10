package org.openmrs.module.mirebalaisreports.definitions;

import org.junit.Before;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseInpatientReportTest extends BaseMirebalaisReportTest {

    protected Patient patient1;
    protected Patient patient2;
    protected Patient patient3;
    protected Patient patient4;
    protected Patient patient5;
    protected Patient patient6;

    @Autowired
    private ConceptService conceptService;

    @Before
    public void setUp() throws Exception {

        // Already admitted at start of 3 Oct (Women's Internal Medicine)
        patient1 = data.randomPatient().save();
        Visit visit1 = data.visit().patient(patient1).visitType(emrApiProperties.getAtFacilityVisitType())
                .started("2013-10-02 09:15:00").stopped("2013-10-14 04:30:00")
                .location(mirebalaisReportsProperties.getMirebalaisHospitalLocation()).save();
        Encounter enc1a = data.encounter().visit(visit1).encounterType(mirebalaisReportsProperties.getCheckInEncounterType())
                .location(mirebalaisReportsProperties.getOutpatientLocation()).encounterDatetime("2013-10-02 09:15:00").save();
        Encounter enc1b = data.encounter().visit(visit1).encounterType(mirebalaisReportsProperties.getAdmissionEncounterType())
                .location(mirebalaisReportsProperties.getWomensInternalMedicineLocation()).encounterDatetime("2013-10-02 12:30:00").save();

        // Admitted and discharged the day before. Visit extends into 3 Oct, but they've already been discharged at that point. Shouldn't be included in report.
        patient2 = data.randomPatient().save();
        Visit visit2 = data.visit().patient(patient2).visitType(emrApiProperties.getAtFacilityVisitType())
                .started("2013-10-01 17:30:00").stopped("2013-10-03 12:45:00")
                .location(mirebalaisReportsProperties.getMirebalaisHospitalLocation()).save();
        data.encounter().visit(visit2).encounterType(mirebalaisReportsProperties.getCheckInEncounterType())
                .location(mirebalaisReportsProperties.getOutpatientLocation()).encounterDatetime("2013-10-01 17:30:00").save();
        data.encounter().visit(visit2).encounterType(emrApiProperties.getAdmissionEncounterType())
                .location(mirebalaisReportsProperties.getWomensInternalMedicineLocation()).encounterDatetime("2013-10-01 18:30:00").save();
        data.encounter().visit(visit2).encounterType(emrApiProperties.getExitFromInpatientEncounterType())
                .location(mirebalaisReportsProperties.getWomensInternalMedicineLocation()).encounterDatetime("2013-10-02 23:45:00").save();

        // Admitted during the day of 3 Oct (Men's Internal Medicine)
        patient3 = data.randomPatient().save();
        Visit visit3a = data.visit().patient(patient3).visitType(emrApiProperties.getAtFacilityVisitType())
                .started("2013-10-03 12:34:00").stopped("2013-10-07 12:45:00")
                .location(mirebalaisReportsProperties.getMirebalaisHospitalLocation()).save();
        data.encounter().visit(visit3a).encounterType(mirebalaisReportsProperties.getCheckInEncounterType())
                .location(mirebalaisReportsProperties.getOutpatientLocation()).encounterDatetime("2013-10-03 12:34:00").save();
        data.encounter().visit(visit3a).encounterType(emrApiProperties.getAdmissionEncounterType())
                .location(mirebalaisReportsProperties.getMensInternalMedicineLocation()).encounterDatetime("2013-10-03 13:57:00").save();
        // This was a readmission, admitted and discharged the previous day
        Visit visit3b = data.visit().patient(patient3).visitType(emrApiProperties.getAtFacilityVisitType())
                .started("2013-10-02 12:34:00").stopped("2013-10-02 16:45:00")
                .location(mirebalaisReportsProperties.getMirebalaisHospitalLocation()).save();
        data.encounter().visit(visit3b).encounterType(mirebalaisReportsProperties.getAdmissionEncounterType())
                .location(mirebalaisReportsProperties.getMensInternalMedicineLocation()).encounterDatetime("2013-10-02 12:34:00").save();
        data.encounter().visit(visit3b).encounterType(emrApiProperties.getExitFromInpatientEncounterType())
                .location(mirebalaisReportsProperties.getMensInternalMedicineLocation()).encounterDatetime("2013-10-02 16:45:00").save();

        // Admitted earlier (Men's Internal Medicine), and discharged on 3 Oct
        patient4 = data.randomPatient().save();
        Visit visit4 = data.visit().patient(patient4).visitType(emrApiProperties.getAtFacilityVisitType())
                .started("2013-10-01 12:34:00").stopped("2013-10-03 12:45:00")
                .location(mirebalaisReportsProperties.getMirebalaisHospitalLocation()).save();
        data.encounter().visit(visit4).encounterType(mirebalaisReportsProperties.getCheckInEncounterType())
                .location(mirebalaisReportsProperties.getOutpatientLocation()).encounterDatetime("2013-10-01 12:34:00").save();
        data.encounter().visit(visit4).encounterType(emrApiProperties.getAdmissionEncounterType())
                .location(mirebalaisReportsProperties.getMensInternalMedicineLocation()).encounterDatetime("2013-10-01 13:57:00").save();
        data.encounter().visit(visit4).encounterType(emrApiProperties.getExitFromInpatientEncounterType())
                .location(mirebalaisReportsProperties.getMensInternalMedicineLocation()).encounterDatetime("2013-10-03 12:45:00").save();
        // consult with discharge
        Encounter encounter4a = data.encounter().visit(visit4).encounterType(emrApiProperties.getConsultEncounterType())
                .location(mirebalaisReportsProperties.getMensInternalMedicineLocation()).encounterDatetime("2013-10-03 12:15:00").save();
        Concept dispositionConcept = conceptService.getConceptByUuid("c8b22b09-e2f2-4606-af7d-e52579996de3");
        data.obs().encounter(encounter4a).concept(dispositionConcept).value("DISCHARGED", "PIH").save();

        // Begins the day of 3 Oct at Women's Inpatient (was admitted earlier), and transferred to Surgical Ward during the day
        patient5 = data.randomPatient().save();
        Visit visit5 = data.visit().patient(patient5).visitType(emrApiProperties.getAtFacilityVisitType())
                .started("2013-10-01 12:34:00").stopped("2013-10-13 12:45:00")
                .location(mirebalaisReportsProperties.getMirebalaisHospitalLocation()).save();
        data.encounter().visit(visit5).encounterType(mirebalaisReportsProperties.getCheckInEncounterType())
                .location(mirebalaisReportsProperties.getOutpatientLocation()).encounterDatetime("2013-10-01 12:34:00").save();
        data.encounter().visit(visit5).encounterType(emrApiProperties.getAdmissionEncounterType())
                .location(mirebalaisReportsProperties.getWomensInternalMedicineLocation()).encounterDatetime("2013-10-01 13:57:00").save();
        data.encounter().visit(visit5).encounterType(emrApiProperties.getTransferWithinHospitalEncounterType())
                .location(mirebalaisReportsProperties.getSurgicalWardLocation()).encounterDatetime("2013-10-03 12:45:00").save();

        //  Checks into ED during the day, transferred to surgery (with no admission), and has surgery
        patient6 = data.randomPatient().save();
        Visit visit6 = data.visit().patient(patient6).visitType(emrApiProperties.getAtFacilityVisitType())
                .started("2013-10-03 10:05:00").stopped("2013-10-03 18:32:21")
                .location(mirebalaisReportsProperties.getMirebalaisHospitalLocation()).save();
        data.encounter().visit(visit6).encounterType(mirebalaisReportsProperties.getCheckInEncounterType())
                .location(mirebalaisReportsProperties.getEmergencyLocation()).encounterDatetime("2013-10-03 10:05:00").save();
        data.encounter().visit(visit6).encounterType(emrApiProperties.getTransferWithinHospitalEncounterType())
                .location(mirebalaisReportsProperties.getSurgicalWardLocation()).encounterDatetime("2013-10-03 12:45:00").save();
        data.encounter().visit(visit6).encounterType(mirebalaisReportsProperties.getPostOpNoteEncounterType())
                .location(mirebalaisReportsProperties.getSurgicalWardLocation()).encounterDatetime("2013-10-03 14:53:00").save();
    }

}
