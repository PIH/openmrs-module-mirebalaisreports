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

import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;

import java.util.List;
import java.util.Map;

/**
 * This interface enables defining a particular ReportDefinition in memory
 */
public interface ReportManager {

	/**
	 * @return the name of the Report
	 */
	public String getName();

	/**
	 * @return the description of the Report
	 */
	public String getDescription();

	/**
	 * @return the parameters of the Report
	 */
	public List<Parameter> getParameters();

	/**
	 * @return the rendering modes of the Report
	 */
	public List<RenderingMode> getRenderingModes();

	/**
	 * @return the privilege required to view or evaluate this report
	 */
	public String getRequiredPrivilege();

	/**
	 * This method provides a mechanism to validate input parameters,
	 * transform input parameters, or provide any other custom logic
	 * needed to set up the appropriate EvaluationContext that should
	 * be used when running this report.
	 * @return the EvaluationContext to use for the report.
	 */
	public EvaluationContext initializeContext(Map<String, Object> parameters);

	/**
	 * @return the ReportDefinition that should be evaluated for the given context
	 */
	public ReportDefinition constructReportDefinition(EvaluationContext context);
}
