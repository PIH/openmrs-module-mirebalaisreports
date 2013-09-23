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
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
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
    private String name;
    private String description;
    private List<String> dataSets;

    public FullDataExportReportManager(String uuid, String name, String description, List<String> dataSets) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.dataSets = dataSets;
    }

    @Override
    public String getUuid() {
        return uuid;
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
	public ReportDefinition constructReportDefinition(EvaluationContext context) {

		log.info("Constructing " + getName());
        ReportDefinition rd = new ReportDefinition();
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());
        rd.setUuid(getUuid());

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

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition, EvaluationContext evaluationContext) {
        return Arrays.asList(xlsReportDesign(reportDefinition));
    }

}
