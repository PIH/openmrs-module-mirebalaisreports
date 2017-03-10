SELECT dos.identifier "No Dossier", date(e.encounter_datetime) "Date de la visite", pn.given_name "Prenom",pn.family_name "Nom de famille",pr.birthdate "Date de naissance",
pr.gender "Sexe", pa.city_village "Commune", pa.address2 "Adresse" ,
IF(IFNULL(prev_checkin.encounter_id,'null')='null','1',' ') "nouvelles",
IF(IFNULL(prev_checkin.encounter_id,'null')='null',' ','1') "Subsequentes",
dx_join.clin_impr "impression clinique",
dx_join.DX_JOINED "diagnostic confirme",
IF(IFNULL(prev_diag.obs_id,'null')='null','1',' ') "cas n",
IF(IFNULL(prev_diag.obs_id,'null')='null',' ','1') "cas a"
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
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type in (:AdultInitEnc, :AdultFollowEnc, :PedInitEnc, :PedFollowEnc)
-- new/subsequent visit
LEFT OUTER JOIN encounter prev_checkin on prev_checkin.patient_id = e.patient_id and prev_checkin.encounter_type = e.encounter_type 
		and prev_checkin.encounter_datetime < e.encounter_datetime  
-- join in diagnoses and clinical impression, also concatenating all diagnoses (coded and non-coded) into one column
LEFT OUTER JOIN
	(select o.encounter_id,
		CONCAT_WS(';',
		 group_concat(CASE when crs.name = 'PIH' and crt.code = 'Diagnosis or problem, non-coded' then o.value_text end separator ';'),
		 group_concat(CASE when crs.name = 'PIH' and crt.code = 'DIAGNOSIS' then cn.name end separator ';')) DX_JOINED,
		          max(CASE when crs.name = 'PIH' and crt.code = 'CLINICAL IMPRESSION COMMENTS' then o.value_text end) clin_impr 
		from encounter e, concept_reference_map crm,  concept_reference_term crt, concept_reference_source crs, obs o
		LEFT OUTER JOIN concept_name cn on o.value_coded = cn.concept_id and cn.locale = 'fr' and cn.locale_preferred = '1'  and cn.voided = 0
		where 1=1 
		and e.encounter_type in (:AdultInitEnc, :AdultFollowEnc, :PedInitEnc, :PedFollowEnc) 
		and crm.concept_reference_term_id = crt.concept_reference_term_id
		and crt.concept_source_id = crs.concept_source_id
		and crm.concept_id = o.concept_id
		and o.encounter_id = e.encounter_id
		and e.voided = 0
		and o.voided = 0
		group by o.encounter_id
	) dx_join on dx_join.encounter_id = e.encounter_id 
-- new/repeat diagnosis
LEFT OUTER JOIN obs diagnosis on diagnosis.encounter_id = e.encounter_id and diagnosis.voided = 0
      and diagnosis.concept_id = :coded  
LEFT OUTER JOIN obs prev_diag on prev_diag.person_id = diagnosis.person_id and prev_diag.value_coded = diagnosis.value_coded and prev_diag.date_created < diagnosis.date_created
WHERE p.voided = 0
-- Exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt 
                         AND voided = 0)
AND date(e.encounter_datetime) >= :startDate 
AND date(e.encounter_datetime) <= :endDate 
GROUP BY e.encounter_id 
;

