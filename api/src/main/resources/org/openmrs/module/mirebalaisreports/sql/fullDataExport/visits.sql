--This export used to UNION two datasets (the second including patients registered on a day, but with no visit) but we removed this on 7-Apr-2014 as part of UHM-1162 because it is no longer necessary

--First dataset, for visits
SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(v.date_started, pr.birthdate)/365.25, 1) age_at_visit, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark, reg.encounter_datetime reg_dt, vt.name visit_type, vl.name visit_location, IF(reg.encounter_datetime IS NOT NULL, reg.encounter_datetime, v.date_started) date_started, v.date_stopped, IF(v.date_stopped IS NOT NULL, TIME_TO_SEC(TIMEDIFF(v.date_stopped, IF(reg.encounter_datetime IS NOT NULL, reg.encounter_datetime, v.date_started)))/3600, NULL) duration, chk.num num_check_in, fchk.dt first_check_in, IF(reg.encounter_datetime < fchk.dt, TIME_TO_SEC(TIMEDIFF(fchk.dt, reg.encounter_datetime))/3600, NULL) reg_to_chk, vit.num num_vit, fvit.dt first_vitals, IF(fvit.dt > fchk.dt, TIME_TO_SEC(TIMEDIFF(fvit.dt, fchk.dt))/3600, NULL) time_to_vitals, cons.num num_consulations, fcons.dt first_consultation, IF(fcons.dt > fvit.dt, TIME_TO_SEC(TIMEDIFF(fcons.dt, fvit.dt))/3600, NULL) time_to_consultation, rad.num num_radiology, frad.dt first_radiology, adm.num num_admissions, fadm.dt first_admission, pop.num num_post_op, fpop.dt first_post_op, dis.num num_discharges, fdis.dt first_discharge, tfr.num num_transfers, ftfr.dt first_transfer, IF(fdis.dt IS NOT NULL, TIME_TO_SEC(TIMEDIFF(fdis.dt, fadm.dt))/86400, NULL) ln_hospitalization, IF(first_visit.date_started = v.date_started, TRUE, FALSE) first_visit, IF(prv.date_started IS NOT NULL, TRUE, FALSE) 2nd_day, v.visit_id, pr.birthdate, pr.birthdate_estimated

FROM patient p

--Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = :zlId AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id

--ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id

--Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = :unknownPt AND un.voided = 0

--Person record
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

--Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id

--Most recent name
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id

--Visit information
INNER JOIN visit v ON p.patient_id = v.patient_id AND v.voided = 0
INNER JOIN visit_type vt ON v.visit_type_id = vt.visit_type_id
INNER JOIN location vl ON v.location_id = vl.location_id

--Registration encounter, if patient was registered same day visit started
LEFT OUTER JOIN encounter reg ON p.patient_id = reg.patient_id AND reg.encounter_type = :regEnc AND reg.voided = 0 AND DATE(reg.encounter_datetime) = DATE(v.date_started)

--Count of check-in encounters, with date and time of first check-in
LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :chkEnc AND voided = 0 GROUP BY visit_id) chk ON v.visit_id = chk.visit_id
LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :chkEnc AND voided = 0 GROUP BY visit_id) fchk ON v.visit_id = fchk.visit_id

--Count of vitals encounters, with date and time of first vitals
LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :vitEnc AND voided = 0 GROUP BY visit_id) vit ON v.visit_id = vit.visit_id
LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :vitEnc AND voided = 0 GROUP BY visit_id) fvit ON v.visit_id = fvit.visit_id

--Count of consultations, with date and time of first consultation
LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :consEnc AND voided = 0 GROUP BY visit_id) cons ON v.visit_id = cons.visit_id
LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :consEnc AND voided = 0 GROUP BY visit_id) fcons ON v.visit_id = fcons.visit_id

--Count of radiology order encounters, with date and time of first
LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :radEnc AND voided = 0 GROUP BY visit_id) rad ON v.visit_id = rad.visit_id
LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :radEnc AND voided = 0 GROUP BY visit_id) frad ON v.visit_id = frad.visit_id

--Count of admissions, with date and time of first admission
LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :admitEnc AND voided = 0 GROUP BY visit_id) adm ON v.visit_id = adm.visit_id
LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :admitEnc AND voided = 0 GROUP BY visit_id) fadm ON v.visit_id = fadm.visit_id

--Count of postop notes, with date and time of first
LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :postOpNoteEnc AND voided = 0 GROUP BY visit_id) pop ON v.visit_id = pop.visit_id
LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :postOpNoteEnc AND voided = 0 GROUP BY visit_id) fpop ON v.visit_id = fpop.visit_id

--Count of discharge encounters, with date and time of first
LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :exitEnc AND voided = 0 GROUP BY visit_id) dis ON v.visit_id = dis.visit_id
LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :exitEnc AND voided = 0 GROUP BY visit_id) fdis ON v.visit_id = fdis.visit_id

--Count of transfer encounters, with date and time of first
LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :transferEnc AND voided = 0 GROUP BY visit_id) tfr ON v.visit_id = tfr.visit_id
LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :transferEnc AND voided = 0 GROUP BY visit_id) ftfr ON v.visit_id = ftfr.visit_id

--Checks to see if this is the first visit for the patient
INNER JOIN (SELECT patient_id, MIN(date_started) date_started FROM visit WHERE voided = 0 GROUP BY patient_id) first_visit ON p.patient_id = first_visit.patient_id

--Checks to see if the patient had a separate visit the previous day
LEFT OUTER JOIN visit prv ON p.patient_id = prv.patient_id AND prv.voided = 0 AND DATE(v.date_started) = DATE(SUBDATE(prv.date_started, INTERVAL 1 DAY))

WHERE p.voided = 0

--Excludes test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt AND voided = 0)

AND v.date_started >= :startDate AND v.date_started < ADDDATE(:endDate, INTERVAL 1 DAY)

GROUP BY v.visit_id
;