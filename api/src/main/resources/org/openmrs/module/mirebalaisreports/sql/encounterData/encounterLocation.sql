select e.encounter_id, adt_l.name
from encounter e
inner join encounter adt
on e.patient_id = adt.patient_id
and adt.encounter_type = :encounterType
and e.encounter_datetime = adt.encounter_datetime
left outer join location adt_l
on adt.location_id = adt_l.location_id
where e.encounter_id in (:encounter_Ids)