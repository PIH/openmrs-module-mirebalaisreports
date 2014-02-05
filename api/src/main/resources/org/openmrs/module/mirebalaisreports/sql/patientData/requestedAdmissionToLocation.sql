select p.patient_id,  concat(( select uuid from location where location_id = (
			select CAST(o.value_text as UNSIGNED)
			from obs o where o.person_id = p.patient_id and o.concept_id= :admissionLocationConceptId and o.voided = 0
			order by o.obs_datetime desc limit 0,1
		) ), '') as locationUuid
from patient p
where p.patient_id in (:patientIds)