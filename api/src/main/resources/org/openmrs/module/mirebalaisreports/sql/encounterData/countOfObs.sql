select e.encounter_id, count(o.obs_id)
from encounter e
inner join obs o
on e.encounter_id = o.encounter_id
and o.voided = false
and o.concept_id = :conceptId
where e.encounter_id in (:encounterIds)
group by e.encounter_id
