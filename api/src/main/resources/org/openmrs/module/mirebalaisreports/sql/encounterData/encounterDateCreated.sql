select e.encounter_id, e.date_created
from encounter e
where e.encounter_id in (:encounterIds)