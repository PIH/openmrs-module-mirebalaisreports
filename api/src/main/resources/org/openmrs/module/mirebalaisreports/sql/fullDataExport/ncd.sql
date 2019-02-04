SELECT 
    p.patient_id,
    zl.identifier zlemr,
    zl_loc.name loc_registered,
    un.value unknown_patient,
    pr.gender,
    ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate) / 365.25,
            1) age_at_enc,
    pa.state_province department,
    pa.city_village commune,
    pa.address3 section,
    pa.address1 locality,
    pa.address2 street_landmark,
    el.name encounter_location,
    CONCAT(pn.given_name, ' ', pn.family_name) provider,
    obsjoins.*
FROM
    patient p
-- Most recent ZL EMR ID
INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type =  :zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
-- ZL EMR ID location
INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
-- Unknown patient
LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = :unknownPt
            AND un.voided = 0
-- Gender
INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0
--  Most recent address
LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created DESC) n ON p.patient_id = n.person_id
INNER JOIN encounter e ON p.patient_id = e.patient_id AND e.voided = 0 AND e.encounter_type IN (:NCDInitEnc, :NCDFollowEnc, :vitEnc, :labResultEnc)
INNER JOIN location el ON e.location_id = el.location_id
--  Provider Name
INNER JOIN encounter_provider ep ON ep.encounter_id = e.encounter_id AND ep.voided = 0
INNER JOIN provider pv ON pv.provider_id = ep.provider_id
INNER JOIN person_name pn ON pn.person_id = pv.person_id AND pn.voided = 0
-- Straight Obs Joins
INNER JOIN
(SELECT 
     o.encounter_id,
    (select date_started from visit where visit_id = e.visit_id) visit_date,
    visit_id,
    MAX(DATE(CASE
            WHEN e.encounter_id = o.encounter_id THEN e.encounter_datetime
        END)) 'encounter_date',
-- Encounter Type
	GROUP_CONCAT(DISTINCT(CASE WHEN e.encounter_id = o.encounter_id THEN (SELECT name FROM encounter_type WHERE encounter_type_id = e.encounter_type) END) 
    SEPARATOR ', ') visit_type,
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Type of referring service'
        THEN
            cn.name
    END) 'Type_of_referring_service',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Known chronic disease before referral'
        THEN
            cn.name
    END) 'Known_disease_before_referral',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Prior treatment for chronic disease'
        THEN
            cn.name
    END) 'Prior_treatment',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Chronic disease controlled during initial visit'
        THEN
            cn.name
    END) 'Disease_controlled_initial_visit',
    GROUP_CONCAT(CASE
            WHEN
                rm.source = 'PIH'
                    AND rm.code = 'NCD category'
            THEN
                cn.name
        END
        SEPARATOR ', ') 'NCD_category',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'NCD category'
        THEN
            o.comments
    END) 'Other_NCD_category',
    MAX(CASE
        WHEN rm.source = 'CIEL' AND rm.code = '5089' THEN o.value_numeric
    END) 'Weight_kg',
    MAX(CASE
        WHEN rm.source = 'CIEL' AND rm.code = '5090' THEN o.value_numeric
    END) 'Height_cm',
    ROUND(MAX(CASE
                WHEN rm.source = 'CIEL' AND rm.code = '5089' THEN o.value_numeric
            END) / ((MAX(CASE
                WHEN rm.source = 'CIEL' AND rm.code = '5090' THEN o.value_numeric
            END) / 100) * (MAX(CASE
                WHEN rm.source = 'CIEL' AND rm.code = '5090' THEN o.value_numeric
            END) / 100)),
            1) 'BMI',
    MAX(CASE
        WHEN rm.source = 'CIEL' AND rm.code = '5085' THEN o.value_numeric
    END) 'Systolic_BP',
    MAX(CASE
        WHEN rm.source = 'CIEL' AND rm.code = '5086' THEN o.value_numeric
    END) 'Diastolic_BP',
    MAX(CASE
        WHEN
            rm.source = 'CIEL'
                AND rm.code = '163080'
        THEN
            o.value_numeric
    END) 'Waist_cm',
    MAX(CASE
        WHEN
            rm.source = 'CIEL'
                AND rm.code = '163081'
        THEN
            o.value_numeric
    END) 'hip_cm',
    ROUND(MAX(CASE
                WHEN
                    rm.source = 'CIEL'
                        AND rm.code = '163080'
                THEN
                    o.value_numeric
            END) / MAX(CASE
                WHEN
                    rm.source = 'CIEL'
                        AND rm.code = '163081'
                THEN
                    o.value_numeric
            END),
            2) 'Waist/Hip Ratio',
    GROUP_CONCAT(CASE
            WHEN
                rm.source = 'PIH'
                    AND rm.code = 'NYHA CLASS'
            THEN
                cn.name
        END
        SEPARATOR ',') 'NYHA_CLASS',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'PATIENTS FLUID MANAGEMENT'
        THEN
            cn.name
    END) 'Patients_Fluid_Management',
    GROUP_CONCAT(CASE
            WHEN
                rm.source = 'PIH'
                    AND rm.code = 'Type of diabetes diagnosis'
            THEN
                cn.name
        END
        SEPARATOR ', ') 'Diabetes_type',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Hypoglycemia symptoms'
        THEN
            cn.name
    END) 'Hypoglycemia_symptoms',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Puffs per week of salbutamol'
        THEN
            o.value_numeric
    END) 'Puffs_week_salbutamol',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Asthma classification'
        THEN
            cn.name
    END) 'Asthma_classification',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Number of seizures since last visit'
        THEN
            o.value_numeric
    END) 'Number_seizures_since_last_visit',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Appearance at appointment time'
        THEN
            cn.name
    END) 'Adherance_to_appointment',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Lack of meds in last 2 days'
        THEN
            cn.name
    END) 'Lack_of_meds_2_days',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'PATIENT HOSPITALIZED SINCE LAST VISIT'
        THEN
            cn.name
    END) 'Patient_hospitalized_since_last_visit',
    GROUP_CONCAT(CASE
            WHEN
                rm.source = 'PIH'
                    AND rm.code = 'Medications prescribed at end of visit'
            THEN
                cn.name
        END
        SEPARATOR ',') 'Medications_Prescribed',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'Medications prescribed at end of visit'
        THEN
            o.comments
    END) 'Other_meds',
    MAX(CASE
        WHEN
            rm.source = 'CIEL'
                AND rm.code = '159644'
        THEN
            o.value_numeric
    END) 'HbA1c',
    MAX(CASE
        WHEN
            rm.source = 'PIH'
                AND rm.code = 'PATIENT PLAN COMMENTS'
        THEN
            o.value_text
    END) 'Patient_Plan_Comments',
     MAX(CASE
         WHEN
             rm.source = 'PIH'
             AND rm.code = 'RETURN VISIT DATE'
             THEN
                 o.value_datetime
         END) 'Next_NCD_appointment'
FROM encounter e, report_mapping rm, obs o
LEFT OUTER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.locale_preferred = '1'  AND cn.voided = 0
LEFT OUTER JOIN obs obs2 ON obs2.obs_id = o.obs_group_id
LEFT OUTER JOIN report_mapping obsgrp ON obsgrp.concept_id = obs2.concept_id
WHERE 1=1
AND
e.encounter_id IN
(   
   SELECT e3.encounter_id
   FROM encounter e3
     INNER JOIN
    (SELECT visit_id, encounter_type, MAX(encounter_datetime) AS enc_date
    FROM encounter
     WHERE 1=1
     AND encounter_type IN (:NCDInitEnc, :NCDFollowEnc, :vitEnc, :labResultEnc)
      GROUP BY visit_id,encounter_type) maxdate
     ON maxdate.visit_id = e3.visit_id AND e3.encounter_type= maxdate.encounter_type AND e3.encounter_datetime = maxdate.enc_date
)
AND rm.concept_id = o.concept_id
AND o.encounter_id = e.encounter_id
AND e.voided = 0
AND o.voided = 0
GROUP BY e.visit_id
) obsjoins ON obsjoins.encounter_id = ep.encounter_id
--  end columns joins
WHERE p.voided = 0
-- exclude test patients
AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt
                         AND voided = 0)
-- Remove all the empty ncd forms.
AND e.visit_id IN (SELECT enc.visit_id FROM encounter enc WHERE encounter_type IN (:NCDInitEnc, :NCDFollowEnc)
AND enc.encounter_id IN (SELECT obs.encounter_id FROM obs JOIN encounter ON
 patient_id = person_id AND encounter_type IN (:NCDInitEnc, :NCDFollowEnc) AND obs.voided = 0))
AND date(e.encounter_datetime) >= date(:startDate )
AND date(e.encounter_datetime) <= date(:endDate )
GROUP BY e.encounter_id ORDER BY p.patient_id
;
