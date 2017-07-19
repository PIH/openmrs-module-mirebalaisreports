select sort_order, maladies, newold "nouveaux_anciens",
nullif(sum(L10),0) "<10ans",
nullif(sum(L15),0) "10-14ans",
nullif(sum(L25),0) "15-24ans",
nullif(sum(L50),0) "25-49ans",
nullif(sum(G50),0) "50ans_et+",
nullif((sum(L10) + sum(L15) + sum(L25) + sum(L50) + sum(G50)),0) "Total_des_cas_référés"
from
(select 1 as sort_order,'Diabète' as maladies, 'nouveaux' as newold
UNION ALL
select 2 as sort_order,'Diabète' as maladies, 'anciens' as newold
UNION ALL
select 3 as sort_order,'HTA' as maladies, 'nouveaux' as newold
UNION ALL
select 4 as sort_order,'HTA' as maladies, 'anciens' as newold
UNION ALL
select 5 as sort_order,'Cancer du col de l''utérus' as maladies, 'nouveaux' as newold
UNION ALL
select 6 as sort_order,'Cancer du col de l''utérus' as maladies, 'anciens' as newold
UNION ALL
select 7 as sort_order,'cancer du sein' as maladies, 'nouveaux' as newold
UNION ALL
select 8 as sort_order,'cancer du sein' as maladies, 'anciens' as newold
UNION ALL
select 9 as sort_order,'cancer de la prostate' as maladies, 'nouveaux' as newold
UNION ALL
select 10 as sort_order,'cancer de la prostate' as maladies, 'anciens' as newold
UNION ALL
select 11 as sort_order,'Obésité','nouveaux'
UNION ALL
select 12 as sort_order,'Obésité','anciens'
UNION ALL
select 13 as sort_order,'Glaucome','nouveaux'
UNION ALL
select 14 as sort_order,'Glaucome','anciens'
UNION ALL
select 15 as sort_order,'Cataracte','nouveaux'
UNION ALL
select 16 as sort_order,'Cataracte','anciens'
UNION ALL
select 17 as sort_order,'Insuffisance rénale','nouveaux'
UNION ALL
select 18 as sort_order,'Insuffisance rénale','anciens'
) dx
LEFT OUTER JOIN
(
select
(CASE
  when ((rm.source = 'CIEL' and rm.code = '162491')
    or (rm.source = 'CIEL' and rm.code = '119441')
    or (rm.source = 'PIH' and rm.code = 'Type 1 diabetes')
    or (rm.source = 'PIH' and rm.code = 'Type 2 diabetes')
    or (rm.source = 'PIH' and rm.code = 'Gestational diabetes')
    or (rm.source = 'PIH' and rm.code = 'DIABETES INSIPIDUS')
    or (rm.source = 'PIH' and rm.code = 'Pre-gestational diabetes')
    or (rm.source = 'PIH' and rm.code = 'DIABETES')
    or (rm.source = 'CIEL' and rm.code = '137941')
    or (rm.source = 'CIEL' and rm.code = '119457')
    or (rm.source = 'PIH' and rm.code = 'DIABETES MELLITUS')) then 'Diabète'
  when ((rm.source = 'CIEL' and rm.code = '133206')
    or (rm.source = 'CIEL' and rm.code = '116023')
    or (rm.source = 'PIH' and rm.code = '146299')
    or (rm.source = 'PIH' and rm.code = '116036')
    or (rm.source = 'CIEL' and rm.code = '145807')) then 'Cancer du col de l''utérus'
  when rm.source = 'PIH' and rm.code = 'Malignant Neoplasm of Breast' then 'Cancer du sein'
  when rm.source = 'CIEL' and rm.code = '134788' then 'cancer de la prostate'
  when rm.source = 'PIH' and rm.code = 'Obesity' then 'Obésité'
  when rm.source = 'PIH' and rm.code = 'Glaucoma' then 'Glaucome'
  when ((rm.source = 'PIH' and rm.code = 'Senile Cataract')
     or (rm.source = 'PIH' and rm.code = 'CATARACT')) then 'Cataracte'
  when ((rm.source = 'PIH' and rm.code = 'RENAL FAILURE')
    or (rm.source = 'CIEL' and rm.code = '120581')
    or (rm.source = 'PIH' and rm.code = 'RENAL FAILURE, ACUTE')
    or (rm.source = 'PIH' and rm.code = 'RENAL FAILURE, CHRONIC')) then 'Insuffisance rénale'
 end) 'Diagnosis',
(CASE when IFNULL(o_prev.obs_id, 1) <> 1 then 'anciens' else 'nouveaux' end) "recurrent",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) < 10 then 1 else 0 end) "L10",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) >= 10 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <15 then 1 else 0 end) "L15",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) >= 15 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <25 then 1 else 0 end) "L25",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) >= 25 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <50 then 1 else 0 end) "L50",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 50 then 1 else 0 end) "G50"
from obs o
INNER JOIN encounter e on e.encounter_id = o.encounter_id
INNER JOIN person pr on pr.person_id = o.person_id
INNER JOIN report_mapping rm on o.value_coded = rm.concept_id
LEFT OUTER JOIN obs o_prev on o_prev.person_id = o.person_id and o_prev.value_coded = o.value_coded and o_prev.obs_id < o.obs_id
where 1=1
and (o.concept_id = (select concept_id from report_mapping where source = 'PIH' and code = 'DIAGNOSIS') or
   (e.encounter_type in (select et.encounter_type_id from encounter_type et where et.name = 'Signes vitaux')))
and o.voided = 0
AND date(o.obs_datetime) >= :startDate
AND date(o.obs_datetime) <= :endDate
group by o.obs_id, rm.source, rm.code
union all
select
(CASE when o_syst.value_numeric >= 140 and o_diast.value_numeric >= 90 then 'HTA' end) "Diagnosis",
(CASE when IFNULL(e_prev.encounter_id, 1) <> 1 then 'anciens' else 'nouveaux' end) "recurrent",
(CASE when round(DATEDIFF(e_vitals.encounter_datetime, pr.birthdate)/365.25, 1) < 10 then 1 else 0 end) "L10",
(CASE when round(DATEDIFF(e_vitals.encounter_datetime, pr.birthdate)/365.25, 1) >= 10 and round(DATEDIFF(e_vitals.encounter_datetime, pr.birthdate)/365.25, 1) <15 then 1 else 0 end) "L15",
(CASE when round(DATEDIFF(e_vitals.encounter_datetime, pr.birthdate)/365.25, 1) >= 15 and round(DATEDIFF(e_vitals.encounter_datetime, pr.birthdate)/365.25, 1) <25 then 1 else 0 end) "L25",
(CASE when round(DATEDIFF(e_vitals.encounter_datetime, pr.birthdate)/365.25, 1) >= 25 and round(DATEDIFF(e_vitals.encounter_datetime, pr.birthdate)/365.25, 1) <50 then 1 else 0 end) "L50",
(CASE when round(DATEDIFF(e_vitals.encounter_datetime, pr.birthdate)/365.25, 1) > 50 then 1 else 0 end) "G50"
from encounter e_vitals
INNER JOIN person pr on pr.person_id = e_vitals.patient_id
INNER JOIN report_mapping rm_syst on rm_syst.code = 'SYSTOLIC BLOOD PRESSURE' and rm_syst.source = 'PIH'
INNER JOIN report_mapping rm_diast on rm_diast.code = 'DIASTOLIC BLOOD PRESSURE' and rm_diast.source = 'PIH'
INNER JOIN obs o_syst on o_syst.concept_id = rm_syst.concept_id and o_syst.encounter_id = e_vitals.encounter_id
INNER JOIN obs o_diast on o_diast.concept_id = rm_diast.concept_id and o_diast.encounter_id = e_vitals.encounter_id
LEFT OUTER JOIN
  (select vitals_prev.encounter_id,vitals_prev.patient_id  from encounter vitals_prev
  INNER JOIN report_mapping rm_syst on rm_syst.code = 'SYSTOLIC BLOOD PRESSURE' and rm_syst.source = 'PIH'
  INNER JOIN report_mapping rm_diast on rm_diast.code = 'DIASTOLIC BLOOD PRESSURE' and rm_diast.source = 'PIH'
  INNER JOIN obs o_syst on o_syst.concept_id = rm_syst.concept_id and o_syst.encounter_id = vitals_prev.encounter_id and o_syst.value_numeric >= 140
  INNER JOIN obs o_diast on o_diast.concept_id = rm_diast.concept_id and o_diast.encounter_id = vitals_prev.encounter_id and o_syst.value_numeric >= 90) e_prev
  on e_prev.patient_id = e_vitals.patient_id
  and e_prev.encounter_id < e_vitals.encounter_id
where e_vitals.voided = 0
AND date(e_vitals.encounter_datetime) >= :startDate
AND date(e_vitals.encounter_datetime) <= :endDate
and e_vitals.encounter_type in (select et.encounter_type_id from encounter_type et where et.name = 'Signes vitaux')
order by Diagnosis desc
) tab on dx.maladies = tab.Diagnosis and dx.newold = tab.recurrent
group by maladies, newold
order by sort_order
;
