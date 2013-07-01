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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for defining the full data export report
 */
@Component
public class FullDataExportReportManager extends BaseReportManager {

    private final Log log = LogFactory.getLog(getClass());

	//***** CONSTANTS *****

	public static final String SQL_DIR = "org/openmrs/module/mirebalaisreports/sql/fullDataExport/";
	public static final String TEMPLATE_DIR = "org/openmrs/module/mirebalaisreports/reportTemplates/";

	public final List<String> dataSetOptions = Arrays.asList(
		"patients", "visits", "checkins", "vitals", "consultations", "diagnoses", "radiologyEncounters", "radiologyOrders"
	);

	public List<String> getDataSetOptions() {
		return dataSetOptions;
	}

	public Parameter getStartDateParameter() {
		return new Parameter("startDate", translate("parameter.startDate"), Date.class);
	}

	public Parameter getEndDateParameter() {
		return new Parameter("endDate", translate("parameter.endDate"), Date.class);
	}

	public Parameter getWhichDataSetParameter() {
		return new Parameter("whichDataSets", translate("parameter.dataToInclude"), String.class, List.class, dataSetOptions, null);
	}

	//***** PROPERTIES *****

    @Autowired
	MirebalaisReportsProperties mirebalaisReportsProperties;

	public void setMirebalaisReportsProperties(MirebalaisReportsProperties mirebalaisReportsProperties) {
		this.mirebalaisReportsProperties = mirebalaisReportsProperties;
	}

	//***** INSTANCE METHODS

	@Override
	protected String getMessageCodePrefix() {
		return "mirebalaisreports.fulldataexport.";
	}

	@Override
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(getStartDateParameter());
		l.add(getEndDateParameter());
		l.add(getWhichDataSetParameter());
		return l;
	}

	@Override
	public List<RenderingMode> getRenderingModes() {
		List<RenderingMode> l = new ArrayList<RenderingMode>();
		{
			RenderingMode mode = new RenderingMode();
			mode.setLabel(translate("output.Excel"));
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
	public ReportDefinition constructReportDefinition(EvaluationContext context) {

		log.info("Constructing " + getName());
        ReportDefinition rd = new ReportDefinition();
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());

		List<String> dataSets = (List<String>)context.getParameterValue(getWhichDataSetParameter().getName());
		if (dataSets == null) {
			dataSets = dataSetOptions;
		}

		for (String key : dataSets) {

			log.debug("Adding dataSet: " + key);

			SqlDataSetDefinition dsd = new SqlDataSetDefinition();
			dsd.setName(MessageUtil.translate("mirebalaisreports.fulldataexport." + key + ".name"));
			dsd.setDescription(MessageUtil.translate("mirebalaisreports.fulldataexport." + key + ".description"));
			dsd.addParameter(getStartDateParameter());
			dsd.addParameter(getEndDateParameter());

			String sql = MirebalaisReportsUtil.getStringFromResource(SQL_DIR + key + ".sql");
			sql = applyMetadataReplacements(sql);
			dsd.setSqlQuery(sql);

			Map<String, Object> mappings =  new HashMap<String, Object>();
			mappings.put("startDate","${startDate}");
			mappings.put("endDate", "${endDate}");

			rd.addDataSetDefinition(key, dsd, mappings);
		}

		return rd;
	}

	protected String applyMetadataReplacements(String sql) {

		log.debug("Replacing metadata references");
		MirebalaisReportsProperties mrp = mirebalaisReportsProperties;

		sql = replace(sql, "zlId", mrp.getZlEmrIdentifierType());
		sql = replace(sql, "dosId", mrp.getDossierNumberIdentifierType());
		sql = replace(sql, "hivId", mrp.getHivEmrIdentifierType());

		sql = replace(sql, "testPt", mrp.getTestPatientPersonAttributeType());

		sql = replace(sql, "regEnc", mrp.getRegistrationEncounterType());
		sql = replace(sql, "chkEnc", mrp.getCheckInEncounterType());
		sql = replace(sql, "vitEnc", mrp.getVitalsEncounterType());
		sql = replace(sql, "consEnc", mrp.getConsultEncounterType());
		sql = replace(sql, "radEnc", mrp.getRadiologyOrderEncounterType());
		sql = replace(sql, "paid", mrp.getAmountPaidConcept());
		sql = replace(sql, "wt", mrp.getWeightConcept());
		sql = replace(sql, "ht", mrp.getHeightConcept());
		sql = replace(sql, "muac", mrp.getMuacConcept());
		sql = replace(sql, "temp", mrp.getTemperatureConcept());
		sql = replace(sql, "hr", mrp.getPulseConcept());
		sql = replace(sql, "rr", mrp.getRespiratoryRateConcept());
		sql = replace(sql, "sbp", mrp.getSystolicBpConcept());
		sql = replace(sql, "dbp", mrp.getDiastolicBpConcept());
		sql = replace(sql, "o2", mrp.getBloodOxygenSaturationConcept());
		sql = replace(sql, "coded", mrp.getCodedDiagnosisConcept());
		sql = replace(sql, "noncoded", mrp.getNonCodedDiagnosisConcept());
		sql = replace(sql, "comment", mrp.getClinicalImpressionsConcept());
		sql = replace(sql, "notifiable", mrp.getSetOfWeeklyNotifiableDiseases());
		sql = replace(sql, "urgent", mrp.getSetOfUrgentDiseases());
		sql = replace(sql, "santeFamn", mrp.getSetOfWomensHealthDiagnoses());
		sql = replace(sql, "psycho", mrp.getSetOfPsychologicalDiagnoses());
		sql = replace(sql, "peds", mrp.getSetOfPediatricDiagnoses());
		sql = replace(sql, "outpatient", mrp.getSetOfOutpatientDiagnoses());
		sql = replace(sql, "ncd", mrp.getSetOfNcdDiagnoses());
		sql = replace(sql, "notDx", mrp.getSetOfNonDiagnoses());
		sql = replace(sql, "ed", mrp.getSetOfEmergencyDiagnoses());
		sql = replace(sql, "ageRst", mrp.getSetOfAgeRestrictedDiagnoses());

		log.debug("Replacing metadata references complete.");
		return sql;
    }

	protected String replace(String sql, String oldValue, OpenmrsObject newValue) {
		String s = sql.replace(":"+oldValue, newValue.getId().toString());
		return s;
	}
}
