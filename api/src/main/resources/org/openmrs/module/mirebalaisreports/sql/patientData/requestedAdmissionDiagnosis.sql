select p.patient_id, (select cn.name
        from concept c, concept_name cn
        where c.concept_id=cn.concept_id
            and c.concept_id= diagnosis.value_coded and cn.voided=0
            and cn.locale=':locale'
            and cn.concept_name_type = 'FULLY_SPECIFIED'
        ) as 'diagnosis'
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
inner join obs diagnosis
	on (dispo_encounter.encounter_id = diagnosis.encounter_id
		and diagnosis.voided =0
		and diagnosis.concept_id = :codedDiagnosis)
inner join obs primaryDiagnosis
	on (primaryDiagnosis.obs_group_id = diagnosis.obs_group_id
	and primaryDiagnosis.voided = 0
	and primaryDiagnosis.concept_id= :diagnosisOrder
	and primaryDiagnosis.value_coded= :primaryDiagnosis)
where p.patient_id in (:patientIds) and v.date_stopped is null and v.voided =0;