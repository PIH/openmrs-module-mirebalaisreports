select e.encounter_id, et.name
from encounter e
inner join location et
on e.location_id = et.location_id
where e.encounter_id in (:encounterIds)