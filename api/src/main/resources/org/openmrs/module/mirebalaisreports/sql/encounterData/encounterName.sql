select e.encounter_id, et.name
from encounter e
inner join encounter_type et
on e.encounter_type = et.encounter_type_id
where e.encounter_id in (:encounterIds)