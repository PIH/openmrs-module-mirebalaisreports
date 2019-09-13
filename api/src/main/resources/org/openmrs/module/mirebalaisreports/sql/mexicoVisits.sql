SELECT CONCAT(DATE(e.encounter_datetime), " ", TIME(e.encounter_datetime)) "date",
       e.visit_id       "visit id",
       given_name        "name",
       family_name       "last name",
       FLOOR(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25) "age",
       pr.gender            "gender",
       pa.city_village      "loc",
       MAX(wt.value_numeric)     "weight",
       MAX(ht.value_numeric)     "height",
       MAX(o2.value_numeric)     "o2",
       MAX(temp.value_numeric)   "temp",
       MAX(hr.value_numeric)     "hr",
       MAX(rr.value_numeric)     "rr",
       MAX(sbp.value_numeric)    "sbp",
       MAX(dbp.value_numeric)    "dbp",
       MAX(subj.value_text)      "subj",
       MAX(obj.value_text)      "obj",
       MAX(analysis.value_text)      "analysis",
       GROUP_CONCAT(DISTINCT dx_name.name SEPARATOR ', ') "dx",
       MAX(plan.value_text)      "plan",
       GROUP_CONCAT(DISTINCT
           drug.name, ' - ', med_instructions.value_text
           SEPARATOR '\n'
       )                          "meds"
FROM patient p
-- Person
         INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
-- Most recent name
         INNER JOIN (SELECT person_id, given_name, family_name
                     FROM person_name
                     WHERE voided = 0
                     ORDER BY date_created desc) n ON p.patient_id = n.person_id
-- Most recent address
         LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa
                         ON p.patient_id = pa.person_id
-- Encounters
         INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND
                                   e.encounter_type in (:vitEnc, :mexConsultEnc)
-- Observations
         LEFT OUTER JOIN obs wt ON e.encounter_id = wt.encounter_id AND wt.voided = 0 AND wt.concept_id = :wt
         LEFT OUTER JOIN obs ht ON e.encounter_id = ht.encounter_id AND ht.voided = 0 AND ht.concept_id = :ht
         LEFT OUTER JOIN obs o2 ON e.encounter_id = o2.encounter_id AND o2.voided = 0 AND o2.concept_id = :o2
         LEFT OUTER JOIN obs temp ON e.encounter_id = temp.encounter_id AND temp.voided = 0 AND temp.concept_id = :temp
         LEFT OUTER JOIN obs hr ON e.encounter_id = hr.encounter_id AND hr.voided = 0 AND hr.concept_id = :hr
         LEFT OUTER JOIN obs rr ON e.encounter_id = rr.encounter_id AND rr.voided = 0 AND rr.concept_id = :rr
         LEFT OUTER JOIN obs sbp ON e.encounter_id = sbp.encounter_id AND sbp.voided = 0 AND sbp.concept_id = :sbp
         LEFT OUTER JOIN obs dbp ON e.encounter_id = dbp.encounter_id AND dbp.voided = 0 AND dbp.concept_id = :dbp
         LEFT OUTER JOIN obs subj ON e.encounter_id = subj.encounter_id AND subj.voided = 0 AND subj.concept_id = :presentingHistory
         LEFT OUTER JOIN obs obj ON e.encounter_id = obj.encounter_id AND obj.voided = 0 AND obj.concept_id = :physicalExam
         LEFT OUTER JOIN obs analysis ON e.encounter_id = analysis.encounter_id AND analysis.voided = 0 AND analysis.concept_id = :clinicalImpressionComments
         LEFT OUTER JOIN obs dx ON e.encounter_id = dx.encounter_id AND dx.voided = 0 AND dx.concept_id = :coded
         LEFT OUTER JOIN obs plan ON e.encounter_id = plan.encounter_id AND plan.voided = 0 AND plan.concept_id = :clinicalManagementPlan
-- Diagnoses
         LEFT OUTER JOIN concept_name dx_name
             ON dx.value_coded = dx_name.concept_id
                    AND dx_name.locale = 'es'
                    AND dx_name.locale_preferred = 1
                    AND dx_name.voided = 0
-- Medications
         LEFT OUTER JOIN obs med_group
             ON e.encounter_id = med_group.encounter_id AND med_group.voided = 0
                    AND med_group.concept_id = :dispensingConstruct
         LEFT OUTER JOIN obs med_obs
             ON e.encounter_id = med_obs.encounter_id AND med_obs.voided = 0
                    AND med_obs.concept_id = :medName AND med_obs.obs_group_id = med_group.obs_id
         LEFT OUTER JOIN drug ON med_obs.value_drug = drug.drug_id
         LEFT OUTER JOIN obs med_instructions
             ON e.encounter_id = med_instructions.encounter_id AND med_instructions.voided = 0
                    AND med_instructions.concept_id = :medInstructions AND med_instructions.obs_group_id = med_group.obs_id
WHERE p.voided = 0
-- Exclude test patients
  AND p.patient_id NOT IN (SELECT person_id
                           FROM person_attribute
                           WHERE value = 'true'
                             AND person_attribute_type_id = :testPt
                             AND voided = 0)
  AND date(e.encounter_datetime) >= :startDate
  AND date(e.encounter_datetime) <= :endDate
GROUP BY e.visit_id
;
