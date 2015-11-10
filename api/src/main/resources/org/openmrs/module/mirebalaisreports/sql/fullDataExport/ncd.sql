SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark,  e.encounter_datetime, el.name encounter_location,
CONCAT(pn.given_name, ' ',pn.family_name) provider,e.visit_id,
obsjoins.*
FROM patient p
-- Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = :zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
-- ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
-- Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id =:unknownPt
            AND un.voided = 0
-- Gender
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
--  Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = :ncdNoteEnc
INNER JOIN location el ON e.location_id = el.location_id
--  Provider Name
INNER JOIN encounter_provider ep ON ep.encounter_id = e.encounter_id and ep.voided = 0
INNER JOIN provider pv ON pv.provider_id = ep.provider_id
INNER JOIN person_name pn ON pn.person_id = pv.person_id and pn.voided = 0
-- Straight Obs Joins
INNER JOIN
(select o.encounter_id,
max(CASE when crs.name = 'PIH' and crt.code = 'Type of referring service' then cn.name end) 'Type_of_referring_service',
max(CASE when crs.name = 'PIH' and crt.code = 'Known chronic disease before referral' then cn.name end) 'Known_disease_before_referral',
max(CASE when crs.name = 'PIH' and crt.code = 'Prior treatment for chronic disease' then cn.name end) 'Prior_treatment',
max(CASE when crs.name = 'PIH' and crt.code = 'Chronic disease controlled during initial visit' then cn.name end) 'Disease_controlled_initial_visit',
group_concat(CASE when crs.name = 'PIH' and crt.code = 'NCD category' then cn.name end separator ',') 'NCD_category',
max(CASE when crs.name = 'PIH' and crt.code = 'NCD category' then o.comments end) 'Other_NCD_category',
max(CASE when crs.name = 'CIEL' and crt.code = '163080' then o.value_numeric end) 'Waist_cm',
max(CASE when crs.name = 'CIEL' and crt.code = '163081' then o.value_numeric end) 'hip_cm',
Round(max(CASE when crs.name = 'CIEL' and crt.code = '163080' then o.value_numeric end)/max(CASE when crs.name = 'CIEL' and crt.code = '163081' then o.value_numeric end),2) 'Waist/Hip Ratio',
group_concat(CASE when crs.name = 'PIH' and crt.code = 'NYHA CLASS' then cn.name end separator ',') 'NYHA_CLASS',
max(CASE when crs.name = 'PIH' and crt.code = 'PATIENTS FLUID MANAGEMENT' then cn.name end) 'Patients_Fluid_Management',
max(CASE when crs.name = 'PIH' and crt.code = 'Hypoglycemia symptoms' then cn.name end) 'Hypoglycemia_symptoms',
max(CASE when crs.name = 'PIH' and crt.code = 'Puffs per week of salbutamol' then o.value_numeric end) 'Puffs_week_salbutamol',
max(CASE when crs.name = 'PIH' and crt.code = 'Asthma classification' then cn.name end) 'Asthma_classification',
max(CASE when crs.name = 'PIH' and crt.code = 'Number of seizures since last visit' then o.value_numeric end) 'Number_seizures_since_last_visit',
max(CASE when crs.name = 'PIH' and crt.code = 'Appearance at appointment time' then cn.name end) 'Adherance_to_appointment',
max(CASE when crs.name = 'PIH' and crt.code = 'Lack of meds in last 2 days' then cn.name end) 'Lack_of_meds_2_days',
max(CASE when crs.name = 'PIH' and crt.code = 'PATIENT HOSPITALIZED SINCE LAST VISIT' then cn.name end) 'Patient_hospitalized_since_last_visit',
group_concat(CASE when crs.name = 'PIH' and crt.code = 'Medications prescribed at end of visit' then cn.name end separator ',') 'Medications_Prescribed',
max(CASE when crs.name = 'PIH' and crt.code = 'Medications prescribed at end of visit' then o.comments end) 'Other_meds',
max(CASE when crs.name = 'PIH' and crt.code = 'PATIENT PLAN COMMENTS' then o.value_text end) 'Patient_Plan_Comments'
from encounter e, concept_reference_map crm,  concept_reference_term crt, concept_reference_source crs, obs o
LEFT OUTER JOIN concept_name cn on o.value_coded = cn.concept_id and cn.locale = 'en' and cn.locale_preferred = '1'  and cn.voided = 0
LEFT OUTER JOIN obs obs2 on obs2.obs_id = o.obs_group_id
LEFT OUTER JOIN
(select crm2.concept_id,crs2.name, crt2.code from concept_reference_map crm2, concept_reference_term crt2, concept_reference_source crs2
where 1=1
and crm2.concept_reference_term_id = crt2.concept_reference_term_id
and crt2.concept_source_id = crs2.concept_source_id) obsgrp on obsgrp.concept_id = obs2.concept_id
where 1=1
and e.encounter_type= :ncdNoteEnc
and crm.concept_reference_term_id = crt.concept_reference_term_id
and crt.concept_source_id = crs.concept_source_id
and crm.concept_id = o.concept_id
and o.encounter_id = e.encounter_id
and e.voided = 0
and o.voided = 0
group by o.encounter_id
) obsjoins ON obsjoins.encounter_id = ep.encounter_id
--  end columns joins
WHERE p.voided = 0
-- exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt
                         AND voided = 0)
AND date(e.encounter_datetime) >= :startDate
AND date(e.encounter_datetime) <= :endDate
GROUP BY e.encounter_id
;
