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

import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of ReportManager that provides some common method implementations
 */
public abstract class BaseReportManager implements ReportManager {

	/**
	 * @return the message code prefix used for all translations for the report
	 */
	protected abstract String getMessageCodePrefix();

	@Override
	public String getName() {
		return translate("name");
	}

	@Override
	public String getDescription() {
		return translate("description");
	}

	@Override
	public List<Parameter> getParameters() {
		return new ArrayList<Parameter>();
	}

	@Override
	public List<RenderingMode> getRenderingModes() {
		return new ArrayList<RenderingMode>();
	}

	@Override
	public String getRequiredPrivilege() {
		return null;
	}

	@Override
	public EvaluationContext initializeContext(Map<String, Object> parameters) {
		EvaluationContext context = new EvaluationContext();
		context.setParameterValues(parameters == null ? new HashMap<String, Object>() : parameters);
		return context;
	}

	protected String translate(String code) {
		String messageCode = getMessageCodePrefix()+code;
		String translation = MessageUtil.translate(messageCode);
		if (messageCode.equals(translation)) {
			return code;
		}
		return translation;
	}

    protected ReportDesign xlsReportDesign(ReportDefinition reportDefinition) {
        ReportDesign design = new ReportDesign();
        design.setName(getMessageCodePrefix() + "output.excel");
        design.setReportDefinition(reportDefinition);
        design.setRendererType(XlsReportRenderer.class);
        return design;
    }
}
