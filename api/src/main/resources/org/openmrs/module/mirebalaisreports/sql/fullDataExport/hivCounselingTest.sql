SELECT
    CONCAT(family_name, ' ', given_name) AS 'PRÉNOM NOM (SURNOMS)',
    pa.value AS 'PRÉNOM DE LA MÈRE',
    commune AS 'Commune',
    section_communal AS 'Sect. Comm',
    (SELECT
            pa.value
        FROM
            person_attribute pa
        WHERE
            person_attribute_type_id = (SELECT
                    person_attribute_type_id
                FROM
                    person_attribute_type
                WHERE
                    name = 'Telephone Number')
                AND pa.person_id = e.patient_id) AS 'Adresse/Tél',
    birthdate 'DATE DE NAISSANCE',
    gender AS 'SEXE',
    CAST(CONCAT(TIMESTAMPDIFF(YEAR, birthdate, NOW()),
                '.',
                MOD(TIMESTAMPDIFF(MONTH, birthdate, NOW()),
                    12))
        AS CHAR) AS AGE,
    category_hiv_screen.result AS 'MOTIF DE DEPISTAGE (#1 - #8)',
    reason_hiv_screen.notes AS 'NOTES',
    (SELECT
            name
        FROM
            concept_name
        WHERE
            concept_id = o.value_coded
                AND locale = 'fr'
                AND concept_name_type = 'SHORT'
                AND voided = 0) AS 'VIOLENCE PHYSIQUE ET/OU EMOTIONNELLE',
    DATE(prcd.value_datetime) AS 'DATE PRÉ-TEST COUNSELING',
    DATE(hivd.value_datetime) AS 'DATE TEST VIH',
    (SELECT
            name
        FROM
            concept_name
        WHERE
            concept_id = hivr.value_coded
                AND locale = 'fr'
                AND concept_name_type = 'FULLY_SPECIFIED'
                AND voided = 0) AS 'RÉSULTAT TEST VIH',
    DATE(hivrd.value_datetime) AS 'DATE RESULTAT VIH',
    DATE(rprd.value_datetime) AS 'DATE TEST RPR',
    (SELECT
            name
        FROM
            concept_name
        WHERE
            concept_id = rpr.value_coded
                AND locale = 'fr'
                AND concept_name_type = 'FULLY_SPECIFIED'
                AND voided = 0) AS 'RÉSULTAT TEST RPR',
    DATE(rprrd.value_datetime) AS 'DATE RESULTAT RPR',
    DATE(postd.value_datetime) AS 'DATE POST-TEST COUNSELING',
    DATE(tbd.obs_datetime) AS 'DATE ÉVALUATION TB',
    tb_evaluation.tb_result AS 'TB EVALUATION',
    DATE(rxrpr.value_datetime) AS 'DATE DEBUT Rx RPR',
    DATE(rxrprd.value_datetime) AS 'DATE FIN Rx RPR',
    DATE(rspec.value_datetime) AS 'DATE RÉFÉRENCE SERVICE PRISE EN CHARGE',
    DATE(pres.value_datetime) AS 'DATE PROPHYLAXIE AES',
    DATE(preas.value_datetime) AS 'DATE PROPHYLAXIE AGRESSION SEXUELLE',
    cmnt.value_text AS 'REMARQUES'
FROM
    encounter e
        INNER JOIN
    current_name_address cna ON cna.person_id = e.patient_id
        AND date(e.encounter_datetime) >= :startDate
  	    AND date(e.encounter_datetime) <= :endDate
        AND e.form_id = (SELECT
            form_id
        FROM
            form
        WHERE
            uuid = '616b6b36-f189-11e7-8c3f-9a214cf093ae')
        INNER JOIN
    person_attribute pa ON pa.person_id = e.patient_id
        AND pa.person_attribute_type_id = (SELECT
            person_attribute_type_id
        FROM
            person_attribute_type
        WHERE
            name = 'First Name of Mother')
        LEFT JOIN
    (SELECT
        person_id,
            encounter_id,
            GROUP_CONCAT(name
                SEPARATOR ', ') AS result
    FROM
        concept_name cn
    JOIN obs o ON cn.concept_id = o.value_coded
        AND locale = 'fr'
        AND concept_name_type = 'FULLY_SPECIFIED'
        AND cn.voided = 0
        AND o.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'CIEL' AND rm.code = 164082)
    GROUP BY o.encounter_id) category_hiv_screen ON category_hiv_screen.person_id = e.patient_id
        AND e.encounter_id = category_hiv_screen.encounter_id
        LEFT JOIN
    (SELECT
        person_id,
            encounter_id,
            GROUP_CONCAT(name
                SEPARATOR ', ') AS notes
    FROM
        concept_name cn
    JOIN obs o ON cn.concept_id = o.value_coded
        AND locale = 'fr'
        AND concept_name_type = 'FULLY_SPECIFIED'
        AND cn.voided = 0
        AND o.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 11535)
    GROUP BY o.encounter_id) reason_hiv_screen ON reason_hiv_screen.person_id = e.patient_id
        AND e.encounter_id = reason_hiv_screen.encounter_id
        LEFT JOIN
    obs o ON o.person_id = e.patient_id
        AND o.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 8849)
        AND value_coded = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'CIEL' AND rm.code = 165088)
        AND o.voided = 0
        AND e.encounter_id = o.encounter_id
        LEFT JOIN
    obs prcd ON prcd.person_id = e.patient_id
        AND prcd.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 11577)
        AND prcd.voided = 0
        AND e.encounter_id = prcd.encounter_id
        LEFT JOIN
    obs hivd ON hivd.person_id = e.patient_id
        AND hivd.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'CIEL' AND rm.code = 164400)
        AND hivd.voided = 0
        AND e.encounter_id = hivd.encounter_id
        LEFT JOIN
    obs hivr ON hivr.person_id = e.patient_id
        AND hivr.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'CIEL' AND rm.code = 163722)
        AND hivd.voided = 0
        AND e.encounter_id = hivr.encounter_id
        LEFT JOIN
    obs hivrd ON hivrd.person_id = e.patient_id
        AND hivrd.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'CIEL' AND rm.code = 160082)
        AND hivrd.voided = 0
        AND e.encounter_id = hivrd.encounter_id
        LEFT JOIN
    obs rprd ON rprd.person_id = e.patient_id
        AND rprd.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 3267)
        AND rprd.voided = 0
        AND e.encounter_id = rprd.encounter_id
        AND rprd.obs_group_id IN (SELECT
            obs_id
        FROM
            obs
        WHERE
            concept_id = (SELECT
                    concept_id
                FROM
                    report_mapping rm
                WHERE
                    rm.source = 'PIH' AND rm.code = 11523)
                AND voided = 0)
        LEFT JOIN
    obs rpr ON rpr.person_id = e.patient_id
        AND rpr.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 1478)
        AND rpr.voided = 0
        AND e.encounter_id = rpr.encounter_id
        AND rpr.obs_group_id IN (SELECT
            obs_id
        FROM
            obs
        WHERE
            concept_id = (SELECT
                    concept_id
                FROM
                    report_mapping rm
                WHERE
                    rm.source = 'PIH' AND rm.code = 11523)
                AND voided = 0)
        LEFT JOIN
    obs rprrd ON rprrd.person_id = e.patient_id
        AND rprrd.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 10783)
        AND rprrd.voided = 0
        AND e.encounter_id = rprrd.encounter_id
        AND rprrd.obs_group_id IN (SELECT
            obs_id
        FROM
            obs
        WHERE
            concept_id = (SELECT
                    concept_id
                FROM
                    report_mapping rm
                WHERE
                    rm.source = 'PIH' AND rm.code = 11523)
                AND voided = 0)
        LEFT JOIN
    obs postd ON postd.person_id = e.patient_id
        AND postd.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 11525)
        AND postd.voided = 0
        AND e.encounter_id = postd.encounter_id
        LEFT JOIN
    obs tbd ON tbd.person_id = e.patient_id
        AND tbd.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 11541)
        AND tbd.voided = 0
        AND e.encounter_id = tbd.encounter_id
        LEFT JOIN
    (SELECT
        person_id,
            encounter_id,
            GROUP_CONCAT(name
                SEPARATOR ', ') AS tb_result
    FROM
        concept_name cn
    JOIN obs otb ON cn.concept_id = otb.value_coded
        AND locale = 'fr'
        AND concept_name_type = 'FULLY_SPECIFIED'
        AND cn.voided = 0
        AND otb.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 11563)
    GROUP BY otb.encounter_id) tb_evaluation ON tb_evaluation.person_id = e.patient_id
        AND e.encounter_id = tb_evaluation.encounter_id
        LEFT JOIN
    obs rxrpr ON rxrpr.person_id = e.patient_id
        AND rxrpr.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 11536)
        AND rxrpr.voided = 0
        AND e.encounter_id = rxrpr.encounter_id
        LEFT JOIN
    obs rxrprd ON rxrprd.person_id = e.patient_id
        AND rxrprd.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 11537)
        AND rxrprd.voided = 0
        AND e.encounter_id = rxrprd.encounter_id
        LEFT JOIN
    obs rspec ON rspec.person_id = e.patient_id
        AND rspec.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 11538)
        AND rspec.voided = 0
        AND e.encounter_id = rspec.encounter_id
        LEFT JOIN
    obs pres ON pres.person_id = e.patient_id
        AND pres.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 11539)
        AND pres.voided = 0
        AND e.encounter_id = pres.encounter_id
        LEFT JOIN
    obs preas ON preas.person_id = e.patient_id
        AND preas.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'PIH' AND rm.code = 11540)
        AND preas.voided = 0
        AND e.encounter_id = preas.encounter_id
        LEFT JOIN
    obs cmnt ON cmnt.person_id = e.patient_id
        AND cmnt.concept_id = (SELECT
            concept_id
        FROM
            report_mapping rm
        WHERE
            rm.source = 'CIEL' AND rm.code = 162749)
        AND cmnt.voided = 0
        AND e.encounter_id = cmnt.encounter_id
ORDER BY cna.family_name;
