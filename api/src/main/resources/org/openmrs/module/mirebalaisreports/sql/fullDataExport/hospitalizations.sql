SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(adm.encounter_datetime, pr.birthdate)/365.25, 1) age_at_adm, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark,

adm.encounter_id, adm.encounter_datetime admission_datetime, adm_l.name admitting_ward, CONCAT(pn.given_name, ' ', pn.family_name) admitting_provider,

transf_1.encounter_datetime transf_1_datetime,
transf_1_l.name transf_1_location,
CONCAT(transf_1_pn.given_name, ' ', transf_1_pn.family_name) transf_1_provider,

transf_2.encounter_datetime transf_2_datetime,
transf_2_l.name transf_2_location,
CONCAT(transf_2_pn.given_name, ' ', transf_2_pn.family_name) transf_2_provider,

--transf_3.encounter_datetime transf_3_datetime,
--transf_3_l.name transf_3_location,
--CONCAT(transf_3_pn.given_name, ' ', transf_3_pn.family_name) transf_3_provider,

--transf_4.encounter_datetime transf_4_datetime,
--transf_4_l.name transf_4_location,
--CONCAT(transf_4_pn.given_name, ' ', transf_4_pn.family_name) transf_4_provider,

IF(dis_dispo.disposition IS NOT NULL, dis_dispo.disposition, IF(pr.dead = 1, 'Patient Died', 'Still Hospitalized')) outcome,
IF(dis.encounter_datetime IS NOT NULL, dis.encounter_datetime, IF(pr.death_date IS NOT NULL, pr.death_date, NOW())) outcome_datetime,
(DATEDIFF(IF(dis.encounter_datetime IS NOT NULL, dis.encounter_datetime, IF(pr.death_date IS NOT NULL, pr.death_date, NOW())), adm.encounter_datetime) + 1) length_of_hospitalization,

dis_dispo.disposition_location transfer_out_location,
IF(pr.dead = 0, NULL, IF(TIME_TO_SEC(TIMEDIFF(pr.death_date, adm.encounter_datetime))/3600 < 48, 'Died < 48hrs', 'Died >= 48 hrs')) died

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

--Admitted in a visit during the period
INNER JOIN visit v ON p.patient_id = v.patient_id AND v.voided = 0 AND v.date_started >= :startDate AND v.date_started < ADDDATE(:endDate, INTERVAL 1 DAY)
INNER JOIN encounter cons_adm ON v.visit_id = cons_adm.visit_id AND cons_adm.voided = 0 AND cons_adm.encounter_type = 8
INNER JOIN obs dispo_adm ON cons_adm.encounter_id = dispo_adm.encounter_id AND dispo_adm.concept_id = 985 AND dispo_adm.voided = 0 AND dispo_adm.value_coded = 1221
INNER JOIN encounter adm ON v.visit_id = adm.visit_id AND adm.voided = 0 AND adm.encounter_type = 12

--Provider with Consulting Clinician encounter role on admission encounter
INNER JOIN encounter_provider ep ON adm.encounter_id = ep.encounter_id AND ep.voided = 0 AND ep.encounter_role_id = 4
INNER JOIN provider epp ON ep.provider_id = epp.provider_id
INNER JOIN person_name pn ON epp.person_id = pn.person_id AND pn.voided = 0

--Location patient was admitted to
INNER JOIN location adm_l ON adm.location_id = adm_l.location_id

--Find next internal transfer encounter (1)
LEFT OUTER JOIN (SELECT * from encounter WHERE voided = 0 AND encounter_type = 13 ORDER BY encounter_datetime ASC) transf_1 ON v.visit_id = transf_1.visit_id AND transf_1.encounter_datetime > adm.encounter_datetime
LEFT OUTER JOIN location transf_1_l ON transf_1.location_id = transf_1_l.location_id
LEFT OUTER JOIN encounter_provider transf_1_ep ON transf_1.encounter_id = transf_1_ep.encounter_id AND transf_1_ep.voided = 0 AND transf_1_ep.encounter_role_id = 4
LEFT OUTER JOIN provider transf_1_epp ON transf_1_ep.provider_id = transf_1_epp.provider_id
LEFT OUTER JOIN person_name transf_1_pn ON transf_1_epp.person_id = transf_1_pn.person_id AND transf_1_pn.voided = 0

--Find next internal transfer encounter (2)
LEFT OUTER JOIN (SELECT * from encounter WHERE voided = 0 AND encounter_type = 13 ORDER BY encounter_datetime ASC) transf_2 ON v.visit_id = transf_2.visit_id AND transf_2.encounter_datetime > transf_1.encounter_datetime
LEFT OUTER JOIN location transf_2_l ON transf_2.location_id = transf_2_l.location_id
LEFT OUTER JOIN encounter_provider transf_2_ep ON transf_2.encounter_id = transf_2_ep.encounter_id AND transf_2_ep.voided = 0 AND transf_2_ep.encounter_role_id = 4
LEFT OUTER JOIN provider transf_2_epp ON transf_2_ep.provider_id = transf_2_epp.provider_id
LEFT OUTER JOIN person_name transf_2_pn ON transf_2_epp.person_id = transf_2_pn.person_id AND transf_2_pn.voided = 0

--Commenting these out in hopes that the query runs faster...
--Find next internal transfer encounter (3)
--LEFT OUTER JOIN (SELECT * from encounter WHERE voided = 0 AND encounter_type = 13 ORDER BY encounter_datetime ASC) transf_3 ON v.visit_id = transf_3.visit_id AND transf_3.encounter_datetime > transf_2.encounter_datetime
--LEFT OUTER JOIN location transf_3_l ON transf_3.location_id = transf_3_l.location_id
--LEFT OUTER JOIN users transf_3_u ON transf_3.creator = transf_3_u.user_id
--LEFT OUTER JOIN person_name transf_3_pn ON transf_3_u.person_id = transf_3_pn.person_id AND transf_3_pn.voided = 0

--Find next internal transfer encounter (4)
--LEFT OUTER JOIN (SELECT * from encounter WHERE voided = 0 AND encounter_type = 13 ORDER BY encounter_datetime ASC) transf_4 ON v.visit_id = transf_4.visit_id AND transf_4.encounter_datetime > transf_3.encounter_datetime
--LEFT OUTER JOIN location transf_4_l ON transf_4.location_id = transf_4_l.location_id
--LEFT OUTER JOIN users transf_4_u ON transf_4.creator = transf_4_u.user_id
--LEFT OUTER JOIN person_name transf_4_pn ON transf_4_u.person_id = transf_4_pn.person_id AND transf_4_pn.voided = 0

--Find discharge encounter
LEFT OUTER JOIN encounter dis ON v.visit_id = dis.visit_id AND dis.voided = 0 AND dis.encounter_type = 11
LEFT OUTER JOIN location dis_l ON dis.location_id = dis_l.location_id
LEFT OUTER JOIN users dis_u ON dis.creator = dis_u.user_id

--Find associated discharge disposition encounter
LEFT OUTER JOIN (SELECT e.visit_id, e.encounter_datetime, e.location_id, e.creator, n.name disposition, dispo_loc_n.name disposition_location
FROM encounter e
INNER JOIN obs o ON e.encounter_id = o.encounter_id AND o.voided = 0 AND o.concept_id = 985 AND o.value_coded IN (461, 984, 983, 981, 1220, 1228)
INNER JOIN concept_name n ON o.value_coded = n.concept_id AND n.locale = 'fr' AND n.locale_preferred = 1
LEFT OUTER JOIN obs dispo_loc ON e.encounter_id = dispo_loc.encounter_id AND dispo_loc.concept_id = 1223 AND dispo_loc.voided = 0
LEFT OUTER JOIN concept_name dispo_loc_n ON dispo_loc.value_coded = dispo_loc_n.concept_id AND dispo_loc_n.locale = 'fr' AND dispo_loc_n.locale_preferred = 1
WHERE e.encounter_type = 8) dis_dispo ON v.visit_id = dis_dispo.visit_id

WHERE p.voided = 0

--Exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = 11 AND voided = 0)

GROUP BY v.visit_id

;