SELECT '' as 'Dossier Found? Y/N',
       d.identifier as '# Dossier',
       date(pc.encounter_datetime) as 'Visit Date',
       loc.name as Location,
       dispo.name as Disposition,
       cd1n.name as 'Coded 1', cd2n.name  as 'Coded 2', cd3n.name  as 'Coded 3', cd4n.name  as 'Coded 4',
       nc1.value_text  as 'Non-Coded 1', nc2.value_text  as 'Non-Coded 2', '' as 'Dossier',
       concat(un.given_name, ' ', un.family_name) as 'Clinician who entered diagnosis',
       pc.encounter_id,
       pc.visit_id
FROM patient p

INNER JOIN encounter pc
   ON p.patient_id = pc.patient_id
  AND pc.encounter_type = :consEnc
  AND pc.voided = 0
  AND pc.encounter_datetime BETWEEN :startDate AND :endDate

INNER JOIN location loc
   ON pc.location_id = loc.location_id
   AND loc.location_id = :location

INNER JOIN patient_identifier d
   ON p.patient_id = d.patient_id
  AND d.identifier_type = :dosId
  AND d.voided = 0

LEFT OUTER JOIN obs d1
  ON pc.encounter_id = d1.encounter_id
 AND d1.concept_id = :dispo
 AND d1.voided = 0

LEFT OUTER JOIN concept_name dispo
  ON d1.value_coded = dispo.concept_id
 AND dispo.locale = 'fr'

LEFT OUTER JOIN obs cd1
  ON pc.encounter_id = cd1.encounter_id
 AND cd1.concept_id = :coded
 AND cd1.voided = 0

LEFT OUTER JOIN concept_name cd1n
  ON cd1.value_coded = cd1n.concept_id
 AND cd1n.locale = 'fr'

LEFT OUTER JOIN obs cd2
  ON pc.encounter_id = cd2.encounter_id
 AND cd2.concept_id = :coded
 AND cd2.voided = 0
 AND cd2.obs_id != cd1.obs_id

LEFT OUTER JOIN concept_name cd2n
  ON cd2.value_coded = cd2n.concept_id
 AND cd2n.locale = 'fr'

LEFT OUTER JOIN obs cd3
 ON pc.encounter_id = cd3.encounter_id
AND cd3.concept_id = :coded
AND cd3.voided = 0
AND cd3.obs_id NOT IN (cd1.obs_id, cd2.obs_id)

LEFT OUTER JOIN concept_name cd3n
  ON cd3.value_coded = cd3n.concept_id
 AND cd3n.locale = 'fr'

LEFT OUTER JOIN obs cd4
  ON pc.encounter_id = cd4.encounter_id
 AND cd4.concept_id = :coded
 AND cd4.voided = 0
 AND cd4.obs_id NOT IN (cd1.obs_id, cd2.obs_id, cd3.obs_id)

LEFT OUTER JOIN concept_name cd4n
  ON cd4.value_coded = cd4n.concept_id
 AND cd4n.locale = 'fr'

LEFT OUTER JOIN obs nc1
 ON pc.encounter_id = nc1.encounter_id
AND nc1.concept_id = :noncoded
AND nc1.voided = 0

LEFT OUTER JOIN obs nc2
  ON pc.encounter_id = nc2.encounter_id
 AND nc2.concept_id = :noncoded
 AND nc2.voided = 0
 AND nc2.obs_id != nc1.obs_id

INNER JOIN encounter_provider enpr
   ON pc.encounter_id = enpr.encounter_id
  AND enpr.voided = 0

INNER JOIN users u
   ON enpr.creator = u.user_id

INNER JOIN person_name un
   ON u.person_id = un.person_id

WHERE p.voided = 0

GROUP BY pc.encounter_id

ORDER BY RAND()
LIMIT 25 ;