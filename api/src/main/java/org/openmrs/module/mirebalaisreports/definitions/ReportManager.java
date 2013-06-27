package org.openmrs.module.mirebalaisreports.definitions;

/**
 * Created with IntelliJ IDEA.
 * User: mseaton
 * Date: 6/26/13
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */

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
