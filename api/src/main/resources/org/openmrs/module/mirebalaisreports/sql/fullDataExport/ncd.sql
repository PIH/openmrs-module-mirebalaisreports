DROP TEMPORARY TABLE IF EXISTS temp_ncd_initial_referral;
DROP TEMPORARY TABLE IF EXISTS temp_ncd_initial_behavior;
DROP TEMPORARY TABLE IF EXISTS temp_ncd_pregnacy;
DROP TEMPORARY TABLE IF EXISTS temp_obs_join;
DROP TEMPORARY TABLE IF EXISTS temp_ncd_family_plan_oral;
DROP TEMPORARY TABLE IF EXISTS temp_ncd_family_plan_provera;
DROP TABLE IF EXISTS temp_ncd_encounters;
DROP TABLE IF EXISTS temp_ncd_family_plan;
DROP TEMPORARY TABLE IF EXISTS temp_ncd_section;

SELECT patient_identifier_type_id INTO @zlId FROM patient_identifier_type WHERE name = "ZL EMR ID";
SELECT person_attribute_type_id INTO @unknownPt FROM person_attribute_type WHERE name = "Unknown patient";
SELECT person_attribute_type_id INTO @testPt FROM person_attribute_type WHERE name = "Test Patient";
SELECT encounter_type_id INTO @NCDInitEnc FROM encounter_type WHERE name = "NCD Initial Consult";
SELECT encounter_type_id INTO @NCDFollowEnc FROM encounter_type WHERE name = "NCD Followup Consult";
SELECT encounter_type_id INTO @vitEnc FROM encounter_type WHERE name = "Signes vitaux";
SELECT encounter_type_id INTO @labResultEnc FROM encounter_type WHERE name = "Laboratory Results";
select concept_id  INTO @family_plan_start_date  from report_mapping where source = "CIEL" and code = "163757";
select concept_id  INTO @family_plan_end_date  from report_mapping where source = "CIEL" and code = "163758";

-- NCD Initial form referral qn
CREATE TEMPORARY TABLE IF NOT EXISTS temp_ncd_initial_referral
AS
(
SELECT
e.encounter_id encounter_id,
    e.patient_id patient_id,
    hist_ill.value_text history_of_present_illness,
    GROUP_CONCAT(DISTINCT(internal_refer_value)) internal_refer_values,
    other_internal_institution.comments other_internal_institution,
    CONCAT(external_institution.comments, ", ", non_pih_institution.comments) external_institution,
    GROUP_CONCAT(DISTINCT(community.comm_values)) community,
    DATE(date_referral.value_datetime) date_of_referral
FROM
encounter e
-- REFERRAL
LEFT JOIN obs hist_ill ON e.patient_id = hist_ill.person_id AND e.encounter_id = hist_ill.encounter_id AND hist_ill.voided = 0
AND hist_ill.concept_id =
(SELECT concept_id FROM report_mapping WHERE source = "PIH" AND code = "PRESENTING HISTORY")
LEFT JOIN (SELECT encounter_id, value_coded, cn.name internal_refer_value FROM obs o JOIN concept_name cn ON cn.concept_id = o.value_coded AND cn.voided = 0
AND o.voided = 0 AND locale="fr" AND concept_name_type = "FULLY_SPECIFIED" AND o.concept_id =
(SELECT concept_id FROM report_mapping WHERE source = "PIH" AND code = "Type of referring service") AND value_coded IN
((SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = 165018),
(SELECT concept_id FROM report_mapping WHERE source = "PIH"  AND code = "ANTENATAL CLINIC"),
(SELECT concept_id FROM report_mapping WHERE source = "PIH"  AND code= "PRIMARY CARE CLINIC" ),
(SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = "163558"),
(SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = "160449"),
(SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = "160448"),
(SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = "165048"),
(SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = "160473"),
(SELECT concept_id FROM report_mapping WHERE source = "PIH" AND code = "OTHER")
)) internal_refer ON e.encounter_id = internal_refer.encounter_id
LEFT JOIN obs other_internal_institution ON e.encounter_id = other_internal_institution.encounter_id AND other_internal_institution.voided = 0
AND other_internal_institution.value_coded = (SELECT concept_id FROM report_mapping WHERE source = "PIH" AND code = "OTHER")
LEFT JOIN obs external_institution ON e.encounter_id = external_institution.encounter_id AND external_institution.voided = 0 AND
external_institution.value_coded = (SELECT concept_id FROM report_mapping WHERE source = "PIH" AND code = "11956")
LEFT JOIN obs non_pih_institution ON e.encounter_id = non_pih_institution.encounter_id AND non_pih_institution.voided = 0 AND
non_pih_institution.value_coded = (SELECT concept_id FROM report_mapping WHERE source = "PIH" AND code = "Non-ZL supported site")
LEFT JOIN (SELECT o.encounter_id, o.value_coded, cn.name comm_values FROM obs o JOIN concept_name cn ON cn.concept_id = o.value_coded AND
o.voided = 0 AND cn.locale= "fr" AND cn.concept_name_type = "FULLY_SPECIFIED" AND o.value_coded IN
(
(SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = "1555"),
(SELECT concept_id FROM report_mapping WHERE source = "PIH" AND code = "11965")
)) community ON e.encounter_id = community.encounter_id
LEFT JOIN obs date_referral ON e.patient_id = date_referral.person_id AND e.encounter_id = date_referral.encounter_id
AND date_referral.voided = 0 AND date_referral.concept_id =
(SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = "163181")
-- Family history
WHERE e.encounter_type = @NCDInitEnc AND e.voided = 0 GROUP BY encounter_id
);

-- NCD initial form behavior qn
CREATE TEMPORARY TABLE IF NOT EXISTS temp_ncd_initial_behavior
AS
(
SELECT
e.patient_id,
e.encounter_id encounter_id,
tob_smoke.conceptname smoker,
tob_num.value_numeric packs_per_year,
sec_smoke.conceptname second_hand_smoker,
alc.conceptname alcohol_use,
ill_drugs.conceptname illegal_drugs,
current_drug_name.value_text current_drug_name
FROM
encounter e
LEFT JOIN
-- History of tobacco use
(SELECT cn.name conceptname, value_coded, encounter_id, person_id FROM obs o JOIN concept_name cn ON cn.concept_id = o.value_coded AND o.voided = 0 AND cn.voided = 0 AND
locale = "en" AND o.concept_id =
(SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = "163731")) tob_smoke ON tob_smoke.encounter_id = e.encounter_id AND tob_smoke.person_id = e.patient_id
LEFT JOIN
obs tob_num ON tob_num.encounter_id = e.encounter_id AND tob_num.person_id = e.patient_id AND tob_num.voided = 0 AND
tob_num.concept_id = (SELECT concept_id FROM report_mapping WHERE source = "PIH" AND code = 11949)
LEFT JOIN
-- Second hand smoke
(SELECT cn.name conceptname, value_coded, encounter_id, person_id FROM obs o JOIN concept_name cn ON cn.concept_id = o.value_coded
AND o.voided = 0 AND cn.voided = 0 AND locale="en" AND concept_name_type="FULLY_SPECIFIED" AND
o.concept_id =
(SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = "152721")) sec_smoke ON
sec_smoke.encounter_id = e.encounter_id AND sec_smoke.person_id = e.patient_id
LEFT JOIN
-- Alcohol
(SELECT cn.name conceptname, value_coded, encounter_id, person_id FROM obs o JOIN concept_name cn ON cn.concept_id = o.value_coded
AND o.voided = 0 AND cn.voided = 0 AND locale="en" AND concept_name_type="FULLY_SPECIFIED" AND
o.concept_id =
(SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = "159449")) alc ON alc.encounter_id = e.encounter_id AND alc.person_id = e.patient_id
LEFT JOIN
-- History of illegal drugs
(SELECT cn.name conceptname, value_coded, encounter_id, person_id FROM obs o JOIN concept_name cn ON cn.concept_id = o.value_coded
AND o.voided = 0 AND cn.voided = 0 AND locale="en" AND concept_name_type="FULLY_SPECIFIED" AND
o.concept_id = (SELECT concept_id FROM report_mapping WHERE source = "CIEL" AND code = "162556"))
ill_drugs ON ill_drugs.encounter_id = e.encounter_id AND ill_drugs.person_id = e.patient_id
LEFT JOIN
-- drug name
obs current_drug_name ON current_drug_name.encounter_id = e.encounter_id AND current_drug_name.person_id = e.patient_id AND
current_drug_name.voided = 0 AND current_drug_name.concept_id =
(SELECT concept_id FROM report_mapping WHERE source = "PIH" AND code = 6489)
WHERE
e.encounter_type = @NCDInitEnc AND e.voided = 0 GROUP BY e.encounter_id
);

CREATE TEMPORARY TABLE temp_ncd_pregnacy
(
person_id INT,
encounter_id INT,
pregnant VARCHAR(50),
last_menstruation_date DATETIME,
estimated_delivery_date DATETIME,
currently_breast_feeding VARCHAR(50)
);
INSERT INTO temp_ncd_pregnacy (person_id, encounter_id, pregnant)
SELECT preg.person_id, preg.encounter_id,  cn.name FROM
obs preg,
concept_name cn
WHERE preg.value_coded = cn.concept_id
AND cn.concept_name_type = "FULLY_SPECIFIED" AND cn.voided = 0 AND cn.locale="en"
AND  preg.concept_id IN (SELECT concept_id FROM report_mapping rm WHERE rm.source = "PIH" AND rm.code = "PREGNANCY STATUS")
AND encounter_id IN (SELECT encounter_id FROM encounter WHERE encounter_type= @NCDInitEnc);

UPDATE temp_ncd_pregnacy tnp
-- estimate_delievery_date
INNER JOIN
(
SELECT encounter_id, value_datetime FROM obs WHERE voided = 0 AND concept_id =
(SELECT concept_id FROM report_mapping WHERE source="PIH" AND code="DATE OF LAST MENSTRUAL PERIOD")
) lmd ON lmd.encounter_id = tnp.encounter_id
SET tnp.last_menstruation_date = lmd.value_datetime;

UPDATE temp_ncd_pregnacy tnp
-- estimated_delivery_date
INNER JOIN
(
SELECT encounter_id, value_datetime FROM obs WHERE voided = 0 AND concept_id =
(SELECT concept_id FROM report_mapping WHERE source="CIEL" AND code="5596")
) edt ON edt.encounter_id = tnp.encounter_id
SET tnp.estimated_delivery_date = edt.value_datetime;

UPDATE temp_ncd_pregnacy tnp
-- breast feeding
INNER JOIN
(
SELECT encounter_id, name FROM obs, concept_name cn WHERE cn.concept_id = obs.value_coded AND obs.voided = 0 AND obs.concept_id =
(SELECT concept_id FROM report_mapping WHERE source="CIEL" AND code="5632")
AND cn.concept_name_type = "FULLY_SPECIFIED" AND cn.voided = 0 AND cn.locale="en"
) breast ON breast.encounter_id = tnp.encounter_id
SET tnp.currently_breast_feeding = breast.name;

CREATE TEMPORARY TABLE temp_ncd_encounters
(
encounter_id int,
patient_id int,
encounter_type int,
encounter_datetime datetime,
visit_id int
);

INSERT INTO temp_ncd_encounters (encounter_id, patient_id)
SELECT encounter_id, patient_id
from encounter
where encounter_type = @NCDInitENC and voided = 0;

create temporary table temp_ncd_family_plan
(
encounter_id int,
patient_id int,
concept_id int,
oral_contraception varchar(50),
oral_contraception_start_date datetime,
oral_contraception_end_date datetime,
depoprovera varchar(50),
depoprovera_start_date datetime,
depoprovera_end_date datetime,
condom varchar(50),
condom_start_date datetime,
condom_end_date datetime,
levonorgestrel varchar(50),
levonorgestrel_start_date datetime,
levonorgestrel_end_date datetime,
intrauterine_device varchar(50),
intrauterine_device_start_date datetime,
intrauterine_device_end_date datetime,
tubal_litigation varchar(50),
tubal_litigation_start_date datetime,
tubal_litigation_end_date datetime,
vasectomy varchar(50),
vasectomy_start_date datetime,
vasectomy_end_date datetime,
family_plan_other varchar(50),
family_plan_other_name varchar(255),
family_plan_other_start_date datetime,
family_plan_other_end_date datetime
);

INSERT INTO temp_ncd_family_plan (encounter_id, patient_id)
(select encounter_id, patient_id from temp_ncd_encounters );

update temp_ncd_family_plan set concept_id = (select concept_id from report_mapping where source = "PIH" and code = "METHOD OF FAMILY PLANNING");

update temp_ncd_family_plan tnmp
left join
     obs o
ON o.value_coded = (select concept_id from report_mapping where source = "PIH" and code = "ORAL CONTRACEPTION")
and tnmp.concept_id = o.concept_id
and tnmp.encounter_id = o.encounter_id
and o.voided = 0
left join obs o1
ON o1.value_coded = (select concept_id from report_mapping where source = "CIEL" and code = "907")
and o1.concept_id = tnmp.concept_id
and tnmp.encounter_id = o1.encounter_id
and o1.voided = 0
left join obs o2
ON o2.value_coded = (select concept_id from report_mapping where source = "CIEL" and code = "190")
and o2.concept_id = tnmp.concept_id
and tnmp.encounter_id = o2.encounter_id
and o2.voided = 0
left join obs o3
ON o3.value_coded = (select concept_id from report_mapping where source = "CIEL" and code = "78796")
and o3.concept_id = tnmp.concept_id
and tnmp.encounter_id = o3.encounter_id
and o3.voided = 0
left join obs o4
ON o4.value_coded = (select concept_id from report_mapping where source = "CIEL" and code = "5275")
and o4.concept_id = tnmp.concept_id
and tnmp.encounter_id = o4.encounter_id
and o4.voided = 0
left join obs o5
ON o5.value_coded = (select concept_id from report_mapping where source = "CIEL" and code = "1472")
and o5.concept_id = tnmp.concept_id
and tnmp.encounter_id = o5.encounter_id
and o5.voided = 0
left join obs o6
ON o6.value_coded = (select concept_id from report_mapping where source = "CIEL" and code = "1489")
and o6.concept_id = tnmp.concept_id
and tnmp.encounter_id = o6.encounter_id
and o6.voided = 0
left join obs o7
ON o7.value_coded = (select concept_id from report_mapping where source = "PIH" and code = "OTHER")
and o7.concept_id = tnmp.concept_id
and tnmp.encounter_id = o7.encounter_id
and o7.voided = 0
SET tnmp.oral_contraception = IF(o.value_coded is not null, "Yes", "No"),
    tnmp.oral_contraception_start_date = (select value_datetime from obs where concept_id = @family_plan_start_date and tnmp.encounter_id = encounter_id and o.obs_group_id = obs_group_id),
	tnmp.oral_contraception_end_date = (select value_datetime from obs where concept_id = @family_plan_end_date  and tnmp.encounter_id = encounter_id and o.obs_group_id = obs_group_id),
    tnmp.depoprovera = IF(o1.value_coded is not null, "Yes", "No"),
    tnmp.depoprovera_start_date = (select value_datetime from obs where concept_id = @family_plan_start_date and tnmp.encounter_id = encounter_id and o1.obs_group_id = obs_group_id),
	tnmp.depoprovera_end_date = (select value_datetime from obs where concept_id = @family_plan_end_date and tnmp.encounter_id = encounter_id and o1.obs_group_id = obs_group_id),
	tnmp.condom = IF(o2.value_coded is not null, "Yes", "No"),
	tnmp.condom_start_date = (select value_datetime from obs where concept_id = @family_plan_start_date and tnmp.encounter_id = encounter_id and o2.obs_group_id = obs_group_id),
    tnmp.condom_end_date = (select value_datetime from obs where concept_id = @family_plan_end_date and tnmp.encounter_id = encounter_id and o2.obs_group_id = obs_group_id),
    tnmp.levonorgestrel = IF(o3.value_coded is not null, "Yes", "No"),
	tnmp.levonorgestrel_start_date = (select value_datetime from obs where concept_id = @family_plan_start_date and tnmp.encounter_id = encounter_id and o3.obs_group_id = obs_group_id),
    tnmp.levonorgestrel_end_date = (select value_datetime from obs where concept_id = @family_plan_end_date and tnmp.encounter_id = encounter_id and o3.obs_group_id = obs_group_id),
    tnmp.intrauterine_device = IF(o4.value_coded is not null, "Yes", "No"),
	tnmp.intrauterine_device_start_date = (select value_datetime from obs where concept_id = @family_plan_start_date and tnmp.encounter_id = encounter_id and o4.obs_group_id = obs_group_id),
    tnmp.intrauterine_device_end_date = (select value_datetime from obs where concept_id = @family_plan_end_date and tnmp.encounter_id = encounter_id and o4.obs_group_id = obs_group_id),
    tnmp.tubal_litigation = IF(o5.value_coded is not null, "Yes", "No"),
	tnmp.tubal_litigation_start_date = (select value_datetime from obs where concept_id = @family_plan_start_date and tnmp.encounter_id = encounter_id and o5.obs_group_id = obs_group_id),
    tnmp.tubal_litigation_end_date = (select value_datetime from obs where concept_id = @family_plan_end_date and tnmp.encounter_id = encounter_id and o5.obs_group_id = obs_group_id),
    tnmp.vasectomy = IF(o6.value_coded is not null, "Yes", "No"),
	tnmp.vasectomy_start_date = (select value_datetime from obs where concept_id = @family_plan_start_date and tnmp.encounter_id = encounter_id and o6.obs_group_id = obs_group_id),
    tnmp.vasectomy_end_date = (select value_datetime from obs where concept_id = @family_plan_end_date and tnmp.encounter_id = encounter_id and o6.obs_group_id = obs_group_id),
    tnmp.family_plan_other = IF(o7.value_coded is not null, "Yes", "No"),
    tnmp.family_plan_other_name = (select value_text from obs where voided = 0 and concept_id = (select concept_id from report_mapping where source = "PIH" and code = "OTHER FAMILY PLANNING METHOD, NON-CODED") and encounter_id = o7.encounter_id),
	tnmp.family_plan_other_start_date = (select value_datetime from obs where concept_id = @family_plan_start_date and tnmp.encounter_id = encounter_id and o7.obs_group_id = obs_group_id),
    tnmp.family_plan_other_end_date = (select value_datetime from obs where concept_id = @family_plan_end_date and tnmp.encounter_id = encounter_id and o7.obs_group_id = obs_group_id)
	;

-- NCD section
create TEMPORARY table temp_ncd_section
(
obs_id int, encounter_id int, person_id int, disease_category text, comments text, waist_circumference double, hip_size double, hypertension_stage text, diabetes_mellitus text,
serum_glucose double, fasting_blood_glucose_test varchar (50), fasting_blood_glucose double, managing_diabetic_foot_care text, 	diabetes_comment text,
 probably_asthma varchar(50), respiratory_diagnosis text, bronchiectasis varchar(50), copd varchar(50), copd_grade varchar(255)
);

INSERT INTO temp_ncd_section (obs_id, encounter_id, person_id, disease_category, comments)
select obs_id, encounter_id, person_id, group_concat(name), comments from obs o, concept_name cn
where
value_coded = cn.concept_id  and locale="en" and concept_name_type="FULLY_SPECIFIED" and cn.voided = 0 and
o.concept_id = (select concept_id from report_mapping where source="PIH" and code = "NCD category") and o.voided = 0
and encounter_id in (select encounter_id from encounter where voided = 0 and encounter_type = @NCDInitEnc) group by encounter_id;

update temp_ncd_section tns
left join obs o on o.encounter_id = tns.encounter_id and o.voided = 0 and o.concept_id = (select concept_id from report_mapping where source = "CIEL" and code = 163080)
left join obs o1 on o1.encounter_id = tns.encounter_id and o1.voided = 0 and o1.concept_id = (select concept_id from report_mapping where source = "CIEL" and code = 163081)
left join (select group_concat(name) names, encounter_id
from concept_name cn join obs o on o.value_coded = cn.concept_id and concept_name_type="FULLY_SPECIFIED" and locale="en" and cn.voided = 0
and o.concept_id = (select concept_id from report_mapping where source = "PIH" and code = "Type of hypertension diagnosis") group by encounter_id) o2 on
o2.encounter_id = tns.encounter_id
set tns.waist_circumference = o.value_numeric,
    tns.hip_size =  o1.value_numeric,
    tns.hypertension_stage = o2.names;

update temp_ncd_section tns
left join (select encounter_id, group_concat(name) names from concept_name cn join obs o on
cn.concept_id = value_coded and locale = "en" and concept_name_type = "FULLY_SPECIFIED"
and (select group_concat(concept_id) from report_mapping where source = "CIEL" and code IN (142474,142473,165207,165208,1449,138291))
and o.concept_id = (select concept_id from report_mapping where source = "PIH" and code = "DIAGNOSIS")
and cn.voided = 0 and o.voided = 0 group by encounter_id) o3 on o3.encounter_id = tns.encounter_id
left join obs o4 on o4.encounter_id = tns.encounter_id and o4.concept_id = (select concept_id from report_mapping where source = "PIH" and code = "SERUM GLUCOSE") and o4.voided = 0
left join obs o5 on o5.encounter_id = tns.encounter_id and o5.concept_id = (select concept_id from report_mapping where source = "CIEL" and code = "160912") and o5.voided = 0
left join (select group_concat(name) names, encounter_id
from concept_name cn join obs o on o.value_coded = cn.concept_id and concept_name_type="FULLY_SPECIFIED" and locale="en" and cn.voided = 0
and o.concept_id = (select concept_id from report_mapping where source = "PIH" and code = "Foot care classification") group by encounter_id)
o6 on o6.encounter_id = tns.encounter_id
left join obs o7 on o7.encounter_id = tns.encounter_id and o7.concept_id = (select concept_id from report_mapping where source = "PIH" and code = "Fasting for blood glucose test") and o7.voided = 0
left join obs o8 on o8.encounter_id = tns.encounter_id and o8.concept_id = (select concept_id from report_mapping where source = "PIH" and code = "11974") and o8.voided = 0
set tns.diabetes_mellitus = o3.names,
    tns.serum_glucose = o4.value_numeric,
    tns.fasting_blood_glucose_test = IF(o7.value_coded = 1, "Yes", "No"),
    tns.fasting_blood_glucose = o5.value_numeric,
    tns.managing_diabetic_foot_care = o6.names,
    tns.diabetes_comment = o8.value_text;

update temp_ncd_section tns
left join (select encounter_id, group_concat(name) name from concept_name cn join obs o on cn.concept_id = value_coded
and concept_name_type = "FULLY_SPECIFIED" and locale = "en"
and o.voided = 0 and o.concept_id = (select concept_id from report_mapping where
source = "PIH" and code = "Asthma classification") group by encounter_id) o on tns.encounter_id = o.encounter_id
left join obs o1 on tns.encounter_id = o1.encounter_id and value_coded =
(select concept_id from report_mapping where source = "CIEL" and code = 121375)
and concept_id = (select concept_id from report_mapping where source = "PIH" and code = "DIAGNOSIS")
and o1.voided = 0
left join obs o2 on
tns.encounter_id = o2.encounter_id and o2.value_coded =
(select concept_id from report_mapping where source = "CIEL" and code = "121011")
and o2.concept_id = (select concept_id from report_mapping where source = "PIH" and code = "DIAGNOSIS")
and o2.voided = 0
left join obs o3 on
tns.encounter_id = o3.encounter_id and o3.value_coded =
(select concept_id from report_mapping where source = "CIEL" and code = "1295")
and o3.concept_id = (select concept_id from report_mapping where source = "PIH" and code = "DIAGNOSIS")
and o3.voided = 0
left join obs o4 on tns.encounter_id = o4.encounter_id and o4.voided = 0 and
o4.concept_id = (select concept_id from report_mapping where source = "PIH" and code="COPD group classification")
set tns.respiratory_diagnosis  = o.name,
     tns.probably_asthma = IF(o1.value_coded is not null, "Yes", "No"),
     tns.bronchiectasis = IF(o2.value_coded is not null, "Yes", "No"),
     tns.copd = IF(o3.value_coded is not null, "Yes", "No"),
     tns.copd_grade = (select name from concept_name where concept_id = o4.value_coded and locale = "en" and voided = 0 and concept_name_type
     = "FULLY_SPECIFIED");

-- obs join
CREATE TEMPORARY table temp_obs_join
AS
(
SELECT
     o.encounter_id,
    (SELECT date_started FROM visit WHERE visit_id = e.visit_id) visit_date,
    visit_id,
    MAX(DATE(CASE
            WHEN e.encounter_id = o.encounter_id THEN e.encounter_datetime
        END)) 'encounter_date',
-- Encounter Type
GROUP_CONCAT(DISTINCT(CASE WHEN e.encounter_id = o.encounter_id THEN (SELECT name FROM encounter_type WHERE encounter_type_id = e.encounter_type) END)
    SEPARATOR ', ') visit_type,
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Type of referring service'
        THEN
            cn.name
    END) 'Type_of_referring_service',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Known chronic disease before referral'
        THEN
            cn.name
    END) 'Known_disease_before_referral',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Prior treatment for chronic disease'
        THEN
            cn.name
    END) 'Prior_treatment',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Chronic disease controlled during initial visit'
        THEN
            cn.name
    END) 'Disease_controlled_initial_visit',
    GROUP_CONCAT(CASE
            WHEN
                rm.source = 'PIH'
                    AND rm.code = 'NCD category'
            THEN
                cn.name
        END
        SEPARATOR ', ') 'NCD_category',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'NCD category'
        THEN
            o.comments
    END) 'Other_NCD_category',
    MAX(CASE
        WHEN rm.source = 'CIEL' AND rm.code = '5089' THEN o.value_numeric
    END) 'Weight_kg',
    MAX(CASE
        WHEN rm.source = 'CIEL' AND rm.code = '5090' THEN o.value_numeric
    END) 'Height_cm',
    ROUND(MAX(CASE
                WHEN rm.source = 'CIEL' AND rm.code = '5089' THEN o.value_numeric
            END) / ((MAX(CASE
                WHEN rm.source = 'CIEL' AND rm.code = '5090' THEN o.value_numeric
            END) / 100) * (MAX(CASE
                WHEN rm.source = 'CIEL' AND rm.code = '5090' THEN o.value_numeric
            END) / 100)),
            1) 'BMI',
    MAX(CASE
        WHEN rm.source = 'CIEL' AND rm.code = '5085' THEN o.value_numeric
    END) 'Systolic_BP',
    MAX(CASE
        WHEN rm.source = 'CIEL' AND rm.code = '5086' THEN o.value_numeric
    END) 'Diastolic_BP',
    MAX(CASE
        WHEN
            rm.source = 'CIEL'
                AND rm.code = '163080'
        THEN
            o.value_numeric
    END) 'Waist_cm',
    MAX(CASE
        WHEN
            rm.source = 'CIEL'
                AND rm.code = '163081'
        THEN
            o.value_numeric
    END) 'hip_cm',
    ROUND(MAX(CASE
                WHEN
                    rm.source = 'CIEL'
                        AND rm.code = '163080'
                THEN
                    o.value_numeric
            END) / MAX(CASE
                WHEN
                    rm.source = 'CIEL'
                        AND rm.code = '163081'
                THEN
                    o.value_numeric
            END),
            2) 'Waist/Hip Ratio',
    GROUP_CONCAT(CASE
            WHEN
                rm.source = 'PIH'
                    AND rm.code = 'NYHA CLASS'
            THEN
                cn.name
        END
        SEPARATOR ',') 'NYHA_CLASS',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'PATIENTS FLUID MANAGEMENT'
        THEN
            cn.name
    END) 'Patients_Fluid_Management',
    GROUP_CONCAT(CASE
            WHEN
                rm.source = 'PIH'
                    AND rm.code = 'Type of diabetes diagnosis'
            THEN
                cn.name
        END
        SEPARATOR ', ') 'Diabetes_type',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Hypoglycemia symptoms'
        THEN
            cn.name
    END) 'Hypoglycemia_symptoms',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Puffs per week of salbutamol'
        THEN
            o.value_numeric
    END) 'Puffs_week_salbutamol',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Asthma classification'
        THEN
            cn.name
    END) 'Asthma_classification',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Number of seizures since last visit'
        THEN
            o.value_numeric
    END) 'Number_seizures_since_last_visit',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Appearance at appointment time'
        THEN
            cn.name
    END) 'Adherance_to_appointment',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Lack of meds in last 2 days'
        THEN
            cn.name
    END) 'Lack_of_meds_2_days',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'PATIENT HOSPITALIZED SINCE LAST VISIT'
        THEN
            cn.name
    END) 'Patient_hospitalized_since_last_visit',
    GROUP_CONCAT(CASE
            WHEN
                rm.source = 'PIH'
                    AND rm.code = 'Medications prescribed at end of visit'
            THEN
                cn.name
        END
        SEPARATOR ',') 'Medications_Prescribed',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Medications prescribed at end of visit'
        THEN
            o.comments
    END) 'Other_meds',
    MAX(CASE
        WHEN
            rm.source = 'CIEL'
                AND rm.code = '159644'
        THEN
            o.value_numeric
    END) 'HbA1c',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'PATIENT PLAN COMMENTS'
        THEN
            o.value_text
    END) 'Patient_Plan_Comments',
     MAX(CASE
         WHEN
             rm.source = 'PIH'
             AND rm.code = 'RETURN VISIT DATE'
             THEN
                 o.value_datetime
         END) 'Next_NCD_appointment'
FROM encounter e, report_mapping rm, obs o
LEFT OUTER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.locale_preferred = '1'  AND cn.voided = 0
LEFT OUTER JOIN obs obs2 ON obs2.obs_id = o.obs_group_id
LEFT OUTER JOIN report_mapping obsgrp ON obsgrp.concept_id = obs2.concept_id
WHERE 1=1
AND
e.encounter_id IN
(
   SELECT e3.encounter_id
   FROM encounter e3
     INNER JOIN
    (SELECT visit_id, encounter_type, MAX(encounter_datetime) AS enc_date
    FROM encounter
     WHERE 1=1
     AND encounter_type IN (@NCDInitEnc, @NCDFollowEnc, @vitEnc, @labResultEnc)
      GROUP BY visit_id,encounter_type) maxdate
     ON maxdate.visit_id = e3.visit_id AND e3.encounter_type= maxdate.encounter_type AND e3.encounter_datetime = maxdate.enc_date
)
AND rm.concept_id = o.concept_id
AND o.encounter_id = e.encounter_id
AND e.voided = 0
AND o.voided = 0
GROUP BY e.visit_id
);

SELECT
    p.patient_id,
    zl.identifier zlemr,
    zl_loc.name loc_registered,
    un.value unknown_patient,
    DATE(pp.date_enrolled) enrolled_in_program,
	cn_state.name program_state,
	cn_out.name program_outcome,
    pr.gender,
    ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate) / 365.25,
            1) age_at_enc,
    pa.state_province department,
    pa.city_village commune,
    pa.address3 section,
    pa.address1 locality,
    pa.address2 street_landmark,
    el.name encounter_location,
    CONCAT(pn.given_name, ' ', pn.family_name) provider,
    temp_obs_join.*,
    history_of_present_illness,
    internal_refer_values internal_institution,
    other_internal_institution,
	external_institution,
	community,
	date_of_referral,
	smoker,
    packs_per_year,
    second_hand_smoker,
    alcohol_use,
    illegal_drugs,
    current_drug_name,
    pregnant,
    last_menstruation_date,
    estimated_delivery_date,
    currently_breast_feeding,
    oral_contraception,
	oral_contraception_start_date,
	oral_contraception_end_date,
	depoprovera,
	depoprovera_start_date,
	depoprovera_end_date,
	condom,
	condom_start_date,
	condom_end_date,
	levonorgestrel,
	levonorgestrel_start_date,
	levonorgestrel_end_date,
	intrauterine_device,
	intrauterine_device_start_date,
	intrauterine_device_end_date,
	tubal_litigation,
	tubal_litigation_start_date,
	tubal_litigation_end_date,
	vasectomy,
	vasectomy_start_date,
	vasectomy_end_date,
	family_plan_other,
	family_plan_other_name,
	family_plan_other_start_date,
	family_plan_other_end_date,
	disease_category,
	comments,
	waist_circumference,
	hip_size,
	hypertension_stage,
	diabetes_mellitus,
    serum_glucose,
    fasting_blood_glucose_test,
    fasting_blood_glucose,
    managing_diabetic_foot_care,
    diabetes_comment,
     probably_asthma,
     respiratory_diagnosis,
     bronchiectasis,
     copd,
     copd_grade
FROM
    patient p
-- Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = @zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
-- ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
-- Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = @unknownPt
            AND un.voided = 0
-- Gender
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
--  Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created DESC) n ON p.patient_id = n.person_id
INNER JOIN encounter e ON p.patient_id = e.patient_id AND e.voided = 0 AND e.encounter_type IN (@NCDInitEnc, @NCDFollowEnc, @vitEnc, @labResultEnc)
INNER JOIN location el ON e.location_id = el.location_id
-- UUID of NCD program
LEFT JOIN patient_program pp ON pp.patient_id = p.patient_id AND pp.voided = 0 AND pp.program_id IN
      (SELECT program_id FROM program WHERE uuid = '515796ec-bf3a-11e7-abc4-cec278b6b50a') -- uuid of the NCD program
-- patient state
LEFT OUTER JOIN patient_state ps ON ps.patient_program_id = pp.patient_program_id AND ps.end_date IS NULL AND ps.voided = 0
LEFT OUTER JOIN program_workflow_state pws ON pws.program_workflow_state_id = ps.state AND pws.retired = 0
LEFT OUTER JOIN concept_name cn_state ON cn_state.concept_id = pws.concept_id  AND cn_state.locale = 'en' AND cn_state.locale_preferred = '1'  AND cn_state.voided = 0
-- outcome
LEFT OUTER JOIN concept_name cn_out ON cn_out.concept_id = pp.outcome_concept_id AND cn_out.locale = 'en' AND cn_out.locale_preferred = '1'  AND cn_out.voided = 0
--  Provider Name
INNER JOIN encounter_provider ep ON ep.encounter_id = e.encounter_id AND ep.voided = 0
INNER JOIN provider pv ON pv.provider_id = ep.provider_id
INNER JOIN person_name pn ON pn.person_id = pv.person_id AND pn.voided = 0
-- Straight Obs Joins
INNER JOIN
temp_obs_join ON temp_obs_join.encounter_id = ep.encounter_id
-- NCD INITIAL form - referral
LEFT JOIN temp_ncd_initial_referral ON temp_ncd_initial_referral.encounter_id = e.encounter_id
-- NCD INITIAL form - behavior
LEFT JOIN temp_ncd_initial_behavior ON temp_ncd_initial_behavior.encounter_id = e.encounter_id
-- NCD INITIAL FORM -- pregnacy
LEFT JOIN temp_ncd_pregnacy ON temp_ncd_pregnacy.encounter_id = e.encounter_id
-- NCD INITAL FORM Family planning
LEFT JOIN temp_ncd_family_plan ON temp_ncd_family_plan.encounter_id = e.encounter_id
-- NCD section
LEFT JOIN temp_ncd_section on temp_ncd_section.encounter_id = e.encounter_id
WHERE p.voided = 0
-- exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = @testPt
                         AND voided = 0)
-- Remove all the empty ncd forms.
AND e.visit_id IN (SELECT enc.visit_id FROM encounter enc WHERE encounter_type IN (@NCDInitEnc, @NCDFollowEnc)
AND enc.encounter_id IN (SELECT obs.encounter_id FROM obs JOIN encounter ON
 patient_id = person_id AND encounter_type IN (@NCDInitEnc, @NCDFollowEnc) AND obs.voided = 0))
AND DATE(e.encounter_datetime) >= date(:startDate)
AND DATE(e.encounter_datetime) <= date(:endDate )
GROUP BY e.encounter_id ORDER BY p.patient_id;