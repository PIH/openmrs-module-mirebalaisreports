DROP TEMPORARY TABLE IF EXISTS temp_social_econ;

set @encounter_type = encounter_type('Socio-economics');
set @education = concept_with_mapping('CIEL','1712');
set @educationAnswers = concept_name(@education);
set @people_living_in_house = concept_with_mapping('CIEL','1474');
set @number_of_children_living = concept_with_mapping('CIEL','1825');
set @number_of_rooms = concept_with_mapping('CIEL','1475');
set @radio = concept_with_mapping('PIH','1318');
set @television = concept_with_mapping('CIEL','159746');
set @fridge = concept_with_mapping('CIEL','159745');
set @bank_account = concept_with_mapping('PIH','11936');
set @toilet = concept_with_mapping('CIEL','159389');
set @latrine = concept_with_mapping('PIH','Latrine');
set @floor = concept_with_mapping('CIEL','159387');
set @roof = concept_with_mapping('CIEL','1290');
set @walls = concept_with_mapping('PIH','1668');
set @transport_method_to_clinic = concept_with_mapping('PIH','975');
set @cost_of_transport = concept_with_mapping('CIEL','159470');
set @travel_time_to_clinic = concept_with_mapping('PIH','CLINIC TRAVEL TIME');
set @main_daily_activities_before_illness = concept_with_mapping('PIH','1402');
set @ability_to_perform_main_daily_activity_since_illness = concept_with_mapping('PIH','11543');
set @recieved_assistance = concept_with_mapping('PIH','SOCIO-ECONOMIC ASSISTANCE ALREADY RECEIVED');
set @recommended_assistance = concept_with_mapping('PIH','SOCIO-ECONOMIC ASSISTANCE RECOMMENDED');
set @other_recommended_or_recieved_assistance_name = concept_with_mapping('PIH','SOCIO-ECONOMIC ASSISTANCE NON-CODED');
set @other = concept_with_mapping('PIH','OTHER');
set @socio_economic_assistance_comment = concept_with_mapping('PIH', 'SOCIO-ECONOMIC ASSISTANCE COMMENT');
set @undernourishment = concept_with_mapping('CIEL', '165491');
set @infant_mortality = concept_with_mapping('CIEL', '165492');
set @completed_six_years_schooling = concept_with_mapping('CIEL', '165493');
set @not_attending_school = concept_with_mapping('CIEL', '165494');
set @cooks_with_dung_wood_charcoal_or_coal = concept_with_mapping('CIEL', '165495');
set @household_sanitation_improvement = concept_with_mapping('CIEL', '165496');
set @improved_drinking_water = concept_with_mapping('CIEL', '165497');
set @electricity = concept_with_mapping('CIEL', '165498');
set @inadequate_housing_materials = concept_with_mapping('CIEL', '165499');
set @household_doesnot_own_assetts = concept_with_mapping('CIEL', '165500');

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
age_at_enc double,
education_answer_concept int,
education varchar(50),
people_living_in_house double,
number_of_children_living double,
number_of_rooms double,
radio varchar(50),
television varchar(50),
fridge varchar(50),
bank_account varchar(50),
toilet varchar(50),
latrine varchar(50),
floor varchar(255),
roof varchar(255),
walls varchar(255),
transport_method_to_clinic varchar(50),
cost_of_transport double,
travel_time_to_clinic varchar(50),
main_daily_activities_before_illness text,
ability_to_perform_main_daily_activity_since_illness text,
recieved_assistance text,
recommended_assistance text,
other_recommended_or_recieved_assistance_name text,
recieved_other_assistance varchar(50),
recommended_other_assistance varchar(50),
socio_economic_assistance_comment text,
undernourishment varchar(255),
infant_mortality varchar(255),
completed_six_years_schooling varchar(255),
not_attending_school varchar(255),
cooks_with_dung_wood_charcoal_or_coal varchar(255),
household_sanitation_improvement varchar(255),
improved_drinking_water varchar(255),
electricity varchar(255),
inadequate_housing_materials varchar(255),
household_doesnot_own_assetts varchar(255)
);

insert into temp_social_econ (patient_id, zl_emr_id, gender, encounter_id, encounter_date, age_at_enc, provider, patient_address, loc_registered, location_id)
select patient_id, zlemr(patient_id), gender(patient_id), encounter_id,  encounter_datetime, age_at_enc(patient_id), provider(encounter_id), person_address(patient_id),
loc_registered(patient_id), location_id
 from encounter where voided = 0 and encounter_type = @encounter_type
-- exclude test patients
AND patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = (select
person_attribute_type_id from person_attribute_type where name = "Test Patient")
                         AND voided = 0)
-- filter by date
 AND date(encounter_datetime) >= date(@startDate)
 AND date(encounter_datetime) <= date(@endDate)
 ;

-- unknown patient

update temp_social_econ tsn
set tsn.unknown_patient = IF(tsn.patient_id = unknown_patient(tsn.patient_id), 'true', NULL);

update temp_social_econ tsn
left join location l on tsn.location_id = l.location_id
set tsn.enc_location = l.name;


-- EDUCATION and HOUSING
update temp_social_econ tsn
left join obs o on tsn.encounter_id = o.encounter_id and voided = 0
set tsn.education_answer_concept = o.value_coded;

update temp_social_econ tsn
set education = concept_name(education_answer_concept);

update temp_social_econ tsn
left join obs o1 on tsn.encounter_id = o1.encounter_id and voided = 0 and concept_id = @people_living_in_house
set tsn.people_living_in_house = o1.value_numeric;

update temp_social_econ tsn
left join obs o2 on tsn.encounter_id = o2.encounter_id and voided = 0 and concept_id = @number_of_children_living
set tsn.number_of_children_living = o2.value_numeric;

update temp_social_econ tsn
left join obs o3 on tsn.encounter_id = o3.encounter_id and voided = 0 and concept_id = @number_of_rooms
set tsn.number_of_rooms = o3.value_numeric;

update temp_social_econ tsn
left join obs o4 on tsn.encounter_id = o4.encounter_id and voided = 0 and concept_id = @radio
set tsn.radio = concept_name(o4.value_coded);

update temp_social_econ tsn
left join obs o5 on tsn.encounter_id = o5.encounter_id and voided = 0 and concept_id = @television
set tsn.television = concept_name(o5.value_coded);

update temp_social_econ tsn
left join obs o6 on tsn.encounter_id = o6.encounter_id and voided = 0 and concept_id = @fridge
set tsn.fridge = concept_name(o6.value_coded);

update temp_social_econ tsn
left join obs o7 on tsn.encounter_id = o7.encounter_id and voided = 0 and concept_id = @bank_account
set tsn.bank_account = concept_name(o7.value_coded);

update temp_social_econ tsn
left join obs o8 on tsn.encounter_id = o8.encounter_id and voided = 0 and concept_id = @toilet
set tsn.toilet = concept_name(o8.value_coded);

update temp_social_econ tsn
left join obs o9 on tsn.encounter_id = o9.encounter_id and voided = 0 and concept_id = @latrine
set tsn.latrine = concept_name(o9.value_coded);

update temp_social_econ tsn
left join obs o10 on tsn.encounter_id = o10.encounter_id and voided = 0 and concept_id = @floor
set tsn.floor = concept_name(o10.value_coded);

update temp_social_econ tsn
left join obs o11 on tsn.encounter_id = o11.encounter_id and voided = 0 and concept_id = @roof
set tsn.roof = concept_name(o11.value_coded);

update temp_social_econ tsn
left join obs o12 on tsn.encounter_id = o12.encounter_id and voided = 0 and concept_id = @walls
set tsn.walls = concept_name(o12.value_coded);

-- TRANSPORTATION
update temp_social_econ tsn
left join obs o on tsn.encounter_id = o.encounter_id and voided = 0 and concept_id = @transport_method_to_clinic
set tsn.transport_method_to_clinic = concept_name(o.value_coded);

update temp_social_econ tsn
left join obs o1 on tsn.encounter_id = o1.encounter_id and voided = 0 and concept_id = @cost_of_transport
set tsn.cost_of_transport = o1.value_numeric;

update temp_social_econ tsn
left join obs o2 on tsn.encounter_id = o2.encounter_id and voided = 0 and concept_id = @travel_time_to_clinic
set tsn.travel_time_to_clinic = concept_name(o2.value_coded);

-- DAILY ACTIVITIES
update temp_social_econ tsn
left join obs o on tsn.encounter_id = o.encounter_id and voided = 0 and concept_id = @main_daily_activities_before_illness
set tsn.main_daily_activities_before_illness = o.value_text;

update temp_social_econ tsn
left join obs o1 on tsn.encounter_id = o1.encounter_id and voided = 0 and concept_id = @ability_to_perform_main_daily_activity_since_illness
set tsn.ability_to_perform_main_daily_activity_since_illness = o1.value_text;

-- ASSISTANCE
update temp_social_econ tsn
left join (
select encounter_id,  group_concat(name) names  from obs o join concept_name cn on cn.concept_id = o.value_coded and cn.voided = 0
and o.voided = 0 and o.concept_id = @recieved_assistance and cn.locale = "en" and concept_name_type = "FULLY_SPECIFIED"
group by encounter_id
) o1 on tsn.encounter_id = o1.encounter_id
set tsn.recieved_assistance = o1.names;

update temp_social_econ tsn
left join (
select encounter_id, o.concept_id concept_id,  value_coded, group_concat(name) names  from obs o join concept_name cn on cn.concept_id = o.value_coded and cn.voided = 0
and o.voided = 0 and o.concept_id = @recommended_assistance and cn.locale = "en" and concept_name_type = "FULLY_SPECIFIED"
group by encounter_id
) o1 on tsn.encounter_id = o1.encounter_id
set tsn.recommended_assistance = o1.names;


-- this is not model the way it appears on the paper form
update temp_social_econ tsn
left join
obs o1 on o1.concept_id = @other_recommended_or_recieved_assistance_name and voided = 0 and tsn.encounter_id = o1.encounter_id
set tsn.other_recommended_or_recieved_assistance_name = o1.value_text;


update temp_social_econ tsn
left join
obs o2 on o2.encounter_id = tsn.encounter_id and o2.voided = 0 and o2.concept_id = @recieved_assistance and o2.value_coded = @other
set tsn.recieved_other_assistance = IF(o2.value_coded = @other, "Yes", NULL);

update temp_social_econ tsn
left join
obs o3 on o3.encounter_id = tsn.encounter_id and o3.voided = 0 and o3.concept_id = @recommended_assistance and value_coded = @other
set tsn.recommended_other_assistance =  IF(o3.value_coded = @other, "Yes", NULL);

-- socio_economic_assistance_comment
update temp_social_econ tsn
left join obs o on o.encounter_id = tsn.encounter_id and voided = 0 and o.concept_id = @socio_economic_assistance_comment
set tsn.socio_economic_assistance_comment = o.value_text;

-- GLOBAL MULTIDIMENSIONAL POVERTY INDEX (MPI 2018)

update temp_social_econ tsn
left join
obs o on o.encounter_id = tsn.encounter_id and o.voided = 0 and o.concept_id = @undernourishment
set tsn.undernourishment = concept_name(o.value_coded);

update temp_social_econ tsn
left join
obs o1 on o1.encounter_id = tsn.encounter_id and o1.voided = 0 and o1.concept_id = @infant_mortality
set tsn.infant_mortality = concept_name(o1.value_coded);

update temp_social_econ tsn
left join
obs o2 on o2.encounter_id = tsn.encounter_id and o2.voided = 0 and o2.concept_id = @completed_six_years_schooling
set tsn.completed_six_years_schooling = concept_name(o2.value_coded);

update temp_social_econ tsn
left join
obs o3 on o3.encounter_id = tsn.encounter_id and o3.voided = 0 and o3.concept_id = @not_attending_school
set tsn.not_attending_school = concept_name(o3.value_coded);

update temp_social_econ tsn
left join
obs o4 on o4.encounter_id = tsn.encounter_id and o4.voided = 0 and o4.concept_id = @cooks_with_dung_wood_charcoal_or_coal
set tsn.cooks_with_dung_wood_charcoal_or_coal = concept_name(o4.value_coded);

update temp_social_econ tsn
left join
obs o5 on o5.encounter_id = tsn.encounter_id and o5.voided = 0 and o5.concept_id = @household_sanitation_improvement
set tsn.household_sanitation_improvement = concept_name(o5.value_coded);

update temp_social_econ tsn
left join
obs o6 on o6.encounter_id = tsn.encounter_id and o6.voided = 0 and o6.concept_id = @improved_drinking_water
set tsn.improved_drinking_water = concept_name(o6.value_coded);

update temp_social_econ tsn
left join
obs o7 on o7.encounter_id = tsn.encounter_id and o7.voided = 0 and o7.concept_id = @electricity
set tsn.electricity = concept_name(o7.value_coded);

update temp_social_econ tsn
left join
obs o8 on o8.encounter_id = tsn.encounter_id and o8.voided = 0 and o8.concept_id = @inadequate_housing_materials
set tsn.inadequate_housing_materials = concept_name(o8.value_coded);

update temp_social_econ tsn
left join
obs o9 on o9.encounter_id = tsn.encounter_id and o9.voided = 0 and o9.concept_id = @household_doesnot_own_assetts
set tsn.household_doesnot_own_assetts = concept_name(o9.value_coded);


select
patient_id,
zl_emr_id,
gender,
unknown_patient,
patient_address,
provider,
loc_registered,
enc_location,
encounter_id,
encounter_date,
age_at_enc,
education,
people_living_in_house,
number_of_children_living,
number_of_rooms,
radio,
television,
fridge,
bank_account,
toilet,
latrine,
floor,
roof,
walls,
transport_method_to_clinic,
cost_of_transport,
travel_time_to_clinic,
main_daily_activities_before_illness,
ability_to_perform_main_daily_activity_since_illness,
recieved_assistance,
recommended_assistance,
other_recommended_or_recieved_assistance_name,
recieved_other_assistance,
recommended_other_assistance,
socio_economic_assistance_comment,
undernourishment,
infant_mortality,
completed_six_years_schooling,
not_attending_school,
cooks_with_dung_wood_charcoal_or_coal,
household_sanitation_improvement,
improved_drinking_water,
electricity,
inadequate_housing_materials,
household_doesnot_own_assetts
from temp_social_econ;