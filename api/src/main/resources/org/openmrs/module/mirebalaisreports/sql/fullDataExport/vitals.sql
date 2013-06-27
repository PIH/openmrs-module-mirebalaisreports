SELECT p.patient_id, zl.identifier zlemr, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, e.encounter_id, e.encounter_datetime, el.name encounter_location, CONCAT(pn.given_name, ' ', pn.family_name) provider, wt.value_numeric weight_kg, ht.value_numeric ht_cm, ROUND(wt.value_numeric/((ht.value_numeric/100)*(ht.value_numeric/100)),1) bmi, muac.value_numeric muac, temp.value_numeric temp_c, hr.value_numeric heart_rate, rr.value_numeric resp_rate, sbp.value_numeric sys_bp, dbp.value_numeric dia_bp, o2.value_numeric o2_sat

FROM patient p

	INNER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = :zlId AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id

	INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

	INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id

	INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = :vitEnc

	INNER JOIN users u ON e.creator = u.user_id

	INNER JOIN person_name pn ON u.person_id = pn.person_id AND pn.voided = 0

	INNER JOIN location el ON e.location_id = el.location_id

	LEFT OUTER JOIN obs wt ON e.encounter_id = wt.encounter_id AND wt.voided = 0 AND wt.concept_id = :wt

	LEFT OUTER JOIN obs ht ON e.encounter_id = ht.encounter_id AND ht.voided = 0 AND ht.concept_id = :ht

	LEFT OUTER JOIN obs muac ON e.encounter_id = muac.encounter_id AND muac.voided = 0 AND muac.concept_id = :muac

	LEFT OUTER JOIN obs temp ON e.encounter_id = temp.encounter_id AND temp.voided = 0 AND temp.concept_id = :temp

	LEFT OUTER JOIN obs hr ON e.encounter_id = hr.encounter_id AND hr.voided = 0 AND hr.concept_id = :hr

	LEFT OUTER JOIN obs rr ON e.encounter_id = rr.encounter_id AND rr.voided = 0 AND rr.concept_id = :rr

	LEFT OUTER JOIN obs sbp ON e.encounter_id = sbp.encounter_id AND sbp.voided = 0 AND sbp.concept_id = :sbp

	LEFT OUTER JOIN obs dbp ON e.encounter_id = dbp.encounter_id AND dbp.voided = 0 AND dbp.concept_id = :dbp

	LEFT OUTER JOIN obs o2 ON e.encounter_id = o2.encounter_id AND o2.voided = 0 AND o2.concept_id = :o2

WHERE p.voided = 0

			AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt AND voided = 0)

			AND e.encounter_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)

GROUP BY e.encounter_id

;