SELECT dos.identifier "No Dossier", date(e.encounter_datetime) "Date de la visite", pn.given_name "Prenom",pn.family_name "Nom de famille",ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) "Age",
pr.gender "Sexe",pa.city_village "Commune",  pa.address2 "Adresse", 
IF(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25<1,"1","") "0-1 ans",
IF(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25>=1 and DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25<5,"1","") "1-4 ans",
IF(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25>=5 and DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25<15,"1","") "5-14 ans",
IF(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25>=15 and DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25<25,"1","") "15-24 ans",
' ' as "Femmes enceintes",
IF(crt.code = 'FAMILY PLANNING SERVICES','1',' ') "Clients PF", 
crt.code,
' ' as "Autres adultes",
' ' as "Handicap moteur",
' ' as "Handicap sensoriel",
IF(IFNULL(prev_checkin.encounter_id,'null')='null','1',' ') "nouvelles",
IF(IFNULL(prev_checkin.encounter_id,'null')='null',' ','1') "Subsequentes",
reason_n.name "Services sollicites"
FROM patient p
-- Most recent Dossier ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type =:dosId
            AND voided = 0 ORDER BY date_created DESC) dos ON p.patient_id = dos.patient_id 
-- Person
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
-- Person Name
INNER JOIN person_name pn on pn.person_id = p.patient_id
-- Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
-- Most recent name
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id
-- Check in encounter
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = :chkEnc
-- Type of visit
LEFT OUTER JOIN obs reason ON e.encounter_id = reason.encounter_id AND reason.voided = 0 AND reason.concept_id =:reasonForVisit
LEFT OUTER JOIN concept_name reason_n ON reason.value_coded = reason_n.concept_id AND reason_n.voided = 0 AND reason_n.locale = 'fr' AND reason_n.locale_preferred = 1
-- Clients PF
LEFT OUTER JOIN concept_reference_map crm on crm.concept_id = reason.value_coded
LEFT OUTER JOIN concept_reference_term crt on crm.concept_reference_term_id = crt.concept_reference_term_id and crt.code = 'FAMILY PLANNING SERVICES'
LEFT OUTER JOIN concept_reference_source crs on crt.concept_source_id = crs.concept_source_id and crs.name = 'PIH'
-- new/subsequent visit
LEFT OUTER JOIN encounter prev_checkin on prev_checkin.patient_id = e.patient_id and prev_checkin.encounter_type = e.encounter_type 
		and prev_checkin.encounter_datetime < e.encounter_datetime  
WHERE p.voided = 0
-- Exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt 
                         AND voided = 0)
AND date(e.encounter_datetime) >= :startDate 
AND date(e.encounter_datetime) <= :endDate 
GROUP BY e.encounter_id;
