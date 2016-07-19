SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark,  e.encounter_datetime, el.name encounter_location,
CONCAT(pn.given_name, ' ',pn.family_name) provider,
obsjoins.*
FROM patient p
-- Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type =:zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
-- ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
-- Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id =:unknownPt
            AND un.voided = 0
-- Gender
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
-- Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type =:EDTriageEnc
INNER JOIN location el ON e.location_id = el.location_id
-- Provider Name
INNER JOIN encounter_provider ep ON ep.encounter_id = e.encounter_id and ep.voided = 0
INNER JOIN provider pv ON pv.provider_id = ep.provider_id
INNER JOIN person_name pn ON pn.person_id = pv.person_id and pn.voided = 0
-- Straight Obs Joins
INNER JOIN
(select o.encounter_id,
-- These all have individual questions...the others have the same question but will soon be in different sets!
max(CASE when crs.name = 'PIH' and crt.code = 'Triage queue status' then cn.name end) 'Triage_queue_status',
max(CASE when crs.name = 'PIH' and crt.code = 'Triage color classification' then cn.name end) 'Triage_Color',
max(CASE when crs.name = 'PIH' and crt.code = 'Triage score' then o.value_numeric end) 'Triage_Score',
max(CASE when crs.name = 'CIEL' and crt.code = '160531' then o.value_text end) 'Chief_Complaint',
max(CASE when crs.name = 'PIH' and crt.code = 'WEIGHT (KG)' then o.value_numeric end) 'Weight_(KG)',
max(CASE when crs.name = 'PIH' and crt.code = 'Mobility' then cn.name end) 'Mobility',
max(CASE when crs.name = 'PIH' and crt.code = 'RESPIRATORY RATE' then o.value_numeric end) 'Respiratory_Rate',
max(CASE when crs.name = 'PIH' and crt.code = 'BLOOD OXYGEN SATURATION' then o.value_numeric end) 'Blood_Oxygen_Saturation',
max(CASE when crs.name = 'PIH' and crt.code = 'PULSE' then o.value_numeric end) 'Pulse',
max(CASE when crs.name = 'PIH' and crt.code = 'SYSTOLIC BLOOD PRESSURE' then o.value_numeric end) 'Systolic_Blood_Pressure',
max(CASE when crs.name = 'PIH' and crt.code = 'DIASTOLIC BLOOD PRESSURE' then o.value_numeric end) 'Diastolic_Blood_Pressure',
max(CASE when crs.name = 'PIH' and crt.code = 'TEMPERATURE (C)' then o.value_numeric end) 'Temperature_(C)',
max(CASE when sets.name = 'PIH' and sets.code = 'Response triage symptom' then cn.name end) 'Response',
max(CASE when sets.name = 'PIH' and sets.code = 'Neurological triage symptom' then cn.name end) 'Neurological',
max(CASE when sets.name = 'PIH' and sets.code = 'Burn triage symptom' then cn.name end) 'Burn',
max(CASE when sets.name = 'PIH' and sets.code = 'Glucose triage symptom' then cn.name end) 'Glucose',
max(CASE when sets.name = 'PIH' and sets.code = 'Trauma triage symptom' then cn.name end) 'Trauma',
max(CASE when sets.name = 'PIH' and sets.code = 'Digestive triage symptom' then cn.name end) 'Digestive',
max(CASE when sets.name = 'PIH' and sets.code = 'Pregrancy triage symptom' then cn.name end) 'Pregnancy',
max(CASE when sets.name = 'PIH' and sets.code = 'Respiratory triage symptom' then cn.name end) 'Respiratory',
max(CASE when sets.name = 'PIH' and sets.code = 'Pain triage symptom' then cn.name end) 'Pain',
max(CASE when sets.name = 'PIH' and sets.code = 'Other triage symptom' then cn.name end) 'Other'
from encounter e, concept_reference_map crm,  concept_reference_term crt, concept_reference_source crs, obs o
LEFT OUTER JOIN concept_name cn on o.value_coded = cn.concept_id and cn.locale = 'en' and cn.locale_preferred = '1'  and cn.voided = 0
LEFT OUTER JOIN obs obs2 on obs2.obs_id = o.obs_group_id
LEFT OUTER JOIN
(select crm2.concept_id,crs2.name, crt2.code from concept_reference_map crm2, concept_reference_term crt2, concept_reference_source crs2
where 1=1
and crm2.concept_reference_term_id = crt2.concept_reference_term_id
and crt2.concept_source_id = crs2.concept_source_id) obsgrp on obsgrp.concept_id = obs2.concept_id
-- The following brings in the tables for the observations whose answers are contained in sets.
-- Note that this would only work if the answers of each question are in unique sets
LEFT OUTER JOIN
(select crss.name, crts.code,cs.concept_id  from concept_reference_source crss, concept_reference_term crts, concept_reference_map crms, concept_set cs
where crms.concept_reference_term_id = crts.concept_reference_term_id
and crts.concept_source_id = crss.concept_source_id
-- and crss.name = 'PIH'
-- and crts.code = 'Burn triage symptom'
and cs.concept_set = crms.concept_id) sets on sets.concept_id = o.value_coded
where 1=1
and e.encounter_type = :EDTriageEnc
and crm.concept_reference_term_id = crt.concept_reference_term_id
and crt.concept_source_id = crs.concept_source_id
and crm.concept_id = o.concept_id
and o.encounter_id = e.encounter_id
and e.voided = 0
and o.voided = 0
 group by o.encounter_id
) obsjoins ON obsjoins.encounter_id = ep.encounter_id
-- end columns joins
WHERE p.voided = 0
-- exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id =:testPt
                         AND voided = 0)
AND date(e.encounter_datetime) >= :startDate
AND date(e.encounter_datetime) <= :endDate
GROUP BY e.encounter_id
;