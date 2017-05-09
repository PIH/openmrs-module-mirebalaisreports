select person_a "person_a_id",  CONCAT(pn_a.given_name, ' ',pn_a.family_name) "person_a_name", zl_a.identifier "person_a_zl_emr_id",
 rt.a_is_to_b,
person_b "person_b_id",
CONCAT(pn_b.given_name, ' ',pn_b.family_name) "person_b_name",
zl_b.identifier "person_b_zl_emr_id",
r.start_date,
r.date_created
from relationship r
INNER JOIN relationship_type rt on r.relationship = rt.relationship_type_id
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY preferred desc, date_created desc) pn_a
   ON pn_a.person_id = r.person_a
INNER JOIN (SELECT person_id, given_name, family_name FROM person_name WHERE voided = 0 ORDER BY preferred desc, date_created desc) pn_b
   ON pn_b.person_id = r.person_b
LEFT OUTER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = :zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl_a ON r.person_a = zl_a.patient_id
LEFT OUTER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = :zlId
            AND voided = 0 AND preferred = 1 ORDER BY date_created DESC) zl_b ON r.person_b = zl_b.patient_id
where r.voided = 0;
