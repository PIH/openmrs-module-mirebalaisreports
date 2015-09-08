SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark, e.encounter_datetime, el.name encounter_location,
CONCAT(pn.given_name, ' ',pn.family_name) provider,obsjoins.encounter_id "Encounter_id", et.name "Encounter_Type",
diagname1.name "Diagnosis_1",
diagname2.name "Diagnosis_2",
diagname3.name "Diagnosis_3",
Cancer_Stage "Cancer_Stage",
Patient_Plan_Details "Patient_Plan_Details",
Presenting_History "Presenting_History",
Disease_Status "Disease_Status",
ECOG "ECOG",
Chemo_Protocol "Chemo_Protocol",
Other_Protocol "Other_Chemo_Protocol",
Chemo_Cycle_Number "Chemo_Cycle_Number",
Planned_Chemo_Cycles "Planned_Chemo_Cycles",
Chemo_Treatment_Received "Chemo_Treatment_Received",
Chemo_Treatment_Received_Reason "Chemo_Treatment_Received_Reason",
Chemo_Treatment_Tolerated "Chemo_Treatment_Tolerated",
Chemo_Treatment_Tolerated_Description "Chemo_Treatment_Tolerated_Description",
Chemo_Side_Effect "Chemo_Side_Effect",
Other_Side_Effect "Other_Side_Effect",
Pain_Scale "Pain_Scale",
Pain_Details "Pain_Details",
Patient_Plan "Patient_Plan",
Important_Visit_Info "Important_Visit_Info"
FROM patient p
-- Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = :zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
-- ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
-- Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = :unknownPt 
            AND un.voided = 0
-- Gender
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
--  Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type in (:oncNoteEnc, :oncIntakeEnc, :chemoEnc)
INNER JOIN location el ON e.location_id = el.location_id
-- Provider Name 
INNER JOIN encounter_provider ep ON ep.encounter_id = e.encounter_id and ep.voided = 0
INNER JOIN provider pv ON pv.provider_id = ep.provider_id 
INNER JOIN person_name pn ON pn.person_id = pv.person_id and pn.voided = 0
-- Encounter ype
INNER JOIN encounter_type et on e.encounter_type = et.encounter_type_id 
-- Joins for all other fields
INNER JOIN 
(select o.encounter_id,
max(CASE when crs.name = 'CIEL' and crt.code = '160786' then cn.name end) 'Cancer_Stage',
max(CASE when crs.name = 'PIH' and crt.code = 'PATIENT PLAN COMMENTS' then o.value_text end) 'Patient_Plan_Details',
max(CASE when crs.name = 'PIH' and crt.code = 'PRESENTING HISTORY' then o.value_text end) 'Presenting_History',
max(CASE when crs.name = 'CIEL' and crt.code = '163050' then cn.name end) 'Disease_Status',
max(CASE when crs.name = 'CIEL' and crt.code = '160379' then o.value_numeric end) 'ECOG',
group_concat(CASE when crs.name = 'CIEL' and crt.code = '163073' then cn.name end separator ',') 'Chemo_Protocol',
max(CASE when crs.name = 'CIEL' and crt.code = '163073' then o.comments end) 'Other_protocol',
max(CASE when crs.name = 'PIH' and crt.code = 'CHEMOTHERAPY CYCLE NUMBER' then o.value_numeric end) 'Chemo_Cycle_Number',
max(CASE when crs.name = 'PIH' and crt.code = 'Total number of planned chemotherapy cycles' then o.value_numeric end) 'Planned_Chemo_Cycles',
max(CASE when crs.name = 'PIH' and crt.code = 'Chemotherapy treatment received' then cn.name end) 'Chemo_Treatment_Received',
max(CASE when crs.name = 'PIH' and crt.code = 'Chemotherapy treatment received' then o.comments end) 'Chemo_Treatment_Received_Reason',
max(CASE when crs.name = 'PIH' and crt.code = 'Chemotherapy treatment tolerated' then cn.name end) 'Chemo_Treatment_Tolerated',
max(CASE when crs.name = 'PIH' and crt.code = 'Chemotherapy treatment tolerated' then o.comments end) 'Chemo_Treatment_Tolerated_Description',
group_concat(CASE when crs.name = 'CIEL' and crt.code = '163075' then cn.name end separator ',') 'Chemo_Side_Effect',
max(CASE when crs.name = 'CIEL' and crt.code = '163075' then o.comments end) 'Other_Side_Effect',
max(CASE when crs.name = 'PIH' and crt.code = 'PAIN SCALE OF 0 TO 10' then o.value_numeric end) 'Pain_Scale',
max(CASE when crs.name = 'CIEL' and crt.code = '163077' then o.value_text end) 'Pain_Details',
max(CASE when crs.name = 'CIEL' and crt.code = '163059' then cn.name end) 'Patient_Plan',
max(CASE when crs.name = 'CIEL' and crt.code = '162749' then o.value_text end) 'Important_Visit_Info'
from encounter e, concept_reference_map crm,  concept_reference_term crt, concept_reference_source crs, obs o
LEFT OUTER JOIN concept_name cn on o.value_coded = cn.concept_id and cn.locale = 'en' and cn.locale_preferred = '1'  and cn.voided = 0
where 1=1 
and e.encounter_type in (:oncNoteEnc, :oncIntakeEnc, :chemoEnc)
and crm.concept_reference_term_id = crt.concept_reference_term_id
and crt.concept_source_id = crs.concept_source_id
and crm.concept_id = o.concept_id
and o.encounter_id = e.encounter_id
and e.voided = 0
and o.voided = 0
group by o.encounter_id
) obsjoins ON obsjoins.encounter_id = e.encounter_id
-- Begin joins for diagnoses
inner join (select crm.concept_id from concept_reference_map crm, concept_reference_term crt, concept_reference_source crs
    where crm.concept_reference_term_id = crt.concept_reference_term_id
    and crt.concept_source_id = crs.concept_source_id
    and crs.name = 'PIH' 
    and crt.code = 'Primary diagnosis'
    ) diagcode
left outer join obs obs_diag1 on obs_diag1.encounter_id = e.encounter_id and obs_diag1.voided = 0 and obs_diag1.concept_id = diagcode.concept_id
left outer join concept_name diagname1 on diagname1.concept_id = obs_diag1.value_coded and diagname1.locale = 'fr' and diagname1.voided = 0 and diagname1.locale_preferred=1
left outer join obs obs_diag2 on obs_diag2.encounter_id = e.encounter_id and obs_diag2.voided = 0 and obs_diag2.concept_id = diagcode.concept_id
   and obs_diag2.obs_id != obs_diag1.obs_id
left outer join concept_name diagname2 on diagname2.concept_id = obs_diag2.value_coded and diagname2.locale = 'fr' and diagname2.voided = 0 and diagname2.locale_preferred=1
left outer join obs obs_diag3 on obs_diag3.encounter_id = e.encounter_id and obs_diag3.voided = 0 and obs_diag3.concept_id = diagcode.concept_id
   and obs_diag3.obs_id not in (obs_diag1.obs_id, obs_diag2.obs_id) 
left outer join concept_name diagname3 on diagname3.concept_id = obs_diag3.value_coded and diagname3.locale = 'fr' and diagname3.voided = 0 and diagname3.locale_preferred=1
-- end columns joins 
WHERE p.voided = 0
-- exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt 
                         AND voided = 0)
AND date(e.encounter_datetime) >= :startDate 
AND date(e.encounter_datetime) <= :endDate 
GROUP BY e.encounter_id
;