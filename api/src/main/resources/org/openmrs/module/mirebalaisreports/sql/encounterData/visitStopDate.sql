select e.encounter_id, v.date_stopped
from encounter e
left outer join visit v
  on e.visit_id = v.visit_id
  and v.voided = 0
where e.encounter_id in (:encounterIds)