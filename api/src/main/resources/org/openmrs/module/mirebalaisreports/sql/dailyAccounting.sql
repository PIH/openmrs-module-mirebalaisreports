select cn.name "Catégorie", CEIL(sum(o.value_numeric)) "Nbr" from
obs o
INNER JOIN obs os on os.encounter_id = o.encounter_id and os.voided = 0 and os.concept_id = 
    (select concept_id from report_mapping where source = 'PIH' and code = 'Type of HUM visit') 
LEFT OUTER JOIN concept_name cn on cn.concept_id = os.value_coded and cn.locale = 'fr' and cn.locale_preferred = '1'  and cn.voided = 0    
where 1=1
and o.voided = 0
and o.concept_id = (select concept_id from report_mapping where source = 'PIH' and code = 'Payment amount') 
AND date(o.obs_datetime) >=  :startDate 
AND date(o.obs_datetime) <=  :endDate 
group by cn.name
UNION ALL
  select 'Total pour cette période', CEIL(sum(o.value_numeric)) "Nbr" from
obs o
where 1=1
and o.voided = 0
and o.concept_id = (select concept_id from report_mapping where source = 'PIH' and code = 'Payment amount') 
AND date(o.obs_datetime) >=  :startDate 
AND date(o.obs_datetime) <=  :endDate 
UNION ALL
select ' ',' ' 
UNION ALL
select 'Nombre de patients exonérés', CEIL(count(o.person_id)) from
obs o
where 1=1
and o.voided = 0
and o.concept_id = (select concept_id from report_mapping where source = 'PIH' and code = 'Payment amount') 
and o.value_numeric = 0
AND date(o.obs_datetime) >=  :startDate 
AND date(o.obs_datetime) <=  :endDate 
UNION ALL
select ' ',' ' 
UNION ALL
select 'Nombre de patients ayant payés plus déune fois', CEIL(count(o.person_id)) from
obs o
where 1=1
and o.voided = 0
and o.concept_id = (select concept_id from report_mapping where source = 'PIH' and code = 'Payment amount') 
and o.value_numeric > 0
AND date(o.obs_datetime) >=  :startDate 
AND date(o.obs_datetime) <=  :endDate 
and exists  
  (select 1 from obs o2 
  where o2.obs_id <> o.obs_id
  and o2.concept_id = o.concept_id
  and o.value_numeric > 0
  and date(o.obs_datetime) = date(o2.obs_datetime)
  )
