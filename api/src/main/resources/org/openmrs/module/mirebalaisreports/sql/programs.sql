SELECT p.patient_id, zl.identifier zlemr, zl_loc.name loc_registered, un.value unknown_patient, pr.gender, ROUND(DATEDIFF(current_timestamp, pr.birthdate)/365.25, 1) age_at_enc, n.department, n.commune, n.section_communal, n.locality, n.street_landmark,
cn_prog.name "program_name", pp.date_enrolled, pp.date_completed,
cn_out.name "outcome",
CONCAT(cn_work.name,':',cn_state.name) "Current_State_1",
CONCAT(cn_work2.name,':',cn_state2.name) "Current_State_2",
CONCAT(cn_work3.name,':',cn_state3.name) "Current_State_3"
FROM patient p
-- Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = :zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
-- ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
-- Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = :unknownPt
            AND un.voided = 0
-- Gender
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
--  Most recent address
INNER JOIN current_name_address n on n.person_id = p.patient_id
-- program info
INNER JOIN patient_program pp on pp.patient_id = p.patient_id
INNER JOIN program prog ON prog.program_id = pp.program_id
INNER JOIN concept_name cn_prog on cn_prog.concept_id = prog.concept_id and cn_prog.locale = 'en' and cn_prog.locale_preferred = '1'  and cn_prog.voided = 0
LEFT OUTER JOIN concept_name cn_out on cn_out.concept_id = pp.outcome_concept_id and cn_out.locale = 'en' and cn_out.locale_preferred = '1'  and cn_out.voided = 0
-- Patient state 1
LEFT OUTER JOIN patient_state ps on ps.patient_state_id = (select patient_state_id from patient_state
     where patient_program_id = pp.patient_program_id and end_date is null order by patient_program_id limit 0,1)
LEFT OUTER JOIN program_workflow_state pws on pws.program_workflow_state_id = ps.state and pws.retired = 0
LEFT OUTER JOIN concept_name cn_state on cn_state.concept_id = pws.concept_id  and cn_state.locale = 'en' and cn_state.locale_preferred = '1'  and cn_state.voided = 0
LEFT OUTER JOIN program_workflow pw on pw.program_workflow_id = pws.program_workflow_id and pw.retired = 0
LEFT OUTER JOIN concept_name cn_work on cn_work.concept_id = pw.concept_id  and cn_work.locale = 'en' and cn_work.locale_preferred = '1'  and cn_work.voided = 0
-- Patient state 2
LEFT OUTER JOIN patient_state ps2 on ps2.patient_state_id = (select patient_state_id from patient_state
     where patient_program_id = pp.patient_program_id and end_date is null and voided = 0 order by patient_program_id limit 1,1)
LEFT OUTER JOIN program_workflow_state pws2 on pws2.program_workflow_state_id = ps2.state and pws2.retired = 0
LEFT OUTER JOIN concept_name cn_state2 on cn_state2.concept_id = pws2.concept_id  and cn_state2.locale = 'en' and cn_state2.locale_preferred = '1'  and cn_state2.voided = 0
LEFT OUTER JOIN program_workflow pw2 on pw2.program_workflow_id = pws2.program_workflow_id and pw2.retired = 0
LEFT OUTER JOIN concept_name cn_work2 on cn_work2.concept_id = pw2.concept_id  and cn_work2.locale = 'en' and cn_work2.locale_preferred = '1'  and cn_work2.voided = 0
-- Patient state 3
LEFT OUTER JOIN patient_state ps3 on ps3.patient_state_id = (select patient_state_id from patient_state
     where patient_program_id = pp.patient_program_id and end_date is null  and voided = 0 order by patient_program_id limit 2,1)
LEFT OUTER JOIN program_workflow_state pws3 on pws3.program_workflow_state_id = ps3.state and pws3.retired = 0
LEFT OUTER JOIN concept_name cn_state3 on cn_state3.concept_id = pws3.concept_id  and cn_state3.locale = 'en' and cn_state3.locale_preferred = '1'  and cn_state3.voided = 0
LEFT OUTER JOIN program_workflow pw3 on pw3.program_workflow_id = pws3.program_workflow_id and pw3.retired = 0
LEFT OUTER JOIN concept_name cn_work3 on cn_work3.concept_id = pw3.concept_id  and cn_work3.locale = 'en' and cn_work3.locale_preferred = '1'  and cn_work3.voided = 0
WHERE p.voided = 0
-- exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt
                         AND voided = 0)
;