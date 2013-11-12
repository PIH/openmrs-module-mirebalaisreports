select e.encounter_id, asst_nc.value_text
from encounter e
inner join obs asst_nc
on e.encounter_id = asst_nc.encounter_id
and asst_nc.concept_id = :otherAssistant
and asst_nc.voided = false
where e.encounter_id in (:encounterIds)