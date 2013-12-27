SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark, e.encounter_id, e.encounter_datetime, el.name encounter_location, CONCAT(pn.given_name, ' ', pn.family_name) entered_by, CONCAT(provn.given_name, ' ', provn.family_name) provider, rsc.order_id, ran.value_text accession_number, images_n.name images_available, proc_perf_n.name procedure_performed, e.date_created,

--Mark as retrospective if more than 30 minutes elapsed between encounter date and creation
IF(TIME_TO_SEC(e.date_created) - TIME_TO_SEC(e.encounter_datetime) > 1800, TRUE, FALSE) retrospective

FROM patient p

--Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = 5 AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id

--ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id

--Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = 10 AND un.voided = 0

--Person record
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

--Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id

--Most recent name
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id

--Radiology study encounter
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = 9

--User who created encounter
INNER JOIN users u ON e.creator = u.user_id
INNER JOIN person_name pn ON u.person_id = pn.person_id AND pn.voided = 0

--Provider with Radiology Technician encounter role
INNER JOIN encounter_provider ep ON e.encounter_id = ep.encounter_id AND ep.voided = 0 AND ep.encounter_role_id = 9
INNER JOIN provider epp ON ep.provider_id = epp.provider_id
INNER JOIN person_name provn ON epp.person_id = provn.person_id AND provn.voided = 0

--Location of encounter
INNER JOIN location el ON e.location_id = el.location_id

--Radiology study construct observation
LEFT OUTER JOIN obs rsc ON e.encounter_id = rsc.encounter_id AND rsc.concept_id = 627 AND rsc.voided = 0

--Radiology accession number
LEFT OUTER JOIN obs ran ON rsc.obs_id = ran.obs_group_id AND ran.concept_id = 625 AND ran.voided = 0

--Images available
LEFT OUTER JOIN obs images ON rsc.obs_id = images.obs_group_id AND images.concept_id = 626 AND images.voided = 0
LEFT OUTER JOIN concept_name images_n ON images.value_coded = images_n.concept_id AND images_n.voided = 0 AND images_n.locale = 'fr' and images_n.locale_preferred = 1

--Procedure performed
LEFT OUTER JOIN obs proc_perf ON rsc.obs_id = proc_perf.obs_group_id AND proc_perf.concept_id = 977 AND proc_perf.voided = 0
LEFT OUTER JOIN concept_name proc_perf_n ON proc_perf.value_coded = proc_perf_n.concept_id AND proc_perf_n.voided = 0 AND proc_perf_n.locale = 'fr' AND proc_perf_n.locale_preferred = 1

WHERE p.voided = 0

--Exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = 11 AND voided = 0)

AND e.encounter_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)

GROUP BY e.encounter_id

;