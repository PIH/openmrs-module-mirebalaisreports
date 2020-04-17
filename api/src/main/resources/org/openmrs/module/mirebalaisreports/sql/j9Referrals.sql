set @prenatal = (select encounter_type_id from encounter_type where uuid = '91DDF969-A2D4-4603-B979-F2D6F777F4AF');
set @pediatric = (select encounter_type_id from encounter_type where uuid = '0CF4717A-479F-4349-AE6F-8602E2AA41D3');
set @postnatal = (select encounter_type_id from encounter_type where uuid = '0E7160DF-2DD1-4728-B951-641BBE4136B8');
set @mat_followup = (select encounter_type_id from encounter_type where uuid = '690670E2-A0CC-452B-854D-B95E2EAB75C9');
set @general_referral_question = (select concept_id from concept where uuid = '1788AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA');
set @yes = (select concept_id from concept where uuid = '3cd6f600-26fe-102b-80cb-0017a47871b2');
set @no = (select concept_id from concept where uuid = '3cd6f86c-26fe-102b-80cb-0017a47871b2');
set @urgent = (select concept_id from concept where uuid = '9e4b6acc-ab97-4ecd-a48c-b3d67e5ef778');
set @family_referred_question = (select concept_id from concept where uuid = '06d89708-2677-44ce-bc87-197ac608c2c1');
set @mh_referral_question = (select concept_id from concept where uuid = '0421d974-2bc0-4805-a842-3ceba3b82be7');
set @tetanus_vaccine_question = (select concept_id from concept where uuid = '9ff12dd0-ff38-49eb-adfc-446bdfee0f9e');
set @malnutrition_referral_question = (select concept_id from concept where uuid = '313c39ae-5fb3-45b2-8315-25b2b714e0bf');
set @ped_vaccine_question = (select concept_id from concept where uuid = 'd84d6aa3-5c68-475a-827e-4cb04624800d');

set @referral_construct = (select concept_id from concept where uuid = '01ed21a6-b1ef-45ff-9856-c8b0e9cc4569');
set @comment = (select concept_id from concept where uuid = '161011AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA');
set @fulfillment = (select concept_id from concept where uuid = 'b8496eb7-daf0-444d-842d-5c331c821c17');
set @unmet = (select concept_id from concept where uuid = '160068AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA');
set @no_show = (select concept_id from concept where uuid = '164143AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA');
set @confirmed_present = (select concept_id from concept where uuid = '165792AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA');
set @completed = (select concept_id from concept where uuid = '3cd93172-26fe-102b-80cb-0017a47871b2');
set @other = (select concept_id from concept where uuid = '	3cee7fb4-26fe-102b-80cb-0017a47871b2');
set @type_referral = (select concept_id from concept where uuid = '1272AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA');
set @fam_member_referral = (select concept_id from concept where uuid = '7e32130b-5be8-4766-bf03-f1b909934141');
set @general_referral = (select concept_id from concept where uuid = '3ce0d472-26fe-102b-80cb-0017a47871b2');
set @mh_referral = (select concept_id from concept where uuid = '3ced9a68-26fe-102b-80cb-0017a47871b2');
set @tetanus_vaccine_referral = (select concept_id from concept where uuid = '9ff12dd0-ff38-49eb-adfc-446bdfee0f9e');
set @ped_vaccine_referral = (select concept_id from concept where uuid = 'd84d6aa3-5c68-475a-827e-4cb04624800d');
set @malnutrition_referral = (select concept_id from concept where uuid = '3ce1e560-26fe-102b-80cb-0017a47871b2');


-- only for testing...remove!
-- set @patient_id = null;
-- set @startDate = '2020-04-16';
-- set @endDate = '2020-04-30';
-- set @locale = 'en';
-- remove above!

drop TEMPORARY TABLE IF EXISTS temp_referrals;

create temporary table temp_referrals
(
    encounter_id int(11),
    person_uuid char(38),
    encounter_uuid char(38),
    visit_uuid char(38),
    zl_emr_id varchar(50),
    patient_name varchar(255),
    referral_date date,
    referral_concept_id int(11),
    referral_value_coded int(11),
    referral_value_datetime datetime,
    urgent_value_coded int(11),
    referral_status_coded int(11)
)
;

-- inserts rows into the temp table for every observation that indicates a referral.
insert into temp_referrals (encounter_id, person_uuid,encounter_uuid,visit_uuid,zl_emr_id,patient_name,referral_date,referral_concept_id,referral_value_coded,referral_value_datetime,urgent_value_coded)
select e.encounter_id, p.uuid "patient_uuid", e.uuid "encounter_uuid",v.uuid "visit_uuid",  zlemr(e.patient_id) "zl_emr_id", concat(cna.given_name, ' ', cna.family_name) "patient_name", date(encounter_datetime) "referral_date",
o_r.concept_id, o_r.value_coded, o_r.value_datetime,ou.value_coded
from encounter e
INNER JOIN person p on p.person_id = e.patient_id and (@patient_id is null or p.person_id = @patient_id)
INNER JOIN visit v on v.visit_id = e.visit_id
LEFT OUTER JOIN current_name_address cna on cna.person_id = e.patient_id
INNER JOIN obs o_r on e.encounter_id = o_r.encounter_id and o_r.voided = 0 and
  ( (o_r.concept_id = @general_referral_question and o_r.value_coded = @yes) or
    (o_r.concept_id = @family_referred_question and o_r.value_coded = @yes) or
    (o_r.concept_id = @tetanus_vaccine_question and o_r.value_coded = @yes) or
    o_r.concept_id = @mh_referral_question or
    o_r.concept_id = @malnutrition_referral_question or
    (o_r.concept_id = @ped_vaccine_question and o_r.value_coded = @yes)
    )
LEFT OUTER JOIN obs ou on e.encounter_id = ou.encounter_id and ou.voided = 0 and ou.concept_id = @urgent
where e.voided = 0
and e.encounter_type in (@prenatal, @pediatric,@postnatal,@mat_followup)
AND date(e.encounter_datetime) >= date(@startDate)
AND date(e.encounter_datetime) <= date(@endDate)
order by referral_date desc
;

-- update referral status
update temp_referrals t
inner join obs rc on rc.voided = 0 and rc.concept_id = @referral_construct and rc.encounter_id = t.encounter_id
inner join obs tr on tr.voided = 0 and tr.obs_group_id = rc.obs_id and tr.concept_id = @type_referral and
  ( (referral_concept_id = @general_referral_question and tr.value_coded = @general_referral) or
    (referral_concept_id = @family_referred_question and tr.value_coded = @fam_member_referral) or
    (referral_concept_id = @mh_referral_question and tr.value_coded = @mh_referral) or
    (referral_concept_id = @tetanus_vaccine_question and tr.value_coded = @tetanus_vaccine_referral) or
    (referral_concept_id = @malnutrition_referral_question and tr.value_coded = @malnutrition_referral) or
    (referral_concept_id = @ped_vaccine_question and tr.value_coded = @ped_vaccine_referral)
  )
inner join obs fs on fs.voided = 0 and  fs.obs_group_id = rc.obs_id and fs.concept_id = @fulfillment
set t.referral_status_coded = fs.value_coded
;


 -- select and translate results.
 -- Note that since a mental health referral with multiple reasons is really just one referral, we are grouping by referral type (and everything else except reason) here.
select t.encounter_id, person_uuid, encounter_uuid, visit_uuid, zl_emr_id, patient_name, referral_date ,
CASE
  WHEN t.referral_concept_id = @general_referral_question and @locale = 'en' THEN "General"
  WHEN t.referral_concept_id = @general_referral_question and @locale = 'fr' THEN "Général"
  WHEN t.referral_concept_id = @family_referred_question and @locale = 'en' THEN "Family Member"
  WHEN t.referral_concept_id = @family_referred_question and @locale = 'fr' THEN "Membre de famille"
  WHEN t.referral_concept_id = @mh_referral_question and @locale = 'en' THEN "Mental Health"
  WHEN t.referral_concept_id = @mh_referral_question and @locale = 'fr' THEN "Santé mentale"
  WHEN t.referral_concept_id = @tetanus_vaccine_question and @locale = 'en' THEN  "Tetanus Vaccination"
  WHEN t.referral_concept_id = @tetanus_vaccine_question and @locale = 'fr' THEN  "Vaccination contre le tétanos"
  WHEN t.referral_concept_id = @malnutrition_referral_question and @locale = 'en' THEN  "Malnutrition"
  WHEN t.referral_concept_id = @malnutrition_referral_question and @locale = 'fr' THEN  "Malnutrition"
  WHEN t.referral_concept_id = @ped_vaccine_question and @locale = 'en' THEN  "Pediatric Vaccination"
  WHEN t.referral_concept_id = @ped_vaccine_question and @locale = 'fr' THEN  "Vaccination pédiatrique"
END "referral_type",
group_concat(
CASE
  WHEN t.referral_concept_id = @general_referral_question and t.urgent_value_coded = @yes and @locale = 'en' THEN "Urgent"
  WHEN t.referral_concept_id = @general_referral_question and t.urgent_value_coded = @yes and @locale = 'fr' THEN "Urgent"
  WHEN t.referral_concept_id = @general_referral_question and t.urgent_value_coded = @no and @locale = 'en' THEN "non-Urgent"
  WHEN t.referral_concept_id = @general_referral_question and t.urgent_value_coded = @no and @locale = 'fr' THEN "Pas urgent"
  WHEN t.referral_concept_id = @mh_referral_question then concept_name(t.referral_value_coded, @locale)
  WHEN t.referral_concept_id = @malnutrition_referral_question then date(t.referral_value_datetime)
END separator ', ') "details",
concept_name(t.referral_status_coded, @locale) 'fulfillment_status'
from temp_referrals t
group by t.encounter_id, person_uuid, encounter_uuid, visit_uuid, zl_emr_id, patient_name, referral_date, referral_type, fulfillment_status
order by referral_date, zl_emr_id, referral_type
;
