SELECT p.patient_id, zl.identifier zlemr, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, e.encounter_id, e.encounter_datetime, el.name encounter_location, CONCAT(pn.given_name, ' ', pn.family_name) provider, ocn_en.name radiology_order_en, ocn_fr.name radiology_order_fr, o.accession_number, o.urgency

FROM patient p

	INNER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = :zlId AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id

	INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

	INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id

	INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = :radEnc

	INNER JOIN users u ON e.creator = u.user_id

	INNER JOIN person_name pn ON u.person_id = pn.person_id AND pn.voided = 0

	INNER JOIN location el ON e.location_id = el.location_id

	INNER JOIN orders o ON e.encounter_id = o.encounter_id AND o.voided = 0

	INNER JOIN concept_name ocn_en ON o.concept_id = ocn_en.concept_id AND ocn_en.voided = 0 AND ocn_en.locale = 'en'

	LEFT OUTER JOIN concept_name ocn_fr ON o.concept_id = ocn_fr.concept_id AND ocn_fr.voided = 0 AND ocn_fr.locale = 'fr'

WHERE p.voided = 0

			AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt AND voided = 0)

			AND e.encounter_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)

GROUP BY o.order_id

;