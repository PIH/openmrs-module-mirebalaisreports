select dx.morbides "PHENOMENES MORBIDES OU NON MORBIDES",
HL5 "Cas_H<5",
FL5 "Cas_F<5",
HL14 "Cas_H_5-14",
FL14 "Cas_F_5-14",
HL50 "Cas_H_15-50",
FL50 "Cas_F_15-50",
HG50 "Cas_H>50",
FG50 "Cas_F>50",
HTotal "Cas_H_Total",
FTotal "Cas_F_Total",
null "DI_H<5",
null "DI_F<5",
null "DI_H_5-14",
null "DI_F_5-14",
null "DI_H_15-50",
null "DI_F_15-50",
null "DI_H>50",
null "DI_F>50",
null "DI_H_Total",
null "DI_F_Total"
from
(select 'Cholera Suspecte' as morbides
UNION ALL
select 'Cholera Probable'
UNION ALL
select 'Diphtheria Probable'
UNION ALL
select 'Meningite Suspecte'
UNION ALL
select 'Paralysie Flasque Aigue'
UNION ALL
select 'Rougeole/Rubeole Suspecte'
UNION ALL
select 'Syndrome de Fievre Hemorragique Aigue'
UNION ALL
select 'Syndrome Rubeole Congenital'
UNION ALL
select 'Agression Par Animal Suspecte de Rage'
UNION ALL
select 'Evenement Supposes Etre Attribuables a la vaccination et a l''immunisation (Esavi)'
UNION ALL
select 'Mortalite Maternelle'
UNION ALL
select 'Peste Suspecte'
UNION ALL
select 'Toxi-Infection Alimentaire Collective (TIAC)'
UNION ALL
select 'Tout Phenomene Inhabituel'
UNION ALL
select 'Chikungunya Suspect'
UNION ALL
select 'Charbon Cutane Suspect'
UNION ALL
select 'Coqueluche Suspecte'
UNION ALL
select 'Diabete'
UNION ALL
select 'Diarrhee Aigue Aqueuse'
UNION ALL
select 'Diarrhee Aigue Sanglante'
UNION ALL
select 'Fievre Typhoide Suspecte'
UNION ALL
select 'Hypertension Arterielle (HTA)'
UNION ALL
select 'Infection Respiratoire Aigue'
UNION ALL
select 'Tetanos'
UNION ALL
select 'Tetanos Neonatal'
UNION ALL
select 'Autre Fievre a investiguer (D''Origine indeterminee)'
UNION ALL
select 'Dengue Suspecte'
UNION ALL
select 'Filariose Probable'
UNION ALL
select 'Infection Sexuellement Transmissable (IST)'
UNION ALL
select 'Lepre Suspecte'
UNION ALL
select 'Malnutrition'
UNION ALL
select 'Paludisme Suspect'
UNION ALL
select 'Paludisme cas Teste'
UNION ALL
select 'Paludisme Confirme'
UNION ALL
select 'Rage Humaine'
UNION ALL
select 'Syndrome Icterique Febrile'
UNION ALL
select 'Tuberculose Confirme (TPM+)'
UNION ALL
select 'VIH Confirme'
UNION ALL
select 'Autres Cas VUS Avec D''Autres Conditions') dx
LEFT OUTER JOIN
(
select
(CASE when diagnosis = 'Cholera' and certainty = 'Confirmed' then 'Cholera Suspecte'
      when diagnosis = 'Cholera' and certainty = 'Presumed' then 'Cholera Probable'
      when diagnosis = 'Plaudisme' and certainty = 'Confirmed' then 'Paludisme Confirme'
      when diagnosis = 'Plaudisme' and certainty = 'Presumed' then 'Paludisme Suspect'      
      else diagnosis
 end) "Morbides",    
nullif(sum(ML5),0) "HL5",
nullif(sum(FL5),0) "FL5",
nullif(sum(ML14),0) "HL14",
nullif(sum(FL14),0) "FL14",
nullif(sum(ML50),0) "HL50",
nullif(sum(FL50),0) "FL50",
nullif(sum(MG50),0) "HG50",
nullif(sum(FG50),0) "FG50",
nullif(sum(ML5)+sum(ML14)+sum(ML50)+ sum(MG50),0) "HTotal",
nullif(sum(FL5)+sum(FL14)+sum(FL50)+ sum(FG50),0) "FTotal"
from (
select
o.obs_id,
o.encounter_id,
o.obs_datetime,
rm.source,
rm.code, -- remove these!
(CASE 
  when rm.source = 'PIH' and rm.code = 'CHOLERA' then 'Cholera'
  when rm.source = 'PIH' and rm.code = 'Diphtheria' then 'Diphtheria Probable'
  when ((rm.source = 'PIH' and rm.code = 'VIRAL MENINGITIS') or (rm.source = 'PIH' and rm.code = 'Bacterial meningitis')) then 'Meningite Suspecte'
  when rm.source = 'PIH' and rm.code = 'Acute flassic paralysis' then 'Paralysie Flasque Aigue'
  when ((rm.source = 'PIH' and rm.code = 'MEASLES') or (rm.source = 'PIH' and rm.code = 'Rubella')) then 'Rougeole/Rubeole Suspecte'
  when rm.source = 'PIH' and rm.code = 'Acute hemorrhagic fever' then 'Syndrome de Fievre Hemorragique Aigue'
  when rm.source = 'CIEL' and rm.code = '139479' then 'Syndrome Rubeole Congenital'
  when rm.source = 'PIH' and rm.code = 'Acute hemorrhagic fever' then 'Syndrome de Fievre Hemorragique Aigue'
  when rm.source = 'CIEL' and rm.code = '139479' then 'Syndrome Rubeole Congenital'
  when rm.source = 'PIH' and rm.code = 'Bitten by suspected rabid animal' then 'Agression Par Animal Suspecte de Rage'
  when rm.source = 'CIEL' and rm.code = '120742' then 'Chikungunya Suspect'
  when rm.source = 'PIH' and rm.code = 'Anthrax' then 'Charbon Cutane Suspect'
  when rm.source = 'PIH' and rm.code = 'PERTUSSIS' then 'Coqueluche Suspecte'
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
    or (rm.source = 'PIH' and rm.code = 'DIABETES MELLITUS')) then 'Diabete'
  when rm.source = 'CIEL' and rm.code = '161887' then 'Diarrhee Aigue Aqueuse'
  when rm.source = 'PIH' and rm.code = 'Bloody diarrhea' then 'Diarrhee Aigue Sanglante'
  when rm.source = 'PIH' and rm.code = 'TYPHOID FEVER' then 'Fievre Typhoide Suspecte'
  when ((rm.source = 'PIH' and rm.code = 'HYPERTENSION') 
    or (rm.source = 'CIEL' and rm.code = '161644')
    or (rm.source = 'PIH' and rm.code = 'HYPERTENSIVE HEART DISEASE')
    or (rm.source = 'PIH' and rm.code = 'Pre-Existing Hypertension Complicating Pregnancy')  
    or (rm.source = 'CIEL' and rm.code = '138197')
    or (rm.source = 'PIH' and rm.code = 'Unspecified maternal hypertension')
    or (rm.source = 'CIEL' and rm.code = '113859')
    or (rm.source = 'CIEL' and rm.code = '129484')) then 'Hypertension Arterielle (HTA)'
  when ((rm.source = 'PIH' and rm.code = 'Upper respiratory tract infection') 
    or (rm.source = 'PIH' and rm.code = 'Acute respiratory infections NOS')) then 'Infection Respiratoire Aigue'
  when rm.source = 'PIH' and rm.code = 'Tetanus' then 'Tetanos'
  when rm.source = 'PIH' and rm.code = 'Tetanus Neonatorum' then 'Tetanos Neonatal'
  when rm.source = 'PIH' and rm.code = 'Fever of unknown origin' then 'Autre Fievre a investiguer (D''Origine indeterminee)'
  when rm.source = 'PIH' and rm.code = 'Dengue' then 'Dengue Suspecte'
  when rm.source = 'PIH' and rm.code = 'Filariasis' then 'Filariose Probable'
  when rm.source = 'PIH' and rm.code = 'SEXUALLY TRANSMITTED INFECTION' then 'Infection Sexuellement Transmissable (IST)'  
  when rm.source = 'PIH' and rm.code = 'LEPROSY' then 'Lepre Suspecte'
  when rm.source = 'PIH' and rm.code = 'MALNUTRITION' then 'Malnutrition'
  when rm.source = 'PIH' and rm.code = 'MALARIA' then 'Plaudisme'    
  when rm.source = 'PIH' and rm.code = 'Rabies' then 'Rage Humaine'
  when rm.source = 'PIH' and rm.code = 'Icteric febrile syndrome' then 'Syndrome Icterique Febrile'  
  when rm.source = 'PIH' and rm.code = 'TUBERCULOSIS' then 'Tuberculose Confirme (TPM+)'
  when rm.source = 'PIH' and rm.code = 'HUMAN IMMUNODEFICIENCY VIRUS' then 'VIH Confirme'
 end) 'Diagnosis',
 c_name.name "Certainty",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) < 5 and pr.gender = 'M' then 1 else 0 end) "ML5",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) < 5 and pr.gender = 'F' then 1 else 0 end) "FL5",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) >= 5 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <15 and pr.gender = 'M' then 1 else 0 end) "ML14",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) >= 5 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <15 and pr.gender = 'F' then 1 else 0 end) "FL14",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 15 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <50 and pr.gender = 'M' then 1 else 0 end) "ML50",
(CASE when round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) > 15 and round(DATEDIFF(o.obs_datetime, pr.birthdate)/365.25, 1) <50 and pr.gender = 'F' then 1 else 0 end) "FL50",
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
