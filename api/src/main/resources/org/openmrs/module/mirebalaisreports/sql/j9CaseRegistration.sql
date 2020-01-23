select p.patient_id,
       per.uuid,
       cna.given_name,
       cna.family_name,
       zl.identifier "ZL_identifier",
       pa.value      "telephone",
       per.birthdate,
       mg.value_text "J9_group",
       wsn.name      "J9_program",
       cna.department,
       cna.commune,
       cna.section_communal,
       cna.locality,
       cna.street_landmark
from patient p
         inner join person per on per.person_id = p.patient_id and per.dead = 0
         inner join program pr on pr.uuid = '41a2715e-8a14-11e8-9a94-a6cf71072f73' -- maternal child health program
         inner join patient_program pp
                    on pp.patient_id = p.patient_id and pp.program_id = pr.program_id and pp.date_completed is null and
                       pp.voided = 0
-- only include J9 patients in prenatal and pediatric groups (not maternal individual)
         inner join patient_state ps on pp.patient_program_id = ps.patient_program_id and ps.voided = 0 and ps.state in
                                                                                                            (select program_workflow_state_id
                                                                                                             from program_workflow_state pws1
                                                                                                             where pws1.uuid in
                                                                                                                   ('41a2753c-8a14-11e8-9a94-a6cf71072f73',
                                                                                                                    '2fa7008c-aa58-11e8-98d0-529269fb1459')) -- prenatal group and pediatric group
-- return J9 program name
         left outer join program_workflow_state pws ON pws.program_workflow_state_id = ps.state and pws.retired = 0
         left outer join concept_name wsn on concept_name_id =
                                             (select concept_name_id
                                              from concept_name cn1
                                              where cn1.concept_id = pws.concept_id
                                                and cn1.locale = 'en'
                                                and wsn.voided = 0
                                              order by cn1.locale_preferred desc
                                              limit 1)
-- name and address
         left outer join current_name_address cna on cna.person_id = p.patient_id
-- ZL identifier
         left outer join patient_identifier zl on zl.patient_identifier_id =
                                                  (select pid2.patient_identifier_id pid2
                                                   from patient_identifier pid2
                                                   where pid2.patient_id = p.patient_id
                                                     and pid2.voided = 0
                                                     and pid2.identifier_type =
                                                         (select patient_identifier_type_id
                                                          from patient_identifier_type pit
                                                          where pit.uuid = 'a541af1e-105c-40bf-b345-ba1fd6a59b85') -- ZL EMR ID
                                                   order by pid2.preferred desc
                                                   limit 1)
-- telephone number
         left outer join person_attribute pa on pa.person_id = p.patient_id and pa.person_attribute_type_id =
                                                                                (select person_attribute_type_id
                                                                                 from person_attribute_type pat
                                                                                 where pat.uuid = '14d4f066-15f5-102d-96e4-000c29c2a5d7') -- telephone number
    and pa.voided = 0
-- J9 group (latest observation of entered J9 group)
         left outer join obs mg on mg.obs_id =
                                   (select mg1.obs_id
                                    from obs mg1
                                    where mg1.person_id = p.patient_id
                                      and mg1.voided = 0
                                      and mg1.concept_id =
                                          (select concept_id
                                           from concept
                                           where uuid = 'c1b2db38-8f72-4290-b6ad-99826734e37e') -- Mothers Group concept
                                    order by mg1.obs_datetime desc
                                    limit 1)
where p.voided = 0
;
