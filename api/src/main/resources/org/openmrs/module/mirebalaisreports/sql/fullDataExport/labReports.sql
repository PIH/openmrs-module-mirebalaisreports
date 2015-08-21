SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark,  e.encounter_datetime, el.name encounter_location,
CONCAT(pn.given_name, ' ',pn.family_name) provider,
obsjoins.*
FROM patient p
-- Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = :zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
-- ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
--Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id =:unknownPt 
            AND un.voided = 0
-- Gender
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
-- Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id
INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = :labResultEnc
INNER JOIN location el ON e.location_id = el.location_id
-- Provider Name
INNER JOIN encounter_provider ep ON ep.encounter_id = e.encounter_id and ep.voided = 0
INNER JOIN provider pv ON pv.provider_id = ep.provider_id 
INNER JOIN person_name pn ON pn.person_id = pv.person_id and pn.voided = 0
-- Straight Obs Joins
INNER JOIN 
(select o.encounter_id,
max(CASE when crs.name = 'PIH' and crt.code = 'DATE OF LABORATORY TEST' then date(o.value_datetime) end) 'Date_of_Lab_Test',
max(CASE when crs.name = 'PIH' and crt.code = 'HEMOGLOBIN' then o.value_numeric end) 'Hemoglobin',
max(CASE when crs.name = 'PIH' and crt.code = 'HbA1c' then o.value_numeric end) 'HbA1c',
max(CASE when crs.name = 'PIH' and crt.code = 'HEMATOCRIT' then o.value_numeric end) 'Hematocrit',
max(CASE when crs.name = 'PIH' and crt.code = 'WHITE BLOOD CELLS' then o.value_numeric end) 'White_Blood_Cells',
max(CASE when crs.name = 'PIH' and crt.code = 'Absolute Neutrophil Count' then o.value_numeric end) 'Absolute_Neutrophil_Count',
max(CASE when crs.name = 'PIH' and crt.code = 'NEUTROPHILS' then o.value_numeric end) 'NEUTROPHILS',
max(CASE when crs.name = 'PIH' and crt.code = 'ABSOLUTE LYMPHOCYTE COUNT' then o.value_numeric end) 'Absolute_Lymphocyte_Count',
max(CASE when crs.name = 'PIH' and crt.code = 'LYMPHOCYTES' then o.value_numeric end) 'Lymphocytes',
max(CASE when crs.name = 'PIH' and crt.code = 'PLATELETS' then o.value_numeric end) 'Platelets',
max(CASE when crs.name = 'PIH' and crt.code = 'ERYTHROCYTE SEDIMENTATION RATE' then o.value_numeric end) 'Erythrocyte_Sedimentation_Rate',
max(CASE when crs.name = 'PIH' and crt.code = 'INTERNATIONAL NORMALIZED RATIO' then o.value_numeric end) 'International_Normalized_Ratio',
max(CASE when crs.name = 'PIH' and crt.code = 'SERUM GLUTAMIC-OXALOACETIC TRANSAMINASE' then o.value_numeric end) 'Serum_Glutamic-Oxaloacetic_Transaminase',
max(CASE when crs.name = 'PIH' and crt.code = 'SERUM GLUTAMIC-PYRUVIC TRANSAMINASE' then o.value_numeric end) 'Serum_Glutamic-Pyruvic_Transaminase',
max(CASE when crs.name = 'PIH' and crt.code = 'TOTAL BILIRUBIN' then o.value_numeric end) 'Total_Bilirubin',
max(CASE when crs.name = 'PIH' and crt.code = 'ALKALINE PHOSPHATASE' then o.value_numeric end) 'Alkaline_Phosphatase',
max(CASE when crs.name = 'PIH' and crt.code = 'SERUM SODIUM' then o.value_numeric end) 'Serum_Sodium',
max(CASE when crs.name = 'PIH' and crt.code = 'SERUM POTASSIUM' then o.value_numeric end) 'Serum_Potassium',
max(CASE when crs.name = 'PIH' and crt.code = 'SERUM CHLORIDE' then o.value_numeric end) 'Serum_Chloride',
max(CASE when crs.name = 'PIH' and crt.code = 'SERUM CARBON DIOXIDE' then o.value_numeric end) 'Serum_Carbon_Dioxide',
max(CASE when crs.name = 'PIH' and crt.code = 'BLOOD UREA NITROGEN' then o.value_numeric end) 'Blood_Urea_Nitrogen',
max(CASE when crs.name = 'PIH' and crt.code = 'SERUM CREATININE' then o.value_numeric end) 'Serum_Creatinine',
max(CASE when crs.name = 'PIH' and crt.code = 'SERUM GLUCOSE' then o.value_numeric end) 'Serum_Glucose',
max(CASE when crs.name = 'PIH' and crt.code = 'SERUM CALCIUM' then o.value_numeric end) 'Serum_Calcium',
max(CASE when crs.name = 'CIEL' and crt.code = '160914' then o.value_numeric end) 'Glucose_Prandial',
max(CASE when crs.name = 'CIEL' and crt.code = '160912' then o.value_numeric end) 'Glucose-Fasting',
max(CASE when crs.name = 'PIH' and crt.code = 'TOTAL CHOLESTEROL' then o.value_numeric end) 'Total_Cholesterol',
max(CASE when crs.name = 'PIH' and crt.code = 'LOW-DENSITY LIPOPROTEIN CHOLESTEROL' then o.value_numeric end) 'Low-Den_Lipoprotein_Cholesterol',
max(CASE when crs.name = 'PIH' and crt.code = 'HIGH-DENSITY LIPOPROTEIN CHOLESTEROL' then o.value_numeric end) 'High-Den_Lipoprotein_Cholesterol',
max(CASE when crs.name = 'PIH' and crt.code = 'TRIGLYCERIDES' then o.value_numeric end) 'Triglycerides',
max(CASE when crs.name = 'PIH' and crt.code = 'CD4 COUNT' then o.value_numeric end) 'CD4_Count',
max(CASE when crs.name = 'PIH' and crt.code = 'MALARIAL SMEAR' then cn.name end) 'Malarial_Smear',
max(CASE when crs.name = 'PIH' and crt.code = 'LACTATE DEHYDROGENASE' then o.value_numeric end) 'Lactate_Dehydrogenase'
from encounter e, concept_reference_map crm,  concept_reference_term crt, concept_reference_source crs, obs o
LEFT OUTER JOIN concept_name cn on o.value_coded = cn.concept_id and cn.locale = 'en' and cn.locale_preferred = '1'  and cn.voided = 0
LEFT OUTER JOIN obs obs2 on obs2.obs_id = o.obs_group_id
LEFT OUTER JOIN 
(select crm2.concept_id,crs2.name, crt2.code from concept_reference_map crm2, concept_reference_term crt2, concept_reference_source crs2
where 1=1
and crm2.concept_reference_term_id = crt2.concept_reference_term_id 
and crt2.concept_source_id = crs2.concept_source_id) obsgrp on obsgrp.concept_id = obs2.concept_id
where 1=1 
and e.encounter_type= :labResultEnc
and crm.concept_reference_term_id = crt.concept_reference_term_id
and crt.concept_source_id = crs.concept_source_id
and crm.concept_id = o.concept_id
and o.encounter_id = e.encounter_id
and e.voided = 0
and o.voided = 0
group by o.encounter_id
) obsjoins ON obsjoins.encounter_id = ep.encounter_id
-- end columns joins
WHERE p.voided = 0
-- exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt
                         AND voided = 0)
AND date(e.encounter_datetime) >= :startDate 
AND date(e.encounter_datetime) <= :endDate
GROUP BY e.encounter_id
;