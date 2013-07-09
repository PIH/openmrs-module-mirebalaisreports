SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark, e.encounter_id, el.name, o.obs_id, o.obs_datetime, CONCAT(pn.given_name, ' ', pn.family_name) provider, IF(o.concept_id = 357, dn.name, o.value_text) diagnosis_entered, psn.name dx_order, scn.name certainty, IF(o.concept_id = 357, TRUE, FALSE) coded, o.value_coded diagnosis_concept, en.name diagnosis_coded_en, icd.code icd10_code,

--Checks to see if diagnosis is a member of a variety of concept sets
IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = 340), TRUE, FALSE) notifiable,
IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = 83), TRUE, FALSE) urgent,
IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = 617), TRUE, FALSE) santeFamn,
IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = 402), TRUE, FALSE) psychological,
IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = 367), TRUE, FALSE) pediatric,
IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = 380), TRUE, FALSE) outpatient,
IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = 425), TRUE, FALSE) ncd,
IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = 542), TRUE, FALSE) non_diagnosis,
IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = 427), TRUE, FALSE) ed,
IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = 341), TRUE, FALSE) age_restricted,
o.date_created,

--Mark as retrospective if more than 30 seconds elapsed between encounter date and creation
IF(TIME_TO_SEC(o.date_created) - TIME_TO_SEC(o.obs_datetime) > 30, TRUE, FALSE) retrospective

FROM patient p

--Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = 5 AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id

--ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id

--Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = 10 AND un.voided = 0

--Person
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

--Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id

--Most recent name
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id

--Consultation encounter
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = 8

--User who created consultation encounter
INNER JOIN users u ON e.creator = u.user_id
INNER JOIN person_name pn ON u.person_id = pn.person_id AND pn.voided = 0

--Location of consultation encounter
INNER JOIN location el ON e.location_id = el.location_id

--Diagnosis observation (both coded and non_coded)
INNER JOIN obs o ON e.encounter_id = o.encounter_id AND o.voided = 0 AND o.concept_id IN (357, 355)

--Diagnosis name chosen by the user
LEFT OUTER JOIN concept_name dn ON o.value_coded_name_id = dn.concept_name_id

--English diagnosis name
LEFT OUTER JOIN concept_name en ON o.value_coded = en.concept_id AND en.locale = 'en' AND en.concept_name_type = 'FULLY_SPECIFIED' AND en.voided = 0

--Diagnosis order (primary or secondary)
LEFT OUTER JOIN obs ps ON o.obs_group_id = ps.obs_group_id AND ps.concept_id = 350 AND ps.voided = 0
LEFT OUTER JOIN concept_name psn ON ps.value_coded = psn.concept_id AND psn.locale = 'en'

--Diagnosis certainty
LEFT OUTER JOIN obs sc ON o.obs_group_id = sc.obs_group_id AND sc.concept_id = 354 AND sc.voided = 0
LEFT OUTER JOIN concept_name scn ON sc.value_coded = scn.concept_id AND scn.locale = 'en'

--ICD 10 code
LEFT OUTER JOIN (SELECT crm.concept_id, crt.code FROM concept_reference_map crm INNER JOIN concept_reference_term crt ON crm.concept_reference_term_id = crt.concept_reference_term_id AND crt.concept_source_id = (SELECT concept_source_id FROM concept_reference_source WHERE concept_source_id = 4) AND crt.retired = 0) icd ON o.value_coded = icd.concept_id

WHERE p.voided = 0

--Exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = 11 AND voided = 0)

AND o.obs_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)

GROUP BY o.obs_id

;