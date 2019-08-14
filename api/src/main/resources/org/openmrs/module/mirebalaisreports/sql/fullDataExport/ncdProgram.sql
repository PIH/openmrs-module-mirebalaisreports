select
p.patient_id,
pid.identifier "ZL_EMR_ID",
dos.identifier "Dossier_ID",
d.given_name, d.family_name, d.birthdate, birthdate_estimated, d.gender, d.country, d.department, d.commune, d.section_communal, d.locality, d.street_landmark,
pa.value as "Telephone_Number",
contact_telephone.value_text "Contact_Telephone_Number",
DATE(pp.date_enrolled) "Enrolled_in_Program",
cn_state.name "Program_State",
cn_out.name "Program_Outcome",
cn_disposition.name "Disposition",
DATE(first_ncd_enc.encounter_datetime) "first_NCD_encounter",
DATE(last_ncd_enc.encounter_datetime) "last_NCD_encounter",
DATE(obs_next_appt.value_datetime) "next_NCD_appointment",
IF(DATEDIFF(CURDATE(), obs_next_appt.value_datetime) > 30, "Oui", NULL) "30_days_past_app",
IF(obs_disposition.value_coded =
(select concept_id from report_mapping rm_dispostion where rm_dispostion.source = 'PIH' and rm_dispostion.code = 'DEATH')
OR pp.outcome_concept_id = (select concept_id from report_mapping rm_dispostion where rm_dispostion.source = 'PIH' and rm_dispostion.code = 'PATIENT DIED')
, "Oui", NULL
) Deceased,
cats.Hypertension,
cats.Diabetes,
cats.Heart_Failure,
cats.Stroke,
cats.Respiratory,
cats.Rehab,
cats.Anemia,
cats.Epilepsy,
cats.Other_Category,
nyha.classes "NYHA class",
cn_lack_meds.name "lack_of_meds",
cn_visit_adherence.name "visit_adherence",
cn_recent_hosp.name "recent_hospitalization",
meds.meds "NCD_meds_prescribed",
Case when lower(meds.meds) like '%insulin%' then 'oui' else 'non' end "prescribed_insulin",
HbA1c_results.value_numeric "HbA1c_result",
HbA1c_results.HbA1c_coll_date "HbA1c_collection_date",
DATE(HbA1c_date.value_datetime) "HbA1c_result_date",
bp_diast.value_numeric "BP_Diastolic",
bp_syst.value_numeric "BP_Systolic",
height.value_numeric Height,
weight.value_numeric weight,
creat_results.value_numeric "Creatinine_Result",
DATE(creat_results.creat_coll_date) "Creatinine_collection_date",
DATE(creat_date.value_datetime) "Creatinine_result_date",
diagname1.name "Last_Diagnosis_1",
diagname2.name "Last_Diagnosis_2",
diagname3.name "Last_Diagnosis_3",
d_nc.value_text "Last_Non_Coded_Diagnosis"
from patient p
-- only return patients in program
INNER JOIN patient_program pp on pp.patient_id = p.patient_id and pp.voided = 0 and pp.program_id in
      (select program_id from program where uuid = '515796ec-bf3a-11e7-abc4-cec278b6b50a') -- uuid of the NCD program
LEFT OUTER JOIN patient_identifier pid ON pid.patient_identifier_id = (select pid2.patient_identifier_id from patient_identifier pid2 where
 pid2.patient_id = p.patient_id and pid2.identifier_type = :zlId
                                                 order by pid2.preferred desc, pid2.date_created desc limit 1)
LEFT OUTER JOIN patient_identifier dos on dos.patient_identifier_id =
     (select patient_identifier_id from patient_identifier dos2
     where dos2.patient_id = p.patient_id
     and dos2.identifier_type = :dosId
     order by dos2.date_created desc
     limit 1)
-- Dossier ID
INNER JOIN current_name_address d on d.person_id = p.patient_id
-- Telephone number
LEFT OUTER JOIN person_attribute pa on pa.person_id = p.patient_id and pa.voided = 0 and pa.person_attribute_type_id = (select person_attribute_type_id
from person_attribute_type where uuid = "14d4f066-15f5-102d-96e4-000c29c2a5d7")
-- telephone number of contact
LEFT OUTER JOIN obs contact_telephone on contact_telephone.person_id = p.patient_id and contact_telephone.voided = 0 and contact_telephone.concept_id = (select concept_id from
report_mapping where source="PIH" and code="TELEPHONE NUMBER OF CONTACT")
-- patient state
LEFT OUTER JOIN patient_state ps on ps.patient_program_id = pp.patient_program_id and ps.end_date is null and ps.voided = 0
LEFT OUTER JOIN program_workflow_state pws on pws.program_workflow_state_id = ps.state and pws.retired = 0
LEFT OUTER JOIN concept_name cn_state on cn_state.concept_id = pws.concept_id  and cn_state.locale = 'en' and cn_state.locale_preferred = '1'  and cn_state.voided = 0
-- outcome
LEFT OUTER JOIN concept_name cn_out on cn_out.concept_id = pp.outcome_concept_id and cn_out.locale = 'en' and cn_out.locale_preferred = '1'  and cn_out.voided = 0
-- last visit
LEFT OUTER JOIN encounter last_ncd_enc on last_ncd_enc.encounter_id =
    (select encounter_id from encounter e2
    where e2.patient_id = p.patient_id
    and e2.encounter_type in (:NCDInitEnc, :NCDFollowEnc)
    and e2.voided = 0
    order by e2.encounter_datetime desc
    limit 1)
-- first visit
LEFT OUTER JOIN encounter first_ncd_enc on first_ncd_enc.encounter_id =
    (select encounter_id from encounter e3
    where e3.patient_id = p.patient_id
    and e3.encounter_type in (:NCDInitEnc, :NCDFollowEnc)
    and e3.voided = 0
    order by e3.encounter_datetime ASC
    limit 1)
-- next visit (obs)
LEFT OUTER JOIN obs obs_next_appt on obs_next_appt.encounter_id = last_ncd_enc.encounter_id and obs_next_appt.concept_id =
     (select concept_id from report_mapping rm_next where rm_next.source = 'PIH' and rm_next.code = 'RETURN VISIT DATE')
     and obs_next_appt.voided = 0
-- latest disposition
LEFT OUTER JOIN obs obs_disposition on obs_disposition.encounter_id = last_ncd_enc.encounter_id and obs_disposition.concept_id =
     (select concept_id from report_mapping rm_dispostion where rm_dispostion.source = 'PIH' and rm_dispostion.code = '8620')
     and obs_disposition.voided = 0
LEFT OUTER JOIN concept_name cn_disposition on cn_disposition.concept_id = obs_disposition.value_coded and cn_disposition.locale = 'fr' and cn_disposition.voided = 0 and cn_disposition.locale_preferred=1
-- last collected HbA1c test
LEFT OUTER JOIN
    (SELECT person_id, value_numeric, HbA1c_test.encounter_id, DATE(obs_datetime), DATE(edate.encounter_datetime) HbA1c_coll_date from obs HbA1c_test JOIN encounter
    edate ON edate.patient_id = HbA1c_test.person_id and HbA1c_test.encounter_id = edate.encounter_id AND
    HbA1c_test.voided = 0 and HbA1c_test.concept_id =
    (select concept_id from report_mapping rm_HbA1c where rm_HbA1c.source = 'PIH' and rm_HbA1c.code = 'HbA1c') and
    obs_datetime IN (select max(obs_datetime) from obs o2 where o2.voided = 0 and o2.concept_id = (select concept_id from report_mapping rm_HbA1c
    where rm_HbA1c.source = 'PIH' and rm_HbA1c.code = 'HbA1c')
    group by o2.person_id)) HbA1c_results on HbA1c_results.person_id = p.patient_id
LEFT OUTER JOIN obs HbA1c_date on HbA1c_date.encounter_id = HbA1c_results.encounter_id and HbA1c_date.voided = 0
  and HbA1c_date.concept_id =
      (select concept_id from report_mapping rm_HbA1c_date where rm_HbA1c_date.source = 'PIH' and rm_HbA1c_date.code = 'DATE OF LABORATORY TEST')
-- last collected Blood Pressure
LEFT OUTER JOIN obs bp_syst on bp_syst.obs_id =
   (select obs_id from obs o2 where o2.person_id = p.patient_id
    and o2.concept_id =
      (select concept_id from report_mapping rm_syst where rm_syst.source = 'PIH' and rm_syst.code = 'Systolic Blood Pressure')
    order by o2.obs_datetime desc limit 1
    ) and bp_syst.voided = 0
LEFT OUTER JOIN obs bp_diast on bp_diast.encounter_id = bp_syst.encounter_id
  and bp_diast.concept_id =
   (select concept_id from report_mapping rm_diast where rm_diast.source = 'PIH' and rm_diast.code = 'Diastolic Blood Pressure')
  and bp_diast.voided = 0
-- last collected Creatinine test
LEFT OUTER JOIN
(SELECT person_id, value_numeric, creat_test.encounter_id, DATE(obs_datetime), DATE(edate.encounter_datetime) creat_coll_date from obs creat_test JOIN encounter
    edate ON edate.patient_id = creat_test.person_id and creat_test.encounter_id = edate.encounter_id AND
    creat_test.voided = 0 and creat_test.concept_id =
    (select concept_id from report_mapping rm_syst where rm_syst.source = 'PIH' and rm_syst.code = 'Creatinine mg per dL') and
    obs_datetime IN (select max(obs_datetime) from obs o2 where o2.voided = 0 and o2.concept_id = (select concept_id from report_mapping rm_syst
    where rm_syst.source = 'PIH' and rm_syst.code = 'HbA1c')
    group by o2.person_id)) creat_results on creat_results.person_id = p.patient_id
LEFT OUTER JOIN obs creat_date on creat_date.encounter_id = creat_results.encounter_id
  and creat_date.concept_id =
      (select concept_id from report_mapping rm_creat_date where rm_creat_date.source = 'PIH' and rm_creat_date.code = 'DATE OF LABORATORY TEST')
   and creat_date.voided = 0
-- last collected Height, Weight
LEFT OUTER JOIN obs height on height.obs_id =
   (select obs_id from obs o2 where o2.person_id = p.patient_id
    and o2.concept_id =
      (select concept_id from report_mapping rm_syst where rm_syst.source = 'PIH' and rm_syst.code = 'HEIGHT (CM)')
    order by o2.obs_datetime desc limit 1
    ) and height.voided = 0
LEFT OUTER JOIN obs weight on weight.obs_id =
   (select obs_id from obs o2 where o2.person_id = p.patient_id
    and o2.concept_id =
      (select concept_id from report_mapping rm_syst where rm_syst.source = 'PIH' and rm_syst.code = 'WEIGHT (KG)')
    order by o2.obs_datetime desc limit 1
    ) and weight.voided = 0
-- NYHA
LEFT OUTER JOIN
  (SELECT obs_nyha.encounter_id, GROUP_CONCAT(cn_nyha.name) "classes"
  from obs obs_nyha
  LEFT OUTER JOIN concept_name cn_nyha on cn_nyha.concept_id = obs_nyha.value_coded and cn_nyha.locale = 'fr' and cn_nyha.locale_preferred = 1
  where obs_nyha.concept_id =
 (select concept_id from report_mapping rm_next where rm_next.source = 'PIH' and rm_next.code = 'NYHA CLASS')
 and obs_nyha.voided = 0
 group by 1
 ) nyha on  nyha.encounter_id = last_ncd_enc.encounter_id
-- Lack of meds
LEFT OUTER JOIN obs obs_lack_meds on obs_lack_meds.encounter_id = last_ncd_enc.encounter_id and obs_lack_meds.concept_id =
    (select concept_id from report_mapping rm_next where rm_next.source = 'PIH' and rm_next.code = 'Lack of meds in last 2 days')
    and obs_lack_meds.voided = 0
LEFT OUTER JOIN concept_name cn_lack_meds on cn_lack_meds.concept_id = obs_lack_meds.value_coded and cn_lack_meds.locale = 'fr' and cn_lack_meds.locale_preferred = 1
-- visit adherence
LEFT OUTER JOIN obs obs_visit_adherence on obs_visit_adherence.encounter_id = last_ncd_enc.encounter_id and obs_visit_adherence.concept_id =
    (select concept_id from report_mapping rm_next where rm_next.source = 'PIH' and rm_next.code = 'Appearance at appointment time')
    and obs_visit_adherence.voided = 0
LEFT OUTER JOIN concept_name cn_visit_adherence on cn_visit_adherence.concept_id = obs_visit_adherence.value_coded
  and cn_visit_adherence.locale = 'fr' and cn_visit_adherence.locale_preferred = 1 and cn_visit_adherence.voided = 0
-- recent hospitalization
LEFT OUTER JOIN obs obs_recent_hosp on obs_recent_hosp.encounter_id = last_ncd_enc.encounter_id and obs_recent_hosp.concept_id =
    (select concept_id from report_mapping rm_next where rm_next.source = 'PIH' and rm_next.code = 'PATIENT HOSPITALIZED SINCE LAST VISIT')
    and obs_recent_hosp.voided = 0
LEFT OUTER JOIN concept_name cn_recent_hosp on cn_recent_hosp.concept_id = obs_recent_hosp.value_coded and cn_recent_hosp.locale = 'fr'
  and cn_recent_hosp.locale_preferred = 1 and cn_recent_hosp.voided = 0
-- meds
LEFT OUTER JOIN
  (SELECT obs_meds.encounter_id, GROUP_CONCAT(cn_meds.name) "meds"
  from obs obs_meds
  LEFT OUTER JOIN concept_name cn_meds on cn_meds.concept_id = obs_meds.value_coded and cn_meds.locale = 'fr' and cn_meds.locale_preferred = 1 and cn_meds.voided = 0
  where obs_meds.concept_id =
 (select concept_id from report_mapping rm_next where rm_next.source = 'PIH' and rm_next.code = 'Medications prescribed at end of visit')
 and obs_meds.voided = 0
 group by obs_meds.encounter_id
 ) meds on meds.encounter_id = last_ncd_enc.encounter_id
--
-- NCD category
LEFT OUTER JOIN
  (SELECT obs_cat.encounter_id,
  max(case when rm_cat.source = 'PIH' and rm_cat.code = 'HYPERTENSION' then '1' end) 'Hypertension',
  max(case when rm_cat.source = 'PIH' and rm_cat.code = 'DIABETES' then '1' end) 'Diabetes',
  max(case when rm_cat.source = 'PIH' and rm_cat.code = 'HEART FAILURE' then '1' end) 'Heart_Failure',
  max(case when rm_cat.source = 'PIH' and rm_cat.code = 'Cerebrovascular Accident' then '1' end) 'Stroke',
  max(case when rm_cat.source = 'PIH' and rm_cat.code = 'Chronic respiratory disease program' then '1' end) 'Respiratory',
  max(case when rm_cat.source = 'PIH' and rm_cat.code = 'Rehab program' then '1' end) 'Rehab',
  max(case when rm_cat.source = 'PIH' and rm_cat.code = 'Sickle-Cell Anemia' then '1' end) 'Anemia',
  max(case when rm_cat.source = 'PIH' and rm_cat.code = 'EPILEPSY' then '1' end) 'Epilepsy',
  max(case when rm_cat.source = 'PIH' and rm_cat.code = 'OTHER' then '1' end) 'Other_Category'
  from obs obs_cat
  LEFT OUTER JOIN report_mapping rm_cat on rm_cat.concept_id = obs_cat.value_coded
  where obs_cat.concept_id =
 (select concept_id from report_mapping rm_next where rm_next.source = 'PIH' and rm_next.code = 'NCD category')
 and obs_cat.voided = 0
 group by 1
 ) cats on cats.encounter_id = last_ncd_enc.encounter_id
-- last 3 diagnoses
inner join report_mapping diag on diag.source = 'PIH' and diag.code = 'DIAGNOSIS'
inner join report_mapping diag_nc on diag_nc.source = 'PIH' and diag_nc.code = 'Diagnosis or problem, non-coded'
left outer join obs diag1 on diag1.obs_id =
  (select obs_id from obs d1
  where d1.concept_id = diag.concept_id
  and d1.voided = 0
  and d1.encounter_id = last_ncd_enc.encounter_id
  order by d1.obs_datetime asc limit 1)
left outer join concept_name diagname1 on diagname1.concept_id = diag1.value_coded and diagname1.locale = 'fr' and diagname1.voided = 0 and diagname1.locale_preferred=1
left outer join obs diag2 on diag2.obs_id =
  (select obs_id from obs d2
  where d2.concept_id = diag.concept_id
  and d2.voided = 0
  and d2.encounter_id = last_ncd_enc.encounter_id
  and d2.value_coded <> diag1.value_coded
  order by d2.obs_datetime asc limit 1)
left outer join concept_name diagname2 on diagname2.concept_id = diag2.value_coded and diagname2.locale = 'fr' and diagname2.voided = 0 and diagname2.locale_preferred=1
left outer join obs diag3 on diag3.obs_id =
  (select obs_id from obs d3
  where d3.concept_id = diag.concept_id
  and d3.voided = 0
  and d3.encounter_id = last_ncd_enc.encounter_id
  and d3.value_coded not in (diag1.value_coded,diag2.value_coded)
  order by d3.obs_datetime asc limit 1)
 left outer join concept_name diagname3 on diagname3.concept_id = diag3.value_coded and diagname3.locale = 'fr' and diagname3.voided = 0 and diagname3.locale_preferred=1
 left outer join obs d_nc on d_nc.concept_id = diag_nc.concept_id and d_nc.voided = 0 and d_nc.encounter_id = last_ncd_enc.encounter_id
 order by last_NCD_encounter desc;