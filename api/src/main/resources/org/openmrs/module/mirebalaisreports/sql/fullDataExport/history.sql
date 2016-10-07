SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, e.encounter_datetime, el.name encounter_location, et.name,
CONCAT(pn.given_name, ' ',pn.family_name) provider, obsjoins.*
FROM patient p
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type in (:AdultInitEnc, :AdultFollowEnc, :PedInitEnc, :PedFollowEnc)
-- Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type =:zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
-- ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
INNER JOIN location el ON e.location_id = el.location_id
-- Encounter Type
INNER JOIN encounter_type et on et.encounter_type_id = e.encounter_type
-- Provider Name
INNER JOIN encounter_provider ep ON ep.encounter_id = e.encounter_id and ep.voided = 0
INNER JOIN provider pv ON pv.provider_id = ep.provider_id
INNER JOIN person_name pn ON pn.person_id = pv.person_id and pn.voided = 0
INNER JOIN
 (select
e.encounter_id,
max(CASE when crs.name = 'PIH' and crt.code = 'PRESENTING HISTORY' THEN o.value_text end) "Presenting_History",
group_concat(CASE when crs.name = 'CIEL' and crt.code = '160592'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'ASTHMA' THEN  par.name end separator ',') "Family_Asthma",
group_concat(CASE when crs.name = 'CIEL' and crt.code = '160592'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'HEART DISEASE' THEN  par.name end separator ',') "Family_Heart_Disease",
group_concat(CASE when crs.name = 'CIEL' and crt.code = '160592'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'DIABETES' THEN  par.name end separator ',') "Family_Diabetes",
group_concat(CASE when crs.name = 'CIEL' and crt.code = '160592'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'EPILEPSY' THEN  par.name end separator ',') "Family_Epilepsy",
group_concat(CASE when crs.name = 'CIEL' and crt.code = '160592'
                   and crs_answer.name = 'CIEL' and crt_answer.code = '117635' THEN  par.name end separator ',') "Family_Hemoglobinopathy",
group_concat(CASE when crs.name = 'CIEL' and crt.code = '160592'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'HYPERTENSION' THEN  par.name end separator ',') "Family_Hypertension",
group_concat(CASE when crs.name = 'CIEL' and crt.code = '160592'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'TUBERCULOSIS' THEN  par.name end separator ',') "Family_Tuberculosis",
group_concat(CASE when crs.name = 'CIEL' and crt.code = '160592'
                   and crs_answer.name = 'CIEL' and crt_answer.code = '116031' THEN  par.name end separator ',') "Family_Cancer",
group_concat(distinct CASE when crs.name = 'CIEL' and crt.code = '160592'
                   and crs_answer.name = 'CIEL' and crt_answer.code = '116031' THEN  fam_com.value_text end separator ',') "Family_Cancer_comment",
group_concat(CASE when crs.name = 'CIEL' and crt.code = '160592'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'OTHER' THEN  par.name end separator ',') "Family_Other" ,
group_concat(distinct CASE when crs.name = 'CIEL' and crt.code = '160592'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'OTHER' THEN  fam_com.value_text end separator ',') "Family_Other_comment",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'ASTHMA' THEN  pres.name end)  "Patient_asthma",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'HEART DISEASE' THEN  "1" end)  "Patient_heart_disease",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'SURGERY' THEN  pres.name end)  "Patient_surgery",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'SURGERY' THEN  pat_com.value_text end)  "Patient_surgery_comment",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'Traumatic Injury' THEN  pres.name end)  "Patient_trauma",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'Traumatic Injury' THEN  pat_com.value_text end)  "Patient_trauma_comment",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'EPILEPSY' THEN  pres.name end)  "Patient_epilepsy",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'CIEL' and crt_answer.code = '117635' THEN  pres.name end)  "Patient_Hemoglobinopathy",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'CIEL' and crt_answer.code = '117635' THEN  pat_com.value_text end)  "Patient_Hemoglobinopathy_comment",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'HYPERTENSION' THEN  pres.name end)  "Patient_hypertension",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'SEXUALLY TRANSMITTED INFECTION' THEN  pres.name end)  "Patient_sti",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'SEXUALLY TRANSMITTED INFECTION' THEN  pat_com.value_text end)  "Patient_sti_comment",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'CIEL' and crt_answer.code = '143849' THEN  pres.name end)  "Patient_congenital_malformation",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'CIEL' and crt_answer.code = '143849' THEN  pat_com.value_text end)  "Patient_con_malform_comment",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'MALNUTRITION' THEN  pres.name end)  "Patient_malnutrition",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'WEIGHT LOSS' THEN  pres.name end)  "Patient_weight_loss",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'MEASLES' THEN  pres.name end)  "Patient_measles",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'TUBERCULOSIS' THEN  pres.name end)  "Patient_tuberculosis",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'VARICELLA' THEN  pres.name end)  "Patient_varicella",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'Diphtheria' THEN  pres.name end)  "Patient_diptheria",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'ACUTE RHEUMATIC FEVER' THEN  pres.name end)  "Patient_raa",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'DIABETES' THEN  pres.name end)  "Patient_diabetes",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'Premature birth of patient' THEN  pres.name end)  "Patient_premature_birth",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'OTHER' THEN  pres.name end)  "Patient_other",
max(CASE when crs.name = 'CIEL' and crt.code = '1628'
                   and crs_answer.name = 'PIH' and crt_answer.code = 'OTHER' THEN  pat_com.value_text end)  "Patient_other_comment",
max(CASE when crs.name = 'PIH' and crt.code = 'BLOOD TYPING' THEN  cn.name end)  "Patient_blood_type",
max(CASE when crs.name = 'PIH' and crt.code = 'Hospitalization comment' THEN  o.value_text end)  "Patient_hospitalization",
max(CASE when crs.name = 'PIH' and crt.code = 'CURRENT MEDICATIONS' THEN  o.value_text end)  "Patient_current_meds",
max(CASE when crs.name = 'PIH' and crt.code = 'DIAGNOSTIC TESTS HISTORY' THEN  o.value_text end)  "Patient_test_history"
from encounter e
INNER JOIN obs o on o.encounter_id = e.encounter_id and o.voided = 0
-- join in mapping of obs question (not needed if this is a standalone export)
INNER JOIN concept_reference_map crm on crm.concept_id = o.concept_id
INNER JOIN concept_reference_term crt on crt.concept_reference_term_id = crm.concept_reference_term_id
INNER JOIN concept_reference_source crs on crs.concept_source_id = crt.concept_source_id
LEFT OUTER JOIN concept_name cn on o.value_coded = cn.concept_id and cn.locale = 'en' and cn.locale_preferred = '1'  and cn.voided = 0
-- join in mapping of obs answer
LEFT OUTER JOIN concept_reference_map crm_answer on crm_answer.concept_id = o.value_coded
LEFT OUTER JOIN concept_reference_term crt_answer on crt_answer.concept_reference_term_id = crm_answer.concept_reference_term_id
LEFT OUTER JOIN concept_reference_source crs_answer on crs_answer.concept_source_id = crt_answer.concept_source_id
 -- include parent joined by obsgroupid
LEFT OUTER JOIN
   (select obspar.encounter_id, obspar.obs_group_id, cn.name
   from obs obspar
   INNER JOIN concept_reference_map crm on crm.concept_id = obspar.value_coded
	INNER JOIN concept_reference_term crt on crt.concept_reference_term_id = crm.concept_reference_term_id and crt.code in ('MOTHER','FATHER')
	INNER JOIN concept_reference_source crs on crs.concept_source_id = crt.concept_source_id and crs.name = 'PIH'
	INNER JOIN concept_name cn on cn.concept_id = obspar.value_coded and cn.voided = 0 and cn.locale = 'en' and cn.locale_preferred = '1'
	where obspar.voided = 0) par
	on par.encounter_id = o.encounter_id and par.obs_group_id = o.obs_group_id
-- include Familiy History comment joined by obsgroupid
LEFT OUTER JOIN
   (select obscom.encounter_id, obscom.obs_group_id, obscom.value_text
   from obs obscom
   INNER JOIN concept_reference_map crm on crm.concept_id = obscom.concept_id
	INNER JOIN concept_reference_term crt on crt.concept_reference_term_id = crm.concept_reference_term_id and crt.code in ('160618') -- mapping for family history comment
	INNER JOIN concept_reference_source crs on crs.concept_source_id = crt.concept_source_id and crs.name = 'CIEL'
	where obscom.voided = 0) fam_com
	on fam_com.encounter_id = o.encounter_id and fam_com.obs_group_id = o.obs_group_id
-- include sign/symptom present joined in by ObsGroupId
LEFT OUTER JOIN
   (select obspres.encounter_id, obspres.obs_group_id, cn.name
   from obs obspres
   INNER JOIN concept_reference_map crm on crm.concept_id = obspres.concept_id
	INNER JOIN concept_reference_term crt on crt.concept_reference_term_id = crm.concept_reference_term_id and crt.code = '1729'
	INNER JOIN concept_reference_source crs on crs.concept_source_id = crt.concept_source_id and crs.name = 'CIEL'
	INNER JOIN concept_name cn on cn.concept_id = obspres.value_coded and cn.voided = 0 and cn.locale = 'en' and cn.locale_preferred = '1'
	where obspres.voided = 0) pres
	on pres.encounter_id = o.encounter_id and pres.obs_group_id = o.obs_group_id
-- include patient history comment, joined by obsgroupid
LEFT OUTER JOIN
   (select obscom.encounter_id, obscom.obs_group_id, obscom.value_text
   from obs obscom
   INNER JOIN concept_reference_map crm on crm.concept_id = obscom.concept_id
	INNER JOIN concept_reference_term crt on crt.concept_reference_term_id = crm.concept_reference_term_id and crt.code in ('160221') -- mapping for patient history comment
	INNER JOIN concept_reference_source crs on crs.concept_source_id = crt.concept_source_id and crs.name = 'CIEL'
	where obscom.voided = 0) pat_com
	on pat_com.encounter_id = o.encounter_id and pat_com.obs_group_id = o.obs_group_id
where e.voided = 0
group by encounter_id) obsjoins on obsjoins.encounter_id = e.encounter_id
WHERE p.voided = 0
-- exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id =:testPt
                         AND voided = 0)
AND date(e.encounter_datetime) >=:startDate
AND date(e.encounter_datetime) <=:endDate
GROUP BY e.encounter_id;
