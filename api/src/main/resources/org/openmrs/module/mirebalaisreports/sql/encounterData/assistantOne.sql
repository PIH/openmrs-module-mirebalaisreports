select e.encounter_id, CONCAT(asst_1_n.given_name, ' ',  asst_1_n.family_name)
from encounter e
inner join encounter_provider asst_1
  on e.encounter_id = asst_1.encounter_id
  and asst_1.voided = 0
  and asst_1.encounter_role_id = :assistantOne
  inner join provider asst_1_p
    on asst_1.provider_id = asst_1_p.provider_id
    inner join person_name asst_1_n
    on asst_1_p.person_id = asst_1_n.person_id
    and asst_1_n.voided = 0
where e.encounter_id in (:encounterIds)