SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark, e.encounter_datetime, el.name encounter_location,
CONCAT(pn.given_name, ' ',pn.family_name) provider,
obsjoins.* ,
obs_proc1.value_text 'Pathology_Site_1', obs_procdate1.value_datetime 'Pathology_Date_1', 
obs_proc2.value_text 'Pathology_Site_2', obs_procdate2.value_datetime 'Pathology_Date_2',
obs_proc3.value_text 'Pathology_Site_3', obs_procdate3.value_datetime 'Pathology_Date_3' 
FROM patient p
-- Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = :zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
-- ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
-- Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id =:unknownPt AND un.voided = 0
-- Gender
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
-- Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = :oncNoteEnc
INNER JOIN location el ON e.location_id = el.location_id
-- Provider Name
INNER JOIN encounter_provider ep ON ep.encounter_id = e.encounter_id and ep.voided = 0
INNER JOIN provider pv ON pv.provider_id = ep.provider_id 
INNER JOIN person_name pn ON pn.person_id = pv.person_id and pn.voided = 0
-- Joins for all other fields except pathology
INNER JOIN 
(select o.encounter_id,
group_concat(CASE when crs.name = 'PIH' and crt.code = 'Type of oncology visit' then cn.name end separator ',') 'Type_of_Oncology_Visit',
max(CASE when crs.name = 'PIH' and crt.code = 'PRESENTING HISTORY' then o.value_text end) 'Presenting_History',
max(CASE when crs.name = 'PIH' and crt.code = 'Primary diagnosis' then cn.name end) 'Primary_diagnosis',
max(CASE when crs.name = 'PIH' and crt.code = 'PHYSICAL SYSTEM COMMENT' then o.value_text end) 'Exam_Findings',
max(CASE when crs.name = 'CIEL' and crt.code = '163050' then cn.name end) 'Disease_Status',
max(CASE when crs.name = 'CIEL' and crt.code = '160379' then o.value_numeric end) 'ECOG',
max(CASE when crs.name = 'CIEL' and crt.code = '162964' then cn.name end) 'Prev_treatment',
max(CASE when crs.name = 'CIEL' and crt.code = '162965' then o.value_text end) 'Prev_Treat_Dscrpn',
max(CASE when crs.name = 'CIEL' and crt.code = '162966' then cn.name end) 'Curr_treatment',
max(CASE when crs.name = 'CIEL' and crt.code = '162967' then o.value_text end) 'Curr_Treat_Dscrpn',
group_concat(CASE when crs.name = 'CIEL' and crt.code = '163073' then cn.name end separator ',') 'Chemo_Protocol',
max(CASE when crs.name = 'CIEL' and crt.code = '163073' then o.comments end) 'Other_protocol',
max(CASE when crs.name = 'CIEL' and crt.code = '160846' then cn.name end) 'Treatment_Intent',
max(CASE when crs.name = 'PIH' and crt.code = 'CHEMOTHERAPY CYCLE NUMBER' then o.value_numeric end) 'Chemo_Cycle_Number',
max(CASE when crs.name = 'PIH' and crt.code = 'Total number of planned chemotherapy cycles' then o.value_numeric end) 'Planned_Chemo_Cycles',
max(CASE when crs.name = 'PIH' and crt.code = 'Chemotherapy treatment received' then cn.name end) 'Chemo_Treatment_Received',
max(CASE when crs.name = 'PIH' and crt.code = 'Chemotherapy treatment received' then o.value_comment end) 'Chemo_Treatment_Received_Reason',
max(CASE when crs.name = 'PIH' and crt.code = 'Chemotherapy treatment tolerated' then cn.name end) 'Chemo_Treatment_Tolerated',
max(CASE when crs.name = 'PIH' and crt.code = 'Chemotherapy treatment tolerated' then o.value_comment end) 'Chemo_Treatment_Tolerated_Description',
group_concat(CASE when crs.name = 'CIEL' and crt.code = '163075' then cn.name end separator ',') 'Chemo_Side_Effect',
max(CASE when crs.name = 'PIH' and crt.code = 'PAIN SCALE OF 0 TO 10' then o.value_numeric end) 'Pain_Scale',
max(CASE when crs.name = 'CIEL' and crt.code = '163077' then o.value_text end) 'Pain_Details',
max(CASE when crs.name = 'CIEL' and crt.code = '163059' then cn.name end) 'Patient_Plan',
max(CASE when crs.name = 'PIH' and crt.code = 'PATIENT PLAN COMMENTS' then o.value_text end) 'Patient_Plan_Details',
max(CASE when crs.name = 'PIH' and crt.code = '6731' then o.value_text end) 'Special_Considerations',
max(CASE when crs.name = 'CIEL' and crt.code = '162749' then o.value_text end) 'Important_Visit_Info'
from encounter e, concept_reference_map crm,  concept_reference_term crt, concept_reference_source crs, obs o
LEFT OUTER JOIN concept_name cn on o.value_coded = cn.concept_id and cn.locale = 'en' and cn.locale_preferred = '1'  and cn.voided = 0
where 1=1 
and e.encounter_type= :oncNoteEnc
and crm.concept_reference_term_id = crt.concept_reference_term_id
and crt.concept_source_id = crs.concept_source_id
and crm.concept_id = o.concept_id
and o.encounter_id = e.encounter_id
and e.voided = 0
and o.voided = 0
group by o.encounter_id
) obsjoins ON obsjoins.encounter_id = e.encounter_id
-- Begin joins for pathology
inner join (select crm.concept_id from concept_reference_map crm, concept_reference_term crt, concept_reference_source crs
    where crm.concept_reference_term_id = crt.concept_reference_term_id
    and crt.concept_source_id = crs.concept_source_id
    and crs.name = 'CIEL' 
    and crt.code in (160715) 
    ) procdatecode
inner join (select crm.concept_id from concept_reference_map crm, concept_reference_term crt, concept_reference_source crs
    where crm.concept_reference_term_id = crt.concept_reference_term_id
    and crt.concept_source_id = crs.concept_source_id
    and crs.name = 'CIEL' 
    and crt.code in (163049) 
    ) proccode    
left outer join obs obs_proc1 on obs_proc1.encounter_id = e.encounter_id and obs_proc1.voided = 0 and obs_proc1.concept_id = proccode.concept_id
left outer join obs obs_procdate1 on obs_procdate1.encounter_id = e.encounter_id and obs_procdate1.voided = 0 
   and obs_procdate1.concept_id = procdatecode.concept_id and obs_procdate1.obs_group_id = obs_proc1.obs_group_id
left outer join obs obs_proc2 on obs_proc2.encounter_id = e.encounter_id and obs_proc2.voided = 0 and obs_proc2.concept_id = proccode.concept_id
   and obs_proc2.obs_id != obs_proc1.obs_id
left outer join obs obs_procdate2 on obs_procdate2.encounter_id = e.encounter_id and obs_procdate2.voided = 0 
   and obs_procdate2.concept_id = procdatecode.concept_id and obs_procdate2.obs_group_id = obs_proc2.obs_group_id
left outer join obs obs_proc3 on obs_proc3.encounter_id = e.encounter_id and obs_proc3.voided = 0 and obs_proc3.concept_id = proccode.concept_id
   and obs_proc3.obs_id not in (obs_proc1.obs_id, obs_proc2.obs_id) 
left outer join obs obs_procdate3 on obs_procdate3.encounter_id = e.encounter_id and obs_procdate3.voided = 0 
   and obs_procdate3.concept_id = procdatecode.concept_id and obs_procdate3.obs_group_id = obs_proc3.obs_group_id  
-- end columns joins
WHERE p.voided = 0
-- exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt 
                         AND voided = 0)
AND date(e.encounter_datetime) >= :startDate 
AND date(e.encounter_datetime) <= :endDate 
GROUP BY e.encounter_id
;