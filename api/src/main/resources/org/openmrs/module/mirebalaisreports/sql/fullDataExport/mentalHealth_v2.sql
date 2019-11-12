DROP TEMPORARY TABLE IF EXISTS temp_mentalhealth_visit;

set @encounter_type = encounter_type('Mental Health Consult');

create temporary table temp_social_econ
(
patient_id int,
zl_emr_id varchar(255),
gender varchar(50),
unknown_patient text,
patient_address text,
provider varchar(255),
loc_registered varchar(255),
location_id int,
enc_location varchar(255),
encounter_id int,
encounter_date datetime,
age_at_enc double
);

insert into temp_mentalhealth_visit (patient_id, zl_emr_id, gender, encounter_id, encounter_date, age_at_enc, provider, patient_address, loc_registered, location_id)
select patient_id, zlemr(patient_id), gender(patient_id), encounter_id,  encounter_datetime, age_at_enc(patient_id), provider(encounter_id), person_address(patient_id),
loc_registered(patient_id), location_id
 from encounter where voided = 0 and encounter_type = @encounter_type
-- filter by date
 AND date(encounter_datetime) >=  date(@startDate)
 AND date(encounter_datetime) <=  date(@endDate)
;

-- exclude test patients
delete from temp_mentalhealth_visit where
patient_id IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = (select
person_attribute_type_id from person_attribute_type where name = "Test Patient")
                         AND voided = 0)
;

-- unknown patient
update temp_mentalhealth_visit tmhv
set tmhv.unknown_patient = IF(tmhv.patient_id = unknown_patient(tmhv.patient_id), 'true', NULL);

update temp_mentalhealth_visit tmhv
left join location l on tmhv.location_id = l.location_id
set tmhv.enc_location = l.name;
