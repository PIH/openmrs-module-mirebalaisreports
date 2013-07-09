SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, numzlemr.num numzlemr, nd.identifier numero_dossier, numnd.num num_nd, hivemr.identifier hivemr, numhiv.num num_hiv, pr.birthdate, pr.birthdate_estimated, pr.gender, pr.dead, pr.death_date, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark, reg.encounter_datetime date_registered, regl.name reg_location, CONCAT(regn.given_name, ' ', regn.family_name) reg_by, ROUND(DATEDIFF(reg.encounter_datetime, pr.birthdate)/365.25, 1) age_at_reg

FROM patient p

--Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = 5 AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id

--ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id

--Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = 10 AND un.voided = 0

--Number of ZL EMRs assigned to this patient
INNER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 5 AND voided = 0 GROUP BY patient_id) numzlemr ON p.patient_id = numzlemr.patient_id

--Most recent Numero Dossier
LEFT OUTER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 ORDER BY date_created DESC) nd ON p.patient_id = nd.patient_id

--Number of Numero Dossiers
LEFT OUTER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 GROUP BY patient_id) numnd ON p.patient_id = numnd.patient_id

--HIV EMR ID
LEFT OUTER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 ORDER BY date_created DESC) hivemr ON p.patient_id = hivemr.patient_id

--Number of HIV EMR IDs
LEFT OUTER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 3 AND voided = 0 GROUP BY patient_id) numhiv ON p.patient_id = numhiv.patient_id

--Person
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

--Most recent name
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id

--Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id

--First registration encounter
LEFT OUTER JOIN (SELECT patient_id, MIN(encounter_id) encounter_id FROM encounter WHERE encounter_type = 6 AND voided = 0 GROUP BY patient_id) first_reg ON p.patient_id = first_reg.patient_id
LEFT OUTER JOIN encounter reg ON first_reg.encounter_id = reg.encounter_id

--User who registered the patient
LEFT OUTER JOIN users u ON reg.creator = u.user_id
LEFT OUTER JOIN person_name regn ON u.person_id = regn.person_id

--Location registered
LEFT OUTER JOIN location regl ON reg.location_id = regl.location_id

--Only show patients with a visit or registration encounter during the period
INNER JOIN (
SELECT patient_id, date_started FROM visit WHERE voided = 0 AND date_started BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)
UNION
SELECT patient_id, encounter_datetime FROM encounter WHERE voided = 0 AND encounter_type = 6 AND encounter_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)
) list ON p.patient_id = list.patient_id

WHERE p.voided = 0

--Exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = 11 AND voided = 0)

GROUP BY p.patient_id

;