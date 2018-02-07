SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, ref_num.identifier ref_num, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark,  e.encounter_datetime, el.name encounter_location,
CONCAT(pn.given_name, ' ',pn.family_name) provider,e.visit_id,
obsjoins.*, obs_occ.name "occupation"
FROM patient p
-- Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = :zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
-- ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
-- Most recent reference number (used in standalone MH system)
LEFT OUTER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = :refNum
            AND voided = 0 ORDER BY date_created DESC) ref_num ON p.patient_id = ref_num.patient_id
-- Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id =:unknownPt
            AND un.voided = 0
-- Gender
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
--  Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = :mentalHealthEnc
INNER JOIN location el ON e.location_id = el.location_id
--  Provider Name
LEFT OUTER JOIN encounter_provider ep ON ep.encounter_id = e.encounter_id and ep.voided = 0
LEFT OUTER JOIN provider pv ON pv.provider_id = ep.provider_id
LEFT OUTER JOIN person_name pn ON pn.person_id = pv.person_id and pn.voided = 0
-- Join in most recent observation of occupation
LEFT OUTER JOIN (select o1.obs_id, cn_occ.name from obs o1, concept_name cn_occ
  where cn_occ.concept_id = o1.value_coded
  and cn_occ.voided = 0
  and cn_occ.locale = 'fr'
  and cn_occ.locale_preferred = '1') obs_occ 
  on obs_occ.obs_id  =
    (select obs_id from obs o2, concept_reference_map crm, concept_reference_term crt, concept_reference_source crs
    where o2.person_id =p.patient_id
    and o2.voided = 0
    and crm.concept_id = o2.concept_id
    and crt.concept_reference_term_id = crm.concept_reference_term_id
    and crs.concept_source_id = crt.concept_source_id
    and crs.name = 'PIH'
    and crt.code = 'Occupation'
    order by o2.obs_datetime desc
    limit 1)
-- Straight Obs Joins
-- note that Suicidal Thoughts is modeled as a diagnosis and Security Plan as an psychological intervention
INNER JOIN
  (select o.encounter_id,
     group_concat(CASE when crs.name = 'PIH' and crt.code = 'Role of referring person' then cn.name end separator ',') 'referred_by',
     group_concat(CASE when crs.name = 'PIH' and crt.code = 'Role of referring person' then o.comments end separator ',') 'referred_by_other',
     max(CASE when crs.name = 'CIEL' and crt.code = '163225' then o.value_numeric end) 'ZLDSI',
     max(CASE when crs.name = 'CIEL' and crt.code = '163228' then o.value_numeric end) 'CES-D',
     max(CASE when crs.name = 'CIEL' and crt.code = '163222' then o.value_numeric end) 'CGI-S',
     max(CASE when crs.name = 'CIEL' and crt.code = '163223' then o.value_numeric end) 'CGI-I',
     max(CASE when crs.name = 'CIEL' and crt.code = '163224' then o.value_numeric end) 'CGI-E',
     max(CASE when crs.name = 'CIEL' and crt.code = '163226' then o.value_numeric end) 'WHODAS',
     max(CASE when crs.name = 'PIH' and crt.code = 'Days with difficulties in past month' then o.value_numeric end) 'difficult_days',
     max(CASE when crs.name = 'PIH' and crt.code = 'Days without usual activity in past month' then o.value_numeric end) 'days_wo_activity',
     max(CASE when crs.name = 'PIH' and crt.code = 'Days with less activity in past month' then o.value_numeric end) 'reduced_days',
     max(CASE when crs.name = 'CIEL' and crt.code = '163227' then cn.name end) 'AIMS',
     max(CASE when crs.name = 'PIH' and crt.code = '6797' then o.value_numeric end) 'seizure_freq_in_months',
     max(CASE when crs.name = 'PIH' and crt.code = 'Suicidal evaluation' then cn.name end) 'suicidal_eval',
     group_concat(CASE when crs.name = 'PIH' and crt.code = 'Mental health diagnosis' then cn.name end separator ',') 'diagnoses',
     group_concat(CASE when crs.name = 'PIH' and crt.code = 'Type of provider' then cn.name end separator ',') 'provider_type',
     group_concat(CASE when crs.name = 'PIH' and crt.code = 'Mental health intervention' then cn.name end separator ',') 'interventions',
     max(CASE when crs.name = 'PIH' and crt.code = 'Mental health intervention' then o.comments end) 'interventions_other',
     group_concat(CASE when crs.name = 'PIH' and crt.code = 'Mental health medication' then cn.name end separator ',') 'medications',
     max(CASE when crs.name = 'PIH' and crt.code = 'Medication comments (text)' then o.value_text end) 'medication_comments',
     max(CASE when crs.name = 'PIH' and crt.code = 'TYPE OF PATIENT' then cn.name end) 'hospitalized',
     group_concat(CASE when crs.name = 'PIH' and crt.code = 'Type of referral role' then cn.name end separator ',') 'referred_to',
     max(CASE when crs.name = 'PIH' and crt.code = 'PATIENT PLAN COMMENTS' then o.value_text end) 'patient_plan_comments',
     max(CASE when crs.name = 'PIH' and crt.code = 'RETURN VISIT DATE' then o.value_datetime end) 'return_visit_date'
from encounter e, concept_reference_map crm,  concept_reference_term crt, concept_reference_source crs, obs o
LEFT OUTER JOIN concept_name cn on o.value_coded = cn.concept_id and cn.locale = 'fr' and cn.locale_preferred = '1'  and cn.voided = 0
LEFT OUTER JOIN obs obs2 on obs2.obs_id = o.obs_group_id
LEFT OUTER JOIN
(select crm2.concept_id,crs2.name, crt2.code from concept_reference_map crm2, concept_reference_term crt2, concept_reference_source crs2
where 1=1
and crm2.concept_reference_term_id = crt2.concept_reference_term_id
and crt2.concept_source_id = crs2.concept_source_id) obsgrp on obsgrp.concept_id = obs2.concept_id
where 1=1
and e.encounter_type= :mentalHealthEnc
and crm.concept_reference_term_id = crt.concept_reference_term_id
and crt.concept_source_id = crs.concept_source_id
and crm.concept_id = o.concept_id
and o.encounter_id = e.encounter_id
and e.voided = 0
and o.voided = 0
group by o.encounter_id
) obsjoins ON obsjoins.encounter_id = e.encounter_id
--  end columns joins
WHERE p.voided = 0
-- exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt
                         AND voided = 0)
AND date(e.encounter_datetime) >= :startDate
AND date(e.encounter_datetime) <= :endDate
GROUP BY e.encounter_id
;
