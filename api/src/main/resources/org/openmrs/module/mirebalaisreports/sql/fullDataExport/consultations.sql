SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark, e.encounter_id, e.encounter_datetime, el.name encounter_location, CONCAT(pn.given_name, ' ', pn.family_name) provider, cd.num num_coded, nc.num num_non_coded, dispo_n.name disposition, transf_out_n.name transfer_out_location, trauma_n.name trauma, trauma_type_n.name trauma_type, DATE(rvd.value_datetime) appointment, com.value_text comments, pr.death_date, adt.encounter_id, adt_t.name dispo_encounter, adt_l.name dispo_location, e.date_created,

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

--Consultation encounter
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = 8

--User who created consultation encounter
INNER JOIN users u ON e.creator = u.user_id
INNER JOIN person_name pn ON u.person_id = pn.person_id AND pn.voided = 0

--Location of consultation encounter
INNER JOIN location el ON e.location_id = el.location_id

--Number of coded diagnoses entered
LEFT OUTER JOIN (SELECT encounter_id, COUNT(obs_id) num FROM obs WHERE voided = 0 AND concept_id = 357 GROUP BY encounter_id) cd ON e.encounter_id = cd.encounter_id

--Number of non_coded diagnoses entered
LEFT OUTER JOIN (SELECT encounter_id, COUNT(obs_id) num FROM obs WHERE voided = 0 AND concept_id = 355 GROUP BY encounter_id) nc ON e.encounter_id = nc.encounter_id

--Disposition
LEFT OUTER JOIN obs dispo ON e.encounter_id = dispo.encounter_id AND dispo.voided = 0 AND dispo.concept_id = 985
LEFT OUTER JOIN concept_name dispo_n ON dispo.value_coded = dispo_n.concept_id AND dispo_n.locale = 'en'

--Transfer out location (for transfers out of the hospital)
LEFT OUTER JOIN obs transf_out ON e.encounter_id = transf_out.encounter_id AND transf_out.voided = 0 AND transf_out.concept_id = 1223
LEFT OUTER JOIN concept_name transf_out_n ON transf_out.value_coded = transf_out_n.concept_id AND transf_out_n.locale = 'en'

--Occurrence of trauma (ED only)
LEFT OUTER JOIN obs trauma ON e.encounter_id = trauma.encounter_id AND trauma.voided = 0 AND trauma.concept_id = 1215
LEFT OUTER JOIN concept_name trauma_n ON trauma.value_coded = trauma_n.concept_id AND trauma_n.locale = 'en'

--Trauma Type
LEFT OUTER JOIN obs trauma_type ON e.encounter_id = trauma_type.encounter_id AND trauma_type.voided = 0 AND trauma_type.concept_id = 1219
LEFT OUTER JOIN concept_name trauma_type_n ON trauma_type.value_coded = trauma_type_n.concept_id AND trauma_type_n.locale = 'en'

--Return visit date
LEFT OUTER JOIN obs rvd ON e.encounter_id = rvd.encounter_id AND rvd.voided = 0 AND rvd.concept_id = 1227

--Comments
LEFT OUTER JOIN obs com ON e.encounter_id = com.encounter_id AND com.voided = 0 AND com.concept_id = 359

--Include associated Admission, Discharge or Transfer Encounter if one was generated by the disposition, by searching for matches on encounter_datetime
LEFT OUTER JOIN encounter adt ON p.patient_id = adt.patient_id AND adt.encounter_type IN (11, 12, 13) AND adt.encounter_datetime = e.encounter_datetime
LEFT OUTER JOIN encounter_type adt_t ON adt.encounter_type = adt_t.encounter_type_id
LEFT OUTER JOIN location adt_l ON adt.location_id = adt_l.location_id

WHERE p.voided = 0

--Exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = 11 AND voided = 0)

AND e.encounter_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)

GROUP BY e.encounter_id

;