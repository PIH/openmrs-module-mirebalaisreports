SET sql_safe_updates = 0;

DROP TEMPORARY TABLE IF EXISTS temp_ncd_program;
DROP TEMPORARY TABLE IF EXISTS temp_ncd_last_ncd_enc;
DROP TEMPORARY TABLE IF EXISTS temp_ncd_first_ncd_enc;
DROP TEMPORARY TABLE IF EXISTS temp_ncd_meds;
DROP TEMPORARY TABLE IF EXISTS temp_latest_ncd_meds;

SELECT 
    patient_identifier_type_id
INTO @zlId FROM
    patient_identifier_type
WHERE
    name = 'ZL EMR ID';
SELECT 
    patient_identifier_type_id
INTO @dosId FROM
    patient_identifier_type
WHERE
    name = 'Nimewo Dosye';
SELECT 
    encounter_type_id
INTO @NCDInitEnc FROM
    encounter_type
WHERE
    UUID = 'ae06d311-1866-455b-8a64-126a9bd74171';
SELECT 
    encounter_type_id
INTO @NCDFollowEnc FROM
    encounter_type
WHERE
    UUID = '5cbfd6a2-92d9-4ad0-b526-9d29bfe1d10c';

-- latest NCD enc table
create temporary table temp_ncd_last_ncd_enc
(
  encounter_id int,
  patient_id int,
  encounter_datetime datetime,
  zlemr_id varchar(255),
  dossier_id varchar(255)
);
insert into temp_ncd_last_ncd_enc(patient_id, encounter_datetime)
  select patient_id, max(encounter_datetime) from encounter where voided = 0
  and encounter_type in (@NCDInitEnc, @NCDFollowEnc) group by patient_id order by patient_id;

UPDATE temp_ncd_last_ncd_enc tlne
        INNER JOIN
    encounter e ON tlne.patient_id = e.patient_id
        AND tlne.encounter_datetime = e.encounter_datetime 
SET 
    tlne.encounter_id = e.encounter_id;

UPDATE temp_ncd_last_ncd_enc tlne
        INNER JOIN
    (SELECT 
        patient_id, identifier
    FROM
        patient_identifier
    WHERE
        identifier_type = @zlId AND voided = 0
            AND preferred = 1
    ORDER BY date_created DESC) zl ON tlne.patient_id = zl.patient_id 
SET 
    tlne.zlemr_id = zl.identifier;

UPDATE temp_ncd_last_ncd_enc tlne
        INNER JOIN
    (SELECT 
        patient_id, MAX(identifier) dos_id
    FROM
        patient_identifier
    WHERE
        identifier_type = @dosId AND voided = 0
    GROUP BY patient_id) dos ON tlne.patient_id = dos.patient_id 
SET 
    tlne.dossier_id = dos.dos_id;

-- initial ncd enc table(ideally it should be ncd initital form only)
create temporary table temp_ncd_first_ncd_enc
(
  encounter_id int,
  patient_id int,
  encounter_datetime datetime
);
insert into temp_ncd_first_ncd_enc(patient_id, encounter_datetime)
  select patient_id, min(encounter_datetime) from encounter where voided = 0
  and encounter_type in (@NCDInitEnc, @NCDFollowEnc) group by patient_id order by patient_id;

UPDATE temp_ncd_first_ncd_enc tfne
        INNER JOIN
    encounter e ON tfne.patient_id = e.patient_id
        AND tfne.encounter_datetime = e.encounter_datetime 
SET 
    tfne.encounter_id = e.encounter_id;

-- ncd program
create temporary table temp_ncd_program(
  patient_program_id int,
  patient_id int,
  date_enrolled datetime,
  date_completed datetime,
  location_id int,
  outcome_concept_id int,
  given_name varchar(255),
  family_name varchar(255),
  birthdate datetime,
  birthdate_estimated varchar(50),
  gender varchar(50),
  country varchar(255),
  department varchar(255),
  commune varchar(255),
  section_communal varchar(255),
  locality varchar(255),
  street_landmark varchar(255),
  telephone_number varchar(255),
  contact_telephone_number  varchar(255),
  program_state varchar(255),
  program_outcome varchar(255),
  first_ncd_encounter datetime,
  last_ncd_encounter datetime,
  next_ncd_appointment datetime,
  thirty_days_past_app varchar(11),
  disposition varchar(255),
  deceased varchar(255),
  HbA1c_result double,
  HbA1c_collection_date datetime,
  HbA1c_result_date datetime,
  bp_diastolic double,
  bp_systolic double,
  height double,
  weight double,
  creatinine_result double,
  creatinine_collection_date datetime,
  creatinine_result_date datetime,
  nyha_classes text,
  lack_of_meds text,
  visit_adherence text,
  recent_hospitalization text,
  hypertension int,
  diabetes int,
  heart_Failure int,
  stroke int,
  respiratory int,
  rehab int,
  anemia int,
  epilepsy int,
  other_Category int,
  last_diagnosis_1 text,
  last_diagnosis_2 text,
  last_diagnosis_3 text,
  last_non_coded_diagnosis text
);

insert into temp_ncd_program (patient_program_id, patient_id, date_enrolled, date_completed, location_id, outcome_concept_id)
  select
  patient_program_id,
  patient_id,
  DATE(date_enrolled),
  DATE(date_completed),
  location_id,
  outcome_concept_id
  from patient_program where voided = 0 and program_id in (select program_id from program where uuid = '515796ec-bf3a-11e7-abc4-cec278b6b50a') -- uuid of the NCD program
  order by patient_id;

UPDATE temp_ncd_program p
        INNER JOIN
    current_name_address d ON d.person_id = p.patient_id 
SET 
    p.given_name = d.given_name,
    p.family_name = d.family_name,
    p.birthdate = d.birthdate,
    p.birthdate_estimated = d.birthdate_estimated,
    p.gender = d.gender,
    p.country = d.country,
    p.department = d.department,
    p.commune = d.commune,
    p.section_communal = d.section_communal,
    p.locality = d.locality,
    p.street_landmark = d.street_landmark;

-- Telephone number
UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    person_attribute pa ON pa.person_id = p.patient_id
        AND pa.voided = 0
        AND pa.person_attribute_type_id = (SELECT 
            person_attribute_type_id
        FROM
            person_attribute_type
        WHERE
            uuid = '14d4f066-15f5-102d-96e4-000c29c2a5d7') 
SET 
    p.telephone_number = pa.value;

-- telephone number of contact
UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    obs contact_telephone ON contact_telephone.person_id = p.patient_id
        AND contact_telephone.voided = 0
        AND contact_telephone.concept_id = (SELECT 
            concept_id
        FROM
            report_mapping
        WHERE
            source = 'PIH'
                AND code = 'TELEPHONE NUMBER OF CONTACT') 
SET 
    p.contact_telephone_number = contact_telephone.value_text;

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    patient_state ps ON ps.patient_program_id = p.patient_program_id
        AND ps.end_date IS NULL
        AND ps.voided = 0
        LEFT OUTER JOIN
    program_workflow_state pws ON pws.program_workflow_state_id = ps.state
        AND pws.retired = 0
        LEFT OUTER JOIN
    concept_name cn_state ON cn_state.concept_id = pws.concept_id
        AND cn_state.locale = 'en'
        AND cn_state.locale_preferred = '1'
        AND cn_state.voided = 0
        LEFT OUTER JOIN
    concept_name cn_out ON cn_out.concept_id = p.outcome_concept_id
        AND cn_out.locale = 'en'
        AND cn_out.locale_preferred = '1'
        AND cn_out.voided = 0 
SET 
    p.program_state = cn_state.name,
    p.program_outcome = cn_out.name;

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    temp_ncd_first_ncd_enc first_ncd_enc ON first_ncd_enc.patient_id = p.patient_id 
SET 
    p.first_ncd_encounter = DATE(first_ncd_enc.encounter_datetime);

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    temp_ncd_last_ncd_enc last_ncd_enc ON last_ncd_enc.patient_id = p.patient_id
        LEFT OUTER JOIN
    obs obs_next_appt ON obs_next_appt.encounter_id = last_ncd_enc.encounter_id
        AND obs_next_appt.concept_id = (SELECT 
            concept_id
        FROM
            report_mapping rm_next
        WHERE
            rm_next.source = 'PIH'
                AND rm_next.code = 'RETURN VISIT DATE')
        AND obs_next_appt.voided = 0 
SET 
    p.last_ncd_encounter = DATE(last_ncd_enc.encounter_datetime),
    p.next_ncd_appointment = DATE(obs_next_appt.value_datetime),
    p.thirty_days_past_app = IF(DATEDIFF(CURDATE(), obs_next_appt.value_datetime) > 30,
        'Oui',
        NULL);

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    temp_ncd_last_ncd_enc ON p.patient_id = temp_ncd_last_ncd_enc.patient_id
        LEFT OUTER JOIN
    obs obs_disposition ON obs_disposition.encounter_id = temp_ncd_last_ncd_enc.encounter_id
        AND obs_disposition.voided = 0
        AND obs_disposition.concept_id = (SELECT 
            concept_id
        FROM
            report_mapping rm_dispostion
        WHERE
            rm_dispostion.source = 'PIH'
                AND rm_dispostion.code = '8620')
        LEFT OUTER JOIN
    concept_name cn_disposition ON cn_disposition.concept_id = obs_disposition.value_coded
        AND cn_disposition.locale = 'fr'
        AND cn_disposition.voided = 0
        AND cn_disposition.locale_preferred = 1 
SET 
    p.disposition = cn_disposition.name,
    p.deceased = IF(obs_disposition.value_coded = (SELECT 
                concept_id
            FROM
                report_mapping rm_dispostion
            WHERE
                rm_dispostion.source = 'PIH'
                    AND rm_dispostion.code = 'DEATH')
            OR p.outcome_concept_id = (SELECT 
                concept_id
            FROM
                report_mapping rm_dispostion
            WHERE
                rm_dispostion.source = 'PIH'
                    AND rm_dispostion.code = 'PATIENT DIED'),
        'Oui',
        NULL);

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    (SELECT 
        person_id,
            value_numeric,
            HbA1c_test.encounter_id,
            DATE(obs_datetime),
            DATE(edate.encounter_datetime) HbA1c_coll_date
    FROM
        obs HbA1c_test
    JOIN encounter edate ON edate.patient_id = HbA1c_test.person_id
        AND HbA1c_test.encounter_id = edate.encounter_id
        AND HbA1c_test.voided = 0
        AND HbA1c_test.concept_id = (SELECT 
            concept_id
        FROM
            report_mapping rm_HbA1c
        WHERE
            rm_HbA1c.source = 'PIH'
                AND rm_HbA1c.code = 'HbA1c')
        AND obs_datetime IN (SELECT 
            MAX(obs_datetime)
        FROM
            obs o2
        WHERE
            o2.voided = 0
                AND o2.concept_id = (SELECT 
                    concept_id
                FROM
                    report_mapping rm_HbA1c
                WHERE
                    rm_HbA1c.source = 'PIH'
                        AND rm_HbA1c.code = 'HbA1c')
        GROUP BY o2.person_id)) HbA1c_results ON HbA1c_results.person_id = p.patient_id
        LEFT OUTER JOIN
    obs HbA1c_date ON HbA1c_date.encounter_id = HbA1c_results.encounter_id
        AND HbA1c_date.voided = 0
        AND HbA1c_date.concept_id = (SELECT 
            concept_id
        FROM
            report_mapping rm_HbA1c_date
        WHERE
            rm_HbA1c_date.source = 'PIH'
                AND rm_HbA1c_date.code = 'DATE OF LABORATORY TEST') 
SET 
    p.HbA1c_result = HbA1c_results.value_numeric,
    p.HbA1c_collection_date = HbA1c_results.HbA1c_coll_date,
    p.HbA1c_result_date = DATE(HbA1c_date.value_datetime);

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    obs bp_syst ON bp_syst.obs_id = (SELECT 
            obs_id
        FROM
            obs o2
        WHERE
            o2.person_id = p.patient_id
                AND o2.concept_id = (SELECT 
                    concept_id
                FROM
                    report_mapping rm_syst
                WHERE
                    rm_syst.source = 'PIH'
                        AND rm_syst.code = 'Systolic Blood Pressure')
        ORDER BY o2.obs_datetime DESC
        LIMIT 1)
        AND bp_syst.voided = 0
        LEFT OUTER JOIN
    obs bp_diast ON bp_diast.encounter_id = bp_syst.encounter_id
        AND bp_diast.concept_id = (SELECT 
            concept_id
        FROM
            report_mapping rm_diast
        WHERE
            rm_diast.source = 'PIH'
                AND rm_diast.code = 'Diastolic Blood Pressure')
        AND bp_diast.voided = 0 
SET 
    p.bp_diastolic = bp_diast.value_numeric,
    p.bp_systolic = bp_syst.value_numeric;

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    (SELECT 
        person_id,
            value_numeric,
            creat_test.encounter_id,
            DATE(obs_datetime),
            DATE(edate.encounter_datetime) creat_coll_date
    FROM
        obs creat_test
    JOIN encounter edate ON edate.patient_id = creat_test.person_id
        AND creat_test.encounter_id = edate.encounter_id
        AND creat_test.voided = 0
        AND creat_test.concept_id = (SELECT 
            concept_id
        FROM
            report_mapping rm_syst
        WHERE
            rm_syst.source = 'PIH'
                AND rm_syst.code = 'Creatinine mg per dL')
        AND obs_datetime IN (SELECT 
            MAX(obs_datetime)
        FROM
            obs o2
        WHERE
            o2.voided = 0
                AND o2.concept_id = (SELECT 
                    concept_id
                FROM
                    report_mapping rm_syst
                WHERE
                    rm_syst.source = 'PIH'
                        AND rm_syst.code = 'HbA1c')
        GROUP BY o2.person_id)) creat_results ON creat_results.person_id = p.patient_id
        LEFT OUTER JOIN
    obs creat_date ON creat_date.encounter_id = creat_results.encounter_id
        AND creat_date.concept_id = (SELECT 
            concept_id
        FROM
            report_mapping rm_creat_date
        WHERE
            rm_creat_date.source = 'PIH'
                AND rm_creat_date.code = 'DATE OF LABORATORY TEST')
        AND creat_date.voided = 0 
SET 
    p.creatinine_result = creat_results.value_numeric,
    p.creatinine_collection_date = DATE(creat_results.creat_coll_date),
    p.creatinine_result_date = DATE(creat_date.value_datetime);

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    obs height ON height.obs_id = (SELECT 
            obs_id
        FROM
            obs o2
        WHERE
            o2.person_id = p.patient_id
                AND o2.concept_id = (SELECT 
                    concept_id
                FROM
                    report_mapping rm_syst
                WHERE
                    rm_syst.source = 'PIH'
                        AND rm_syst.code = 'HEIGHT (CM)')
        ORDER BY o2.obs_datetime DESC
        LIMIT 1)
        AND height.voided = 0
        LEFT OUTER JOIN
    obs weight ON weight.obs_id = (SELECT 
            obs_id
        FROM
            obs o2
        WHERE
            o2.person_id = p.patient_id
                AND o2.concept_id = (SELECT 
                    concept_id
                FROM
                    report_mapping rm_syst
                WHERE
                    rm_syst.source = 'PIH'
                        AND rm_syst.code = 'WEIGHT (KG)')
        ORDER BY o2.obs_datetime DESC
        LIMIT 1)
        AND weight.voided = 0 
SET 
    p.height = height.value_numeric,
    p.weight = weight.value_numeric;

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    temp_ncd_last_ncd_enc ON p.patient_id = temp_ncd_last_ncd_enc.patient_id
        LEFT OUTER JOIN
    (SELECT 
        obs_nyha.encounter_id,
            GROUP_CONCAT(cn_nyha.name
                SEPARATOR ' | ') 'classes'
    FROM
        obs obs_nyha
    LEFT OUTER JOIN concept_name cn_nyha ON cn_nyha.concept_id = obs_nyha.value_coded
        AND cn_nyha.locale = 'fr'
        AND cn_nyha.locale_preferred = 1
    WHERE
        obs_nyha.concept_id = (SELECT 
                concept_id
            FROM
                report_mapping rm_next
            WHERE
                rm_next.source = 'PIH'
                    AND rm_next.code = 'NYHA CLASS')
            AND obs_nyha.voided = 0
    GROUP BY 1) nyha ON nyha.encounter_id = temp_ncd_last_ncd_enc.encounter_id 
SET 
    p.nyha_classes = nyha.classes;

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    temp_ncd_last_ncd_enc ON p.patient_id = temp_ncd_last_ncd_enc.patient_id
        LEFT OUTER JOIN
    obs obs_lack_meds ON obs_lack_meds.encounter_id = temp_ncd_last_ncd_enc.encounter_id
        AND obs_lack_meds.concept_id = (SELECT 
            concept_id
        FROM
            report_mapping rm_next
        WHERE
            rm_next.source = 'PIH'
                AND rm_next.code = 'Lack of meds in last 2 days')
        AND obs_lack_meds.voided = 0
        LEFT OUTER JOIN
    concept_name cn_lack_meds ON cn_lack_meds.concept_id = obs_lack_meds.value_coded
        AND cn_lack_meds.locale = 'fr'
        AND cn_lack_meds.locale_preferred = 1 
SET 
    p.lack_of_meds = cn_lack_meds.name;

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    temp_ncd_last_ncd_enc ON p.patient_id = temp_ncd_last_ncd_enc.patient_id
        LEFT OUTER JOIN
    obs obs_visit_adherence ON obs_visit_adherence.encounter_id = temp_ncd_last_ncd_enc.encounter_id
        AND obs_visit_adherence.concept_id = (SELECT 
            concept_id
        FROM
            report_mapping rm_next
        WHERE
            rm_next.source = 'PIH'
                AND rm_next.code = 'Appearance at appointment time')
        AND obs_visit_adherence.voided = 0
        LEFT OUTER JOIN
    concept_name cn_visit_adherence ON cn_visit_adherence.concept_id = obs_visit_adherence.value_coded
        AND cn_visit_adherence.locale = 'fr'
        AND cn_visit_adherence.locale_preferred = 1
        AND cn_visit_adherence.voided = 0 
SET 
    p.visit_adherence = cn_visit_adherence.name;

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    temp_ncd_last_ncd_enc ON p.patient_id = temp_ncd_last_ncd_enc.patient_id
        LEFT OUTER JOIN
    obs obs_recent_hosp ON obs_recent_hosp.encounter_id = temp_ncd_last_ncd_enc.encounter_id
        AND obs_recent_hosp.concept_id = (SELECT 
            concept_id
        FROM
            report_mapping rm_next
        WHERE
            rm_next.source = 'PIH'
                AND rm_next.code = 'PATIENT HOSPITALIZED SINCE LAST VISIT')
        AND obs_recent_hosp.voided = 0
        LEFT OUTER JOIN
    concept_name cn_recent_hosp ON cn_recent_hosp.concept_id = obs_recent_hosp.value_coded
        AND cn_recent_hosp.locale = 'fr'
        AND cn_recent_hosp.locale_preferred = 1
        AND cn_recent_hosp.voided = 0 
SET 
    p.recent_hospitalization = cn_recent_hosp.name;

-- ncd meds
CREATE TEMPORARY TABLE IF NOT EXISTS temp_ncd_meds
(
person_id int(11),
encounter_id int(11),
ncd_meds_prescribed text
);

INSERT INTO temp_ncd_meds (person_id, encounter_id, ncd_meds_prescribed)
SELECT person_id, obs_meds.encounter_id, GROUP_CONCAT(cn_meds.name separator " | ") "meds"
from obs obs_meds
  INNER JOIN encounter e on e.encounter_id = obs_meds.encounter_id and e.voided = 0 and e.patient_id = obs_meds.person_id and e.encounter_type IN (@NCDInitEnc, @NCDFollowEnc)
  LEFT OUTER JOIN concept_name cn_meds on cn_meds.concept_id = obs_meds.value_coded and 
  cn_meds.locale = 'en' and cn_meds.locale_preferred = 1 and cn_meds.voided = 0
where obs_meds.concept_id IN 
(select concept_id from report_mapping rm_next where (rm_next.source = 'PIH' and rm_next.code = 'Medications prescribed at end of visit')
OR (rm_next.source = 'PIH' and rm_next.code = 'MEDICATION ORDERS'))
and obs_meds.voided = 0
group by obs_meds.encounter_id order by obs_meds.person_id, obs_meds.encounter_id DESC;

CREATE TEMPORARY TABLE IF NOT EXISTS temp_latest_ncd_meds
(
person_id int(11),
encounter_id int(11),
ncd_meds_prescribed text,
prescribed_insulin varchar(50)
);

INSERT INTO temp_latest_ncd_meds (person_id, encounter_id, ncd_meds_prescribed)
SELECT person_id,
max(encounter_id),
ncd_meds_prescribed from temp_ncd_meds group by person_id;

UPDATE temp_latest_ncd_meds tlnm 
SET 
    tlnm.prescribed_insulin = (CASE
        WHEN LOWER(tlnm.ncd_meds_prescribed) LIKE '%insulin%' THEN 'oui'
        ELSE 'non'
    END);

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    temp_ncd_last_ncd_enc ON p.patient_id = temp_ncd_last_ncd_enc.patient_id
        LEFT OUTER JOIN
    (SELECT 
        obs_cat.encounter_id,
            MAX(CASE
                WHEN
                    rm_cat.source = 'PIH'
                        AND rm_cat.code = 'HYPERTENSION'
                THEN
                    '1'
            END) 'Hypertension',
            MAX(CASE
                WHEN
                    rm_cat.source = 'PIH'
                        AND rm_cat.code = 'DIABETES'
                THEN
                    '1'
            END) 'Diabetes',
            MAX(CASE
                WHEN
                    rm_cat.source = 'PIH'
                        AND rm_cat.code = 'HEART FAILURE'
                THEN
                    '1'
            END) 'Heart_Failure',
            MAX(CASE
                WHEN
                    rm_cat.source = 'PIH'
                        AND rm_cat.code = 'Cerebrovascular Accident'
                THEN
                    '1'
            END) 'Stroke',
            MAX(CASE
                WHEN
                    rm_cat.source = 'PIH'
                        AND rm_cat.code = 'Chronic respiratory disease program'
                THEN
                    '1'
            END) 'Respiratory',
            MAX(CASE
                WHEN
                    rm_cat.source = 'PIH'
                        AND rm_cat.code = 'Rehab program'
                THEN
                    '1'
            END) 'Rehab',
            MAX(CASE
                WHEN
                    rm_cat.source = 'PIH'
                        AND rm_cat.code = 'Sickle-Cell Anemia'
                THEN
                    '1'
            END) 'Anemia',
            MAX(CASE
                WHEN
                    rm_cat.source = 'PIH'
                        AND rm_cat.code = 'EPILEPSY'
                THEN
                    '1'
            END) 'Epilepsy',
            MAX(CASE
                WHEN
                    rm_cat.source = 'PIH'
                        AND rm_cat.code = 'OTHER'
                THEN
                    '1'
            END) 'Other_Category'
    FROM
        obs obs_cat
    LEFT OUTER JOIN report_mapping rm_cat ON rm_cat.concept_id = obs_cat.value_coded
    WHERE
        obs_cat.concept_id = (SELECT 
                concept_id
            FROM
                report_mapping rm_next
            WHERE
                rm_next.source = 'PIH'
                    AND rm_next.code = 'NCD category')
            AND obs_cat.voided = 0
    GROUP BY 1) cats ON cats.encounter_id = temp_ncd_last_ncd_enc.encounter_id 
SET 
    p.hypertension = cats.Hypertension,
    p.diabetes = cats.Diabetes,
    p.heart_Failure = cats.Heart_Failure,
    p.stroke = cats.Stroke,
    p.respiratory = cats.Respiratory,
    p.rehab = cats.Rehab,
    p.anemia = cats.Anemia,
    p.epilepsy = cats.Epilepsy,
    p.other_category = cats.Other_Category;

UPDATE temp_ncd_program p
        LEFT OUTER JOIN
    temp_ncd_last_ncd_enc ON p.patient_id = temp_ncd_last_ncd_enc.patient_id
        INNER JOIN
    report_mapping diag ON diag.source = 'PIH'
        AND diag.code = 'DIAGNOSIS'
        INNER JOIN
    report_mapping diag_nc ON diag_nc.source = 'PIH'
        AND diag_nc.code = 'Diagnosis or problem, non-coded'
        LEFT OUTER JOIN
    obs diag1 ON diag1.obs_id = (SELECT 
            obs_id
        FROM
            obs d1
        WHERE
            d1.concept_id = diag.concept_id
                AND d1.voided = 0
                AND d1.encounter_id = temp_ncd_last_ncd_enc.encounter_id
        ORDER BY d1.obs_datetime ASC
        LIMIT 1)
        LEFT OUTER JOIN
    concept_name diagname1 ON diagname1.concept_id = diag1.value_coded
        AND diagname1.locale = 'fr'
        AND diagname1.voided = 0
        AND diagname1.locale_preferred = 1
        LEFT OUTER JOIN
    obs diag2 ON diag2.obs_id = (SELECT 
            obs_id
        FROM
            obs d2
        WHERE
            d2.concept_id = diag.concept_id
                AND d2.voided = 0
                AND d2.encounter_id = temp_ncd_last_ncd_enc.encounter_id
                AND d2.value_coded <> diag1.value_coded
        ORDER BY d2.obs_datetime ASC
        LIMIT 1)
        LEFT OUTER JOIN
    concept_name diagname2 ON diagname2.concept_id = diag2.value_coded
        AND diagname2.locale = 'fr'
        AND diagname2.voided = 0
        AND diagname2.locale_preferred = 1
        LEFT OUTER JOIN
    obs diag3 ON diag3.obs_id = (SELECT 
            obs_id
        FROM
            obs d3
        WHERE
            d3.concept_id = diag.concept_id
                AND d3.voided = 0
                AND d3.encounter_id = temp_ncd_last_ncd_enc.encounter_id
                AND d3.value_coded NOT IN (diag1.value_coded , diag2.value_coded)
        ORDER BY d3.obs_datetime ASC
        LIMIT 1)
        LEFT OUTER JOIN
    concept_name diagname3 ON diagname3.concept_id = diag3.value_coded
        AND diagname3.locale = 'fr'
        AND diagname3.voided = 0
        AND diagname3.locale_preferred = 1
        LEFT OUTER JOIN
    obs d_nc ON d_nc.concept_id = diag_nc.concept_id
        AND d_nc.voided = 0
        AND d_nc.encounter_id = temp_ncd_last_ncd_enc.encounter_id 
SET 
    p.last_diagnosis_1 = diagname1.name,
    p.last_diagnosis_2 = diagname2.name,
    p.last_diagnosis_3 = diagname3.name,
    p.last_non_coded_diagnosis = d_nc.value_text;

SELECT 
    p.patient_id 'patient_id',
    ZLemr_id,
    dossier_id,
    given_name,
    family_name,
    birthdate,
    birthdate_estimated,
    gender,
    country,
    department,
    commune,
    section_communal,
    locality,
    street_landmark,
    telephone_number,
    contact_telephone_number,
    DATE(date_enrolled) 'enrolled_in_program',
    program_state,
    program_outcome,
    disposition,
    first_ncd_encounter,
    last_ncd_encounter,
    next_ncd_appointment,
    thirty_days_past_app '30_days_past_app',
    deceased,
    hypertension,
    diabetes,
    heart_Failure,
    stroke,
    respiratory,
    rehab,
    anemia,
    epilepsy,
    other_category,
    nyha_classes,
    lack_of_meds,
    visit_adherence,
    recent_hospitalization,
    ncd_meds_prescribed,
    prescribed_insulin,
    HbA1c_result,
    HbA1c_collection_date,
    HbA1c_result_date,
    bp_diastolic,
    bp_systolic,
    height,
    weight,
    creatinine_result,
    creatinine_collection_date,
    creatinine_result_date,
    last_diagnosis_1,
    last_diagnosis_2,
    last_diagnosis_3,
    last_non_coded_diagnosis
FROM
    temp_ncd_program p
        LEFT OUTER JOIN
    temp_ncd_last_ncd_enc tlne ON p.patient_id = tlne.patient_id
        LEFT OUTER JOIN
    temp_latest_ncd_meds tlnc ON p.patient_id = tlnc.person_id;