SELECT p.patient_id, zl.identifier zlemr, pr.gender, ROUND(DATEDIFF(v.date_started, pr.birthdate)/365.25, 1) age_at_visit, reg.encounter_datetime reg_dt, vt.name visit_type, vl.name visit_location, v.date_started, v.date_stopped, IF(v.date_stopped IS NOT NULL, TIME_TO_SEC(TIMEDIFF(v.date_stopped, v.date_started))/3600, NULL) duration, chk.num num_check_in, fchk.dt first_check_in, IF(reg.encounter_datetime < fchk.dt, TIME_TO_SEC(TIMEDIFF(fchk.dt, reg.encounter_datetime))/3600, NULL) reg_to_chk, vit.num num_vit, fvit.dt first_vitals, IF(fvit.dt > fchk.dt, TIME_TO_SEC(TIMEDIFF(fvit.dt, fchk.dt))/3600, NULL) time_to_vitals, cons.num num_consulations, fcons.dt first_consultation, IF(fcons.dt > fvit.dt, TIME_TO_SEC(TIMEDIFF(fcons.dt, fvit.dt))/3600, NULL) time_to_consultation, rad.num num_radiology, frad.dt first_radiology, IF(first_visit.date_started = v.date_started, TRUE, FALSE) first_visit, IF(prv.date_started IS NOT NULL, TRUE, FALSE) 2nd_day

FROM patient p

	INNER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = :zlId AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id

	INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

	INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id

	INNER JOIN visit v ON p.patient_id = v.patient_id AND v.voided = 0

	INNER JOIN visit_type vt ON v.visit_type_id = vt.visit_type_id

	INNER JOIN location vl ON v.location_id = vl.location_id

	LEFT OUTER JOIN encounter reg ON p.patient_id = reg.patient_id AND reg.encounter_type = :regEnc AND reg.voided = 0 AND DATE(reg.encounter_datetime) = DATE(v.date_started)

	LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :chkEnc AND voided = 0 GROUP BY visit_id) chk ON v.visit_id = chk.visit_id

	LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :chkEnc AND voided = 0 GROUP BY visit_id) fchk ON v.visit_id = fchk.visit_id

	LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :vitEnc AND voided = 0 GROUP BY visit_id) vit ON v.visit_id = vit.visit_id

	LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :vitEnc AND voided = 0 GROUP BY visit_id) fvit ON v.visit_id = fvit.visit_id

	LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :consEnc AND voided = 0 GROUP BY visit_id) cons ON v.visit_id = cons.visit_id

	LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :consEnc AND voided = 0 GROUP BY visit_id) fcons ON v.visit_id = fcons.visit_id

	LEFT OUTER JOIN (SELECT visit_id, COUNT(encounter_id) num FROM encounter WHERE encounter_type = :radEnc AND voided = 0 GROUP BY visit_id) rad ON v.visit_id = rad.visit_id

	LEFT OUTER JOIN (SELECT visit_id, MIN(encounter_datetime) dt FROM encounter WHERE encounter_type = :radEnc AND voided = 0 GROUP BY visit_id) frad ON v.visit_id = frad.visit_id

	INNER JOIN (SELECT patient_id, MIN(date_started) date_started FROM visit WHERE voided = 0 GROUP BY patient_id) first_visit ON p.patient_id = first_visit.patient_id

	LEFT OUTER JOIN visit prv ON p.patient_id = prv.patient_id AND prv.voided = 0 AND DATE(v.date_started) = DATE(SUBDATE(prv.date_started, INTERVAL 1 DAY))

WHERE p.voided = 0

			AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt AND voided = 0)

			AND v.date_started BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)

GROUP BY v.visit_id

ORDER BY v.date_started
;