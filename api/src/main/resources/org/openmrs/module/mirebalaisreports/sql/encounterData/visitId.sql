select e.encounter_id, e.visit_id
from encounter e
where e.encounter_id in (:encounterIds)