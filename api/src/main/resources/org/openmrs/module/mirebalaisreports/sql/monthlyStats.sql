select dx.morbides "Phenomenes_mordbides_ou_non_morbides",
HL1 "H<1",
FL1 "F<1",
HL4 "H_1-4",
FL4 "F_1-4",
HL9 "H_5-9",
FL9 "F_5-9",
HL14 "H_10-14",
FL14 "F_10-14",
HL24 "H_15-24",
FL24 "F_15-24",
HL49 "H_25-49",
FL49 "F_25-49",
HG50 "H>50",
FG50 "F>50",
HTotal "H_Total",
FTotal "F_Total"
from
(select 'Agression par animal suspect de rage' as morbides
UNION ALL
select 'Charbon Cutane'
UNION ALL
select 'Chikungunya'
UNION ALL
select 'Cholera'
UNION ALL
select 'Coqueluche'
UNION ALL
select 'Dengue'
UNION ALL
select 'Diarrhee Aqueuse'
UNION ALL
select 'Diarrhee Sanglante'
UNION ALL
select 'Diphtheria'
UNION ALL
select 'Epilepsie'
UNION ALL
select 'ESAVI'
UNION ALL
select 'Fievre d''origine indeterminee'
UNION ALL
select 'Fievre Hemorragique'
UNION ALL
select 'Filariose lymphatique'
UNION ALL
select 'Infection Respiratoire Aigue'
UNION ALL
select 'IST'
UNION ALL
select 'Toxi-Infection Alimentaire Collective (TIAC)'
UNION ALL
select 'Lčpre'
UNION ALL
select 'Leptospirose'
UNION ALL
select 'malaria confirmee traitee'
UNION ALL
select 'Malnutrition'
UNION ALL
select 'Meningite bacteriennes'
UNION ALL
select 'Oreillons'
UNION ALL
select 'Paralysie Flasque Aigue'
UNION ALL
select 'Parasitose intestinale'
UNION ALL
select 'Phenomene Anormal'
UNION ALL
select 'Rage Humaine'
UNION ALL
select 'Rougeole/Rubéole'
UNION ALL
select 'Rubeole Congenital'
UNION ALL
select 'Syndrome Icterique Febrile'
UNION ALL
select 'Syndrome respiratoire aigu sévčre (SRAS)'
UNION ALL
select 'Syphilis'
UNION ALL
select 'Syphilis congenitales'
UNION ALL
select 'Tetanos'
UNION ALL
select 'Tetanos Neonatal'
UNION ALL
select 'Tuberculose Pulmonaire bacteriologiquement confirmee'
UNION ALL
select 'Tuberculose Pulmonaire bacteriologiquement non confirmee'
UNION ALL
select 'Tuberculose suspecte'
UNION ALL
select 'Typhoide'
UNION ALL
select 'VIH+'
UNION ALL
select 'Xerophthalmie'
UNION ALL
select 'Autres maladies') dx
LEFT OUTER JOIN
(
select
(CASE when diagnosis = 'Tuberculose Pulmonaire bacteriologiquement' and certainty = 'Confirmed' then 'Tuberculose Pulmonaire bacteriologiquement confirmee'
      when diagnosis = 'Tuberculose Pulmonaire bacteriologiquement' and certainty = 'Presumed' then 'Tuberculose Pulmonaire bacteriologiquement non confirmee'
 else diagnosis
 end) "Morbides",    
nullif(sum(ML1),0) "HL1",
nullif(sum(FL1),0) "FL1",
nullif(sum(ML4),0) "HL4",
nullif(sum(FL4),0) "FL4",
nullif(sum(ML9),0) "HL9",
nullif(sum(FL9),0) "FL9",
nullif(sum(ML14),0) "HL14",
nullif(sum(FL14),0) "FL14",
nullif(sum(ML24),0) "HL24",
nullif(sum(FL24),0) "FL24",
nullif(sum(ML49),0) "HL49",
nullif(sum(FL49),0) "FL49",
nullif(sum(MG50),0) "HG50",
nullif(sum(FG50),0) "FG50",
nullif(sum(ML1)+sum(ML4)+sum(ML9)+ sum(ML14)+sum(ML24)+sum(ML49)+sum(MG50),0) "HTotal",
nullif(sum(FL1)+sum(FL4)+sum(FL9)+ sum(FL14)+sum(FL24)+sum(FL49)+sum(FG50),0) "FTotal"
from (
select
o.obs_id,
o.encounter_id,
o.obs_datetime,
rm.source,
rm.code, -- remove these!
(CASE 
  when rm.source = 'PIH' and rm.code = 'Bitten by suspected rabid animal' then 'Agression par animal suspect de rage'
  when rm.source = 'PIH' and rm.code = 'Anthrax' then 'Charbon Cutane'
  when rm.source = 'CIEL' and rm.code = '120742' then 'Chikungunya'
  when rm.source = 'PIH' and rm.code = 'CHOLERA' then 'Cholera'
  when rm.source = 'PIH' and rm.code = 'PERTUSSIS' then 'Coqueluche'
  when rm.source = 'CIEL' and rm.code = '142592' then 'Dengue'
  when rm.source = 'CIEL' and rm.code = '161887' then 'Diarrhee Aqueuse'  
  when rm.source = 'PIH' and rm.code = 'Bloody diarrhea' then 'Diarrhee Sanglante'  
  when rm.source = 'PIH' and rm.code = 'Diphtheria' then 'Diphtheria'  
  when rm.source = 'PIH' and rm.code = 'EPILEPSY' then 'Epilepsie'  
  when rm.source = 'PIH' and rm.code = 'Fever of unknown origin' then 'Fievre d''origine indeterminee'  
  when rm.source = 'CIEL' and rm.code = '123112' then 'Fievre Hemorragique'  
  when rm.source = 'PIH' and rm.code = 'Filariasis' then 'Filariose lymphatique'  
  when ((rm.source = 'PIH' and rm.code = 'Upper respiratory tract infection') or (rm.source = 'PIH' and rm.code = 'Acute respiratory infections NOS')) then 'Infection Respiratoire Aigue'
  when rm.source = 'PIH' and rm.code = 'SEXUALLY TRANSMITTED INFECTION' then 'IST'  
  when rm.source = 'PIH' and rm.code = 'LEPROSY' then 'Lčpre'  
  when rm.source = 'PIH' and rm.code = '7582' then 'Leptospirose'
  when rm.source = 'PIH' and rm.code = 'MALARIA' then 'Malaria Confirmee Traitee'
  when rm.source = 'PIH' and rm.code = 'MALNUTRITION' then 'Malnutrition'
  when rm.source = 'PIH' and rm.code = 'Bacterial meningitis' then 'Meningite bacteriennes'
  when rm.source = 'PIH' and rm.code = 'MUMPS' then 'Oreillons'
  when rm.source = 'PIH' and rm.code = 'Acute flassic paralysis' then 'Paralysie Flasque Aigue'
  when rm.source = 'PIH' and rm.code = 'Intestinal parasites' then 'Parasitose intestinale'
  when rm.source = 'PIH' and rm.code = 'Rabies' then 'Rage Humaine'
  when ((rm.source = 'PIH' and rm.code = 'MEASLES') or (rm.source = 'PIH' and rm.code = 'Rubella')) then 'Rougeole/Rubéole'
  when rm.source = 'CIEL' and rm.code = '139479' then 'Rubeole Congenital'
  when rm.source = 'PIH' and rm.code = 'Icteric febrile syndrome' then 'Syndrome Icterique Febrile'
  when rm.source = 'PIH' and rm.code = 'SARS' then 'Syndrome respiratoire aigu sévčre (SRAS)'
  when rm.source = 'PIH' and rm.code = 'SYPHILIS' then 'Syphilis'
  when rm.source = 'PIH' and rm.code = 'Congenital Syphilis' then 'Syphilis congenitales'
  when rm.source = 'PIH' and rm.code = 'Tetanus' then 'Tetanos'
  when rm.source = 'PIH' and rm.code = 'Tetanus Neonatorum' then 'Tetanos Neonatal'
  when rm.source = 'PIH' and rm.code = 'PULMONARY TUBERCULOSIS' then 'Tuberculose Pulmonaire bacteriologiquement'
  when rm.source = 'PIH' and rm.code = 'TUBERCULOSIS' then 'Tuberculose suspecte'
  when rm.source = 'PIH' and rm.code = 'TYPHOID FEVER' then 'Typhoide'
  when rm.source = 'PIH' and rm.code = 'HUMAN IMMUNODEFICIENCY VIRUS' then 'VIH+'
  when rm.source = 'PIH' and rm.code = 'Xerophthalmia' then 'Xerophthalmie'
end) 'Diagnosis',
 c_name.name "Certainty",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) < 1 and pr.gender = 'M' then 1 else 0 end) "ML1",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) < 1 and pr.gender = 'F' then 1 else 0 end) "FL1",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) >= 1 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <5 and pr.gender = 'M' then 1 else 0 end) "ML4",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) >= 1 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <5 and pr.gender = 'F' then 1 else 0 end) "FL4",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 5 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <10 and pr.gender = 'M' then 1 else 0 end) "ML9",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 5 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <10 and pr.gender = 'F' then 1 else 0 end) "FL9",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 10 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <15 and pr.gender = 'M' then 1 else 0 end) "ML14",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 10 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <15 and pr.gender = 'F' then 1 else 0 end) "FL14",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 15 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <25 and pr.gender = 'M' then 1 else 0 end) "ML24",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 15 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <25 and pr.gender = 'F' then 1 else 0 end) "FL24",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 25 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <50 and pr.gender = 'M' then 1 else 0 end) "ML49",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 25 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <50 and pr.gender = 'F' then 1 else 0 end) "FL49",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 50 and pr.gender = 'M' then 1 else 0 end) "MG50",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 50 and pr.gender = 'F' then 1 else 0 end) "FG50"
from obs o
INNER JOIN person pr on pr.person_id = o.person_id
INNER JOIN report_mapping rm on o.value_coded = rm.concept_id
LEFT OUTER JOIN obs oc on oc.encounter_id = o.encounter_id and oc.voided = 0 and oc.obs_group_id = o.obs_group_id and oc.concept_id =
    (select concept_id from report_mapping where source = 'PIH' and code = 'CLINICAL IMPRESSION DIAGNOSIS CONFIRMED' )
LEFT OUTER JOIN concept_name c_name on c_name.concept_id = oc.value_coded and  c_name.locale = 'en' and c_name.locale_preferred = '1' and c_name.voided = 0
where o.concept_id = (select concept_id from report_mapping where source = 'PIH' and code = 'DIAGNOSIS')
and o.voided = 0 
AND date(o.obs_datetime) >= :startDate 
AND date(o.obs_datetime) <= :endDate 
group by o.obs_id, rm.source, rm.code
) oo
where diagnosis is not null
group by Morbides
) tab on dx.morbides = tab.Morbides
;
