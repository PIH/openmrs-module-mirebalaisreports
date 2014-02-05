select v.patient_id, v.visit_id
from visit v
where v.patient_id in (:patientIds) and v.voided=0 and v.date_stopped is null
order by v.date_started desc