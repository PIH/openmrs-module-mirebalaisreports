
SELECT 	p.patient_id, zl.identifier zlemr, numzlemr.num numzlemr, nd.identifier numero_dossier,
				numnd.num num_nd, hivemr.identifier hivemr, numhiv.num num_hiv, pr.birthdate, pr.birthdate_estimated,
				pr.gender, pr.dead, pr.death_date, pa.state_province department, pa.city_village commune,
				pa.address3 section, pa.address1 locality, pa.address2 street_landmark,
				reg.encounter_datetime date_registered, regl.name reg_location,
				CONCAT(regn.given_name, ' ', regn.family_name) reg_by,
				ROUND(DATEDIFF(reg.encounter_datetime, pr.birthdate)/365.25, 1) age_at_reg,
				COUNT(v.visit_id) num_visits

FROM 		patient p

INNER JOIN
				(SELECT patient_id, identifier
				 FROM patient_identifier
				 WHERE identifier_type = :zlId AND voided = 0
				 ORDER BY date_created DESC) zl
				ON p.patient_id = zl.patient_id

INNER JOIN
				(SELECT patient_id, COUNT(patient_identifier_id) num
				 FROM patient_identifier
				 WHERE identifier_type = :zlId AND voided = 0
				 GROUP BY patient_id) numzlemr
				ON p.patient_id = numzlemr.patient_id

LEFT OUTER JOIN
				(SELECT patient_id, identifier
				 FROM patient_identifier
				 WHERE identifier_type = :dosId AND voided = 0
				 ORDER BY date_created DESC) nd
				ON p.patient_id = nd.patient_id

LEFT OUTER JOIN
				(SELECT patient_id, COUNT(patient_identifier_id) num
				 FROM patient_identifier
				 WHERE identifier_type = :dosId AND voided = 0
				 GROUP BY patient_id) numnd
				ON p.patient_id = numnd.patient_id

LEFT OUTER JOIN
				(SELECT patient_id, identifier
				 FROM patient_identifier
				 WHERE identifier_type = :hivId AND voided = 0
				 ORDER BY date_created DESC) hivemr
				ON p.patient_id = hivemr.patient_id

LEFT OUTER JOIN
				(SELECT patient_id, COUNT(patient_identifier_id) num
				 FROM patient_identifier
				 WHERE identifier_type = :hivId AND voided = 0
				 GROUP BY patient_id) numhiv
				ON p.patient_id = numhiv.patient_id

INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

INNER JOIN
				(SELECT person_id, given_name, family_name
				 FROM person_name
				 WHERE voided = 0
				 ORDER BY date_created desc) n
				ON p.patient_id = n.person_id

LEFT OUTER JOIN
				(SELECT *
				 FROM person_address
				 WHERE voided = 0
				 ORDER BY date_created DESC) pa
				ON p.patient_id = pa.person_id

LEFT OUTER JOIN
				(SELECT patient_id, MIN(encounter_id) encounter_id
				 FROM encounter
				 WHERE encounter_type = :regEnc AND voided = 0
				 GROUP BY patient_id) first_reg
				ON p.patient_id = first_reg.patient_id

LEFT OUTER JOIN encounter reg ON first_reg.encounter_id = reg.encounter_id

LEFT OUTER JOIN users u ON reg.creator = u.user_id

LEFT OUTER JOIN person_name regn ON u.person_id = regn.person_id

LEFT OUTER JOIN location regl ON reg.location_id = regl.location_id

INNER JOIN visit v ON p.patient_id = v.patient_id AND v.voided = 0 AND v.date_started BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)

WHERE 		p.voided = 0
AND 			p.patient_id NOT IN
				 		(SELECT person_id FROM person_attribute
						 WHERE value = 'true' AND person_attribute_type_id = :testPt AND voided = 0)
GROUP BY	p.patient_id
;
