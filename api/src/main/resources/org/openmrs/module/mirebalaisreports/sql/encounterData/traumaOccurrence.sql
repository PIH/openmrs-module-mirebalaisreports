select e.encounter_id, c.name
from encounter e
inner join obs o
  on e.encounter_id = o.encounter_id
  and o.voided = false
  and o.concept_id = :trauma
inner join concept_name c
    on o.value_coded = c.concept_id
    and c.locale = 'fr'
    and c.locale_preferred = true
where e.encounter_id in (:encounterIds)