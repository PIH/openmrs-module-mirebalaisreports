## set @startDate='2019-01-01';
## set @endDate='2019-06-30';

SELECT patient_identifier_type_id into @zlId from patient_identifier_type where uuid='a541af1e-105c-40bf-b345-ba1fd6a59b85';
SELECT person_attribute_type_id into @unknownPt FROM person_attribute_type where uuid='8b56eac7-5c76-4b9c-8c6f-1deab8d3fc47';
SELECT encounter_type_id into @labResultEnc from encounter_type where uuid= '4d77916a-0620-11e5-a6c0-1697f925ec7b';
  
drop temporary table if exists temp_laborders_spec;
create temporary table temp_laborders_spec
(
  order_number    varchar(50) Primary key,
  concept_id          int(11),
  encounter_id        int(11),
  encounter_datetime  datetime,
  patient_id          int(11)

);

-- This first query writes one row per order of the correct order type and within the date range that has a specimen collection encounter
-- All data from the order and specimen collection encounter that's needed is written here 
insert into temp_laborders_spec (order_number,concept_id,encounter_id,encounter_datetime,patient_id)
  select o.order_number,
    o.concept_id,
    sco.encounter_id,
    e.encounter_datetime,
    e.patient_id
  from orders o
    INNER JOIN obs sco on sco.value_text = o.order_number and sco.voided = 0
    INNER JOIN encounter e on e.encounter_id = sco.encounter_id and e.voided = 0
  where o.order_type_id =
        (select ot.order_type_id from order_type ot where ot.uuid = '52a447d3-a64a-11e3-9aeb-50e549534c5e') -- Test Order
        and order_action = 'NEW'
        and date(e.encounter_datetime) >= date(@startDate)
        and date(e.encounter_datetime) <= date(@endDate)
  group by o.order_number;

SELECT t.patient_id,
       zl.identifier as 'Patient_ZL_ID',
       zl_loc.name as 'loc_registered',
       un.value as 'unknown_patient',
       pr.gender,
       ROUND(DATEDIFF(t.encounter_datetime, pr.birthdate)/365.25, 1) as 'age_at_enc',
       pa.state_province as 'department',
       pa.city_village as 'commune',
       pa.address3 as 'section',
       pa.address1 as 'locality',
       pa.address2 as 'street_landmark',
       t.order_number,
       ocn.name as 'orderable',
       cnq.name as 'test',
       t.encounter_datetime as 'specimen_collection_date',
       res_date.value_datetime as 'results_date',
       CASE when res.value_numeric is not null then res.value_numeric
       when res.value_text is not null then res.value_text
       when cna.name is not null then cna.name
       END as 'result',
       cu.units,
       t.encounter_id
from temp_laborders_spec t
  -- ZL EMR ID
  LEFT OUTER JOIN patient_identifier zl on zl.patient_identifier_id =
                                           (select pid2.patient_identifier_id pid2 from patient_identifier pid2 where pid2.patient_id = t.patient_id and pid2.voided = 0 and pid2.identifier_type = @zlId
                                            order by pid2.preferred desc limit 1)
  -- ZL EMR ID location
  INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
  -- Unknown patient
  LEFT OUTER JOIN person_attribute un ON t.patient_id = un.person_id AND un.person_attribute_type_id = @unknownPt
                                         AND un.voided = 0
  -- Gender
  INNER JOIN person pr ON t.patient_id = pr.person_id AND pr.voided = 0
  -- Address
  LEFT OUTER JOIN person_address pa ON pa.person_address_id = (select person_address_id from person_address a2 where a2.person_id =  t.patient_id and a2.voided = 0
                                                               order by a2.preferred desc, a2.date_created desc limit 1)
  -- Orderable
  LEFT OUTER JOIN concept_name ocn on ocn.concept_name_id = (select concept_name_id from concept_name ocn2 where ocn2.concept_id = t.concept_id and ocn2.locale in ('fr','en','ht') order by field(ocn2.locale,'fr','en','ht'), ocn2.locale_preferred desc
                                                             limit 1)
  -- bring in actual results obs below. Note that we're excluding obs that are not lab results
  INNER JOIN obs res on res.encounter_id = t.encounter_id
                        and res.voided = 0
                        and res.concept_id not in
                            (select concept_id from concept where UUID in
                                                                  ('393dec41-2fb5-428f-acfa-36ea85da6666',  -- test order number
                                                                   '68d6bd27-37ff-4d7a-87a0-f5e0f9c8dcc0', -- date of test results
                                                                   '5dc35a2a-228c-41d0-ae19-5b1e23618eda', -- reason lab not performed
                                                                   'e9732df4-971d-4a9a-9129-e2e610552468', -- test location
                                                                   '7e0cf626-dbe8-42aa-9b25-483b51350bf8', -- test staus
                                                                   '87f506e3-4433-40ec-b16c-b3c65e402989') -- estimated collection date
                            )
                        and (res.value_numeric is not null or res.value_text is not null or res.value_coded is not null)
  LEFT OUTER JOIN concept_name cnq on cnq.concept_name_id = (select concept_name_id from concept_name cn where cn.concept_id = res.concept_id and cn.voided = 0 and cn.locale in ('fr','en','ht') order by field(cn.locale,'fr','en','ht'), cn.locale_preferred desc limit 1)
  LEFT OUTER JOIN concept_name cna on cna.concept_name_id = (select concept_name_id from concept_name cn where cn.concept_id = res.value_coded and cn.voided = 0 and cn.locale in ('fr','en','ht') order by field(cn.locale,'fr','en','ht'), cn.locale_preferred desc limit 1)
  -- units
  LEFT OUTER JOIN concept_numeric cu on cu.concept_id = res.concept_id
  -- results date
  LEFT OUTER JOIN obs res_date on res_date.voided = 0 and res_date.encounter_id = t.encounter_id and res_date.concept_id =    
      (select concept_id from concept where UUID = '68d6bd27-37ff-4d7a-87a0-f5e0f9c8dcc0')  
-- the following will filter out the rare case that an empty obs was left in the specimen collection encounter
WHERE (res.value_numeric is not null or res.value_text is not null or cna.name is not null)
UNION
-- the following select statement brings in the standalone lab results entered in on the lab results encounter
SELECT e.patient_id,
       zl.identifier as 'Patient_ZL_ID',
       zl_loc.name as 'loc_registered',
       un.value as 'unknown_patient',
       pr.gender,
       ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) as 'age_at_enc',
       pa.state_province as 'department',
       pa.city_village as 'commune',
       pa.address3 as 'section',
       pa.address1 as 'locality',
       pa.address2 as 'street_landmark',
       null 'order_number',
       null 'orderable',
       cnq.name as 'test',
       e.encounter_datetime as 'specimen_collection_date',
       res_date.value_datetime as 'results_date',
       CASE when res.value_numeric is not null then res.value_numeric
       when res.value_text is not null then res.value_text
       when cna.name is not null then cna.name
       END as 'result',
       cu.units,
       e.encounter_id
from encounter e
  -- ZL EMR ID
  LEFT OUTER JOIN patient_identifier zl on zl.patient_identifier_id =
                                           (select pid2.patient_identifier_id pid2 from patient_identifier pid2 where pid2.patient_id = e.patient_id and pid2.voided = 0 and pid2.identifier_type = @zlId
                                            order by pid2.preferred desc limit 1)
  -- ZL EMR ID location
  LEFT OUTER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
  -- Unknown patient
  LEFT OUTER JOIN person_attribute un ON e.patient_id = un.person_id AND un.person_attribute_type_id = @unknownPt
                                         AND un.voided = 0
  -- Gender
  INNER JOIN person pr ON e.patient_id = pr.person_id AND pr.voided = 0
  -- Address
  LEFT OUTER JOIN person_address pa ON pa.person_address_id = (select person_address_id from person_address a2 where a2.person_id =  e.patient_id and a2.voided = 0
                                                               order by a2.preferred desc, a2.date_created desc limit 1)
   -- bring in actual results obs below. Note that we're excluding obs that are not lab results
  INNER JOIN obs res on res.encounter_id = e.encounter_id
                        and res.voided = 0
                        and res.concept_id not in
                            (select concept_id from concept where UUID in
                                                                  ('68d6bd27-37ff-4d7a-87a0-f5e0f9c8dcc0', -- date of test results
                                                                   'e9732df4-971d-4a9a-9129-e2e610552468', -- test location
                                                                   '87f506e3-4433-40ec-b16c-b3c65e402989') -- estimated collection date
                            )
                        and (res.value_numeric is not null or res.value_text is not null or res.value_coded is not null)
  LEFT OUTER JOIN concept_name cnq on cnq.concept_name_id = (select concept_name_id from concept_name cn where cn.concept_id = res.concept_id and cn.voided = 0 and cn.locale in ('fr','en','ht') order by field(cn.locale,'fr','en','ht'), cn.locale_preferred desc limit 1)
  LEFT OUTER JOIN concept_name cna on cna.concept_name_id = (select concept_name_id from concept_name cn where cn.concept_id = res.value_coded and cn.voided = 0 and cn.locale in ('fr','en','ht') order by field(cn.locale,'fr','en','ht'), cn.locale_preferred desc limit 1)
  -- units
  LEFT OUTER JOIN concept_numeric cu on cu.concept_id = res.concept_id
  -- result date
  LEFT OUTER JOIN obs res_date on res_date.voided = 0 and res_date.encounter_id = e.encounter_id and res_date.concept_id =    
      (select concept_id from concept where UUID = '68d6bd27-37ff-4d7a-87a0-f5e0f9c8dcc0')
WHERE e.voided = 0
and e.encounter_type = @labResultEnc
and date(e.encounter_datetime) >= date(@startDate)
and date(e.encounter_datetime) <= date(@endDate)
;
