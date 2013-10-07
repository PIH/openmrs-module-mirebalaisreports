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

import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.mirebalaisreports.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.mirebalaisreports.library.PatientDataLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PersonAttributeCohortDefinition;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

    @Autowired
    private PatientDataLibrary patientDataLibrary;

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

        {
            CompositionCohortDefinition baseCohortDefinition = new CompositionCohortDefinition();
            baseCohortDefinition.addParameter(getStartDateParameter());
            baseCohortDefinition.addParameter(getEndDateParameter());

            // --Only show patients with a visit or registration encounter during the period
            // INNER JOIN (
            // SELECT patient_id, date_started FROM visit WHERE voided = 0 AND date_started BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)
            // UNION
            // SELECT patient_id, encounter_datetime FROM encounter WHERE voided = 0 AND encounter_type = 6 AND encounter_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)
            // ) list ON p.patient_id = list.patient_id

            VisitCohortDefinition visitDuringPeriod = new VisitCohortDefinition();
            visitDuringPeriod.addParameter(new Parameter("startedOnOrAfter", "", Date.class));
            visitDuringPeriod.addParameter(new Parameter("startedOnOrBefore", "", Date.class));
            baseCohortDefinition.addSearch("visitDuringPeriod", this.<CohortDefinition>map(visitDuringPeriod, "startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));

            EncounterCohortDefinition registrationEncounterDuringPeriod = new EncounterCohortDefinition();
            registrationEncounterDuringPeriod.addEncounterType(mirebalaisReportsProperties.getRegistrationEncounterType());
            registrationEncounterDuringPeriod.addParameter(new Parameter("onOrAfter", "", Date.class));
            registrationEncounterDuringPeriod.addParameter(new Parameter("onOrBefore", "", Date.class));
            baseCohortDefinition.addSearch("registrationEncounterDuringPeriod", this.<CohortDefinition>map(registrationEncounterDuringPeriod, "onOrAfter=${startDate},onOrBefore=${endDate}"));

            // --Exclude test patients
            // AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = 11 AND voided = 0)
            PersonAttributeCohortDefinition testPatient = new PersonAttributeCohortDefinition();
            testPatient.setAttributeType(emrApiProperties.getTestPatientPersonAttributeType());
            testPatient.addValue("true");
            baseCohortDefinition.addSearch("testPatient", testPatient, null);

            baseCohortDefinition.setCompositionString("(visitDuringPeriod OR registrationEncounterDuringPeriod) AND NOT testPatient");
            rd.setBaseCohortDefinition(this.<CohortDefinition>map(baseCohortDefinition, "startDate=${startDate},endDate=${endDate}"));
        }

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

        dsd.addParameter(getStartDateParameter());
        dsd.addParameter(getEndDateParameter());

        dsd.addColumn("patient_id", patientDataLibrary.getDefinition("patientId"), "");

        // Most recent ZL EMR ID
        // INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = 5 AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
        dsd.addColumn("zlemr", patientDataLibrary.getDefinition("mostRecentZlEmrId.identifier"), "");

        // ZL EMR ID location
        // INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
        dsd.addColumn("loc_registered", patientDataLibrary.getDefinition("mostRecentZlEmrId.location"), "");

        // un.value unknown_patient
        // Unknown patient
        // LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = 10 AND un.voided = 0
        dsd.addColumn("unknown_patient", patientDataLibrary.getDefinition("unknownPatient.value"), "");

        // --Number of ZL EMRs assigned to this patient
        // INNER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 5 AND voided = 0 GROUP BY patient_id) numzlemr ON p.patient_id = numzlemr.patient_id
        // TODO difference: returns 0 where the existing behavior is to leave those blank
        dsd.addColumn("numzlemr", patientDataLibrary.getDefinition("numberOfZlEmrIds"), "");

        // --Most recent Numero Dossier
        // LEFT OUTER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 ORDER BY date_created DESC) nd ON p.patient_id = nd.patient_id
        dsd.addColumn("numero_dossier", patientDataLibrary.getDefinition("mostRecentDossierNumber.identifier"), "");

        // --Number of Numero Dossiers
        // LEFT OUTER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 GROUP BY patient_id) numnd ON p.patient_id = numnd.patient_id
        // TODO difference: returns 0 where the existing behavior is to leave those blank
        dsd.addColumn("num_nd", patientDataLibrary.getDefinition("numberOfDossierNumbers"), "");

        // --HIV EMR ID
        // LEFT OUTER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 ORDER BY date_created DESC) hivemr ON p.patient_id = hivemr.patient_id
        dsd.addColumn("hivemr", patientDataLibrary.getDefinition("mostRecentHivEmrId.identifier"), "");

        // --Number of HIV EMR IDs
        // LEFT OUTER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 3 AND voided = 0 GROUP BY patient_id) numhiv ON p.patient_id = numhiv.patient_id
        // TODO difference: returns 0 where the existing behavior is to leave those blank
        dsd.addColumn("num_hiv", patientDataLibrary.getDefinition("numberOfHivEmrIds"), "");

        // pr.birthdate
        dsd.addColumn("birthdate", patientDataLibrary.getDefinition("birthdate.ymd"), "");

        // pr.birthdate_estimated
        dsd.addColumn("birthdate_estimated", patientDataLibrary.getDefinition("birthdate.estimated"), "");

        // pr.gender
        dsd.addColumn("gender", patientDataLibrary.getDefinition("gender"), "");

        // pr.dead
        dsd.addColumn("dead", patientDataLibrary.getDefinition("vitalStatus.dead"), "");

        // pr.death_date
        dsd.addColumn("death_date", patientDataLibrary.getDefinition("vitalStatus.deathDate"), "");

        // --Most recent address
        // LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
        // TODO: implemented this with preferred address rather than most recent one

        // pa.state_province department
        dsd.addColumn("department", patientDataLibrary.getDefinition("preferredAddress.department"), "");

        // pa.city_village commune
        dsd.addColumn("commune", patientDataLibrary.getDefinition("preferredAddress.commune"), "");

        // pa.address3 section
        dsd.addColumn("section", patientDataLibrary.getDefinition("preferredAddress.section"), "");

        // pa.address1 locality
        dsd.addColumn("locality", patientDataLibrary.getDefinition("preferredAddress.locality"), "");

        // pa.address2 street_landmark
        dsd.addColumn("street_landmark", patientDataLibrary.getDefinition("preferredAddress.streetLandmark"), "");

        // reg.encounter_datetime date_registered
        // --First registration encounter
        // LEFT OUTER JOIN (SELECT patient_id, MIN(encounter_id) encounter_id FROM encounter WHERE encounter_type = 6 AND voided = 0 GROUP BY patient_id) first_reg ON p.patient_id = first_reg.patient_id
        // LEFT OUTER JOIN encounter reg ON first_reg.encounter_id = reg.encounter_id

        dsd.addColumn("date_registered", patientDataLibrary.getDefinition("registration.encounterDatetime"), "");

        // regl.name reg_location
        // --Location registered
        // LEFT OUTER JOIN location regl ON reg.location_id = regl.location_id
        dsd.addColumn("reg_location", patientDataLibrary.getDefinition("registration.location"), "");

        // CONCAT(regn.given_name, ' ', regn.family_name) reg_by
        // --User who registered the patient
        // LEFT OUTER JOIN users u ON reg.creator = u.user_id
        // LEFT OUTER JOIN person_name regn ON u.person_id = regn.person_id
        dsd.addColumn("reg_by", patientDataLibrary.getDefinition("registration.creator.name"), "");

        // ROUND(DATEDIFF(reg.encounter_datetime, pr.birthdate)/365.25, 1) age_at_reg
        dsd.addColumn("age_at_reg", patientDataLibrary.getDefinition("registration.age"), "");

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
