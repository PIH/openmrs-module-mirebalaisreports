select p.patient_id, CONCAT_WS(' ', n2.given_name, n2.family_name) as providerName
from patient p
inner join visit v on p.patient_id = v.patient_id
inner join encounter dispo_encounter
	on dispo_encounter.visit_id = v.visit_id
	and dispo_encounter.voided = 0
inner join obs dispo
	on dispo.encounter_id = dispo_encounter.encounter_id
    and dispo.concept_id = :dispositionConceptId
    and dispo.value_coded = :admissionDispositionConceptId
    and dispo.voided = 0
inner join encounter_provider as ep on (dispo_encounter.encounter_id = ep.encounter_id)
inner join provider on (ep.provider_id = provider.provider_id)
inner join person_name as n2 on (provider.person_id = n2.person_id and n2. voided = 0 )
where p.patient_id in (:patientIds) and v.date_stopped is null and v.voided =0;