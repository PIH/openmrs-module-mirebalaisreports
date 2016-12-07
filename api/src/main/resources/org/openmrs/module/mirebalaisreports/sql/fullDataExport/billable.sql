SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, dos_num1.identifier "Dossier_num1",dos_num2.identifier "Dossier_num2", pr.gender,pr.birthdate, 
pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark,  
e.encounter_datetime, el.name encounter_location,
ins_name.name "Insurance_Company", ins_other.value_text "Other_Insurance", ins_policy.value_text "Policy_Number", 
cn_diag1.name "Visit_Diagnosis_1",
cn_diag2.name "Visit_Diagnosis_2",
cn_diag3.name "Visit_Diagnosis_3",
et.name "Encounter_type" , 
cn_rad.name "Test_order", med.medication, med.quantity, 
obsjoins.Outpatient_procedure,
obsjoins.Surgical_Service,
obsjoins.Surgical_Procedure,
obsjoins.Chemo_Regimen,
obsjoins.Lab_Tests_Ordered
FROM patient p
-- Preferred ZL EMR ID
INNER JOIN patient_identifier zl on zl.patient_id = p.patient_id and zl.identifier_type = :zlId
            and zl.voided = 0 and zl.preferred = 1  
-- ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
-- Dossier number 1
LEFT OUTER JOIN  patient_identifier dos_num1 on dos_num1.identifier_type = :dosId and dos_num1.patient_id = p.patient_id and dos_num1.voided = 0
LEFT OUTER JOIN  patient_identifier dos_num2 on dos_num2.identifier_type = :dosId and dos_num2.patient_id = p.patient_id and dos_num2.voided = 0 and dos_num2.identifier <> dos_num1.identifier
-- Gender
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
--  Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id
-- encounter
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0
INNER JOIN encounter_type et ON et.encounter_type_id = e.encounter_type
INNER JOIN location el ON e.location_id = el.location_id
-- Insurance information
LEFT OUTER JOIN (select insco.person_id, cn_ins.name from obs insco, concept_name cn_ins where insco.voided = 0 AND insco.concept_id = 
  (select crm.concept_id from concept_reference_map crm, concept_reference_term crt, concept_reference_source crs
   where crt.concept_reference_term_id = crm.concept_reference_term_id
   and crs.concept_source_id = crt.concept_source_id
   and crs.name = 'PIH'
   and crt.code = 'Haiti insurance company name')
   and cn_ins.concept_id = insco.value_coded
   and cn_ins.voided = 0
   and cn_ins.locale = 'en' and cn_ins.locale_preferred = 1) ins_name
   on ins_name.person_id = pr.person_id
LEFT OUTER JOIN (select insco.person_id, value_text from obs insco where insco.voided = 0 AND insco.concept_id = 
  (select crm.concept_id from concept_reference_map crm, concept_reference_term crt, concept_reference_source crs
   where crt.concept_reference_term_id = crm.concept_reference_term_id
   and crs.concept_source_id = crt.concept_source_id
   and crs.name = 'PIH'
   and crt.code = 'Insurance company name (text)')
   ) ins_other
   on ins_other.person_id = pr.person_id
LEFT OUTER JOIN (select insco.person_id, value_text from obs insco where insco.voided = 0 AND insco.concept_id = 
  (select crm.concept_id from concept_reference_map crm, concept_reference_term crt, concept_reference_source crs
   where crt.concept_reference_term_id = crm.concept_reference_term_id
   and crs.concept_source_id = crt.concept_source_id
   and crs.name = 'PIH'
   and crt.code = 'Insurance policy number')
   ) ins_policy
   on ins_policy.person_id = pr.person_id
-- Radiology order   
LEFT OUTER JOIN orders o_rad on o_rad.encounter_id = e.encounter_id and o_rad.voided = 0
LEFT OUTER JOIN concept_name cn_rad on cn_rad.concept_id = o_rad.concept_id  
   and cn_rad.voided = 0
   and cn_rad.locale = 'en' and cn_rad.locale_preferred = 1
-- Medication dispensed   
LEFT OUTER JOIN (
select o_med.encounter_id,  o_med.obs_group_id,
max(CASE when crs.name = 'PIH' and crt.code = 'MEDICATION ORDERS' then cn.name end) 'Medication',
max(CASE when crs.name = 'PIH' and crt.code = '9071' then o_med.value_numeric end) 'Quantity'
from 
obs o_med
INNER JOIN concept_reference_map crm on crm.concept_id = o_med.concept_id
INNER JOIN concept_reference_term crt ON crt.concept_reference_term_id = crm.concept_reference_term_id
    and crt.code in ('MEDICATION ORDERS','9071')
INNER JOIN concept_reference_source crs ON  crs.concept_source_id = crt.concept_source_id 
LEFT OUTER JOIN concept_name cn ON cn.concept_id = o_med.value_coded and cn.voided = 0 and cn.locale = 'en' and cn.locale_preferred = '1'
where o_med.obs_group_id is not null
group by o_med.obs_group_id) med on med.encounter_id = e.encounter_id
-- Diagnosis 1
LEFT OUTER JOIN 
  (select e.encounter_id, e.visit_id, o.obs_id, o.value_coded from encounter e, obs o
   where o.encounter_id = e.encounter_id
   and e.voided = 0 and o.voided = 0
   and o.concept_id = 
     (select crm.concept_id from concept_reference_map crm, concept_reference_term crt, concept_reference_source crs
      where crt.concept_reference_term_id = crm.concept_reference_term_id
      and crs.concept_source_id = crt.concept_source_id
      and crs.name = 'PIH'
      and crt.code = 'DIAGNOSIS')
   ) diag1 on diag1.visit_id = e.visit_id    
LEFT OUTER JOIN concept_name cn_diag1 on cn_diag1.concept_id = diag1.value_coded and cn_diag1.voided = 0 and cn_diag1.locale = 'fr' and cn_diag1.locale_preferred = 1
-- Diagnosis 2
LEFT OUTER JOIN 
  (select e.encounter_id, e.visit_id, o.obs_id, o.value_coded from encounter e, obs o
   where o.encounter_id = e.encounter_id
   and e.voided = 0 and o.voided = 0
   and o.concept_id = 
     (select crm.concept_id from concept_reference_map crm, concept_reference_term crt, concept_reference_source crs
      where crt.concept_reference_term_id = crm.concept_reference_term_id
      and crs.concept_source_id = crt.concept_source_id
      and crs.name = 'PIH'
      and crt.code = 'DIAGNOSIS')
   ) diag2 on diag2.visit_id = e.visit_id and diag2.obs_id <> diag1.obs_id   
LEFT OUTER JOIN concept_name cn_diag2 on cn_diag2.concept_id = diag2.value_coded and cn_diag2.voided = 0 and cn_diag2.locale = 'fr' and cn_diag2.locale_preferred = 1
-- Diagnosis 3
LEFT OUTER JOIN 
  (select e.encounter_id, e.visit_id, o.obs_id, o.value_coded from encounter e, obs o
   where o.encounter_id = e.encounter_id
   and e.voided = 0 and o.voided = 0
   and o.concept_id = 
     (select crm.concept_id from concept_reference_map crm, concept_reference_term crt, concept_reference_source crs
      where crt.concept_reference_term_id = crm.concept_reference_term_id
      and crs.concept_source_id = crt.concept_source_id
      and crs.name = 'PIH'
      and crt.code = 'DIAGNOSIS')
   ) diag3 on diag3.visit_id = e.visit_id and diag3.obs_id not in (diag1.obs_id,diag2.obs_id)   
LEFT OUTER JOIN concept_name cn_diag3 on cn_diag3.concept_id = diag3.value_coded and cn_diag3.voided = 0 and cn_diag3.locale = 'fr' and cn_diag3.locale_preferred = 1
-- procedures, surgery and chemo
LEFT OUTER JOIN 
(select o.encounter_id,
max(CASE when crs.name = 'PIH' and crt.code = 'Outpatient procedure' then cn.name end) 'Outpatient_procedure',
max(CASE when crs.name = 'PIH' and crt.code = 'Surgical service' then cn.name end) 'Surgical_Service',
max(CASE when crs.name = 'PIH' and crt.code = 'Surgical procedure' then cn.name end) 'Surgical_Procedure',
max(CASE when crs.name = 'CIEL' and crt.code = '163073' then cn.name end) 'Chemo_Regimen',
group_concat(CASE when crs.name = 'PIH' and crt.code = 'Lab test ordered coded' then cn.name end separator ',') 'Lab_Tests_Ordered'
from encounter e, concept_reference_map crm,  concept_reference_term crt, concept_reference_source crs, obs o
LEFT OUTER JOIN concept_name cn on o.value_coded = cn.concept_id and cn.locale = 'en' and cn.locale_preferred = '1'  and cn.voided = 0
LEFT OUTER JOIN obs obs2 on obs2.obs_id = o.obs_group_id
LEFT OUTER JOIN 
(select crm2.concept_id,crs2.name, crt2.code from concept_reference_map crm2, concept_reference_term crt2, concept_reference_source crs2
where crm2.concept_reference_term_id = crt2.concept_reference_term_id 
and crt2.concept_source_id = crs2.concept_source_id) obsgrp on obsgrp.concept_id = obs2.concept_id
where crm.concept_reference_term_id = crt.concept_reference_term_id
and crt.concept_source_id = crs.concept_source_id
and crm.concept_id = o.concept_id
and o.encounter_id = e.encounter_id
and e.voided = 0
and o.voided = 0
AND date(e.encounter_datetime) >= date(:startDate)
AND date(e.encounter_datetime) <= date(:endDate)  
group by o.encounter_id
) obsjoins ON obsjoins.encounter_id = e.encounter_id
WHERE p.voided = 0
-- exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt 
                         AND voided = 0)
 AND (ins_name.name is not null or ins_other.value_text is not null) -- change to is NOT null
   and (cn_rad.name is not null
     or medication is not null 
     or quantity is not null 
     or Outpatient_procedure is not null
     or Surgical_Service is not null
     or Surgical_Procedure is not null
     or Chemo_Regimen is not null
     or Lab_Tests_Ordered is not null)
AND date(e.encounter_datetime) >= date(:startDate)
AND date(e.encounter_datetime) <= date(:endDate) 
GROUP BY e.encounter_id, med.obs_group_id
ORDER BY date(e.encounter_datetime), zlemr,encounter_datetime 
;