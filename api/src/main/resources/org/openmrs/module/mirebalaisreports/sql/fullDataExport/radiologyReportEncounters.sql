SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark, e.encounter_id, e.encounter_datetime, el.name encounter_location, CONCAT(pn.given_name, ' ', pn.family_name) provider, rrc.order_id, ran.value_text accession_number, report_type_n.name report_type, proc_perf_n.name procedure_performed, comments.value_text comments, e.date_created,

--Mark as retrospective if more than 30 minutes elapsed between encounter date and creation
IF(TIME_TO_SEC(e.date_created) - TIME_TO_SEC(e.encounter_datetime) > 1800, TRUE, FALSE) retrospective

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

--Radiology report encounter
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = 10

--User who created encounter
INNER JOIN users u ON e.creator = u.user_id
INNER JOIN person_name pn ON u.person_id = pn.person_id AND pn.voided = 0

--Location of encounter
INNER JOIN location el ON e.location_id = el.location_id

--Radiology report construct observation
LEFT OUTER JOIN obs rrc ON e.encounter_id = rrc.encounter_id AND rrc.concept_id = 632 AND rrc.voided = 0

--Accession number
LEFT OUTER JOIN obs ran ON rrc.obs_id = ran.obs_group_id AND ran.concept_id = 625 AND ran.voided = 0

--Report type
LEFT OUTER JOIN obs report_type ON rrc.obs_id = report_type.obs_group_id AND report_type.concept_id = 630 AND report_type.voided = 0
LEFT OUTER JOIN concept_name report_type_n ON report_type.value_coded = report_type_n.concept_id AND report_type_n.voided = 0 AND report_type_n.locale = 'fr' AND report_type_n.locale_preferred = 1

--Procedure performed
LEFT OUTER JOIN obs proc_perf ON rrc.obs_id = proc_perf.obs_group_id AND proc_perf.concept_id = 977 AND proc_perf.voided = 0
LEFT OUTER JOIN concept_name proc_perf_n ON proc_perf.value_coded = proc_perf_n.concept_id AND proc_perf_n.voided = 0 AND proc_perf_n.locale = 'fr' AND proc_perf_n.locale_preferred = 1

--Comments
LEFT OUTER JOIN obs comments ON rrc.obs_id = comments.obs_group_id AND comments.concept_id = 625 AND comments.voided = 0

WHERE p.voided = 0

--Exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = 11 AND voided = 0)

AND e.encounter_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)

GROUP BY e.encounter_id

;