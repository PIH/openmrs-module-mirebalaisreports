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

package org.openmrs.module.mirebalaisreports;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.Provider;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.emr.EmrProperties;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.haiticore.metadata.HaitiPatientIdentifierTypes;
import org.openmrs.module.pihcore.metadata.core.EncounterTypes;
import org.openmrs.module.pihcore.metadata.core.OrderTypes;
import org.openmrs.module.pihcore.metadata.core.program.ZikaProgram;
import org.openmrs.module.pihcore.metadata.haiti.PihHaitiPatientIdentifierTypes;
import org.openmrs.module.pihcore.metadata.mexico.MexicoEncounterTypes;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TODO: Consider whether these should all be moved into the mirebalaismetadata module / emrapi module
 */
@Component("mirebalaisReportsProperties")
public class MirebalaisReportsProperties extends EmrProperties {

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    protected ProgramWorkflowService programWorkflowService;


    //***** DATE FORMATS ******
    public static final String DATE_FORMAT = "dd MMM yyyy";
    public static final String DATETIME_FORMAT= "dd MMM yyyy hh:mm aa";
    public static final String TIME_FORMAT = "hh:mm aa";


    //***** PRIVILEGES ******
    public static final String PRIVILEGE_PATIENT_DASHBOARD = "App: coreapps.patientDashboard";

    //***** GLOBAL PROPERTIES ******
    public static final String DAILY_CHECKINS_HIDE_COUNTS= "mirebalaisreports.dailyCheckins.hideCounts";

    //***** REPORT DEFINITIONS *****
    public static final String FULL_DATA_EXPORT_REPORT_DEFINITION_UUID = "8c3752e2-20bb-11e3-b5bd-0bec7fb71852";
    public static final String DASHBOARD_DATA_EXPORT_REPORT_DEFINITION_UUID = "6d9b292a-2aad-11e3-a840-5b9e0b589afb";
    public static final String RADIOLOGY_DATA_EXPORT_REPORT_DEFINITION_UUID = "9e7dc296-2aad-11e3-a840-5b9e0b589afb";
    public static final String RADIOLOGY_CONSOIDATED_DATA_EXPORT_REPORT_DEFINITION_UUID = "3264db16-d5dd-46b8-ad1e-99085bca0064";
    public static final String SURGERY_DATA_EXPORT_REPORT_DEFINITION_UUID = "a3c9a17a-2aad-11e3-a840-5b9e0b589afb";
    public static final String HOSPITALIZATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID = "bfa1b522-2aad-11e3-a840-5b9e0b589afb";
    public static final String CONSULTATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID = "c427f48a-2aad-11e3-a840-5b9e0b589afb";
    public static final String PATIENTS_DATA_EXPORT_REPORT_DEFINITION_UUID = "d9436988-4cc9-11e3-9325-f3ae8db9f6a7";
    public static final String ENCOUNTERS_DATA_EXPORT_REPORT_DEFINITION_UUID = "f35033c8-8469-11e3-aca2-080027ab5716";
    public static final String ALL_PATIENTS_WITH_IDS_REPORT_DEFINITION_UUID = "d534683e-20bd-11e3-b5bd-0bec7fb71852";
    public static final String LQAS_DIAGNOSES_REPORT_DEFINITION_UUID = "f277f5b4-20bd-11e3-b5bd-0bec7fb71852";
    public static final String NON_CODED_DIAGNOSES_REPORT_DEFINITION_UUID = "3737be52-2265-11e3-818c-c7ea4184d59e";
    public static final String BASIC_STATISTICS_REPORT_DEFINITION_UUID = "5650dbc4-2266-11e3-818c-c7ea4184d59e";
    public static final String INPATIENT_STATS_DAILY_REPORT_DEFINITION_UUID = "f3bb8094-3738-11e3-b90a-a351ac6b1528";
    public static final String INPATIENT_STATS_MONTHLY_REPORT_DEFINITION_UUID = "b6addbae-89ed-11e3-af23-f07d9ea14ea1";
    public static final String INPATIENT_LIST_REPORT_DEFINITION_UUID = "2fecbf2e-52e9-11e3-a1a7-68525fe6674f";
    public static final String DAILY_REGISTRATIONS_REPORT_DEFINITION_UUID = "2e91bd04-4c7a-11e3-9325-f3ae8db9f6a7";
    public static final String DAILY_CLINICAL_ENCOUNTERS_REPORT_DEFINITION_UUID = "5dd60b6c-4d45-11e3-9325-f3ae8db9f6a7";
    public static final String DAILY_CHECK_INS_REPORT_DEFINITION_UUID = "f170699a-50af-11e3-ba00-27a0ac7f78d9";
    public static final String DISPENSING_DATA_EXPORT_REPORT_DEFINITION_UUID = "8b2f46e0-5d13-11e3-949a-0800200c9a66";
    public static final String VISITS_DATA_EXPORT_REPORT_DEFINITION_UUID = "fa46aee9-fc73-11e3-8248-08002769d9ae";
    public static final String VITALS_DATA_EXPORT_REPORT_DEFINITION_UUID = "09c9f9ee-fc74-11e3-8248-08002769d9ae";
    public static final String CHECKINS_DATA_EXPORT_REPORT_DEFINITION_UUID = "1c72b461-fc74-11e3-8248-08002769d9ae";
    public static final String DIAGNOSES_DATA_EXPORT_REPORT_DEFINITION_UUID = "257bb3e9-fc74-11e3-8248-08002769d9ae";
    public static final String USERS_AND_PROVIDERS_REPORT_DEFINITION_UUID = "e4d1d6b0-642d-11e3-949a-0800200c9a66";
    public static final String APPOINTMENTS_REPORT_DEFINITION_UUID = "7fd11020-e5d1-11e3-ac10-0800200c9a66";
    public static final String REGISTRATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID = "a4f410a8-f8cc-11e4-a7d0-e82aea237783";
    public static final String LAB_RESULTS_EXPORT_DEFINITION_UUID = "9BCEEAE9-C804-499E-AA78-1F03FE937637";
    public static final String LAB_ORDERS_DATA_EXPORT_REPORT_DEFINITION_UUID = "6E90A739-CB1F-4042-BC11-6548779881C9";
    public static final String ONCOLOGY_DATA_EXPORT_REPORT_DEFINITION_UUID = "202e6ee0-45d6-11e5-b970-0800200c9a66";
    public static final String NCD_DATA_EXPORT_REPORT_DEFINITION_UUID = "13629220-6b9e-11e5-a837-0800200c9a66";
    public static final String NCD_DATA_PROGRAM_EXPORT_REPORT_DEFINITION_UUID = "1de224fc-82d4-40cc-be07-f2e95ed2e452";
    public static final String MENTAL_HEALTH_DATA_EXPORT_REPORT_DEFINITION_UUID = "d2e7c170-e703-11e5-a837-0800200c9a66";
    public static final String MENTAL_HEALTH_PROGRAM_DATA_EXPORT_REPORT_DEFINITION_UUID = "e2d8b170-e793-10e5-a837-0888200c9a66";
    public static final String ED_TRIAGE_DATA_EXPORT_REPORT_DEFINITION_UUID = "87588ec0-4deb-11e6-bdf4-0800200c9a66";
    public static final String SOCIAL_ECONOMICS_DATA_EXPORT_REPORT_DEFINITION_UUID = "c40bf377-5954-481e-b58b-bffc26052d45";
    public static final String VISIT_NOTE_DATA_EXPORT_REPORT_DEFINITION_UUID = "228BCF82-D496-49B0-929B-5B3C6AF3767E";
    public static final String BILLABLE_EXPORT_REPORT_DEFINITION_UUID = "6bd65742-5b45-4f50-a5a8-90c81a387f90";
    public static final String ALLERGIES_EXPORT_REPORT_DEFINITION_UUID = "3b83bbd7-f16a-4df1-9ba8-280c0e4ea977";
    public static final String PATHOLOGY_EXPORT_REPORT_DEFINITION_UUID = "7877eef3-a3bb-4efc-a9ef-3582378053ca";
    public static final String RELATIONSHIPS_REPORT_DEFINITION_UUID = "f42366c3-388d-47f7-93ff-a64270a613eb";
    public static final String WEEKLY_MONITORING_REPORT_DEFINITION_UUID = "bfac0d81-09fc-4981-adf2-d6fa9e5ec852";
    public static final String NEW_DISEASE_EPISODES_REPORTING_DEFINITION_UUID = "172163ed-c9eb-418c-bc32-f32a861ee7c9";
    public static final String ACCOUNTING_REPORTING_DEFINITION_UUID = "e0c7b080-d63f-414d-92d2-05078490ea89";
    public static final String MORBIDITY_REGISTRY_REPORTING_DEFINITION_UUID = "9d77c3bf-19c8-484d-af94-e2f2bb487797";
    public static final String VISIT_REGISTRY_REPORTING_DEFINITION_UUID = "4f4dbafc-eda6-4910-b9aa-b7be62bef49f";
    public static final String PROGRAMS_REPORT_DEFINITION_UUID = "345556d3-0975-49c5-9123-e712f8ccbe99";
    public static final String ZIKA_REPORT_DEFINITION_UUID = "c6bafa69-1745-4838-9171-cffaacf8fb1d";
    public static final String CHRONIC_MALADIES_REPORTING_DEFINITION_UUID = "66a975d4-790e-4348-9a88-8dfbcf551afc";
    public static final String HIV_SUMMARY_REPORT_DEFINITION_UUID = "bccfa83d-3c28-42ea-9240-941aa93a2cce";
    public static final String PATIENTS_WITH_FINGERPRINTS_DEFINITION_UUID = "bf1fbee4-0a35-4d7c-9054-6681f3c06463";
    public static final String VCT_REPORT_DEFINITION_UUID = "8189ccd0-cd86-11e8-a8d5-f2801f1b9fd1";
    public static final String MCH_PROVIDER_REPORT_DEFINITION_UUID = "6a35bb00-e2d2-11e8-9f32-f2801f1b9fd1";
    public static final String MCH_REPORT_DEFINITION_UUID = "85656ac4-0faa-11e9-ab14-d663bd873d93";
    public static final String MCH_J9_CASE_REGISTRATION_REPORT_DEFINITION_UUID = "ed980032-3c77-11ea-8806-0242ac110002";
    public static final String MEXICO_VISITS_REPORT_DEFINITION_UUID = "b1d80ad0-f9a2-46c6-a89f-0050d7d724cb";
    public static final String MEXICO_SUIVE_SIMPLE_REPORT_DEFINITION_UUID = "620b25e7-c1f4-43d4-ba8d-944973425597";
    public static final String MEXICO_SIS_VISITS_REPORT_DEFINITION_UUID = "171fafba-e39e-4054-a1cc-a285683e68f8";
    public static final String MEXICO_SIS_DIAGNOSES_REPORT_DEFINITION_UUID = "6d271d1c-26c5-4ee5-989d-2316dea1a561";
    public static final String MEXICO_CES_MEDS_REPORT_DEFINITION_UUID = "f03e1621-8801-47c9-a845-3839ba44fa03";
    public static final String MEXICO_SSA_MEDS_REPORT_DEFINITION_UUID = "84ed6470-84f8-4cf6-ba23-bb27c7e4fa76";

    //***** SCHEDULED REPORT REQUESTS *****
    public static final String ALL_PATIENTS_SCHEDULED_REPORT_REQUEST_UUID = "733cd7c0-2ed0-11e4-8c21-0800200c9a66";
    public static final String APPOINTMENTS_SCHEDULED_REPORT_REQUEST_UUID = "f19ff350-2ed9-11e4-8c21-0800200c9a66";
    public static final String CHECKINS_DATA_EXPORT_SCHEDULED_REPORT_REQUEST_UUID = "f9e01270-2ed9-11e4-8c21-0800200c9a66";
    public static final String FULL_DATA_EXPORT_SCHEDULED_REPORT_REQUEST_UUID = "2619c140-5b0e-11e5-a837-0800200c9a66";

	//***** LOCATIONS *****

	public static final String OUTPATIENT_CLINIC_UUID = "199e7d87-92a0-4398-a0f8-11d012178164";
    public static final String WOMEN_CLINIC_UUID = "9b2066a2-7087-47f6-9b3a-b001037432a3";
    public static final String EMERGENCY_DEPARTMENT_UUID = "f3a5586e-f06c-4dfb-96b0-6f3451a35e90";
    public static final String EMERGENCY_RECEPTION_UUID = "afa09010-43b6-4f19-89e0-58d09941bcbd";
    public static final String MIREBALAIS_HOSPITAL_UUID = "a084f714-a536-473b-94e6-ec317b152b43";
    public static final String CLINIC_REGISTRATION_UUID = "787a2422-a7a2-400e-bdbb-5c54b2691af5";
    public static final String WOMENS_INTERNAL_MEDICINE_UUID = "2c93919d-7fc6-406d-a057-c0b640104790";
    public static final String MENS_INTERNAL_MEDICINE_UUID = "e5db0599-89e8-44fa-bfa2-07e47d63546f";
    public static final String SURGICAL_WARD_UUID = "7d6cc39d-a600-496f-a320-fd4985f07f0b";
    public static final String ANTEPARTUM_WARD_UUID = "272bd989-a8ee-4a16-b5aa-55bad4e84f5c";
    public static final String LABOR_AND_DELIVERY_UUID = "dcfefcb7-163b-47e5-84ae-f715cf3e0e92";
    public static final String POSTPARTUM_WARD_UUID = "950852f3-8a96-4d82-a5f8-a68a92043164";
    public static final String NEONATAL_ICU_UUID = "62a9500e-a1a5-4235-844f-3a8cc0765d53";

    public Location getOutpatientLocation() {
		return getLocationByUuid(OUTPATIENT_CLINIC_UUID);
	}

	public Location getWomenLocation() {
		return getLocationByUuid(WOMEN_CLINIC_UUID);
	}

    public Location getEmergencyLocation() {
        return getLocationByUuid(EMERGENCY_DEPARTMENT_UUID);
    }

    public Location getEmergencyReceptionLocation() {
        return getLocationByUuid(EMERGENCY_RECEPTION_UUID);
    }

    public Location getMirebalaisHospitalLocation() {
        return getLocationByUuid(MIREBALAIS_HOSPITAL_UUID);
    }

    public Location getClinicRegistrationLocation() {
        return getLocationByUuid(CLINIC_REGISTRATION_UUID);
    }

    public Location getWomensInternalMedicineLocation() {
        return getLocationByUuid(WOMENS_INTERNAL_MEDICINE_UUID);
    }

    public Location getMensInternalMedicineLocation() {
        return getLocationByUuid(MENS_INTERNAL_MEDICINE_UUID);
    }

    public Location getSurgicalWardLocation() {
        return getLocationByUuid(SURGICAL_WARD_UUID);
    }

    public Location getAntepartumWardLocation() {
        return getLocationByUuid(ANTEPARTUM_WARD_UUID);
    }

    public Location getLaborAndDeliveryLocation() {
        return getLocationByUuid(LABOR_AND_DELIVERY_UUID);
    }

    public Location getPostpartumWardLocation() {
        return getLocationByUuid(POSTPARTUM_WARD_UUID);
    }

    public Location getNeonatalIcuLocation() {
        return getLocationByUuid(NEONATAL_ICU_UUID);
    }

    private Location getLocationByUuid(String uuid) {
		return locationService.getLocationByUuid(uuid);
	}

    public List<Provider> getAllProviders(){
        List<Provider> providers = providerService.getAllProviders(true);
        if (providers != null && providers.size() > 0){
            Collections.sort(providers, new Comparator<Provider>() {
                @Override
                public int compare(Provider p1, Provider p2) {
                    return OpenmrsUtil.compareWithNullAsGreatest(p1.getName(), p2.getName());
                }
            });
        }
        return providers;
    }


	//***** IDENTIFIER TYPES *****


    public static final String DOSSIER_NUMBER_UUID = "e66645eb-03a8-4991-b4ce-e87318e37566";

    public static final String HIV_EMR_ID_UUID = "139766e8-15f5-102d-96e4-000c29c2a5d7";

    public PatientIdentifierType getZlEmrIdentifierType() {
		return getIdentifierTypeByUuid(PihHaitiPatientIdentifierTypes.ZL_EMR_ID.uuid());
	}

	public PatientIdentifierType getDossierNumberIdentifierType() {
		return getIdentifierTypeByUuid(PihHaitiPatientIdentifierTypes.DOSSIER_NUMBER.uuid());
	}


	public PatientIdentifierType getHivEmrIdentifierType() {
		return getIdentifierTypeByUuid(PihHaitiPatientIdentifierTypes.HIVEMR_V1.uuid());
	}

    public PatientIdentifierType getUserEnteredReferenceNumberIdentifierType() {
        return getIdentifierTypeByUuid(PihHaitiPatientIdentifierTypes.USER_ENTERED_REF_NUMBER.uuid());
    }


    public PatientIdentifierType getBiometricIdentifierType() {
        return getIdentifierTypeByUuid(HaitiPatientIdentifierTypes.BIOMETRIC_REF_NUMBER.uuid());
    }

	private PatientIdentifierType getIdentifierTypeByUuid(String uuid) {
		return patientService.getPatientIdentifierTypeByUuid(uuid);
	}

	//***** PERSON ATTRIBUTE TYPES

	public static final String TEST_PERSON_ATTRIBUTE_UUID = "4f07985c-88a5-4abd-aa0c-f3ec8324d8e7";
    public static final String TELEPHONE_PERSON_ATTRIBUTE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
    public static final String UNKNOWN_PATIENT_PERSON_ATTRIBUTE_UUID = "8b56eac7-5c76-4b9c-8c6f-1deab8d3fc47";

	public PersonAttributeType getTestPatientPersonAttributeType() {
		return getPersonAttributeTypeByUuid(TEST_PERSON_ATTRIBUTE_UUID);
	}

    public PersonAttributeType getTelephoneNumberPersonAttributeType() {
        return getPersonAttributeTypeByUuid(TELEPHONE_PERSON_ATTRIBUTE_UUID);
    }

    public PersonAttributeType getUnknownPatientPersonAttributeType() {
        return getPersonAttributeTypeByUuid(UNKNOWN_PATIENT_PERSON_ATTRIBUTE_UUID);
    }

	private PersonAttributeType getPersonAttributeTypeByUuid(String uuid) {
		return personService.getPersonAttributeTypeByUuid(uuid);
	}


	//***** ENCOUNTER TYPES *****

	public EncounterType getRegistrationEncounterType() {
		return getEncounterTypeByUuid(EncounterTypes.PATIENT_REGISTRATION.uuid());
	}

	public EncounterType getCheckInEncounterType() {
		return getEncounterTypeByUuid(EncounterTypes.CHECK_IN.uuid());
	}

	public EncounterType getPaymentEncounterType() {
		return getEncounterTypeByUuid(EncounterTypes.PAYMENT.uuid());
	}

	public EncounterType getVitalsEncounterType() {
		return getEncounterTypeByUuid(EncounterTypes.VITALS.uuid());
	}

	public EncounterType getConsultEncounterType() {
		return getEncounterTypeByUuid(EncounterTypes.CONSULTATION.uuid());
	}

	public EncounterType getRadiologyOrderEncounterType() {
		return getEncounterTypeByUuid(EncounterTypes.RADIOLOGY_ORDER.uuid());
	}

    public EncounterType getRadiologyStudyEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.RADIOLOGY_STUDY.uuid());
    }

    public EncounterType getRadiologyReportEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.RADIOLOGY_REPORT.uuid());
    }

    public EncounterType getPostOpNoteEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.POST_OPERATIVE_NOTE.uuid());
    }

    public EncounterType getVCTEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.VCT.uuid());
    }

    public EncounterType getANCInitialEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.ANC_INTAKE.uuid());
    }

    public EncounterType getANCFollowupEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.ANC_FOLLOWUP.uuid());
    }

    public EncounterType getDeliveryEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.MCH_DELIVERY.uuid());
    }

    public EncounterType getAdmissionEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.ADMISSION.uuid());
    }

    public EncounterType getExitFromInpatientEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.EXIT_FROM_CARE.uuid());
    }

    public EncounterType getTransferEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.TRANSFER.uuid());
    }

    public EncounterType getLabResultsEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.LAB_RESULTS.uuid());
    }

    public EncounterType getOncologyEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.ONCOLOGY_CONSULT.uuid());
    }

    public EncounterType getOncologyIntakeEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.ONCOLOGY_INITIAL_VISIT.uuid());
    }

    public EncounterType getChemotherapyEncounterType() {
	    return getEncounterTypeByUuid(EncounterTypes.CHEMOTHERAPY_SESSION.uuid());
    }

    public EncounterType getMentalHealthAssessmentEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.MENTAL_HEALTH_ASSESSMENT.uuid());
    }

    public EncounterType getEDTriageEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.EMERGENCY_TRIAGE.uuid());
    }

    public EncounterType getPedsInitialConsultEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.PRIMARY_CARE_PEDS_INITIAL_CONSULT.uuid());
    }

    public EncounterType getPedsFollowupConsultEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.PRIMARY_CARE_PEDS_FOLLOWUP_CONSULT.uuid());
    }

    public EncounterType getAdultInitialConsultEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.PRIMARY_CARE_ADULT_INITIAL_CONSULT.uuid());
    }

    public EncounterType getAdultFollowupConsultEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.PRIMARY_CARE_ADULT_FOLLOWUP_CONSULT.uuid());
    }

    public EncounterType getNCDConsultEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.NCD_INITIAL_CONSULT.uuid());
    }

    public EncounterType getNCDInitialEncounterType() {
	    return getEncounterTypeByUuid(EncounterTypes.NCD_INITIAL_CONSULT.uuid());
    }

    public EncounterType getNCDFollowupEncounterType() {
        return getEncounterTypeByUuid(EncounterTypes.NCD_FOLLOWUP_CONSULT.uuid());
    }

    public EncounterType getMedicationDispensedEncountertype() {
	    return getEncounterTypeByUuid(EncounterTypes.MEDICATION_DISPENSED.uuid());
    }

    public EncounterType getMexicoConsultEncounterType() {
	    return getEncounterTypeByUuid(MexicoEncounterTypes.MEXICO_CONSULT.uuid());
    }

    /**
	 * @return all encounter types <em>except for</em> Registration, Payment, and Check-In
	 */
	@Transactional(readOnly = true)
	public List<EncounterType> getClinicalEncounterTypes() {
		List<EncounterType> encounterTypes = encounterService.getAllEncounterTypes(false);
		encounterTypes.remove(getRegistrationEncounterType());
		encounterTypes.remove(getPaymentEncounterType());
		encounterTypes.remove(getCheckInEncounterType());
		return encounterTypes;
	}

	/**
	 * @return all encounter types <em>except for</em> Registration
	 */
	@Transactional(readOnly = true)
	public List<EncounterType> getVisitEncounterTypes() {
		List<EncounterType> encounterTypes = encounterService.getAllEncounterTypes(false);
		encounterTypes.remove(getRegistrationEncounterType());
		return encounterTypes;
	}

	private EncounterType getEncounterTypeByUuid(String uuid) {
		return encounterService.getEncounterTypeByUuid(uuid);
	}

    //***** CONCEPT SOURCES *****

	public static final String ICD10_CONCEPT_SOURCE_UUID = "3f65bd34-26fe-102b-80cb-0017a47871b2";

	public ConceptSource getIcd10ConceptSource() {
		return conceptService.getConceptSourceByUuid(ICD10_CONCEPT_SOURCE_UUID);
	}

	public ConceptSource getPihConceptSource() {
		return conceptService.getConceptSourceByName("PIH");
	}

	public ConceptSource getMirebalaisReportsConceptSource() {
		return conceptService.getConceptSourceByName("org.openmrs.module.mirebalaisreports");
	}

    public Concept getAdmissionLocationConcept() {
        return dispositionService.getDispositionDescriptor().getAdmissionLocationConcept();
    }

    public Concept getInternalTransferLocationConcept() {
        return dispositionService.getDispositionDescriptor().getInternalTransferLocationConcept();
    }

    public Concept getDispositionConcept() {
        return dispositionService.getDispositionDescriptor().getDispositionConcept();
    }

    public Concept getAdmissionDispositionConcept() {
        return getConceptForDisposition("admitToHospital");
    }

    public Concept getDischargeDispositionConcept() {
        return getConceptForDisposition("discharge");
    }

    public Concept getLeftWithoutSeeingClinicianDispositionConcept() {
        return getConceptForDisposition("leftWithoutSeeingAClinician");
    }

    public Concept getLeftWithoutCompletingTreatmentDispositionConcept() {
        return getConceptForDisposition("leftWithoutCompletionOfTreatment");
    }

    public Concept getTransferOutOfHospitalDispositionConcept() {
        return getConceptForDisposition("transferOutOfHospital");
    }

    public Concept getTransferWithinHospitalDispositionConcept() {
        return getConceptForDisposition("transferWithinHospital");
    }

    public Concept getStillHospitalizedDispositionConcept() {
        return getConceptForDisposition("stillHospitalized");
    }

    public Concept getEdObservationDispositionConcept() {
        return getConceptForDisposition("edObservation");
    }

    public Concept getDeathDispositionConcept() {
        return getConceptForDisposition("markPatientDead");
    }

    private Concept getConceptForDisposition(String dispositionCode) {
        Concept conceptForDisposition = null;
        Disposition disposition = dispositionService.getDispositionByUniqueId(dispositionCode);
        if (disposition != null) {
            String conceptCode = disposition.getConceptCode();
            if (StringUtils.isNotBlank(conceptCode)) {
                String[] conceptMap = conceptCode.split(":");
                if ((conceptMap != null) && (conceptMap.length == 2)) {
                    conceptForDisposition = conceptService.getConceptByMapping(conceptMap[1], conceptMap[0]);
                }
            }
        }
        return conceptForDisposition;
    }

	//***** CONCEPTS *****

	public static final String AMOUNT_PATIENT_PAID_CONCEPT_UUID  = "5d1bc5de-6a35-4195-8631-7322941fe528";

	public Concept getAmountPaidConcept() {
		return getConceptByUuid(AMOUNT_PATIENT_PAID_CONCEPT_UUID);
	}

    public static final String REASON_FOR_VISIT_CONCEPT_UUID  = "e2964359-790a-419d-be53-602e828dcdb9";

    public Concept getReasonForVisitConcept() {
        return getConceptByUuid(REASON_FOR_VISIT_CONCEPT_UUID);
    }

	public static final String WEIGHT_CONCEPT_UUID = "3ce93b62-26fe-102b-80cb-0017a47871b2";

	public Concept getWeightConcept() {
		return getConceptByUuid(WEIGHT_CONCEPT_UUID);
	}

	public static final String HEIGHT_CONCEPT_UUID = "3ce93cf2-26fe-102b-80cb-0017a47871b2";

	public Concept getHeightConcept() {
		return getConceptByUuid(HEIGHT_CONCEPT_UUID);
	}

	public static final String MUAC_CONCEPT_UUID = "e3e03a93-de7f-41ea-b8f2-60b220b970e9";

	public Concept getMuacConcept() {
		return getConceptByUuid(MUAC_CONCEPT_UUID);
	}

	public static final String TEMPERATURE_CONCEPT_UUID = "3ce939d2-26fe-102b-80cb-0017a47871b2";

	public Concept getTemperatureConcept() {
		return getConceptByUuid(TEMPERATURE_CONCEPT_UUID);
	}

	public static final String PULSE_CONCEPT_UUID = "3ce93824-26fe-102b-80cb-0017a47871b2";

	public Concept getPulseConcept() {
		return getConceptByUuid(PULSE_CONCEPT_UUID);
	}

	public static final String RESPIRATORY_RATE_CONCEPT_UUID = "3ceb11f8-26fe-102b-80cb-0017a47871b2";

	public Concept getRespiratoryRateConcept() {
		return getConceptByUuid(RESPIRATORY_RATE_CONCEPT_UUID);
	}

	public static final String BLOOD_OXYGEN_SATURATION_CONCEPT_UUID = "3ce9401c-26fe-102b-80cb-0017a47871b2";

	public Concept getBloodOxygenSaturationConcept() {
		return getConceptByUuid(BLOOD_OXYGEN_SATURATION_CONCEPT_UUID);
	}

	public static final String SYSTOLIC_BP_CONCEPT_UUID = "3ce934fa-26fe-102b-80cb-0017a47871b2";

	public Concept getSystolicBpConcept() {
		return getConceptByUuid(SYSTOLIC_BP_CONCEPT_UUID);
	}

	public static final String DIASTOLIC_BP_CONCEPT_UUID = "3ce93694-26fe-102b-80cb-0017a47871b2";

	public Concept getDiastolicBpConcept() {
		return getConceptByUuid(DIASTOLIC_BP_CONCEPT_UUID);
	}

	public static final String DIAGNOSIS_CODED_CONCEPT_UUID = "226ed7ad-b776-4b99-966d-fd818d3302c2";

	public Concept getCodedDiagnosisConcept() {
		return getConceptByUuid(DIAGNOSIS_CODED_CONCEPT_UUID);
	}

	public static final String DIAGNOSIS_NONCODED_CONCEPT_UUID = "970d41ce-5098-47a4-8872-4dd843c0df3f";

	public Concept getNonCodedDiagnosisConcept() {
		return getConceptByUuid(DIAGNOSIS_NONCODED_CONCEPT_UUID);
	}

    public Concept getDiagnosisOrderConcept() {
        return conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
    }

    public Concept getDiagnosisCertaintyConcept() {
        return conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
    }

    public Concept getPrimaryDiagnosisConcept(){
        return conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_PRIMARY, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
    }

	public static final String CLINICAL_IMPRESSIONS_CONCEPT_UUID = "3cd9d956-26fe-102b-80cb-0017a47871b2";

	public Concept getClinicalImpressionsConcept() {
		return getConceptByUuid(CLINICAL_IMPRESSIONS_CONCEPT_UUID);
	}

    public static final String PRESENTING_HISTORY_CONCEPT_UUID = "3cd65c90-26fe-102b-80cb-0017a47871b2";

    public Concept getPresentingHistoryConcept() {
        return getConceptByUuid(PRESENTING_HISTORY_CONCEPT_UUID);
    }

    public static final String PHYSICAL_EXAM_CONCEPT_UUID = "3cd9ae0e-26fe-102b-80cb-0017a47871b2";

    public Concept getPhysicalExamConcept() {
        return getConceptByUuid(PHYSICAL_EXAM_CONCEPT_UUID);
    }

    public static final String CLINICAL_IMPRESSIONS_COMMENTS_CONCEPT_UUID = "3cd9d956-26fe-102b-80cb-0017a47871b2";

    public Concept getClinicalImpressionCommentsConcept() {
        return getConceptByUuid(CLINICAL_IMPRESSIONS_COMMENTS_CONCEPT_UUID);
    }

    public static final String CLINICAL_MANAGEMENT_PLAN_CONCEPT_UUID = "162749AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    public Concept getClinicalManagementPlanConcept() {
        return getConceptByUuid(CLINICAL_MANAGEMENT_PLAN_CONCEPT_UUID);
    }

    public static final String DISPENSING_CONSTRUCT_UUID = "cef4a703-8521-4c2d-9932-d1429a57e684";

    public Concept getDispensingConstruct() {
        return getConceptByUuid(DISPENSING_CONSTRUCT_UUID);
    }

    public static final String MEDICATION_NAME_CONCEPT_UUID = "3cd9491e-26fe-102b-80cb-0017a47871b2";

    public Concept getMedicationNameConcept() {
        return getConceptByUuid(MEDICATION_NAME_CONCEPT_UUID);
    }

    public static final String MEDICATION_INSTRUCTIONS_CONCEPT_UUID = "ef7f742b-76e6-4a83-84ca-534ad6705494";

    public Concept getMedicationInstructionsConcept() {
        return getConceptByUuid(MEDICATION_INSTRUCTIONS_CONCEPT_UUID);
    }

	public static final String SET_OF_WEEKLY_NOTIFIABLE_DISEASES_CONCEPT_UUID = "ddb35fb6-e69b-49cb-9540-ba11cf40ffd7";

	public Concept getSetOfWeeklyNotifiableDiseases() {
		return getConceptByUuid(SET_OF_WEEKLY_NOTIFIABLE_DISEASES_CONCEPT_UUID);
	}

	public static final String SET_OF_URGENT_DISEASES_CONCEPT_UUID = "0f8dc745-5f4d-494d-805b-6f8c8b5fe258";

	public Concept getSetOfUrgentDiseases() {
		return getConceptByUuid(SET_OF_URGENT_DISEASES_CONCEPT_UUID);
	}

	public static final String SET_OF_WOMENS_HEALTH_DIAGNOSES_CONCEPT_UUID = "27b6675d-02ea-4331-a5fc-9a8224f90660";

	public Concept getSetOfWomensHealthDiagnoses() {
		return getConceptByUuid(SET_OF_WOMENS_HEALTH_DIAGNOSES_CONCEPT_UUID);
	}

	public static final String SET_OF_PSYCHOLOGICAL_DIAGNOSES_CONCEPT_UUID = "3b85c049-1e2d-4f58-bad4-bf3bc98ed098";

	public Concept getSetOfPsychologicalDiagnoses() {
		return getConceptByUuid(SET_OF_PSYCHOLOGICAL_DIAGNOSES_CONCEPT_UUID);
	}

	public static final String SET_OF_PEDIATRIC_DIAGNOSES_CONCEPT_UUID = "231ac3ac-2ad4-4c41-9989-7e6b85393b51";

	public Concept getSetOfPediatricDiagnoses() {
		return getConceptByUuid(SET_OF_PEDIATRIC_DIAGNOSES_CONCEPT_UUID);
	}

	public static final String SET_OF_OUTPATIENT_DIAGNOSES_CONCEPT_UUID = "11c8b2ab-2d4a-4d3e-8733-e10e5a3f1404";

	public Concept getSetOfOutpatientDiagnoses() {
		return getConceptByUuid(SET_OF_OUTPATIENT_DIAGNOSES_CONCEPT_UUID);
	}

	public static final String SET_OF_NCD_DIAGNOSES_CONCEPT_UUID = "6581641f-ee7e-4a8a-b271-2148e6ffec77";

	public Concept getSetOfNcdDiagnoses() {
		return getConceptByUuid(SET_OF_NCD_DIAGNOSES_CONCEPT_UUID);
	}

	public static final String SET_OF_NON_DIAGNOSIS_CONCEPT_UUID = "a2d2124b-fc2e-4aa2-ac87-792d4205dd8d";

	public Concept getSetOfNonDiagnoses() {
		return getConceptByUuid(SET_OF_NON_DIAGNOSIS_CONCEPT_UUID);
	}

	public static final String SET_OF_EMERGENCY_DIAGNOSIS_CONCEPT_UUID = "cfe2f068-0dd1-4522-80f5-c71a5b5f2c8b";

	public Concept getSetOfEmergencyDiagnoses() {
		return getConceptByUuid(SET_OF_EMERGENCY_DIAGNOSIS_CONCEPT_UUID);
	}

	public static final String SET_OF_AGE_RESTRICTED_DISEASES_CONCEPT_UUID = "2231e6b8-6259-426d-a9b2-d3cb8fbbd6a3";

	public Concept getSetOfAgeRestrictedDiagnoses() {
		return getConceptByUuid(SET_OF_AGE_RESTRICTED_DISEASES_CONCEPT_UUID);
	}

    public static final String SET_OF_ONCOLOGY_DIAGNOSES_CONCEPT_UUID = "36489682-f68a-4a82-9cf8-4d2dca2221c6";

    public Concept getSetOfOncologyDiagnoses() {
        return getConceptByUuid(SET_OF_ONCOLOGY_DIAGNOSES_CONCEPT_UUID);
    }

    public static final String RETURN_VISIT_DATE_CONCEPT_UUID = "3ce94df0-26fe-102b-80cb-0017a47871b2";

    public Concept getReturnVisitDate() {
        return getConceptByUuid(RETURN_VISIT_DATE_CONCEPT_UUID);
    }

    public static final String TIMING_OF_PRESCRIPTION_CONCEPT_MAP = "9292";

    public Concept getTimingOfPrescriptionConcept() {
        return getSingleConceptByMapping(getPihConceptSource(), TIMING_OF_PRESCRIPTION_CONCEPT_MAP);
    }

    public static final String DISCHARGE_LOCATION_CONCEPT_MAP = "9293";

    public Concept getDischargeLocationConcept() {
        return getSingleConceptByMapping(getPihConceptSource(), DISCHARGE_LOCATION_CONCEPT_MAP);
    }

    public static final String TRANSFER_OUT_LOCATION_CONCEPT_UUID = "113a5ce0-6487-4f45-964d-2dcbd7d23b67";

    public Concept getTransferOutLocationConcept() {
        return getConceptByUuid(TRANSFER_OUT_LOCATION_CONCEPT_UUID);
    }

    public static final String OCCURRENCE_OF_TRAUMA_CONCEPT_UUID = "f8134959-62d2-4f94-af6c-3580312b07a0";

    public Concept getOccurrenceOfTraumaConcept() {
        return getConceptByUuid(OCCURRENCE_OF_TRAUMA_CONCEPT_UUID);
    }

    public static final String TRAUMA_TYPE_CONCEPT_UUID = "7c5ef8cd-3c2b-46c1-b995-20e52c11ce94";

    public Concept getTraumaTypeConcept() {
        return getConceptByUuid(TRAUMA_TYPE_CONCEPT_UUID);
    }

    public static final String CODED_DIAGNOSIS_CONCEPT_UUID = "226ed7ad-b776-4b99-966d-fd818d3302c2";

    public Concept getCodedDiagnosis() {
        return getConceptByUuid(CODED_DIAGNOSIS_CONCEPT_UUID);
    }

    public static final String NON_CODED_DIAGNOSIS_CONCEPT_UUID = "970d41ce-5098-47a4-8872-4dd843c0df3f";

    public Concept getNonCodedDiagnosis() {
        return getConceptByUuid(NON_CODED_DIAGNOSIS_CONCEPT_UUID);
    }

    public static final String SURGICAL_SERVICE_CONCEPT_UUID = "84834856-23f3-4885-994e-33091d587964";

    public Concept getSurgicalService() {
        return getConceptByUuid(SURGICAL_SERVICE_CONCEPT_UUID);
    }

    public static final String OTHER_ASSISTANT_CONCEPT_UUID = "bb34602b-0d91-4fe9-a88e-ff86c4af913d";

    public Concept getOtherAssistant() {
        return getConceptByUuid(OTHER_ASSISTANT_CONCEPT_UUID);
    }

    public static final String CHEST_RADIOLOGY_EXAM_SET_UUID = "cf739c45-e5e6-4544-b06a-16670898706e";
    public static final String SPINE_RADIOLOGY_EXAM_SET_UUID = "35ca061d-91d4-4549-aa80-be6b82706053";
    public static final String HEAD_AND_NECK_RADIOLOGY_EXAM_SET_UUID = "c271e719-8bf7-4f06-a8d5-853210c34592";
    public static final String VASCULAR_RADIOLOGY_EXAM_SET_UUID = "4419626d-236c-4281-968d-961cf90567fb";
    public static final String ABDOMEN_AND_PELVIS_RADIOLOGY_EXAM_SET_UUID = "da40f72e-8c3e-4b82-8295-b4bbd656afa8";
    public static final String MUSCULOSKELETAL_NON_CRANIAL_AND_SPINAL_RADIOLOGY_EXAM_SET_UUID = "2d26d7be-f7fa-400a-9e26-2fdf5e01e9ab";

    public Concept getChestRadiologyExamSetConcept() {
        return getConceptByUuid(CHEST_RADIOLOGY_EXAM_SET_UUID);
    }

    public Concept getSpineRadiologyExamSetConcept() {
        return getConceptByUuid(SPINE_RADIOLOGY_EXAM_SET_UUID);
    }

    public Concept getHeadAndNeckRadiologyExamSetConcept() {
        return getConceptByUuid(HEAD_AND_NECK_RADIOLOGY_EXAM_SET_UUID);
    }

    public Concept getVascularRadiologyExamSetConcept() {
        return getConceptByUuid(VASCULAR_RADIOLOGY_EXAM_SET_UUID);
    }

    public Concept getAbdomenAndPelvisRadiologyExamSetConcept() {
        return getConceptByUuid(ABDOMEN_AND_PELVIS_RADIOLOGY_EXAM_SET_UUID);
    }

    public Concept getMusculoskeletalNonCranialAndSpinalRadiologyExamSetConcept() {
        return getConceptByUuid(MUSCULOSKELETAL_NON_CRANIAL_AND_SPINAL_RADIOLOGY_EXAM_SET_UUID);
    }

    public static final String BOARDING_FOR_CONCEPT_UUID = "83a54c1d-510e-4860-8971-61755c71f0ed";

    public Concept getBoardingForConcept() {
        return getConceptByUuid(BOARDING_FOR_CONCEPT_UUID);
    }

    public static final String TYPE_OF_PATIENT_CONCEPT_UUID = "4813f780-ba12-48c3-befb-401ac0246929";

    public Concept getTypeOfPatientConcept() {
        return getConceptByUuid(TYPE_OF_PATIENT_CONCEPT_UUID);
    }

    public static final String HIV_TEST_RESULT_CONCEPT_MAPPING = "PIH:RESULT OF HIV TEST";

    public Concept getHivTestResultConcept() { return getConceptByMapping(HIV_TEST_RESULT_CONCEPT_MAPPING); }

    public static final String POSITIVE_CONCEPT_MAPPING = "CIEL:POSITIVE";

    public Concept getPositiveConcept() { return getConceptByMapping(POSITIVE_CONCEPT_MAPPING); }

    public static final String NEGATIVE_CONCEPT_MAPPING = "CIEL:NEGATIVE";

    public Concept getNegativeConcept() { return getConceptByMapping(NEGATIVE_CONCEPT_MAPPING); }

	private Concept getConceptByUuid(String uuid) {
		return conceptService.getConceptByUuid(uuid);
	}

    private Concept getConceptByMapping(String mapping) {
        int index = mapping.indexOf(":");
        String mappingCode = mapping.substring(0, index).trim();
        String conceptCode = mapping.substring(index + 1, mapping.length()).trim();
        Concept c = conceptService.getConceptByMapping(conceptCode, mappingCode);
        return c;
    }


    // ****** ENCOUNTER ROLES ****

    public static final String ENCOUNTER_ROLE_DISPENSER_UUID = "bad21515-fd04-4ff6-bfcd-78456d12f168";
    public static final String ENCOUNTER_ROLE_PRESCRIBED_BY_UUID = "c458d78e-8374-4767-ad58-9f8fe276e01c";
    public static final String CONSULTING_CLINICIAN_ENCOUNTER_ROLE_UUID = "4f10ad1a-ec49-48df-98c7-1391c6ac7f05";
    public static final String ATTENDING_SURGEON_ENCOUNTER_ROLE_UUID = "9b135b19-7ebe-4a51-aea2-69a53f9383af";
    public static final String ASSISTING_SURGEON_ENCOUNTER_ROLE_UUID = "6e630e03-5182-4cb3-9a82-a5b1a85c09a7";
    public static final String ADMINISTRATIVE_CLERK_ENCOUNTER_ROLE_UUID = "cbfe0b9d-9923-404c-941b-f048adc8cdc0";
    public static final String NURSE_ENCOUNTER_ROLE_UUID = "98bf2792-3f0a-4388-81bb-c78b29c0df92";
    public static final String RADIOLOGY_TECHNICIAN_ENCOUNTER_ROLE_UUID = "8f4d96e2-c97c-4285-9319-e56b9ba6029c";
    public static final String ORDERING_PROVIDER_ENCOUNTER_ROLE_UUID = "c458d78e-8374-4767-ad58-9f8fe276e01c";
    public static final String PRINCIPAL_RESULTS_INTERPRETER_ENCOUNTER_ROLE_UUID = "08f73be2-9452-44b5-801b-bdf7418c2f71";
    public static final String ANESTHESIOLOGIST_ENCOUNTER_ROLE_UUID = "de11b25c-a641-4630-9524-5b85ece9a4f8";

    public EncounterRole getDispenserEncounterRole() {
        return getEncounterRoleByUuid(ENCOUNTER_ROLE_DISPENSER_UUID);
    }

    public EncounterRole getPrescribedByEncounterRole() {
        return getEncounterRoleByUuid(ENCOUNTER_ROLE_PRESCRIBED_BY_UUID);
    }

    public EncounterRole getConsultingClinicianEncounterRole() {
        return getEncounterRoleByUuid(CONSULTING_CLINICIAN_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getAttendingSurgeonEncounterRole() {
        return getEncounterRoleByUuid(ATTENDING_SURGEON_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getAssistingSurgeonEncounterRole() {
        return getEncounterRoleByUuid(ASSISTING_SURGEON_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getAdministrativeClerkEncounterRole() {
        return getEncounterRoleByUuid(ADMINISTRATIVE_CLERK_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getNurseEncounterRole() {
        return getEncounterRoleByUuid(NURSE_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getRadiologyTechnicianEncounterRole() {
        return getEncounterRoleByUuid(RADIOLOGY_TECHNICIAN_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getOrderingProviderEncounterRole() {
        return getEncounterRoleByUuid(ORDERING_PROVIDER_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getPrincipalResultsInterpreterEncounterRole() {
        return getEncounterRoleByUuid(PRINCIPAL_RESULTS_INTERPRETER_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getAnesthesiologistEncounterRole() {
        return getEncounterRoleByUuid(ANESTHESIOLOGIST_ENCOUNTER_ROLE_UUID);
    }

    private EncounterRole getEncounterRoleByUuid(String uuid) {
        return encounterService.getEncounterRoleByUuid(uuid);
    }

    // ****** ORDER TYPES *********
    public OrderType getTestOrderType() {
        return orderService.getOrderTypeByUuid(OrderTypes.TEST_ORDER.uuid());
    }

    public OrderType getPathologyTestOrderType() {
        return orderService.getOrderTypeByUuid(OrderTypes.PATHOLOGY_TEST_ORDER.uuid());
    }

    // ****** PROGRAMS ************
    public Program getZikaProgram() {
        return programWorkflowService.getProgramByUuid(ZikaProgram.ZIKA.uuid());
    }

    /**
     * For testing
     * @param dispositionService
     */
    public void setDispositionService(DispositionService dispositionService) {
        this.dispositionService = dispositionService;
    }

}
