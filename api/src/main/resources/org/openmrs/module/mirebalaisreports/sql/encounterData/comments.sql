select e.encounter_id, o.value_text
from encounter e
inner join obs o
  on e.encounter_id = o.encounter_id
  and o.voided = false
  and o.concept_id = :comments
where e.encounter_id in (:encounterIds)