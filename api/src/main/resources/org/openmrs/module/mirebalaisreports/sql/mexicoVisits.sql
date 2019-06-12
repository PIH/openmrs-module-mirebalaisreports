SELECT CONCAT(DATE(e.encounter_datetime), " ", TIME(e.encounter_datetime)) "date",
       e.visit_id       "visit id",
       given_name        "name",
       family_name       "last name",
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
       MAX(note.value_text)      "note",
       MAX(dx.value_text)        "dx",
       MAX(plan.value_text)      "plan"
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
         LEFT OUTER JOIN obs note ON e.encounter_id = note.encounter_id AND note.voided = 0 AND note.concept_id = :presentingHistory
         LEFT OUTER JOIN obs dx ON e.encounter_id = dx.encounter_id AND dx.voided = 0 AND dx.concept_id = :coded
         LEFT OUTER JOIN obs plan ON e.encounter_id = plan.encounter_id AND plan.voided = 0 AND plan.concept_id = :clinicalManagementPlan
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
