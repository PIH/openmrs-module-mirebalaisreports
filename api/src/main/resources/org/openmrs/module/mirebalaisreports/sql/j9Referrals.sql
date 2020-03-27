set @prenatal = (select encounter_type_id from encounter_type where uuid = '91DDF969-A2D4-4603-B979-F2D6F777F4AF');
set @pediatric = (select encounter_type_id from encounter_type where uuid = '0CF4717A-479F-4349-AE6F-8602E2AA41D3');
set @postnatal = (select encounter_type_id from encounter_type where uuid = '0E7160DF-2DD1-4728-B951-641BBE4136B8');
set @mat_followup = (select encounter_type_id from encounter_type where uuid = '690670E2-A0CC-452B-854D-B95E2EAB75C9');
set @general_referral = (select concept_id from concept where uuid = '1788AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA');
set @yes = (select concept_id from concept where uuid = '3cd6f600-26fe-102b-80cb-0017a47871b2');
set @no = (select concept_id from concept where uuid = '3cd6f86c-26fe-102b-80cb-0017a47871b2');
set @urgent = (select concept_id from concept where uuid = '9e4b6acc-ab97-4ecd-a48c-b3d67e5ef778');
set @family_referred = (select concept_id from concept where uuid = '06d89708-2677-44ce-bc87-197ac608c2c1');
set @mh_referral = (select concept_id from concept where uuid = '0421d974-2bc0-4805-a842-3ceba3b82be7');
set @tetanus_vaccine = (select concept_id from concept where uuid = '9ff12dd0-ff38-49eb-adfc-446bdfee0f9e');
set @malnutrition_referral = (select concept_id from concept where uuid = '313c39ae-5fb3-45b2-8315-25b2b714e0bf');
set @patient_id = (select person_id from person where uuid = @patient);
-- set @ped_vaccine = (select concept_id from concept where uuid = '??????????');  -- need to add pediatric vaccine
set @loc = IF(@locale is null, 'fr', @locale);


select e.encounter_id,p.uuid "person_uuid", e.uuid "encounter_uuid",v.uuid "visit_uuid",  zlemr(e.patient_id) "zl_emr_id", concat(cna.given_name, ' ', cna.family_name) "patient_name", date(encounter_datetime) "referral_date",
CASE  -- locale?
  WHEN o_r.concept_id = @general_referral and @loc = 'en' THEN "General"
  WHEN o_r.concept_id = @general_referral and @loc = 'fr' THEN "Général"
  WHEN o_r.concept_id = @family_referred and @loc = 'en' THEN "Family Member"
  WHEN o_r.concept_id = @family_referred and @loc = 'fr' THEN "Membre de famille"
  WHEN o_r.concept_id = @mh_referral and @loc = 'en' THEN "Mental Health"
  WHEN o_r.concept_id = @mh_referral and @loc = 'fr' THEN "Santé mentale"
  WHEN o_r.concept_id = @tetanus_vaccine and @loc = 'en' THEN  "Tetanus Vaccination"
  WHEN o_r.concept_id = @tetanus_vaccine and @loc = 'fr' THEN  "Vaccination contre le tétanos"
  WHEN o_r.concept_id = @malnutrition_referral and @loc = 'en' THEN  "Malnutrition"
  WHEN o_r.concept_id = @malnutrition_referral and @loc = 'fr' THEN  "Malnutrition"
  --  need to add pediatric vaccine
END "referral_type",
CASE
  WHEN o_r.concept_id = @general_referral and ou.value_coded = @yes and @loc = 'en' THEN "Urgent"
  WHEN o_r.concept_id = @general_referral and ou.value_coded = @yes and @loc = 'fr' THEN "Urgent"
  WHEN o_r.concept_id = @general_referral and ou.value_coded = @no and @loc = 'en' THEN "non-Urgent"
  WHEN o_r.concept_id = @general_referral and ou.value_coded = @no and @loc = 'fr' THEN "Pas urgent"
  WHEN o_r.concept_id = @mh_referral then concept_name(o_r.value_coded, @loc)
  WHEN o_r.concept_id = @malnutrition_referral then date(o_r.value_datetime)
END "details"
from encounter e
INNER JOIN person p on p.person_id = e.patient_id and (@patient_id is null or p.person_id = @patient_id)
INNER JOIN visit v on v.visit_id = e.visit_id
LEFT OUTER JOIN current_name_address cna on cna.person_id = e.patient_id
INNER JOIN obs o_r on e.encounter_id = o_r.encounter_id and o_r.voided = 0 and
  ( (o_r.concept_id = @general_referral and o_r.value_coded = @yes) or
    (o_r.concept_id = @family_referred and o_r.value_coded = @yes) or
    (o_r.concept_id = @tetanus_vaccine and o_r.value_coded = @yes) or
    o_r.concept_id = @mh_referral or
    o_r.concept_id = @malnutrition_referral -- haven't tested yet
    -- need to add criteria for pediatric vaccine
   )
LEFT OUTER JOIN obs ou on e.encounter_id = ou.encounter_id and ou.voided = 0 and ou.concept_id = @urgent
where e.voided = 0
and e.encounter_type in (@prenatal, @pediatric,@postnatal,@mat_followup)
AND date(e.encounter_datetime) >= date(@startDate)
AND date(e.encounter_datetime) <= date(@endDate)
order by referral_date desc
;
