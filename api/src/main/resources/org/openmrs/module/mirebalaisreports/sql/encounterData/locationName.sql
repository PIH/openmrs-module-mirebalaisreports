select e.encounter_id, el.name
from encounter e
inner join location el
  on e.location_id = el.location_id
where e.encounter_id in (:encounterIds)