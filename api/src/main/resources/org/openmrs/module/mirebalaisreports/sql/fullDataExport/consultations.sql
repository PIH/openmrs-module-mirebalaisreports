SELECT p.patient_id, zl.identifier zlemr, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, e.encounter_id, e.encounter_datetime, el.name encounter_location, CONCAT(pn.given_name, ' ', pn.family_name) provider, cd.num num_coded, nc.num num_non_coded, com.value_text comments

FROM patient p

	INNER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = :zlId AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id

	INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

	INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id

	INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = :consEnc

	INNER JOIN users u ON e.creator = u.user_id

	INNER JOIN person_name pn ON u.person_id = pn.person_id AND pn.voided = 0

	INNER JOIN location el ON e.location_id = el.location_id

	LEFT OUTER JOIN (SELECT encounter_id, COUNT(obs_id) num FROM obs WHERE voided = 0 AND concept_id = :coded GROUP BY encounter_id) cd ON e.encounter_id = cd.encounter_id

	LEFT OUTER JOIN (SELECT encounter_id, COUNT(obs_id) num FROM obs WHERE voided = 0 AND concept_id = :noncoded GROUP BY encounter_id) nc ON e.encounter_id = nc.encounter_id

	LEFT OUTER JOIN obs com ON e.encounter_id = com.encounter_id AND com.voided = 0 AND com.concept_id = :comment

WHERE p.voided = 0

			AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt AND voided = 0)

			AND e.encounter_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)

GROUP BY e.encounter_id

;