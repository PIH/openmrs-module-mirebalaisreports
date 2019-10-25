DROP TEMPORARY TABLE IF EXISTS temp_mentalhealth_program;

set @program_id = program('Mental Health');
set @latest_diagnosis = concept_from_mapping('PIH', 'Mental health diagnosis');
set @encounter_type = encounter_type('Mental Health Consult');
set @zlds_score = concept_from_mapping('CIEL', '163225');
set @whodas_score = concept_from_mapping('CIEL', '163226');
set @seizures = concept_from_mapping('PIH', 'Number of seizures in the past month');
set @medication = concept_from_mapping('PIH', 'Mental health medication');
set @mh_intervention =  concept_from_mapping('PIH', 'Mental health intervention');
set @other_noncoded = concept_from_mapping('PIH', 'OTHER NON-CODED');
set @return_visit_date = concept_from_mapping('PIH', 'RETURN VISIT DATE');

create temporary table temp_mentalhealth_program
(
patient_id int,
patient_program_id int,
prog_location_id int,
zlemr varchar(255),
gender varchar(50),
age double,
assigned_chw text,
location_when_registered_in_program varchar(255),
date_enrolled date,
date_completed date,
number_of_days_in_care double,
program_status_outcome varchar(255),
unknown_patient varchar(50),
encounter_id int,
encounter_datetime datetime,
latest_diagnosis text,
latest_zlds_score double,
recent_date_zlds_score date,
previous_zlds_score double,
previous_date_zlds_score date,
baseline_zlds_score double,
baseline_date_zlds_score date,
latest_whodas_score double,
recent_date_whodas_score date,
previous_whodas_score double,
previous_date_whodas_score date,
baseline_whodas_score double,
baseline_date_whodas_score date,
latest_seizure_number double,
latest_seizure_date date,
previous_seizure_number double,
previous_seizure_date date,
baseline_seizure_number double,
baseline_seizure_date date,
latest_medication_given text,
latest_medication_date date,
latest_intervention text,
other_intervention text,
last_intervention_date date,
last_visit_date date,
next_scheduled_visit_date date,
patient_came_within_14_days_appt varchar(50),
three_months_since_latest_return_date varchar(50),
six_months_since_latest_return_date varchar(50)
);

insert into temp_mentalhealth_program (patient_id, patient_program_id, prog_location_id, zlemr, gender, date_enrolled, date_completed, number_of_days_in_care, program_status_outcome
                                        )
select patient_id,
	   patient_program_id,
       location_id,
	   zlemr(patient_id),
       gender(patient_id),
	   date(date_enrolled),
       date(date_completed),
       If(date_completed is null, datediff(now(), date_enrolled), datediff(date_completed, date_enrolled)),
       concept_name(outcome_concept_id, 'fr')
       from patient_program where program_id = @program_id and voided = 0;

-- exclude test patients
delete from temp_mentalhealth_program where
patient_id IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = (select
person_attribute_type_id from person_attribute_type where name = "Test Patient")
                         AND voided = 0)
;

-- unknown patient
update temp_mentalhealth_program tmhp
set tmhp.unknown_patient = IF(tmhp.patient_id = unknown_patient(tmhp.patient_id), 'true', NULL);

update temp_mentalhealth_program tmhp
left join person p on person_id = patient_id and p.voided = 0
set tmhp.age = CAST(CONCAT(timestampdiff(YEAR, p.birthdate, NOW()), '.', MOD(timestampdiff(MONTH, p.birthdate, NOW()), 12) ) as CHAR);

-- relationship
update temp_mentalhealth_program tmhp
inner join (select patient_program_id, patient_id, person_a, GROUP_CONCAT(' ',CONCAT(pn.given_name,' ',pn.family_name)) chw  from patient_program join relationship r on person_b = patient_id and program_id = @program_id
and r.voided = 0 and relationship = relation_type('Community Health Worker') join person_name pn on person_a = pn.person_id and pn.voided = 0 group by patient_program_id) relationship
on relationship.patient_id = tmhp.patient_id and tmhp.patient_program_id = relationship.patient_program_id
set tmhp.assigned_chw = relationship.chw;

-- location registered in Program
update temp_mentalhealth_program tmhp
left join location l on location_id = tmhp.prog_location_id and l.retired = 0
set tmhp.location_when_registered_in_program = l.name;

-- latest dignoses
update temp_mentalhealth_program tmh
LEFT JOIN (
select pp.patient_id, patient_program_id, GROUP_CONCAT(cnd.name) "diagnoses", date_enrolled, date_completed from patient_program pp
INNER JOIN
encounter e on e.encounter_id =
    (select encounter_id from encounter e2 where
     e2.voided = 0
     and e2.patient_id = pp.patient_id
     and e2.encounter_type = @encounter_type
     and date(e2.encounter_datetime) >= date(date_enrolled) or date(e2.encounter_datetime)  <= date(date_completed)
     and exists (select 1 from obs where encounter_id = e2.encounter_id and concept_id = @latest_diagnosis and voided = 0)
     order by e2.encounter_datetime desc
     limit 1)
INNER JOIN obs o on o.voided =0 and o.concept_id = @latest_diagnosis  and o.encounter_id = e.encounter_id
INNER JOIN concept_name cnd on cnd.concept_name_id  =
   (select cnd2.concept_name_id from concept_name cnd2
    where o.value_coded = cnd2.concept_id
    and cnd2.voided = 0
    order by field(cnd2.locale,'fr','en','ht'), cnd2.locale_preferred desc
    limit 1)
group by pp.patient_id
) tld
on tld.patient_program_id = tmh.patient_program_id
set tmh.latest_diagnosis = tld.diagnoses;

-- latest zlds non null score
update temp_mentalhealth_program tmh
LEFT JOIN (
select pp.patient_id, patient_program_id, value_numeric, date_enrolled, date_completed, date(encounter_datetime) enc_date from patient_program pp
INNER JOIN
encounter e on e.encounter_id =
    (select encounter_id from encounter e2 where
     e2.voided =0
     and e2.patient_id = pp.patient_id
     and e2.encounter_type = @encounter_type
     -- and date(e2.encounter_datetime) >= date(date_enrolled) or date(e2.encounter_datetime)  <= date(date_completed)
     and exists (select 1 from obs where encounter_id = e2.encounter_id and concept_id = @zlds_score and voided = 0)
     order by e2.encounter_datetime desc
     limit 1)
INNER JOIN obs o on o.voided =0 and o.concept_id = @zlds_score and o.encounter_id = e.encounter_id
group by pp.patient_id
) tzld
on tzld.patient_program_id = tmh.patient_program_id
set tmh.latest_zlds_score = tzld.value_numeric,
	tmh.recent_date_zlds_score = tzld.enc_date;

-- Previous zlds non-null score
update temp_mentalhealth_program tmh
LEFT JOIN (
select pp.patient_id, patient_program_id, value_numeric, date_enrolled, date_completed, date(encounter_datetime) enc_date from patient_program pp
INNER JOIN
encounter e on e.encounter_id =
    (select encounter_id from encounter e2 where
     e2.voided =0
     and e2.patient_id = pp.patient_id
     and e2.encounter_type = @encounter_type
     -- and date(e2.encounter_datetime) >= date(date_enrolled) or date(e2.encounter_datetime)  <= date(date_completed)
     and exists (select 1 from obs where encounter_id = e2.encounter_id and concept_id = @zlds_score and voided = 0)
     order by e2.encounter_datetime desc
     limit 1,1)
INNER JOIN obs o on o.voided =0 and o.concept_id = @zlds_score and o.encounter_id = e.encounter_id
group by pp.patient_id
) tzld_prev
on tzld_prev.patient_program_id = tmh.patient_program_id
set tmh.previous_zlds_score = tzld_prev.value_numeric,
	tmh.previous_date_zlds_score = tzld_prev.enc_date;

-- Baseline zlds non-null score
update temp_mentalhealth_program tmh
LEFT JOIN (
select pp.patient_id, patient_program_id, value_numeric, date_enrolled, date_completed, date(encounter_datetime) enc_date from patient_program pp
INNER JOIN
encounter e on e.encounter_id =
    (select encounter_id from encounter e2 where
     e2.voided =0
     and e2.patient_id = pp.patient_id
     and e2.encounter_type = @encounter_type
     -- and date(e2.encounter_datetime) >= date(date_enrolled) or date(e2.encounter_datetime)  <= date(date_completed)
     and exists (select 1 from obs where encounter_id = e2.encounter_id and concept_id = @zlds_score and voided = 0)
     order by e2.encounter_datetime asc
     limit 1)
INNER JOIN obs o on o.voided =0 and o.concept_id = @zlds_score and o.encounter_id = e.encounter_id
group by pp.patient_id
) tzld_baseline
on tzld_baseline.patient_program_id = tmh.patient_program_id
set tmh.baseline_zlds_score = tzld_baseline.value_numeric,
	tmh.baseline_date_zlds_score = tzld_baseline.enc_date;

-- latest WHODAS score
update temp_mentalhealth_program tmh
LEFT JOIN (
select pp.patient_id, patient_program_id, value_numeric, date_enrolled, date_completed, date(encounter_datetime) enc_date, e.encounter_id enc_id from patient_program pp
INNER JOIN
encounter e on e.encounter_id =
    (select encounter_id from encounter e2 where
     e2.voided =0
     and e2.patient_id = pp.patient_id
     and e2.encounter_type = @encounter_type
     -- and date(e2.encounter_datetime) >= date(date_enrolled) or date(e2.encounter_datetime)  <= date(date_completed)
     and exists (select 1 from obs where encounter_id = e2.encounter_id and concept_id = @whodas_score and voided = 0)
     order by e2.encounter_datetime desc
     limit 1)
INNER JOIN obs o on o.voided =0 and o.concept_id = @whodas_score and o.encounter_id = e.encounter_id
group by pp.patient_id
) twhodas
on twhodas.patient_program_id = tmh.patient_program_id
set tmh.latest_whodas_score = twhodas.value_numeric,
	tmh.recent_date_whodas_score = twhodas.enc_date,
    tmh.encounter_id = twhodas.enc_id;

-- Previous WHODAS score
update temp_mentalhealth_program tmh
LEFT JOIN (
select pp.patient_id, patient_program_id, value_numeric, date_enrolled, date_completed, date(encounter_datetime) enc_date from patient_program pp
INNER JOIN
encounter e on e.encounter_id =
    (select encounter_id from encounter e2 where
     e2.voided =0
     and e2.patient_id = pp.patient_id
     and e2.encounter_type = @encounter_type
     -- and date(e2.encounter_datetime) >= date(date_enrolled) or date(e2.encounter_datetime)  <= date(date_completed)
     and exists (select 1 from obs where encounter_id = e2.encounter_id and concept_id = @whodas_score and voided = 0)
     order by e2.encounter_datetime desc
     limit 1,1)
INNER JOIN obs o on o.voided =0 and o.concept_id = @whodas_score and o.encounter_id = e.encounter_id
group by pp.patient_id
) twhodas_prev
on twhodas_prev.patient_program_id = tmh.patient_program_id
set tmh.previous_whodas_score = twhodas_prev.value_numeric,
	tmh.previous_date_whodas_score = twhodas_prev.enc_date;

-- first/baseline WHODAS
update temp_mentalhealth_program tmh
LEFT JOIN (
select pp.patient_id, patient_program_id, value_numeric, date_enrolled, date_completed, date(encounter_datetime) enc_date from patient_program pp
INNER JOIN
encounter e on e.encounter_id =
    (select encounter_id from encounter e2 where
     e2.voided =0
     and e2.patient_id = pp.patient_id
     and e2.encounter_type = @encounter_type
     -- and date(e2.encounter_datetime) >= date(date_enrolled) or date(e2.encounter_datetime)  <= date(date_completed)
     and exists (select 1 from obs where encounter_id = e2.encounter_id and concept_id = @whodas_score and voided = 0)
     order by e2.encounter_datetime asc
     limit 1)
INNER JOIN obs o on o.voided =0 and o.concept_id = @whodas_score and o.encounter_id = e.encounter_id
group by pp.patient_id
) twhodas_baseline
on twhodas_baseline.patient_program_id = tmh.patient_program_id
set tmh.baseline_whodas_score = twhodas_baseline.value_numeric,
	tmh.baseline_date_whodas_score = twhodas_baseline.enc_date;

-- latest number of seizures
update temp_mentalhealth_program tmh
LEFT JOIN (
select pp.patient_id, patient_program_id, value_numeric, date_enrolled, date_completed, date(encounter_datetime) enc_date from patient_program pp
INNER JOIN
encounter e on e.encounter_id =
    (select encounter_id from encounter e2 where
     e2.voided =0
     and e2.patient_id = pp.patient_id
     and e2.encounter_type = @encounter_type
     -- and date(e2.encounter_datetime) >= date(date_enrolled) or date(e2.encounter_datetime)  <= date(date_completed)
     and exists (select 1 from obs where encounter_id = e2.encounter_id and concept_id = @seizures and voided = 0)
     order by e2.encounter_datetime desc
     limit 1)
INNER JOIN obs o on o.voided =0 and o.concept_id = @seizures and o.encounter_id = e.encounter_id
group by pp.patient_id
) seizure
on seizure.patient_program_id = tmh.patient_program_id
set tmh.latest_seizure_number = seizure.value_numeric,
	tmh.latest_seizure_date = seizure.enc_date;

-- Previous number of seizures
update temp_mentalhealth_program tmh
LEFT JOIN (
select pp.patient_id, patient_program_id, value_numeric, date_enrolled, date_completed, date(encounter_datetime) enc_date from patient_program pp
INNER JOIN
encounter e on e.encounter_id =
    (select encounter_id from encounter e2 where
     e2.voided =0
     and e2.patient_id = pp.patient_id
     and e2.encounter_type = @encounter_type
     -- and date(e2.encounter_datetime) >= date(date_enrolled) or date(e2.encounter_datetime)  <= date(date_completed)
     and exists (select 1 from obs where encounter_id = e2.encounter_id and concept_id = @seizures and voided = 0)
     order by e2.encounter_datetime desc
     limit 1,1)
INNER JOIN obs o on o.voided =0 and o.concept_id = @seizures and o.encounter_id = e.encounter_id
group by pp.patient_id
) seizure_prev
on seizure_prev.patient_program_id = tmh.patient_program_id
set tmh.previous_seizure_number = seizure_prev.value_numeric,
	tmh.previous_seizure_date = seizure_prev.enc_date;

-- first/baseline number or seizures
update temp_mentalhealth_program tmh
LEFT JOIN (
select pp.patient_id, patient_program_id, value_numeric, date_enrolled, date_completed, date(encounter_datetime) enc_date from patient_program pp
INNER JOIN
encounter e on e.encounter_id =
    (select encounter_id from encounter e2 where
     e2.voided =0
     and e2.patient_id = pp.patient_id
     and e2.encounter_type = @encounter_type
     -- and date(e2.encounter_datetime) >= date(date_enrolled) or date(e2.encounter_datetime)  <= date(date_completed)
     and exists (select 1 from obs where encounter_id = e2.encounter_id and concept_id = @seizures and voided = 0)
     order by e2.encounter_datetime asc
     limit 1)
INNER JOIN obs o on o.voided =0 and o.concept_id = @seizures and o.encounter_id = e.encounter_id
group by pp.patient_id
) seizure_baseline
on seizure_baseline.patient_program_id = tmh.patient_program_id
set tmh.baseline_seizure_number = seizure_baseline.value_numeric,
	tmh.baseline_seizure_date = seizure_baseline.enc_date;

-- last Medication recorded
update temp_mentalhealth_program tmh
LEFT JOIN
(
select pp.patient_id, patient_program_id, GROUP_CONCAT(cnd.name) "medication_names", date_enrolled, date_completed, date(encounter_datetime) enc_date from patient_program pp
INNER JOIN
encounter e on e.encounter_id =
    (select encounter_id from encounter e2 where
     e2.voided =0
     and e2.patient_id = pp.patient_id
     and e2.encounter_type = @encounter_type
	-- and date(e2.encounter_datetime) >= date(date_enrolled) or date(e2.encounter_datetime)  <= date(date_completed)
     and exists (select 1 from obs where encounter_id = e2.encounter_id and concept_id = @medication and voided = 0)
     order by e2.encounter_datetime desc
     limit 1)
INNER JOIN obs o on o.voided = 0 and o.concept_id = @medication and o.encounter_id = e.encounter_id
INNER JOIN drug cnd on cnd.drug_id  = o.value_drug
group by pp.patient_id
) medication
on medication.patient_program_id = tmh.patient_program_id
set tmh.latest_medication_given = medication.medication_names,
	tmh.latest_medication_date = medication.enc_date;

-- latest intervention
UPDATE temp_mentalhealth_program tmh
        LEFT JOIN
    (SELECT
        pp.patient_id,
            patient_program_id,
            GROUP_CONCAT(cnd.name) 'intervention',
            date_enrolled,
            date_completed,
            e.encounter_id enc_id,
            DATE(encounter_datetime) enc_date
    FROM
        patient_program pp
    INNER JOIN encounter e ON e.encounter_id = (SELECT
            encounter_id
        FROM
            encounter e2
        WHERE
            e2.voided = 0
                AND e2.patient_id = pp.patient_id
                AND e2.encounter_type = @encounter_type
                AND EXISTS( SELECT
                    1
                FROM
                    obs
                WHERE
                    encounter_id = e2.encounter_id
                        AND concept_id = @mh_intervention
                        AND voided = 0)
        ORDER BY e2.encounter_datetime DESC
        LIMIT 1)
    INNER JOIN obs o ON o.voided = 0
        AND o.concept_id = @mh_intervention
        AND o.encounter_id = e.encounter_id
    INNER JOIN concept_name cnd ON cnd.concept_name_id = (SELECT
            cnd2.concept_name_id
        FROM
            concept_name cnd2
        WHERE
            o.value_coded = cnd2.concept_id
                AND cnd2.voided = 0
        ORDER BY FIELD(cnd2.locale, 'fr', 'en', 'ht') , cnd2.locale_preferred DESC
        LIMIT 1)
    GROUP BY pp.patient_id) tli ON tli.patient_program_id = tmh.patient_program_id
SET
    tmh.latest_intervention = tli.intervention,
    tmh.other_intervention = (SELECT
            comments
        FROM
            obs o
        WHERE
            o.concept_id = @mh_intervention
                AND value_coded = @other_noncoded
                AND o.voided = 0
                AND o.encounter_id = tli.enc_id),
    tmh.last_intervention_date = tli.enc_date;

-- Last Visit Date
UPDATE temp_mentalhealth_program tmh
LEFT JOIN encounter e on e.encounter_id = tmh.encounter_id and e.encounter_type = @encounter_type and e.voided = 0
set tmh.last_visit_date = date(e.encounter_datetime);

-- Next Scheduled Visit Date
UPDATE temp_mentalhealth_program tmh
LEFT JOIN obs o on tmh.encounter_id = o.encounter_id and o.voided = 0 and concept_id = @return_visit_date
set tmh.next_scheduled_visit_date = date(o.value_datetime),
    tmh.patient_came_within_14_days_appt = IF(datediff(now(), tmh.last_visit_date) <= 14, 'Oui', 'No'),
    tmh.three_months_since_latest_return_date = IF(datediff(now(), tmh.last_visit_date) <= 91.2501, 'No', 'Oui'),
	tmh.six_months_since_latest_return_date = IF(datediff(now(), tmh.last_visit_date) <= 182.5, 'No', 'Oui');

select
patient_id,
zlemr,
gender,
age,
unknown_patient,
assigned_chw,
person_address_state_province(patient_id) 'province',
person_address_city_village(patient_id) 'city_village',
person_address_three(patient_id) 'address3',
person_address_one(patient_id) 'address1',
person_address_two(patient_id) 'address2',
location_when_registered_in_program,
date_enrolled,
date_completed,
number_of_days_in_care,
program_status_outcome,
latest_diagnosis,
latest_zlds_score,
recent_date_zlds_score,
previous_zlds_score,
previous_date_zlds_score,
baseline_zlds_score,
baseline_date_zlds_score,
latest_whodas_score,
recent_date_whodas_score,
previous_whodas_score,
previous_date_whodas_score,
baseline_whodas_score,
baseline_date_whodas_score,
latest_seizure_number,
latest_seizure_date,
previous_seizure_number,
previous_seizure_date,
baseline_seizure_number,
baseline_seizure_date,
latest_medication_given,
latest_medication_date,
latest_intervention,
other_intervention,
last_intervention_date,
last_visit_date,
next_scheduled_visit_date,
three_months_since_latest_return_date,
six_months_since_latest_return_date
from temp_mentalhealth_program;