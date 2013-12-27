SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark, e.encounter_id, e.encounter_datetime, el.name encounter_location, CONCAT(pn.given_name, ' ', pn.family_name) entered_by, CONCAT(provn.given_name, ' ', provn.family_name) provider, wt.value_numeric weight_kg, ht.value_numeric ht_cm, ROUND(wt.value_numeric/((ht.value_numeric/100)*(ht.value_numeric/100)),1) bmi, muac.value_numeric muac, temp.value_numeric temp_c, hr.value_numeric heart_rate, rr.value_numeric resp_rate, sbp.value_numeric sys_bp, dbp.value_numeric dia_bp, o2.value_numeric o2_sat, e.date_created,

--Mark as retrospective if more than 30 minutes elapsed between encounter date and creation
IF(TIME_TO_SEC(e.date_created) - TIME_TO_SEC(e.encounter_datetime) > 1800, TRUE, FALSE) retrospective

FROM patient p

--Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = 5 AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id

--ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id

--Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = 10 AND un.voided = 0

--Person
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

--Most recent person address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id

--Most recent person name
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id

--Vitals encounter
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = 5

--User who created vitals encounter
INNER JOIN users u ON e.creator = u.user_id
INNER JOIN person_name pn ON u.person_id = pn.person_id AND pn.voided = 0

--Provider with Nurse encounter role in vitals encounters
INNER JOIN encounter_provider ep ON e.encounter_id = ep.encounter_id AND ep.voided = 0 AND ep.encounter_role_id = 3
INNER JOIN provider epp ON ep.provider_id = epp.provider_id
INNER JOIN person_name provn ON epp.person_id = provn.person_id AND provn.voided = 0

--Location of vitals encounter
INNER JOIN location el ON e.location_id = el.location_id

--Weight (kg)
LEFT OUTER JOIN obs wt ON e.encounter_id = wt.encounter_id AND wt.voided = 0 AND wt.concept_id = 344

--Height (cm)
LEFT OUTER JOIN obs ht ON e.encounter_id = ht.encounter_id AND ht.voided = 0 AND ht.concept_id = 529

--MUAC
LEFT OUTER JOIN obs muac ON e.encounter_id = muac.encounter_id AND muac.voided = 0 AND muac.concept_id = 528

--TEMP (C)
LEFT OUTER JOIN obs temp ON e.encounter_id = temp.encounter_id AND temp.voided = 0 AND temp.concept_id = 345

--Heart Rate
LEFT OUTER JOIN obs hr ON e.encounter_id = hr.encounter_id AND hr.voided = 0 AND hr.concept_id = 430

--Respiratory Rate
LEFT OUTER JOIN obs rr ON e.encounter_id = rr.encounter_id AND rr.voided = 0 AND rr.concept_id = 346

--Systolic blood pressure
LEFT OUTER JOIN obs sbp ON e.encounter_id = sbp.encounter_id AND sbp.voided = 0 AND sbp.concept_id = 342

--Diastolic blood pressure
LEFT OUTER JOIN obs dbp ON e.encounter_id = dbp.encounter_id AND dbp.voided = 0 AND dbp.concept_id = 429

--O2 Saturation
LEFT OUTER JOIN obs o2 ON e.encounter_id = o2.encounter_id AND o2.voided = 0 AND o2.concept_id = 343

WHERE p.voided = 0

--Exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = 11 AND voided = 0)

AND e.encounter_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)

GROUP BY e.encounter_id

;