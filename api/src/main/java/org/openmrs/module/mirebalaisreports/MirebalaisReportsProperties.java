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
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.module.emr.EmrProperties;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionService;
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
    DispositionService dispositionService;

    //***** DATE FORMATS ******
    public static final String DATE_FORMAT = "dd MMM yyyy";
    public static final String DATETIME_FORMAT= "dd MMM yyyy hh:mm aa";

    //***** REPORT DEFINITIONS *****
    public static final String FULL_DATA_EXPORT_REPORT_DEFINITION_UUID = "8c3752e2-20bb-11e3-b5bd-0bec7fb71852";
    public static final String DASHBOARD_DATA_EXPORT_REPORT_DEFINITION_UUID = "6d9b292a-2aad-11e3-a840-5b9e0b589afb";
    public static final String RADIOLOGY_DATA_EXPORT_REPORT_DEFINITION_UUID = "9e7dc296-2aad-11e3-a840-5b9e0b589afb";
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
    public static final String USERS_AND_PROVIDERS_REPORT_DEFINITION_UUID = "e4d1d6b0-642d-11e3-949a-0800200c9a66";

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
		return getRequiredLocationByUuid(OUTPATIENT_CLINIC_UUID);
	}

	public Location getWomenLocation() {
		return getRequiredLocationByUuid(WOMEN_CLINIC_UUID);
	}

    public Location getEmergencyLocation() {
        return getRequiredLocationByUuid(EMERGENCY_DEPARTMENT_UUID);
    }

    public Location getEmergencyReceptionLocation() {
        return getRequiredLocationByUuid(EMERGENCY_RECEPTION_UUID);
    }

    public Location getMirebalaisHospitalLocation() {
        return getRequiredLocationByUuid(MIREBALAIS_HOSPITAL_UUID);
    }

    public Location getClinicRegistrationLocation() {
        return getRequiredLocationByUuid(CLINIC_REGISTRATION_UUID);
    }

    public Location getWomensInternalMedicineLocation() {
        return getRequiredLocationByUuid(WOMENS_INTERNAL_MEDICINE_UUID);
    }

    public Location getMensInternalMedicineLocation() {
        return getRequiredLocationByUuid(MENS_INTERNAL_MEDICINE_UUID);
    }

    public Location getSurgicalWardLocation() {
        return getRequiredLocationByUuid(SURGICAL_WARD_UUID);
    }

    public Location getAntepartumWardLocation() {
        return getRequiredLocationByUuid(ANTEPARTUM_WARD_UUID);
    }

    public Location getLaborAndDeliveryLocation() {
        return getRequiredLocationByUuid(LABOR_AND_DELIVERY_UUID);
    }

    public Location getPostpartumWardLocation() {
        return getRequiredLocationByUuid(POSTPARTUM_WARD_UUID);
    }

    public Location getNeonatalIcuLocation() {
        return getRequiredLocationByUuid(NEONATAL_ICU_UUID);
    }

    private Location getRequiredLocationByUuid(String uuid) {
		Location location = locationService.getLocationByUuid(uuid);
		if (location == null) {
			throw new IllegalStateException("Missing required location with uuid: " + uuid);
		}
		return location;
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

	public static final String ZL_EMR_ID_UUID = "a541af1e-105c-40bf-b345-ba1fd6a59b85";

	public PatientIdentifierType getZlEmrIdentifierType() {
		return getRequiredIdentifierTypeByUuid(ZL_EMR_ID_UUID);
	}

	public static final String DOSSIER_NUMBER_UUID = "e66645eb-03a8-4991-b4ce-e87318e37566";

	public PatientIdentifierType getDossierNumberIdentifierType() {
		return getRequiredIdentifierTypeByUuid(DOSSIER_NUMBER_UUID);
	}

	public static final String HIV_EMR_ID_UUID = "139766e8-15f5-102d-96e4-000c29c2a5d7";

	public PatientIdentifierType getHivEmrIdentifierType() {
		return getRequiredIdentifierTypeByUuid(HIV_EMR_ID_UUID);
	}

	private PatientIdentifierType getRequiredIdentifierTypeByUuid(String uuid) {
		PatientIdentifierType t = patientService.getPatientIdentifierTypeByUuid(uuid);
		if (t == null) {
			throw new IllegalStateException("Missing required patient identifier type with uuid: " + uuid);
		}
		return t;
	}


	//***** PERSON ATTRIBUTE TYPES

	public static final String TEST_PERSON_ATTRIBUTE_UUID = "4f07985c-88a5-4abd-aa0c-f3ec8324d8e7";
    public static final String TELEPHONE_PERSON_ATTRIBUTE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
    public static final String UNKNOWN_PATIENT_PERSON_ATTRIBUTE_UUID = "8b56eac7-5c76-4b9c-8c6f-1deab8d3fc47";

	public PersonAttributeType getTestPatientPersonAttributeType() {
		return getRequiredPersonAttributeTypeByUuid(TEST_PERSON_ATTRIBUTE_UUID);
	}

    public PersonAttributeType getTelephoneNumberPersonAttributeType() {
        return getRequiredPersonAttributeTypeByUuid(TELEPHONE_PERSON_ATTRIBUTE_UUID);
    }

    public PersonAttributeType getUnknownPatientPersonAttributeType() {
        return getRequiredPersonAttributeTypeByUuid(UNKNOWN_PATIENT_PERSON_ATTRIBUTE_UUID);
    }

	private PersonAttributeType getRequiredPersonAttributeTypeByUuid(String uuid) {
		PersonAttributeType t = personService.getPersonAttributeTypeByUuid(uuid);
		if (t == null) {
			throw new IllegalStateException("Missing required person attribute type with uuid: " + uuid);
		}
		return t;
	}


	//***** ENCOUNTER TYPES *****

	public static final String REGISTRATION_ENCOUNTER_TYPE_UUID = "873f968a-73a8-4f9c-ac78-9f4778b751b6";
    public static final String CHECK_IN_ENCOUNTER_TYPE_UUID = "55a0d3ea-a4d7-4e88-8f01-5aceb2d3c61b";
    public static final String PAYMENT_ENCOUNTER_TYPE_UUID = "f1c286d0-b83f-4cd4-8348-7ea3c28ead13";
    public static final String VITALS_ENCOUNTER_TYPE_UUID = "4fb47712-34a6-40d2-8ed3-e153abbd25b7";
    public static final String CONSULT_ENCOUNTER_TYPE_UUID = "92fd09b4-5335-4f7e-9f63-b2a663fd09a6";
    public static final String RADIOLOGY_ORDER_ENCOUNTER_TYPE_UUID = "1b3d1e13-f0b1-4b83-86ea-b1b1e2fb4efa";
    public static final String RADIOLOGY_STUDY_ENCOUNTER_TYPE_UUID = "5b1b4a4e-0084-4137-87db-dba76c784439";
    public static final String RADIOLOGY_REPORT_ENCOUNTER_TYPE_UUID = "d5ca53a7-d3b5-44ac-9aa2-1491d2a4b4e9";
    public static final String POST_OP_NOTE_ENCOUNTER_TYPE_UUID = "c4941dee-7a9b-4c1c-aa6f-8193e9e5e4e5";
    public static final String ADMISSION_ENCOUNTER_TYPE_UUID = "260566e1-c909-4d61-a96f-c1019291a09d";
    public static final String EXIT_FROM_INPATIENT_ENCOUNTER_TYPE_UUID = "b6631959-2105-49dd-b154-e1249e0fbcd7";
    public static final String TRANSFER_ENCOUNTER_TYPE_UUID = "436cfe33-6b81-40ef-a455-f134a9f7e580";

	public EncounterType getRegistrationEncounterType() {
		return getRequiredEncounterTypeByUuid(REGISTRATION_ENCOUNTER_TYPE_UUID);
	}

	public EncounterType getCheckInEncounterType() {
		return getRequiredEncounterTypeByUuid(CHECK_IN_ENCOUNTER_TYPE_UUID);
	}

	public EncounterType getPaymentEncounterType() {
		return getRequiredEncounterTypeByUuid(PAYMENT_ENCOUNTER_TYPE_UUID);
	}

	public EncounterType getVitalsEncounterType() {
		return getRequiredEncounterTypeByUuid(VITALS_ENCOUNTER_TYPE_UUID);
	}

	public EncounterType getConsultEncounterType() {
		return getRequiredEncounterTypeByUuid(CONSULT_ENCOUNTER_TYPE_UUID);
	}

	public EncounterType getRadiologyOrderEncounterType() {
		return getRequiredEncounterTypeByUuid(RADIOLOGY_ORDER_ENCOUNTER_TYPE_UUID);
	}

    public EncounterType getRadiologyStudyEncounterType() {
        return getRequiredEncounterTypeByUuid(RADIOLOGY_STUDY_ENCOUNTER_TYPE_UUID);
    }

    public EncounterType getRadiologyReportEncounterType() {
        return getRequiredEncounterTypeByUuid(RADIOLOGY_REPORT_ENCOUNTER_TYPE_UUID);
    }

    public EncounterType getPostOpNoteEncounterType() {
        return getRequiredEncounterTypeByUuid(POST_OP_NOTE_ENCOUNTER_TYPE_UUID);
    }

    public EncounterType getAdmissionEncounterType() {
        return getRequiredEncounterTypeByUuid(ADMISSION_ENCOUNTER_TYPE_UUID);
    }

    public EncounterType getExitFromInpatientEncounterType() {
        return getRequiredEncounterTypeByUuid(EXIT_FROM_INPATIENT_ENCOUNTER_TYPE_UUID);
    }

    public EncounterType getTransferEncounterType() {
        return getRequiredEncounterTypeByUuid(TRANSFER_ENCOUNTER_TYPE_UUID);
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

	private EncounterType getRequiredEncounterTypeByUuid(String uuid) {
		EncounterType encounterType = encounterService.getEncounterTypeByUuid(uuid);
		if (encounterType == null) {
			throw new IllegalStateException("Missing required encounter type with uuid: " + uuid);
		}
		return encounterType;
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
        String conceptCode = disposition.getConceptCode();
        if ( StringUtils.isNotBlank(conceptCode) ) {
            String[] conceptMap = conceptCode.split(":");
            if ( (conceptMap !=null) && (conceptMap.length == 2) ) {
                conceptForDisposition = conceptService.getConceptByMapping(conceptMap[1], conceptMap[0]);
            }
        }
        return conceptForDisposition;
    }

	//***** CONCEPTS *****

	public static final String AMOUNT_PATIENT_PAID_CONCEPT_UUID  = "5d1bc5de-6a35-4195-8631-7322941fe528";

	public Concept getAmountPaidConcept() {
		return getRequiredConceptByUuid(AMOUNT_PATIENT_PAID_CONCEPT_UUID);
	}

	public static final String WEIGHT_CONCEPT_UUID = "3ce93b62-26fe-102b-80cb-0017a47871b2";

	public Concept getWeightConcept() {
		return getRequiredConceptByUuid(WEIGHT_CONCEPT_UUID);
	}

	public static final String HEIGHT_CONCEPT_UUID = "3ce93cf2-26fe-102b-80cb-0017a47871b2";

	public Concept getHeightConcept() {
		return getRequiredConceptByUuid(HEIGHT_CONCEPT_UUID);
	}

	public static final String MUAC_CONCEPT_UUID = "e3e03a93-de7f-41ea-b8f2-60b220b970e9";

	public Concept getMuacConcept() {
		return getRequiredConceptByUuid(MUAC_CONCEPT_UUID);
	}

	public static final String TEMPERATURE_CONCEPT_UUID = "3ce939d2-26fe-102b-80cb-0017a47871b2";

	public Concept getTemperatureConcept() {
		return getRequiredConceptByUuid(TEMPERATURE_CONCEPT_UUID);
	}

	public static final String PULSE_CONCEPT_UUID = "3ce93824-26fe-102b-80cb-0017a47871b2";

	public Concept getPulseConcept() {
		return getRequiredConceptByUuid(PULSE_CONCEPT_UUID);
	}

	public static final String RESPIRATORY_RATE_CONCEPT_UUID = "3ceb11f8-26fe-102b-80cb-0017a47871b2";

	public Concept getRespiratoryRateConcept() {
		return getRequiredConceptByUuid(RESPIRATORY_RATE_CONCEPT_UUID);
	}

	public static final String BLOOD_OXYGEN_SATURATION_CONCEPT_UUID = "3ce9401c-26fe-102b-80cb-0017a47871b2";

	public Concept getBloodOxygenSaturationConcept() {
		return getRequiredConceptByUuid(BLOOD_OXYGEN_SATURATION_CONCEPT_UUID);
	}

	public static final String SYSTOLIC_BP_CONCEPT_UUID = "3ce934fa-26fe-102b-80cb-0017a47871b2";

	public Concept getSystolicBpConcept() {
		return getRequiredConceptByUuid(SYSTOLIC_BP_CONCEPT_UUID);
	}

	public static final String DIASTOLIC_BP_CONCEPT_UUID = "3ce93694-26fe-102b-80cb-0017a47871b2";

	public Concept getDiastolicBpConcept() {
		return getRequiredConceptByUuid(DIASTOLIC_BP_CONCEPT_UUID);
	}

	public static final String DIAGNOSIS_CODED_CONCEPT_UUID = "226ed7ad-b776-4b99-966d-fd818d3302c2";

	public Concept getCodedDiagnosisConcept() {
		return getRequiredConceptByUuid(DIAGNOSIS_CODED_CONCEPT_UUID);
	}

	public static final String DIAGNOSIS_NONCODED_CONCEPT_UUID = "970d41ce-5098-47a4-8872-4dd843c0df3f";

	public Concept getNonCodedDiagnosisConcept() {
		return getRequiredConceptByUuid(DIAGNOSIS_NONCODED_CONCEPT_UUID);
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
		return getRequiredConceptByUuid(CLINICAL_IMPRESSIONS_CONCEPT_UUID);
	}

	public static final String SET_OF_WEEKLY_NOTIFIABLE_DISEASES_CONCEPT_UUID = "ddb35fb6-e69b-49cb-9540-ba11cf40ffd7";

	public Concept getSetOfWeeklyNotifiableDiseases() {
		return getRequiredConceptByUuid(SET_OF_WEEKLY_NOTIFIABLE_DISEASES_CONCEPT_UUID);
	}

	public static final String SET_OF_URGENT_DISEASES_CONCEPT_UUID = "0f8dc745-5f4d-494d-805b-6f8c8b5fe258";

	public Concept getSetOfUrgentDiseases() {
		return getRequiredConceptByUuid(SET_OF_URGENT_DISEASES_CONCEPT_UUID);
	}

	public static final String SET_OF_WOMENS_HEALTH_DIAGNOSES_CONCEPT_UUID = "27b6675d-02ea-4331-a5fc-9a8224f90660";

	public Concept getSetOfWomensHealthDiagnoses() {
		return getRequiredConceptByUuid(SET_OF_WOMENS_HEALTH_DIAGNOSES_CONCEPT_UUID);
	}

	public static final String SET_OF_PSYCHOLOGICAL_DIAGNOSES_CONCEPT_UUID = "3b85c049-1e2d-4f58-bad4-bf3bc98ed098";

	public Concept getSetOfPsychologicalDiagnoses() {
		return getRequiredConceptByUuid(SET_OF_PSYCHOLOGICAL_DIAGNOSES_CONCEPT_UUID);
	}

	public static final String SET_OF_PEDIATRIC_DIAGNOSES_CONCEPT_UUID = "231ac3ac-2ad4-4c41-9989-7e6b85393b51";

	public Concept getSetOfPediatricDiagnoses() {
		return getRequiredConceptByUuid(SET_OF_PEDIATRIC_DIAGNOSES_CONCEPT_UUID);
	}

	public static final String SET_OF_OUTPATIENT_DIAGNOSES_CONCEPT_UUID = "11c8b2ab-2d4a-4d3e-8733-e10e5a3f1404";

	public Concept getSetOfOutpatientDiagnoses() {
		return getRequiredConceptByUuid(SET_OF_OUTPATIENT_DIAGNOSES_CONCEPT_UUID);
	}

	public static final String SET_OF_NCD_DIAGNOSES_CONCEPT_UUID = "6581641f-ee7e-4a8a-b271-2148e6ffec77";

	public Concept getSetOfNcdDiagnoses() {
		return getRequiredConceptByUuid(SET_OF_NCD_DIAGNOSES_CONCEPT_UUID);
	}

	public static final String SET_OF_NON_DIAGNOSIS_CONCEPT_UUID = "a2d2124b-fc2e-4aa2-ac87-792d4205dd8d";

	public Concept getSetOfNonDiagnoses() {
		return getRequiredConceptByUuid(SET_OF_NON_DIAGNOSIS_CONCEPT_UUID);
	}

	public static final String SET_OF_EMERGENCY_DIAGNOSIS_CONCEPT_UUID = "cfe2f068-0dd1-4522-80f5-c71a5b5f2c8b";

	public Concept getSetOfEmergencyDiagnoses() {
		return getRequiredConceptByUuid(SET_OF_EMERGENCY_DIAGNOSIS_CONCEPT_UUID);
	}

	public static final String SET_OF_AGE_RESTRICTED_DISEASES_CONCEPT_UUID = "2231e6b8-6259-426d-a9b2-d3cb8fbbd6a3";

	public Concept getSetOfAgeRestrictedDiagnoses() {
		return getRequiredConceptByUuid(SET_OF_AGE_RESTRICTED_DISEASES_CONCEPT_UUID);
	}

    public static final String RETURN_VISIT_DATE_CONCEPT_UUID = "3ce94df0-26fe-102b-80cb-0017a47871b2";

    public Concept getReturnVisitDate() {
        return getRequiredConceptByUuid(RETURN_VISIT_DATE_CONCEPT_UUID);
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
        return getRequiredConceptByUuid(TRANSFER_OUT_LOCATION_CONCEPT_UUID);
    }

    public static final String OCCURRENCE_OF_TRAUMA_CONCEPT_UUID = "f8134959-62d2-4f94-af6c-3580312b07a0";

    public Concept getOccurrenceOfTraumaConcept() {
        return getRequiredConceptByUuid(OCCURRENCE_OF_TRAUMA_CONCEPT_UUID);
    }

    public static final String TRAUMA_TYPE_CONCEPT_UUID = "7c5ef8cd-3c2b-46c1-b995-20e52c11ce94";

    public Concept getTraumaTypeConcept() {
        return getRequiredConceptByUuid(TRAUMA_TYPE_CONCEPT_UUID);
    }

    public static final String CODED_DIAGNOSIS_CONCEPT_UUID = "226ed7ad-b776-4b99-966d-fd818d3302c2";

    public Concept getCodedDiagnosis() {
        return getRequiredConceptByUuid(CODED_DIAGNOSIS_CONCEPT_UUID);
    }

    public static final String NON_CODED_DIAGNOSIS_CONCEPT_UUID = "970d41ce-5098-47a4-8872-4dd843c0df3f";

    public Concept getNonCodedDiagnosis() {
        return getRequiredConceptByUuid(NON_CODED_DIAGNOSIS_CONCEPT_UUID);
    }

    public static final String SURGICAL_SERVICE_CONCEPT_UUID = "84834856-23f3-4885-994e-33091d587964";

    public Concept getSurgicalService() {
        return getRequiredConceptByUuid(SURGICAL_SERVICE_CONCEPT_UUID);
    }

    public static final String OTHER_ASSISTANT_CONCEPT_UUID = "bb34602b-0d91-4fe9-a88e-ff86c4af913d";

    public Concept getOtherAssistant() {
        return getRequiredConceptByUuid(OTHER_ASSISTANT_CONCEPT_UUID);
    }

	private Concept getRequiredConceptByUuid(String uuid) {
		Concept c = conceptService.getConceptByUuid(uuid);
		if (c == null) {
			throw new IllegalStateException("Missing required concept with uuid: " + uuid);
		}
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
        return getRequiredEncounterRoleByUuid(ENCOUNTER_ROLE_DISPENSER_UUID);
    }

    public EncounterRole getPrescribedByEncounterRole() {
        return getRequiredEncounterRoleByUuid(ENCOUNTER_ROLE_PRESCRIBED_BY_UUID);
    }

    public EncounterRole getConsultingClinicianEncounterRole() {
        return getRequiredEncounterRoleByUuid(CONSULTING_CLINICIAN_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getAttendingSurgeonEncounterRole() {
        return getRequiredEncounterRoleByUuid(ATTENDING_SURGEON_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getAssistingSurgeonEncounterRole() {
        return getRequiredEncounterRoleByUuid(ASSISTING_SURGEON_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getAdministrativeClerkEncounterRole() {
        return getRequiredEncounterRoleByUuid(ADMINISTRATIVE_CLERK_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getNurseEncounterRole() {
        return getRequiredEncounterRoleByUuid(NURSE_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getRadiologyTechnicianEncounterRole() {
        return getRequiredEncounterRoleByUuid(RADIOLOGY_TECHNICIAN_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getOrderingProviderEncounterRole() {
        return getRequiredEncounterRoleByUuid(ORDERING_PROVIDER_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getPrincipalResultsInterpreterEncounterRole() {
        return getRequiredEncounterRoleByUuid(PRINCIPAL_RESULTS_INTERPRETER_ENCOUNTER_ROLE_UUID);
    }

    public EncounterRole getAnesthesiologistEncounterRole() {
        return getRequiredEncounterRoleByUuid(ANESTHESIOLOGIST_ENCOUNTER_ROLE_UUID);
    }

    private EncounterRole getRequiredEncounterRoleByUuid(String uuid) {
        EncounterRole role = encounterService.getEncounterRoleByUuid(uuid);
        if (role== null) {
            throw new IllegalStateException("Missing required encounter role with uuid: " + uuid);
        }
        return role;
    }

    /**
     * For testing
     * @param dispositionService
     */
    public void setDispositionService(DispositionService dispositionService) {
        this.dispositionService = dispositionService;
    }

}
