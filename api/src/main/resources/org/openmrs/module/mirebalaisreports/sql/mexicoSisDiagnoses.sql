SELECT
    CONCAT(DATE(e.encounter_datetime), " ", TIME(e.encounter_datetime)) "Fecha",
    CONCAT(given_name, " ", family_name) "Nombre",
    ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) "Edad",
    pr.gender "Genero",
    IF(ins.value_coded = seguro_popular.concept_id, '1', '') "SPSS",
    '' "1a vez",  #TODO MEX-410
    MAX(cn.name) "Dx",
    MAX(crt.code) "ICD-10"
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
-- Most recent insurance
         LEFT OUTER JOIN obs ins
             ON ins.obs_id = (
                 SELECT obs_id FROM obs ins2
                    WHERE ins2.person_id = pr.person_id
                      AND ins2.voided = 0
                      AND ins2.concept_id = (
                          SELECT concept_id FROM concept_reference_map WHERE concept_reference_term_id = (
                              SELECT concept_reference_term_id FROM concept_reference_term WHERE code LIKE "mexico insurance coded"
                          )
                      )
                    ORDER BY ins2.date_created DESC
                    LIMIT 1)
-- Look up Seguro Popular concept
         LEFT OUTER JOIN concept seguro_popular ON seguro_popular.concept_id = (
            SELECT concept_id
            FROM concept_reference_map
            WHERE concept_reference_term_id = (
                SELECT concept_reference_term_id FROM concept_reference_term WHERE code LIKE "seguro popular"
            )
        )
-- Encounters
         INNER JOIN encounter e ON p.patient_id = e.patient_id AND e.voided = 0
-- Diagnoses
         LEFT OUTER JOIN obs dx ON e.encounter_id = dx.encounter_id AND dx.voided = 0 AND dx.concept_id = :coded
         LEFT OUTER JOIN concept c ON c.concept_id = dx.value_coded
         LEFT OUTER JOIN concept_name cn ON cn.concept_id = c.concept_id AND cn.voided = 0 AND cn.locale = 'es'
-- Codes
         LEFT OUTER JOIN concept_reference_map crm ON crm.concept_id = c.concept_id
         LEFT OUTER JOIN concept_reference_term crt
             ON crm.concept_reference_term_id = crt.concept_reference_term_id AND
                crt.retired = 0 AND
                crt.concept_source_id = (SELECT concept_source_id FROM concept_reference_source
                                         WHERE name LIKE "ICD-10-WHO")
-- Visit
        LEFT OUTER JOIN visit v ON v.visit_id = e.visit_id AND v.voided = 0
WHERE p.voided = 0
-- Exclude test patients
  AND p.patient_id NOT IN (SELECT person_id
                           FROM person_attribute
                           WHERE value = 'true'
                             AND person_attribute_type_id = :testPt
                             AND voided = 0)
  AND date(e.encounter_datetime) >= :startDate
  AND date(e.encounter_datetime) <= :endDate
GROUP BY v.visit_id, dx.value_coded
;
