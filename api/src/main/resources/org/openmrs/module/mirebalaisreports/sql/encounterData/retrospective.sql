select e.encounter_id, IF(TIME_TO_SEC(e.date_created) - TIME_TO_SEC(e.encounter_datetime) > 1800, TRUE, FALSE)
from encounter e
where e.encounter_id in (:encounterIds)