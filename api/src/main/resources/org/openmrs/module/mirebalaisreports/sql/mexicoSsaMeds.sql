SELECT
    d.name "Medicamento",
    SUM(disp_quant.value_numeric) "Cantidad"
FROM obs disp_quant
         JOIN obs disp_drug
              ON disp_drug.obs_group_id = disp_quant.obs_group_id
                  AND disp_drug.concept_id = (
                      SELECT concept_id
                      FROM concept_reference_map
                      WHERE concept_reference_term_id = (
                          SELECT crt.concept_reference_term_id
                          FROM concept_reference_term crt
                                   JOIN concept_reference_source crs
                                        ON crt.concept_source_id = crs.concept_source_id
                          WHERE crt.code LIKE "medication orders"
                            AND crs.name LIKE "PIH"
                      )
                  )
         INNER JOIN drug d
                    ON disp_drug.value_drug = d.drug_id
WHERE disp_quant.concept_id = (
    SELECT concept_id
    FROM concept_reference_map
    WHERE concept_reference_term_id = (
        SELECT crt.concept_reference_term_id
        FROM concept_reference_term crt
                 JOIN concept_reference_source crs
                      ON crt.concept_source_id = crs.concept_source_id
        WHERE crt.code LIKE "1443"
          AND crs.name LIKE "CIEL"
    )
)
  AND date(disp_quant.obs_datetime) >= :startDate
  AND date(disp_quant.obs_datetime) <= :endDate
  AND d.name LIKE "SSA: %"
GROUP BY d.drug_id;

