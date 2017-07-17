select -- p.patient_id,
pid.identifier "ZL_EMR_ID", pp.date_enrolled "Enrolled_in_Program",
d.given_name, d.family_name, d.birthdate, birthdate_estimated, d.gender, d.country, d.department, d.commune, d.section_communal, d.locality, d.street_landmark,
Birth_Country,Birth_Department,Birth_Commune,Birth_Section_Communal,Birth_Locality,Birth_street_landmark,
Civil_Status,Occupation,Religion,
last_us.date "last_ultrasound_date",
zika_test.Collection_Date,
zika_test.Results_Date,
zika_test.Zika_Test_Result,
pid_child1.identifier "Child_1",
pid_child2.identifier "Child_2",
pid_child3.identifier "Child_3",
pid_parent1.identifier "Parent_1",
pid_parent2.identifier "Parent_2"
from patient p
-- only return patients in program
INNER JOIN patient_program pp on pp.patient_id = p.patient_id and pp.program_id in
      (select program_id from program where uuid = :zikaProgram) -- uuid of the ZIKA program
LEFT OUTER JOIN patient_identifier pid ON pid.patient_identifier_id = (select pid2.patient_identifier_id from patient_identifier pid2 where pid2.patient_id = p.patient_id and pid2.identifier_type = :zlId
                                                 order by pid2.preferred desc, pid2.date_created desc limit 1)
INNER JOIN current_name_address d on d.person_id = p.patient_id
LEFT OUTER JOIN
(
  select reg.patient_id,
   max(CASE when rm_b.source = 'PIH' and rm_b.code = 'Country' then birthplace.value_text end) 'Birth_Country',
   max(CASE when rm_b.source = 'PIH' and rm_b.code = 'Address1' then birthplace.value_text end) 'Birth_Commune',
   max(CASE when rm_b.source = 'PIH' and rm_b.code = 'State Province' then birthplace.value_text end) 'Birth_Department',
   max(CASE when rm_b.source = 'PIH' and rm_b.code = 'Address3' then birthplace.value_text end) 'Birth_Section_Communal',
   max(CASE when rm_b.source = 'PIH' and rm_b.code = 'City Village' then birthplace.value_text end) 'Birth_Locality',
   max(CASE when rm_b.source = 'PIH' and rm_b.code = 'Address2' then birthplace.value_text end) 'Birth_street_landmark'
  from encounter reg
     INNER JOIN obs obs_birth_group on obs_birth_group.encounter_id = reg.encounter_id
     INNER JOIN report_mapping rm_group on rm_group.concept_id = obs_birth_group.concept_id and rm_group.source = 'PIH' and rm_group.code = 'Birthplace address construct'
     INNER JOIN obs birthplace on birthplace.obs_group_id = obs_birth_group.obs_id
     INNER JOIN report_mapping rm_b on rm_b.concept_id = birthplace.concept_id
     where reg.encounter_type = :regEnc
  group by reg.patient_id
 ) bp on bp.patient_id = p.patient_id
 LEFT OUTER JOIN
 (
     select reg.patient_id,
    max(CASE when rm_demo.source = 'PIH' and rm_demo.code = 'CIVIL STATUS' then cn.name end) 'Civil_Status',
    max(CASE when rm_demo.source = 'PIH' and rm_demo.code = 'Occupation' then cn.name end) 'Occupation',
    max(CASE when rm_demo.source = 'PIH' and rm_demo.code = 'Religion' then cn.name end) 'Religion'
    from encounter reg
       INNER JOIN obs obs_demo on obs_demo.encounter_id = reg.encounter_id
       INNER JOIN report_mapping rm_demo on rm_demo.concept_id = obs_demo.concept_id
       LEFT OUTER JOIN concept_name cn on cn.concept_id = obs_demo.value_coded and cn.locale = 'en' and cn.locale_preferred = 1
       where reg.encounter_type = 1
    group by reg.patient_id
  ) demo on demo.patient_id = p.patient_id
 -- Last Ultrasound ordered
 LEFT OUTER JOIN
 (
     select e_rad.patient_id, max(o_us.obs_datetime) "date" from encounter e_rad
    INNER JOIN report_mapping rm_us on rm_us.source = 'PIH' and rm_us.code = 'HUM Ultrasound Orderables'
    INNER JOIN concept_set cs_us on cs_us.concept_set = rm_us.concept_id
    INNER JOIN report_mapping rm_radord on rm_radord.source = 'PIH' and rm_radord.code = 'Radiology procedure performed'
     INNER JOIN obs o_us on o_us.encounter_id = e_rad.encounter_id and o_us.concept_id = rm_radord.concept_id and o_us.value_coded = cs_us.concept_id
    where e_rad.encounter_type = :radStudyEnc
    group by e_rad.patient_id
 ) last_us on last_us.patient_id = p.patient_id
-- last collected Zika Test
  LEFT OUTER JOIN
 (
    select lab.patient_id,
    max(CASE when rm_lab.source = 'CIEL' and rm_lab.code = '164920' then cn.name end) 'Zika_Test_Result',
    max(CASE when rm_lab.source = 'PIH' and rm_lab.code = 'SPUTUM COLLECTION DATE' then obs_lab.value_datetime end) 'Collection_Date',
    max(CASE when rm_lab.source = 'PIH' and rm_lab.code = 'DATE OF LABORATORY TEST' then obs_lab.value_datetime end) 'Results_Date'
    from encounter lab
       INNER JOIN obs obs_lab on obs_lab.encounter_id = lab.encounter_id
       INNER JOIN report_mapping rm_lab on rm_lab.concept_id = obs_lab.concept_id
       LEFT OUTER JOIN concept_name cn on cn.concept_id = obs_lab.value_coded and cn.locale = 'en' and cn.locale_preferred = 1
       where lab.encounter_type= :labResultEnc
    and lab.encounter_id =
      (select obs_maxdate.encounter_id from obs obs_maxdate
      where obs_maxdate.concept_id = (select concept_id from report_mapping rm_max where rm_max.source = 'PIH' and rm_max.code = 'SPUTUM COLLECTION DATE')
      order by value_datetime desc limit 1)
    group by lab.patient_id
 ) zika_test on zika_test.patient_id = p.patient_id
  -- Child #1
 LEFT OUTER JOIN relationship_type rt_parent on rt_parent.a_is_to_b = 'Parent'
 LEFT OUTER JOIN relationship r_child1 on r_child1.relationship_id =
        (select relationship_id from relationship r_child1a
        where r_child1a.person_a = p.patient_id
        and r_child1a.voided = 0
        and r_child1a.relationship = rt_parent.relationship_type_id
        order by r_child1a.date_created desc limit 1)
 LEFT OUTER JOIN patient_identifier pid_child1 ON pid_child1.patient_identifier_id = (select pid2.patient_identifier_id from patient_identifier pid2 where pid2.patient_id = r_child1.person_b and pid2.identifier_type = :zlId
                                                order by pid2.preferred desc, pid2.date_created desc limit 1)
-- Child #2
 LEFT OUTER JOIN relationship r_child2 on r_child2.relationship_id =
        (select relationship_id from relationship r_child2a
        where r_child2a.person_a = p.patient_id
        and r_child2a.voided = 0
        and r_child2a.relationship = rt_parent.relationship_type_id
        and r_child2a.person_b <> r_child1.person_b
        order by r_child2a.date_created desc limit 1)
 LEFT OUTER JOIN patient_identifier pid_child2 ON pid_child2.patient_identifier_id = (select pid2.patient_identifier_id from patient_identifier pid2 where pid2.patient_id = r_child2.person_b and pid2.identifier_type = :zlId
                                                order by pid2.preferred desc, pid2.date_created desc limit 1)
  -- Child #3
LEFT OUTER JOIN relationship r_child3 on r_child3.relationship_id =
        (select relationship_id from relationship r_child3a
        where r_child3a.person_a = p.patient_id
        and r_child3a.voided = 0
        and r_child3a.relationship = rt_parent.relationship_type_id
        and r_child3a.person_b not in (r_child1.person_b,r_child2.person_b)
        order by r_child3a.date_created desc limit 1)
 LEFT OUTER JOIN patient_identifier pid_child3 ON pid_child3.patient_identifier_id = (select pid2.patient_identifier_id from patient_identifier pid2 where pid2.patient_id = r_child3.person_a and pid2.identifier_type = :zlId
                                                order by pid2.preferred desc, pid2.date_created desc limit 1)
-- parent #1
 LEFT OUTER JOIN relationship r_parent1 on r_parent1.relationship_id =
        (select relationship_id from relationship r_parent1a
        where r_parent1a.person_b = p.patient_id
        and r_parent1a.voided = 0
        and r_parent1a.relationship = rt_parent.relationship_type_id
        order by r_parent1a.date_created desc limit 1)
 LEFT OUTER JOIN patient_identifier pid_parent1 ON pid_parent1.patient_identifier_id = (select pid2.patient_identifier_id from patient_identifier pid2 where pid2.patient_id = r_parent1.person_a and pid2.identifier_type = :zlId
                                                order by pid2.preferred desc, pid2.date_created desc limit 1)
 -- parent #2
 LEFT OUTER JOIN relationship r_parent2 on r_parent2.relationship_id =
        (select relationship_id from relationship r_parent2a
        where r_parent2a.person_b = p.patient_id
        and r_parent2a.voided = 0
        and r_parent2a.relationship = rt_parent.relationship_type_id
        and r_parent2a.person_a <> r_parent1.person_a
        order by r_parent2a.date_created desc limit 1)
 LEFT OUTER JOIN patient_identifier pid_parent2 ON pid_parent2.patient_identifier_id = (select pid2.patient_identifier_id from patient_identifier pid2 where pid2.patient_id = r_parent2.person_a and pid2.identifier_type = :zlId
                                                order by pid2.preferred desc, pid2.date_created desc limit 1)
 order by ZL_EMR_ID;
