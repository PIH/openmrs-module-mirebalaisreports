SELECT p.patient_id, zl.identifier zlemr, pr.gender, ROUND(DATEDIFF(e.encounter_datetime, pr.birthdate)/365.25, 1) age_at_enc, e.encounter_id, el.name, o.obs_id, o.obs_datetime, CONCAT(pn.given_name, ' ', pn.family_name) provider, IF(o.concept_id = 357, dn.name, o.value_text) diagnosis_entered, IF(o.concept_id = 357, TRUE, FALSE) coded, o.value_coded diagnosis_concept, en.name diagnosis_coded_en, icd.code icd10_code,

										 IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = :notifiable), TRUE, FALSE) notifiable,
										 IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = :urgent), TRUE, FALSE) urgent,
										 IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = :santeFamn), TRUE, FALSE) santeFamn,
										 IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = :psycho), TRUE, FALSE) psychological,
										 IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = :peds), TRUE, FALSE) pediatric,
										 IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = :outpatient), TRUE, FALSE) outpatient,
										 IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = :ncd), TRUE, FALSE) ncd,
										 IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = :notDx), TRUE, FALSE) non_diagnosis,
										 IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = :ed), TRUE, FALSE) ed,
										 IF(o.value_coded IN(SELECT concept_id FROM concept_set WHERE concept_set = :ageRst), TRUE, FALSE) age_restricted

FROM patient p

	INNER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = :zlId AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id

	INNER JOIN person pr ON p.patient_id = pr.person_id AND pr.voided = 0

	INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY date_created desc) n ON p.patient_id = n.person_id

	INNER JOIN encounter e ON p.patient_id = e.patient_id and e.voided = 0 AND e.encounter_type = :consEnc

	INNER JOIN users u ON e.creator = u.user_id

	INNER JOIN person_name pn ON u.person_id = pn.person_id AND pn.voided = 0

	INNER JOIN location el ON e.location_id = el.location_id

	INNER JOIN obs o ON e.encounter_id = o.encounter_id AND o.voided = 0 AND o.concept_id IN (:coded, :noncoded)

	LEFT OUTER JOIN concept_name dn ON o.value_coded_name_id = dn.concept_name_id

	LEFT OUTER JOIN concept_name en ON o.value_coded = en.concept_id AND en.locale = 'en' AND en.concept_name_type = 'FULLY_SPECIFIED' AND en.voided = 0

	LEFT OUTER JOIN (SELECT crm.concept_id, crt.code FROM concept_reference_map crm INNER JOIN concept_reference_term crt ON crm.concept_reference_term_id = crt.concept_reference_term_id AND crt.concept_source_id = (SELECT concept_source_id FROM concept_reference_source WHERE uuid = '3f65bd34-26fe-102b-80cb-0017a47871b2') AND crt.retired = 0) icd ON o.value_coded = icd.concept_id

WHERE p.voided = 0

			AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = :testPt AND voided = 0)

			AND o.obs_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)

GROUP BY o.obs_id

;
