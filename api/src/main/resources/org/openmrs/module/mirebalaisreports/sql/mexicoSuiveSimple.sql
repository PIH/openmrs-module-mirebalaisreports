SELECT
    CONCAT(DATE(e.encounter_datetime), " ", TIME(e.encounter_datetime)) "Fecha",
    CONCAT(given_name, " ", family_name) "Nombre",
    MAX(cn.name) "Dx",
    MAX(crt.code) "ICD-10",
    IF(IFNULL(prev_diag.obs_id, 'null') = 'null', '1', ' ') "1a vez",
    IF(certainty_name.name LIKE "Confirmado", '1', ' ') "Confirmado",
    ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) "Edad",
    pr.gender "Genero",
    pa.city_village "Loc"
FROM patient p
-- Person
         INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
-- Most recent name
         INNER JOIN (SELECT person_id, given_name, family_name
                     FROM person_name
                     WHERE voided = 0
                     ORDER BY date_created desc) n ON p.patient_id = n.person_id
-- Most recent address
         LEFT OUTER JOIN person_address pa
                         ON pa.person_address_id = (SELECT person_address_id FROM person_address a2
                                                    WHERE a2.person_id =  pr.person_id AND a2.voided = 0
                                                    ORDER BY a2.preferred DESC, a2.date_created DESC
                                                    LIMIT 1)
-- Encounters
         INNER JOIN encounter e ON p.patient_id = e.patient_id AND e.voided = 0 AND
                                   e.encounter_type = :mexConsultEnc
-- Diagnoses
         INNER JOIN obs dx ON e.encounter_id = dx.encounter_id AND dx.voided = 0 AND dx.concept_id = :coded
         LEFT OUTER JOIN concept c ON c.concept_id = dx.value_coded
         LEFT OUTER JOIN concept_name cn ON cn.concept_id = c.concept_id AND cn.voided = 0 AND cn.locale = 'es'
-- 1a vez
         LEFT OUTER JOIN obs prev_diag ON prev_diag.person_id = dx.person_id AND
                                          prev_diag.value_coded = dx.value_coded AND
                                          prev_diag.date_created < dx.date_created
-- Codes
         LEFT OUTER JOIN concept_reference_map crm ON crm.concept_id = c.concept_id
         LEFT OUTER JOIN concept_reference_term crt
                         ON crm.concept_reference_term_id = crt.concept_reference_term_id AND
                            crt.retired = 0 AND
                            crt.concept_source_id = (SELECT concept_source_id FROM concept_reference_source
                                                     WHERE name LIKE "ICD-10-WHO")
-- Confirmed
         LEFT OUTER JOIN obs certainty
                         ON certainty.obs_group_id = dx.obs_group_id AND
                            certainty.concept_id = (
                                SELECT concept_id
                                FROM concept_reference_map
                                WHERE concept_reference_term_id = (
                                    SELECT concept_reference_term_id
                                    FROM concept_reference_term
                                    WHERE code LIKE "Diagnosis Certainty"
                                )
                            ) AND
                            certainty.voided = 0
         LEFT OUTER JOIN concept_name certainty_name
                         ON certainty_name.concept_id = certainty.value_coded AND
                            certainty_name.locale = "es" AND
                            certainty_name.locale_preferred = 1
WHERE p.voided = 0

-- Exclude test patients
  AND p.patient_id NOT IN (SELECT person_id
                           FROM person_attribute
                           WHERE value = 'true'
                             AND person_attribute_type_id = :testPt
                             AND voided = 0)
  AND date(e.encounter_datetime) >= :startDate
  AND date(e.encounter_datetime) <= :endDate
GROUP BY dx.obs_id
;
