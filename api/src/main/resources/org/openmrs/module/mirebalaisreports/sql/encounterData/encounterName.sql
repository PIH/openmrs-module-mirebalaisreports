select e.encounter_id, adt_t.name
from encounter e
inner join encounter adt
on e.patient_id = adt.patient_id
and adt.encounter_type in (11, 12, 13)
and e.encounter_datetime = adt.encounter_datetime
inner join encounter_type adt_t
on adt.encounter_type = adt_t.encounter_type_id
where e.encounter_id in (:encounterIds)