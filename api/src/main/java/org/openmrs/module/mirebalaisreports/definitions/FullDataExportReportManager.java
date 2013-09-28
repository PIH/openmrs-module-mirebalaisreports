/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.Encounter;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.User;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.mirebalaisreports.data.converter.CountConverter;
import org.openmrs.module.mirebalaisreports.data.converter.MostRecentlyCreatedConverter;
import org.openmrs.module.reporting.common.Birthdate;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.common.VitalStatus;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.EncountersForPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.AgeAtDateOfOtherDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredAddressDataDefinition;
import org.openmrs.module.reporting.data.person.definition.VitalStatusDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for defining the full data export report
 * @see FullDataExportBuilder
 */
public class FullDataExportReportManager extends BaseMirebalaisReportManager {

	//***** CONSTANTS *****

	public static final String SQL_DIR = "org/openmrs/module/mirebalaisreports/sql/fullDataExport/";
	public static final String TEMPLATE_DIR = "org/openmrs/module/mirebalaisreports/reportTemplates/";

    private String uuid;
    private String messageCodePrefix;
    private List<String> dataSets;

    public FullDataExportReportManager(String uuid, String messageCodePrefix, List<String> dataSets) {
        this.uuid = uuid;
        this.messageCodePrefix = messageCodePrefix;
        this.dataSets = dataSets;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

	//***** INSTANCE METHODS

	@Override
	protected String getMessageCodePrefix() {
		return messageCodePrefix;
	}

	@Override
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(getStartDateParameter());
		l.add(getEndDateParameter());
		return l;
	}

	@Override
	public List<RenderingMode> getRenderingModes() {
		List<RenderingMode> l = new ArrayList<RenderingMode>();
		{
			RenderingMode mode = new RenderingMode();
			mode.setLabel(translate("output.excel"));
			mode.setRenderer(new XlsReportRenderer());
			mode.setSortWeight(50);
			mode.setArgument("");
			l.add(mode);
		}
		return l;
	}

	@Override
	public String getRequiredPrivilege() {
		return "Report: mirebalaisreports.fulldataexport";
	}

	@Override
	public ReportDefinition constructReportDefinition() {

		log.info("Constructing " + getName());
        ReportDefinition rd = new ReportDefinition();
		rd.setName(getMessageCodePrefix() + "name");
		rd.setDescription(getMessageCodePrefix() + "description");
		rd.setParameters(getParameters());
        rd.setUuid(getUuid());

		for (String key : dataSets) {

			log.debug("Adding dataSet: " + key);

            DataSetDefinition dsd;
            if ("patients-new".equals(key)) {
                dsd = constructPatientsDataSetDefinition();
            }
            else {
                dsd = constructSqlDataSetDefinition(key);
            }
            dsd.setName(MessageUtil.translate("mirebalaisreports.fulldataexport." + key + ".name"));
            dsd.setDescription(MessageUtil.translate("mirebalaisreports.fulldataexport." + key + ".description"));
            dsd.addParameter(getStartDateParameter());
            dsd.addParameter(getEndDateParameter());

			Map<String, Object> mappings =  new HashMap<String, Object>();
			mappings.put("startDate","${startDate}");
			mappings.put("endDate", "${endDate}");

			rd.addDataSetDefinition(key, dsd, mappings);
		}

		return rd;
	}

    private SqlDataSetDefinition constructSqlDataSetDefinition(String key) {
        SqlDataSetDefinition sqlDsd = new SqlDataSetDefinition();

        String sql = MirebalaisReportsUtil.getStringFromResource(SQL_DIR + key + ".sql");
        sql = applyMetadataReplacements(sql);
        sqlDsd.setSqlQuery(sql);
        return sqlDsd;
    }

    private DataSetDefinition constructPatientsDataSetDefinition() {
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();

        dsd.addColumn("patient_id", new PatientIdDataDefinition(), "");

        PatientIdentifierDataDefinition zlEmrIds = new PatientIdentifierDataDefinition("zlemr", mirebalaisReportsProperties.getZlEmrIdentifierType());
        PatientIdentifierDataDefinition numeroDossiers = new PatientIdentifierDataDefinition("numero_dossier", mirebalaisReportsProperties.getDossierNumberIdentifierType());
        PatientIdentifierDataDefinition hivEmrIds = new PatientIdentifierDataDefinition("hivemr", mirebalaisReportsProperties.getHivEmrIdentifierType());
        VitalStatusDataDefinition vitalStatus = new VitalStatusDataDefinition();
        PreferredAddressDataDefinition preferredAddress = new PreferredAddressDataDefinition();

        EncountersForPatientDataDefinition firstRegistrationEncounter = new EncountersForPatientDataDefinition();
        firstRegistrationEncounter.setWhich(TimeQualifier.FIRST);
        firstRegistrationEncounter.setTypes(Arrays.asList(mirebalaisReportsProperties.getRegistrationEncounterType()));


        // Most recent ZL EMR ID
        // INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = 5 AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
        dsd.addColumn("zlemr", zlEmrIds, "", new MostRecentlyCreatedConverter(PatientIdentifier.class), new PropertyConverter(PatientIdentifier.class, "identifier"));

        // ZL EMR ID location
        // INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
        dsd.addColumn("loc_registered", zlEmrIds, "", new MostRecentlyCreatedConverter(PatientIdentifier.class), new PropertyConverter(PatientIdentifier.class, "location"), new ObjectFormatter());

        // un.value unknown_patient
        // Unknown patient
        // LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = 10 AND un.voided = 0
        dsd.addColumn("unknown_patient", new PersonAttributeDataDefinition(emrApiProperties.getUnknownPatientPersonAttributeType()), "", new PropertyConverter(PersonAttribute.class, "value"));

        // --Number of ZL EMRs assigned to this patient
        // INNER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 5 AND voided = 0 GROUP BY patient_id) numzlemr ON p.patient_id = numzlemr.patient_id
        dsd.addColumn("numzlemr", zlEmrIds, "", new CountConverter());

        // --Most recent Numero Dossier
        // LEFT OUTER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 ORDER BY date_created DESC) nd ON p.patient_id = nd.patient_id
        dsd.addColumn("numero_dossier", numeroDossiers, "", new PropertyConverter(PatientIdentifier.class, "identifier"));

        // --Number of Numero Dossiers
        // LEFT OUTER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 GROUP BY patient_id) numnd ON p.patient_id = numnd.patient_id
        dsd.addColumn("num_nd", numeroDossiers, "", new CountConverter());

        // --HIV EMR ID
        // LEFT OUTER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 ORDER BY date_created DESC) hivemr ON p.patient_id = hivemr.patient_id
        dsd.addColumn("hivemr", hivEmrIds, "", new PropertyConverter(PatientIdentifier.class, "identifier"));

        // --Number of HIV EMR IDs
        // LEFT OUTER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 3 AND voided = 0 GROUP BY patient_id) numhiv ON p.patient_id = numhiv.patient_id
        dsd.addColumn("num_hiv", hivEmrIds, "", new CountConverter());

        // pr.birthdate
        dsd.addColumn("birthdate", new BirthdateDataDefinition(), "", new PropertyConverter(Birthdate.class, "birthdate"));

        // pr.birthdate_estimated
        dsd.addColumn("birthdate_estimated", new BirthdateDataDefinition(), "", new PropertyConverter(Birthdate.class, "estimated"));

        // pr.gender
        dsd.addColumn("gender", new GenderDataDefinition(), "");

        // pr.dead
        dsd.addColumn("dead", vitalStatus, "", new PropertyConverter(VitalStatus.class, "dead"));

        // pr.death_date
        dsd.addColumn("death_date", vitalStatus, "", new PropertyConverter(VitalStatus.class, "deathDate"));

        // --Most recent address
        // LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
        // TODO: implemented this with preferred address rather than most recent one

        // pa.state_province department
        dsd.addColumn("department", preferredAddress, "", new PropertyConverter(PersonAddress.class, "stateProvince"));

        // pa.city_village commune
        dsd.addColumn("commune", preferredAddress, "", new PropertyConverter(PersonAddress.class, "cityVillage"));

        // pa.address3 section
        dsd.addColumn("section", preferredAddress, "", new PropertyConverter(PersonAddress.class, "address3"));

        // pa.address1 locality
        dsd.addColumn("locality", preferredAddress, "", new PropertyConverter(PersonAddress.class, "address1"));

        // pa.address2 street_landmark
        dsd.addColumn("street_landmark", preferredAddress, "", new PropertyConverter(PersonAddress.class, "address2"));

        // reg.encounter_datetime date_registered
        // --First registration encounter
        // LEFT OUTER JOIN (SELECT patient_id, MIN(encounter_id) encounter_id FROM encounter WHERE encounter_type = 6 AND voided = 0 GROUP BY patient_id) first_reg ON p.patient_id = first_reg.patient_id
        // LEFT OUTER JOIN encounter reg ON first_reg.encounter_id = reg.encounter_id
        dsd.addColumn("date_registered", firstRegistrationEncounter, "", new PropertyConverter(Encounter.class, "encounterDatetime"));

        // regl.name reg_location
        // --Location registered
        // LEFT OUTER JOIN location regl ON reg.location_id = regl.location_id
        dsd.addColumn("reg_location", firstRegistrationEncounter, "", new PropertyConverter(Encounter.class, "location"), new ObjectFormatter());

        // CONCAT(regn.given_name, ' ', regn.family_name) reg_by
        // --User who registered the patient
        // LEFT OUTER JOIN users u ON reg.creator = u.user_id
        // LEFT OUTER JOIN person_name regn ON u.person_id = regn.person_id
        dsd.addColumn("reg_by", firstRegistrationEncounter, "", new PropertyConverter(Encounter.class, "creator"), new PropertyConverter(User.class, "personName"), new ObjectFormatter("{givenName} {familyName}"));

        // TODO figure out whether we can use AgeAtDateOfOtherDataDefinition to get Age on Registration encounter, or if we need a custom DataDefinition for this
        // ROUND(DATEDIFF(reg.encounter_datetime, pr.birthdate)/365.25, 1) age_at_reg
        AgeAtDateOfOtherDataDefinition ageAtRegistration = new AgeAtDateOfOtherDataDefinition();
        //ageAtRegistration.setEffectiveDateDefinition(new MappedData<PersonDataDefinition>(firstRegistrationEncounter, mappings("onOrAfter", "onOrBefore"), new PropertyConverter(Encounter.class, "encounterDatetime")));
        //dsd.addColumn("age_at_reg", ageAtRegistration, "", new PropertyConverter(Age.class, "fullYears"));

        return dsd;
    }

    private Map<String, Object> mappings(String startDatePropertyName, String endDatePropertyName) {
        Map<String, Object> mappings = new HashMap<String, Object>();

        if (startDatePropertyName != null) {
            mappings.put(startDatePropertyName, "${startDate}");
        }
        if (endDatePropertyName != null) {
            mappings.put(endDatePropertyName, "${endDate}");
        }
        return mappings;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        return Arrays.asList(xlsReportDesign(reportDefinition));
    }

}
