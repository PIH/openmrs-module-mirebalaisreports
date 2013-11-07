select e.encounter_id, ssrv_n.name
from encounter e
inner join obs ssrv
  on e.encounter_id = ssrv.encounter_id
  and ssrv.voided = false
  and ssrv.concept_id = :surgicalService
inner join concept_name ssrv_n
    on ssrv.value_coded = ssrv_n.concept_id
    and ssrv_n.locale = 'en'
    and ssrv_n.locale_preferred = true
    and ssrv_n.voided = 0
    and ssrv_n.concept_name_type = 'FULLY_SPECIFIED'
where e.encounter_id in (:encounterIds)