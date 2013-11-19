select e.encounter_id, CONCAT(attending_n.given_name, ' ',  attending_n.family_name)
from encounter e
inner join encounter_provider attending
  on e.encounter_id = attending.encounter_id
  and attending.voided = 0
  and attending.encounter_role_id = 6
  inner join provider attending_p
    on attending.provider_id = attending_p.provider_id
    inner join person_name attending_n
    on attending_p.person_id = attending_n.person_id
    and attending_n.voided = 0
where e.encounter_id in (:encounterIds)