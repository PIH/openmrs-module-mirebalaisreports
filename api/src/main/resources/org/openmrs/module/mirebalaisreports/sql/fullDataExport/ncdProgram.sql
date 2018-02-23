select
pid.identifier "ZL_EMR_ID",
dos.identifier "Dossier_ID",
d.given_name, d.family_name, d.birthdate, birthdate_estimated, d.gender, d.country, d.department, d.commune, d.section_communal, d.locality, d.street_landmark,
pp.date_enrolled "Enrolled_in_Program",
cn_state.name "Program_State",
cn_out.name "Program_Outcome",
last_ncd_enc.encounter_datetime "last_NCD_encounter",
obs_next_appt.value_datetime "next_NCD_appointment",
NCD_category,
HbA1c_test.Collection_Date "HbA1c_Collection_Date",
HbA1c_test.Results_Date "HbA1c_Results_Date",
HbA1c_test.HbA1c_Test_Result,
BP.encounter_datetime "Last_BP_Datetime",
BP.Systolic_Blood_Pressure,
BP.Diastolic_Blood_Pressure,
NYHA.encounter_datetime "Last_NYHA_Datetime",
NYHA.NYHA_class,
Creatinine_test.Collection_Date "Creatinine_Collection_Date",
Creatinine_test.Results_Date "Creatinine_Results_Date",
Creatinine_test.Creatinine_Test_Result,
NCD_obs.medication_compliance,
NCD_obs.visit_adherence,
NCD_obs.recent_hospitalization,
NCD_obs.Insulin,
diagname1.name "Last_Diagnosis_1",
diagname2.name "Last_Diagnosis_2",
diagname3.name "Last_Diagnosis_3",
d_nc.value_text "Last_Non_Coded_Diagnosis"
from patient p
-- only return patients in program
INNER JOIN patient_program pp on pp.patient_id = p.patient_id and pp.voided = 0 and pp.program_id in
      (select program_id from program where uuid = '515796ec-bf3a-11e7-abc4-cec278b6b50a') -- uuid of the NCD program
LEFT OUTER JOIN patient_identifier pid ON pid.patient_identifier_id = (select pid2.patient_identifier_id from patient_identifier pid2 where pid2.patient_id = p.patient_id and pid2.identifier_type = :zlId
                                                 order by pid2.preferred desc, pid2.date_created desc limit 1)
-- Dossier ID
LEFT OUTER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type =:dosId
            AND voided = 0 ORDER BY date_created DESC) dos ON p.patient_id = dos.patient_id
INNER JOIN current_name_address d on d.person_id = p.patient_id
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
    and e2.encounter_type in (:AdultNCDInitEnc, :AdultNCDFollowEnc)
    order by e2.encounter_datetime desc
    limit 1)
-- next visit (obs)
LEFT OUTER JOIN obs obs_next_appt on obs_next_appt.encounter_id = last_ncd_enc.encounter_id and obs_next_appt.concept_id =
     (select concept_id from report_mapping rm_next where rm_next.source = 'PIH' and rm_next.code = 'RETURN VISIT DATE')
     and obs_next_appt.voided = 0
-- last collected HbA1c test
  LEFT OUTER JOIN
 (
    select lab.patient_id,
    max(CASE when rm_lab.source = 'PIH' and rm_lab.code = 'HbA1c' then obs_lab.value_numeric end) 'HbA1c_Test_Result',
    max(CASE when rm_lab.source = 'PIH' and rm_lab.code = 'SPUTUM COLLECTION DATE' then obs_lab.value_datetime end) 'Collection_Date',
    max(CASE when rm_lab.source = 'PIH' and rm_lab.code = 'DATE OF LABORATORY TEST' then obs_lab.value_datetime end) 'Results_Date'
    from encounter lab
       INNER JOIN obs obs_lab on obs_lab.encounter_id = lab.encounter_id
       INNER JOIN report_mapping rm_lab on rm_lab.concept_id = obs_lab.concept_id
    where lab.encounter_id =
      (select obs_maxdate.encounter_id from obs obs_maxdate
      where obs_maxdate.concept_id = (select concept_id from report_mapping rm_max where rm_max.source = 'PIH' and rm_max.code = 'HbA1c')
      and obs_maxdate.person_id = lab.patient_id
      order by value_datetime desc limit 1)
    group by lab.patient_id
 ) HbA1c_test on HbA1c_test.patient_id = p.patient_id
 -- last collected Blood Pressure
  LEFT OUTER JOIN
 (
    select lab.patient_id,lab.encounter_datetime,
    max(CASE when rm_lab.source = 'PIH' and rm_lab.code = 'DIASTOLIC BLOOD PRESSURE' then obs_lab.value_numeric end) 'Diastolic_Blood_Pressure',
    max(CASE when rm_lab.source = 'PIH' and rm_lab.code = 'SYSTOLIC BLOOD PRESSURE' then obs_lab.value_numeric end) 'Systolic_Blood_Pressure'
    from encounter lab
       INNER JOIN obs obs_lab on obs_lab.encounter_id = lab.encounter_id
       INNER JOIN report_mapping rm_lab on rm_lab.concept_id = obs_lab.concept_id
    where lab.encounter_id =
      (select obs_maxdate.encounter_id from obs obs_maxdate
      where obs_maxdate.concept_id = (select concept_id from report_mapping rm_max where rm_max.source = 'PIH' and rm_max.code = 'DIASTOLIC BLOOD PRESSURE')
      and obs_maxdate.person_id = lab.patient_id
      order by value_datetime desc limit 1)
    group by lab.patient_id
 ) BP on BP.patient_id = p.patient_id
 -- last collected NYHA test
  LEFT OUTER JOIN
 (
    select lab.patient_id,lab.encounter_datetime,
    max(CASE when rm_lab.source = 'PIH' and rm_lab.code = 'NYHA CLASS' then cn.name end) 'NYHA_class'
    from encounter lab
       INNER JOIN obs obs_lab on obs_lab.encounter_id = lab.encounter_id
       INNER JOIN report_mapping rm_lab on rm_lab.concept_id = obs_lab.concept_id
       LEFT OUTER JOIN concept_name cn on cn.concept_id = obs_lab.value_coded and cn.locale = 'en' and cn.locale_preferred = 1
    where lab.encounter_id =
      (select obs_maxdate.encounter_id from obs obs_maxdate
      where obs_maxdate.concept_id = (select concept_id from report_mapping rm_max where rm_max.source = 'PIH' and rm_max.code = 'NYHA CLASS')
      and obs_maxdate.person_id = lab.patient_id
      order by value_datetime desc limit 1)
    group by lab.patient_id
 ) NYHA on NYHA.patient_id = p.patient_id
 -- last collected Creatinine test
  LEFT OUTER JOIN
 (
    select lab.patient_id,
    max(CASE when rm_lab.source = 'PIH' and rm_lab.code = 'Creatinine mg per dL' then obs_lab.value_numeric end) 'Creatinine_Test_Result',
    max(CASE when rm_lab.source = 'PIH' and rm_lab.code = 'SPUTUM COLLECTION DATE' then obs_lab.value_datetime end) 'Collection_Date',
    max(CASE when rm_lab.source = 'PIH' and rm_lab.code = 'DATE OF LABORATORY TEST' then obs_lab.value_datetime end) 'Results_Date'
    from encounter lab
       INNER JOIN obs obs_lab on obs_lab.encounter_id = lab.encounter_id
       INNER JOIN report_mapping rm_lab on rm_lab.concept_id = obs_lab.concept_id
    where lab.encounter_id =
      (select obs_maxdate.encounter_id from obs obs_maxdate
      where obs_maxdate.concept_id = (select concept_id from report_mapping rm_max where rm_max.source = 'PIH' and rm_max.code = 'Creatinine mg per dL')
      and obs_maxdate.person_id = lab.patient_id
      order by value_datetime desc limit 1)
    group by lab.patient_id
 ) Creatinine_test on Creatinine_test.patient_id = p.patient_id
 -- last collected indicators
  LEFT OUTER JOIN
 (
    select lab.patient_id,
    group_concat(distinct CASE when rm_ncd.source = 'PIH' and rm_ncd.code = 'NCD category' then cn.name end separator ',') 'NCD_category',
    max(CASE when rm_ncd.source = 'PIH' and rm_ncd.code = 'Lack of meds in last 2 days' then cn.name end) 'medication_compliance',
    max(CASE when rm_ncd.source = 'PIH' and rm_ncd.code = 'Appearance at appointment time' then cn.name end) 'visit_adherence',
    max(CASE when rm_ncd.source = 'PIH' and rm_ncd.code = 'PATIENT HOSPITALIZED SINCE LAST VISIT' then cn.name end) 'recent_hospitalization',
    max(CASE when rm_ncd.source = 'PIH' and rm_ncd_answer.code = 'Insulin 70/30' or rm_ncd_answer.code = 'Lente insulin' then 'Oui' end) 'Insulin'
    from encounter lab
       INNER JOIN obs obs_lab on obs_lab.encounter_id = lab.encounter_id
       INNER JOIN report_mapping rm_ncd on rm_ncd.concept_id = obs_lab.concept_id
       LEFT OUTER JOIN report_mapping rm_ncd_answer on rm_ncd_answer.concept_id = obs_lab.value_coded
       LEFT OUTER JOIN concept_name cn on cn.concept_id = obs_lab.value_coded and cn.locale = 'fr' and cn.locale_preferred = 1
    where lab.encounter_id =
      (select encounter_id from encounter last_ncd
      where last_ncd.encounter_type in (:AdultNCDInitEnc, :AdultNCDFollowEnc)
      and last_ncd.patient_id = lab.patient_id
      order by last_ncd.encounter_datetime desc limit 1)
    group by lab.patient_id
 ) NCD_obs on NCD_obs.patient_id = p.patient_id
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
 order by last_NCD_encounter desc
 ;
