select e.encounter_id, DATE(o.value_datetime)
from encounter e
inner join obs o
  on e.encounter_id = o.encounter_id
  and o.voided = false
  and o.concept_id = :returnVisitDate
where e.encounter_id in (:encounterIds)