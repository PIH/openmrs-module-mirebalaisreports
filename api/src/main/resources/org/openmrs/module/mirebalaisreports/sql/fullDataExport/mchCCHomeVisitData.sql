DROP TEMPORARY TABLE IF EXISTS temp_j9_mother_home_visit;

SET sql_safe_updates = 0;
SET SESSION group_concat_max_len = 100000;

-- set @startDate = '2020-03-01';
-- set @endDate = '2020-03-30';

set @prenatal_homeasess_encounter_type = encounter_type('Prenatal Home Assessment');
set @systolic_bp = concept_from_mapping('CIEL', '5085');
set @diastolic_bp = concept_from_mapping('CIEL', '5086');
set @temp_c = concept_from_mapping('CIEL', '5088');
set @heart_rate = concept_from_mapping('CIEL', '5087');
set @respiratory_rate = concept_from_mapping('CIEL', '5242');
set @symptom_present = concept_from_mapping('PIH', 'SYMPTOM PRESENT');
set @referred_to_hospital = concept_from_mapping('CIEL', '1788');
set @referral_emergency = concept_from_mapping('PIH','7813');
set @other_family_member_refferred = concept_from_mapping('PIH', '12745');
set @counseling_pregnancy_danger_signs = concept_from_mapping('CIEL', '1912');
set @mental_health_referral_reason = concept_from_mapping('PIH', '12746');
set @tetanus_vaccination_refferal = concept_from_mapping('PIH', '12747');
set @return_visit_date = concept_from_mapping('CIEL', '5096');
set @clinical_comments = concept_from_mapping('CIEL', '159395');

create temporary table temp_j9_mother_home_visit
(
encounter_id int(11),
patient_id int(11),
encounter_date datetime,
systolic_bp int(11),
diastolic_bp int(11),
temp_c double,
heart_rate int(11),
respiratory_rate int(11),
symptom_present text,
referred_to_hospital text(50),
referral_emergency text(50),
other_family_member_refferred varchar(50),
counseling_pregnancy_danger_signs text,
mental_health_referral_reason text,
tetanus_vaccination_refferal varchar(50),
return_visit_date datetime,
clinical_comments text
);

insert into temp_j9_mother_home_visit (encounter_id, patient_id, encounter_date)

select encounter_id, patient_id, date(encounter_datetime) from encounter where encounter_type = @prenatal_homeasess_encounter_type
and voided = 0
-- filter by date
and date(encounter_datetime) >=  date(@startDate)
and date(encounter_datetime) <=  date(@endDate);

update temp_j9_mother_home_visit tj9mhv
left join obs o on tj9mhv.encounter_id = o.encounter_id and o.concept_id = @systolic_bp and voided = 0
set tj9mhv.systolic_bp = o.value_numeric,
	tj9mhv.diastolic_bp = (select value_numeric from obs where concept_id = @diastolic_bp and voided = 0 and encounter_id = tj9mhv.encounter_id),
    tj9mhv.temp_c = (select value_numeric from obs where concept_id = @temp_c and voided = 0 and encounter_id = tj9mhv.encounter_id),
    tj9mhv.heart_rate = (select value_numeric from obs where concept_id = @heart_rate and voided = 0 and encounter_id = tj9mhv.encounter_id),
	tj9mhv.respiratory_rate = (select value_numeric from obs where concept_id = @respiratory_rate and voided = 0 and encounter_id = tj9mhv.encounter_id);


-- SYMPTOM PRESENT
update temp_j9_mother_home_visit tj9mhv
left join
(
select o.encounter_id encounterid, group_concat(name separator ' | ') symptom_present from obs o join concept_name cn on o.voided = 0 and cn.concept_id = o.value_coded and cn.voided = 0 and o.concept_id =
@symptom_present and cn.locale = "fr" and concept_name_type = "FULLY_SPECIFIED" group by o.encounter_id
) o on o.encounterid = tj9mhv.encounter_id
set tj9mhv.symptom_present = o.symptom_present;

update temp_j9_mother_home_visit tj9mhv
left join obs o on tj9mhv.encounter_id = o.encounter_id and o.concept_id = @referred_to_hospital and voided = 0
set
-- referred_to_hospital Required field
	tj9mhv.referred_to_hospital = IF(o.value_coded = 1, "Yes", "No");

update temp_j9_mother_home_visit tj9mhv
left join obs o on tj9mhv.encounter_id = o.encounter_id and o.concept_id = @referral_emergency and voided = 0
set
-- referral_emergency
    tj9mhv.referral_emergency = (SELECT CASE o.value_coded WHEN 1 THEN "Urgent" WHEN 2 THEN "Non urgent" ELSE "NULL" END);


update temp_j9_mother_home_visit tj9mhv
left join obs o on tj9mhv.encounter_id = o.encounter_id and o.concept_id = @other_family_member_refferred and voided = 0
set
-- Other_family_member_referred_to_hospital
	tj9mhv.other_family_member_refferred = IF(o.value_coded = 1, "Yes", "No");

-- Counseling, danger signs of pregnancy
update temp_j9_mother_home_visit tj9mhv
left join
(
select o.encounter_id encounterid, group_concat(name separator ' | ') counseling_pregnancy_danger_signs from obs o join concept_name cn on
o.voided = 0 and cn.concept_id = o.value_coded and cn.voided = 0 and o.concept_id =
@counseling_pregnancy_danger_signs and cn.locale = "fr" and concept_name_type = "FULLY_SPECIFIED" group by o.encounter_id
) o on o.encounterid = tj9mhv.encounter_id
set tj9mhv.counseling_pregnancy_danger_signs = o.counseling_pregnancy_danger_signs;

-- Reason for mental health referral
update temp_j9_mother_home_visit tj9mhv
left join
(
select o.encounter_id encounterid, group_concat(name separator ' | ') mental_health_referral_reason from obs o join concept_name cn on
o.voided = 0 and cn.concept_id = o.value_coded and cn.voided = 0 and o.concept_id =
@mental_health_referral_reason and cn.locale = "fr" and concept_name_type = "FULLY_SPECIFIED" group by o.encounter_id
) o on o.encounterid = tj9mhv.encounter_id
set tj9mhv.mental_health_referral_reason = o.mental_health_referral_reason;

update temp_j9_mother_home_visit tj9mhv
left join obs o on tj9mhv.encounter_id = o.encounter_id and o.concept_id = @tetanus_vaccination_refferal and voided = 0
set
-- tetanus vaccination refferal
	tj9mhv.tetanus_vaccination_refferal = (SELECT CASE o.value_coded WHEN 1 THEN "Yes" WHEN 2 THEN "No" ELSE "NULL" END);

update temp_j9_mother_home_visit tj9mhv
left join obs o on tj9mhv.encounter_id = o.encounter_id and o.concept_id = @return_visit_date and voided = 0
set
-- return visit date
	tj9mhv.return_visit_date = o.obs_datetime;

update temp_j9_mother_home_visit tj9mhv
left join obs o on tj9mhv.encounter_id = o.encounter_id and o.concept_id = @clinical_comments and voided = 0
set
-- return visit date
	tj9mhv.clinical_comments = o.value_text;


select
		patient_id,
        encounter_id,
        zlemr(patient_id) emr_id,
        dosId(patient_id) dossier_id,
        date(encounter_date) encounter_date,
        given_name,
        family_name,
        country,
        department,
        commune,
        section_communal,
        locality,
        street_landmark,
        birthdate_estimated,
		systolic_bp,
		diastolic_bp,
		temp_c,
		heart_rate,
		respiratory_rate,
		symptom_present,
		referred_to_hospital,
		referral_emergency,
		other_family_member_refferred,
		counseling_pregnancy_danger_signs,
		mental_health_referral_reason,
		tetanus_vaccination_refferal,
		return_visit_date,
		clinical_comments
from temp_j9_mother_home_visit inner join current_name_address on person_id = patient_id order by patient_id, encounter_date;