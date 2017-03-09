--  This export used to UNION two datasets (the second including patients registered on a day, but with no visit) but we removed this on 7-Apr-2014 as part of UHM-1162 because it is no longer necessary

--  First dataset, for visits
SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(v.date_started, pr.birthdate)/365.25, 1) age_at_visit, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark, reg.encounter_datetime reg_dt, vt.name visit_type, vl.name visit_location, IF(reg.encounter_datetime IS NOT NULL, reg.encounter_datetime, v.date_started) date_started, v.date_stopped, IF(v.date_stopped IS NOT NULL, TIME_TO_SEC(TIMEDIFF(v.date_stopped, IF(reg.encounter_datetime IS NOT NULL, reg.encounter_datetime, v.date_started)))/3600, NULL) duration, 
j.num_check_in, j.first_check_in, IF(reg.encounter_datetime < j.first_check_in, TIME_TO_SEC(TIMEDIFF(j.first_check_in, reg.encounter_datetime))/3600, NULL) reg_to_chk, 
j.num_vit, j.first_vitals, IF(j.first_vitals > j.first_check_in, TIME_TO_SEC(TIMEDIFF(j.first_vitals, j.first_check_in))/3600, NULL) time_to_vitals, 
j.num_consulations, j.first_consultation, IF(j.first_consultation > j.first_vitals, TIME_TO_SEC(TIMEDIFF(j.first_consultation, j.first_vitals))/3600, NULL) time_to_consultation,
j.num_radiology, j.first_radiology, 
j.num_admissions, j.first_admission, 
j.num_post_op, j.first_post_op, 
j.num_discharges,j.first_discharge,
j.num_transfers, j.first_transfer, IF(j.first_discharge IS NOT NULL, TIME_TO_SEC(TIMEDIFF(j.first_discharge, j.first_admission))/86400, NULL) ln_hospitalization,
IF(first_visit.date_started = v.date_started, TRUE, FALSE) first_visit, IF(prv.date_started IS NOT NULL, TRUE, FALSE) 2nd_day, v.visit_id, pr.birthdate, pr.birthdate_estimated, addr_section.user_generated_id as section_communale_CDC_ID
FROM patient p

--  Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = :zlId 
AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id

--  ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id

-- Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = :unknownPt 
AND un.voided = 0

-- Person record
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

-- Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id

--  CDC ID of address
LEFT OUTER JOIN address_hierarchy_entry addr_section ON addr_section.name = pa.address3 AND addr_section.level_id = (SELECT address_hierarchy_level_id FROM address_hierarchy_level WHERE address_field='ADDRESS_3')
LEFT OUTER JOIN address_hierarchy_entry addr_commune ON addr_commune.name  = pa.city_village AND addr_commune.address_hierarchy_entry_id = addr_section.parent_id AND addr_commune.level_id = (SELECT address_hierarchy_level_id FROM address_hierarchy_level WHERE address_field='CITY_VILLAGE')
LEFT OUTER JOIN address_hierarchy_entry addr_department ON addr_department.name = pa.state_province AND addr_department.address_hierarchy_entry_id = addr_commune.parent_id AND addr_department.level_id = (SELECT address_hierarchy_level_id FROM address_hierarchy_level WHERE address_field='STATE_PROVINCE')

-- Most recent name
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id

-- Visit information
INNER JOIN visit v ON p.patient_id = v.patient_id AND v.voided = 0
INNER JOIN visit_type vt ON v.visit_type_id = vt.visit_type_id
INNER JOIN location vl ON v.location_id = vl.location_id

-- Registration encounter, if patient was registered same day visit started
LEFT OUTER JOIN encounter reg ON p.patient_id = reg.patient_id AND reg.encounter_type = :regEnc 
AND reg.voided = 0 AND DATE(reg.encounter_datetime) = DATE(v.date_started)

-- join in of counts and first encounters for check-in, vitals, consultations, radiology, admissions, post-op note, discharges and transfers
-- joining these in one query runs faster than joining in an instance of the encounters table for each column
LEFT OUTER JOIN (
    SELECT encounter_type, visit_id , 
   max(case when encounter_type = :chkEnc then num end) "num_check_in", 
   max(case when encounter_type = :chkEnc then first end) "first_check_in",
   max(case when encounter_type = :vitEnc then num end) "num_vit", 
   max(case when encounter_type = :vitEnc then first end) "first_vitals", 
   max(case when encounter_type = :consEnc then num end) "num_consulations", 
   max(case when encounter_type = :consEnc then first end) "first_consultation", 
   max(case when encounter_type = :radEnc then num end) "num_radiology",
   max(case when encounter_type = :radEnc then first end) "first_radiology", 
   max(case when encounter_type = :admitEnc then num end) "num_admissions", 
   max(case when encounter_type = :admitEnc then first end) "first_admission", 
   max(case when encounter_type = :postOpNoteEnc then num end) "num_post_op", 
   max(case when encounter_type = :postOpNoteEnc then first end) "first_post_op",
   max(case when encounter_type = :exitEnc then num end) "num_discharges", 
   max(case when encounter_type = :exitEnc then first end) "first_discharge",
   max(case when encounter_type = :transferEnc then num end) "num_transfers",
   max(case when encounter_type = :transferEnc then first end) "first_transfer" 
    FROM (
    SELECT encounter_type, visit_id, COUNT(encounter_id) num  ,MIN(encounter_datetime) first FROM encounter 
    WHERE voided = 0 
    GROUP BY encounter_type, visit_id) join_inner group by join_inner.visit_id) j ON j.visit_id = v.visit_id

-- Checks to see if this is the first visit for the patient
INNER JOIN (SELECT patient_id, MIN(date_started) date_started FROM visit WHERE voided = 0 GROUP BY patient_id) first_visit ON p.patient_id = first_visit.patient_id

-- Checks to see if the patient had a separate visit the previous day
LEFT OUTER JOIN visit prv ON p.patient_id = prv.patient_id AND prv.voided = 0 AND DATE(v.date_started) = DATE(SUBDATE(prv.date_started, INTERVAL 1 DAY))

WHERE p.voided = 0

-- Excludes test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id =:testPt 
AND voided = 0)

AND v.date_started >= :startDate 
AND v.date_started < ADDDATE(:endDate, INTERVAL 1 DAY)
GROUP BY v.visit_id
;
