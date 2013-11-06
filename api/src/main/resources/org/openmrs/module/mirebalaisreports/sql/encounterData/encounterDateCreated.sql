select e.encounter_id, adt.date_created
from encounter e
inner join encounter adt
on e.patient_id = adt.patient_id
and adt.encounter_type in (11,12,13)
and e.encounter_datetime = adt.encounter_datetime
where e.encounter_id in (:encounterIds)