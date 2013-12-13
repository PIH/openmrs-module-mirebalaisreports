select distinct pe.given_name,
                u.username,
                u.retired as "user_status",
                u.date_created,
                (select group_concat(role) from user_role where user_id = u.user_id ) as roles
           from person_name pe

           inner join users u
           on u.person_id=pe.person_id

           inner join user_role ur
           on ur.user_id = u.user_id

           inner join provider p
           on u.person_id = p.person_id

           inner join providermanagement_provider_role pp
           on pp.provider_role_id = p.provider_role_id;