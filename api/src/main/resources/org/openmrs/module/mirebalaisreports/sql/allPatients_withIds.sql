select pn.family_name as 'Nom', pn.given_name as 'Prénom',
       pa.address1 as Locality,
       pa.city_village as Commune,
       p.birthdate as 'Date de naissance',
       CAST(CONCAT(timestampdiff(YEAR, p.birthdate, NOW()), '.', MOD(timestampdiff(MONTH, p.birthdate, NOW()), 12) ) as CHAR) as Age,
       p.gender as Sexe,
       zl.identifier as 'ZL ID',
       dos.identifier as Dossier,
       t.value as TelephoneNumber
  from patient pat

 inner join person p
    on pat.patient_id = p.person_id
   and p.voided = 0

 inner join person_name pn
    on pat.patient_id = pn.person_id
   and pn.voided = 0

 left outer join person_address pa
    on pat.patient_id = pa.person_id
   and pa.voided = 0

 left outer join person_attribute t
    on pat.patient_id = t.person_id
   and t.voided = 0
   and t.person_attribute_type_id = :phoneType

 left outer join patient_identifier dos
   on pat.patient_id = dos.patient_id
  and identifier_type = :dosId
  and dos.voided = 0

 left outer join patient_identifier zl
   on pat.patient_id = zl.patient_id
  and zl.identifier_type = :zlId
  and zl.voided = 0

where pat.voided = 0

group by p.person_id

order by pn.family_name, pn.given_name,
         p.gender, p.birthdate;