select o.patient_id, zl.identifier Patient_ZL_ID, zl_loc.name loc_registered,
       un.value unknown_patient, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc,
       pa.state_province department, pa.city_village commune, pa.address3 section, pa.address1 locality, pa.address2 street_landmark,
       o.order_number 'order_number',
       ocn.name 'orderable',
       CASE
       WHEN o.date_stopped is not null and sc.obs_id is null THEN 'Cancelled'
       WHEN o.auto_expire_date < CURDATE() and sc.obs_id is null THEN 'Expired'
       WHEN rep.obs_id is not null THEN 'Reported'
       WHEN sc.obs_id is not null and rep.obs_id is null THEN 'Collected'
       ELSE 'Ordered'
       END 'status',
       CONCAT(pn.given_name, ' ',pn.family_name) 'orderer',
       o.date_activated 'order_datetime',
       ol.name 'ordering_location',
       o.urgency,
       date(sce.encounter_datetime) 'specimen_collection_datetime',
       MAX(CASE WHEN rmq.source = 'PIH' and rmq.code = 11781 THEN 'yes/oui' END) 'collection_date_estimated',
       MAX(CASE WHEN rmq.source = 'PIH' and rmq.code = 11791 THEN  cna.name END) 'test_location',
       MAX(CASE WHEN rmq.source = 'PIH' and rmq.code = 'Date of test results' THEN  res.value_datetime END) 'result_date'
from orders o
  INNER JOIN encounter e ON e.encounter_id = o.encounter_id
  LEFT OUTER JOIN patient_identifier zl on zl.patient_identifier_id =
                                           (select pid2.patient_identifier_id pid2 from patient_identifier pid2 where pid2.patient_id = e.patient_id and pid2.voided = 0 and pid2.identifier_type = :zlId
                                            order by pid2.preferred desc limit 1)
  -- ZL EMR ID location
  INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
  -- Unknown patient
  LEFT OUTER JOIN person_attribute un ON e.patient_id = un.person_id AND un.person_attribute_type_id = :unknownPt
                                         AND un.voided = 0
  -- Gender
  INNER JOIN person pr ON e.patient_id = pr.person_id AND pr.voided = 0
  -- Address
  LEFT OUTER JOIN person_address pa ON pa.person_address_id = (select person_address_id from person_address a2 where a2.person_id =  e.patient_id and a2.voided = 0
                                                               order by a2.preferred desc, a2.date_created desc limit 1)
  -- Orderable
  LEFT OUTER JOIN concept_name ocn on ocn.concept_name_id = (select concept_name_id from concept_name ocn2 where ocn2.concept_id = o.concept_id and ocn2.locale in ('fr','en','ht') order by field(ocn2.locale,'fr','en','ht'), ocn2.locale_preferred desc
                                                             limit 1)
  -- Orderer
  INNER JOIN provider pv ON pv.provider_id = o.orderer
  INNER JOIN person_name pn ON pn.person_id = pv.person_id and pn.voided = 0
  -- Ordering Location
  INNER JOIN  location ol on ol.location_id = e.location_id
-- Specimen collection encounter
  LEFT OUTER JOIN obs sc on sc.value_text = o.order_number and sc.voided = 0
  LEFT OUTER JOIN encounter sce on sce.encounter_id = sc.encounter_id and sce.voided = 0
  -- The following brings back any obs in the specimen collection encounter that are NOT in the list of concepts below.  This is used to determine status.
  -- If there are any obs returned here, the order is 'reported'
  LEFT OUTER JOIN obs rep on rep.obs_id =
                             (select obs_id from obs o2
                             where o2.encounter_id = sc.encounter_id
                                   and o2.voided =0
                                   and o2.concept_id not in
                                       (select concept_id from concept where UUID in
                                                                             ( '393dec41-2fb5-428f-acfa-36ea85da6666',  -- test order number
                                                                               '68d6bd27-37ff-4d7a-87a0-f5e0f9c8dcc0', -- date of test results
                                                                               '5dc35a2a-228c-41d0-ae19-5b1e23618eda', -- reason lab not performed
                                                                               'e9732df4-971d-4a9a-9129-e2e610552468', -- location of lab
                                                                               '87f506e3-4433-40ec-b16c-b3c65e402989', -- collection date estimated
                                                                               '7e0cf626-dbe8-42aa-9b25-483b51350bf8') -- test status
                                       )
                              limit 1
                             )
  -- The following brings back all obs of the specimen collection encounter.
  -- Using aggregate functions,collection_date_estimated, test_location, result_date are parsed out above
  LEFT OUTER JOIN obs res on res.encounter_id = sc.encounter_id and res.voided = 0
  LEFT OUTER JOIN report_mapping rmq on rmq.concept_id = res.concept_id
  LEFT OUTER JOIN concept_name cna on cna.concept_name_id = (select concept_name_id from concept_name cn where cn.concept_id = res.value_coded and cn.voided = 0 and cn.locale in ('fr','en','ht') order by field(cn.locale,'fr','en','ht'), cn.locale_preferred desc limit 1)
where o.order_type_id =
      (select ot.order_type_id from order_type ot where ot.uuid = '52a447d3-a64a-11e3-9aeb-50e549534c5e') -- Test Order
      and order_action = 'NEW'
      and date(o.date_activated) >= date(:startDate)
      and date(o.date_activated) <= date(:endDate)
group by o.order_number
order by o.date_activated asc ;
