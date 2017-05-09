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

import org.apache.commons.io.IOUtils;
import org.openmrs.Location;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.config.ConfigDescriptor;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.ReportProcessorConfiguration;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.processor.DiskReportProcessor;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Base implementation of ReportManager that provides some common method implementations
 */
public abstract class BaseReportManager implements ReportManager {

    public enum Category { OVERVIEW, DAILY, DATA_EXPORT, DATA_QUALITY, MONITORING };

     // TODO these control display order, should really be somewhere else?
     public static final List<String> REPORTING_OVERVIEW_REPORTS_ORDER = Arrays.asList(
        MirebalaisReportsProperties.DAILY_REGISTRATIONS_REPORT_DEFINITION_UUID,
        MirebalaisReportsProperties.DAILY_CHECK_INS_REPORT_DEFINITION_UUID,
        MirebalaisReportsProperties.DAILY_CLINICAL_ENCOUNTERS_REPORT_DEFINITION_UUID,
         MirebalaisReportsProperties.INPATIENT_STATS_DAILY_REPORT_DEFINITION_UUID,
        MirebalaisReportsProperties.INPATIENT_STATS_MONTHLY_REPORT_DEFINITION_UUID);

    public static final List<String> REPORTING_DATA_EXPORT_REPORTS_ORDER = Arrays.asList(
            MirebalaisReportsProperties.USERS_AND_PROVIDERS_REPORT_DEFINITION_UUID,
            MirebalaisReportsProperties.RELATIONSHIPS_REPORT_DEFINITION_UUID,
            MirebalaisReportsProperties.LQAS_DIAGNOSES_REPORT_DEFINITION_UUID,
            MirebalaisReportsProperties.ALL_PATIENTS_WITH_IDS_REPORT_DEFINITION_UUID,
            MirebalaisReportsProperties.APPOINTMENTS_REPORT_DEFINITION_UUID);

    public static final List<String> REPORTING_MONITORING_REPORTS_ORDER = Arrays.asList(
            MirebalaisReportsProperties.WEEKLY_MONITORING_REPORT_DEFINITION_UUID,
            MirebalaisReportsProperties.NEW_DISEASE_EPISODES_REPORTING_DEFINITION_UUID,
            MirebalaisReportsProperties.ACCOUNTING_REPORTING_DEFINITION_UUID,
            MirebalaisReportsProperties.VISIT_REGISTRY_REPORTING_DEFINITION_UUID,
            MirebalaisReportsProperties.MORBIDITY_REGISTRY_REPORTING_DEFINITION_UUID
    );

	/**
	 * @return the message code prefix used for all translations for the report
	 */

    public Category getCategory() { return null; }

    public Integer getOrder() { return 9999; }

    public List<ConfigDescriptor.Country> getCountries() {
        return Collections.emptyList();
    }

    public List<ConfigDescriptor.Site> getSites() {
        return Collections.emptyList();
    }

	@Override
	public String getName() {
		return null;
	}

    public String getMessageCodePrefix() {
        return "mirebalaisreports." + (getName() != null ? getName() : "") + ".";
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

    public Parameter getStartDateParameter() {
        return new Parameter("startDate", "mirebalaisreports.parameter.startDate", Date.class);
    }

    public Parameter getEndDateParameter() {
        return new Parameter("endDate", "mirebalaisreports.parameter.endDate", Date.class);
    }

    public Parameter getLocationParameter() {
        return new Parameter("location", "mirebalaisreports.parameter.location", Location.class);
    }

    protected List<Parameter> getStartAndEndDateParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(getStartDateParameter());
        l.add(getEndDateParameter());
        return l;
    }

    protected Map<String,Object> getStartAndEndDateMappings() {
        Map<String, Object> mappings =  new HashMap<String, Object>();
        mappings.put("startDate","${startDate}");
        mappings.put("endDate", "${endDate}");
        return mappings;
    }

	protected String translate(String code) {
		String messageCode = getMessageCodePrefix()+code;
		String translation = MessageUtil.translate(messageCode);
		if (messageCode.equals(translation)) {
			return code;
		}
		return translation;
	}

    public CohortIndicator buildIndicator(String name, CohortDefinition cd, String mappings) {
        CohortIndicator indicator = new CohortIndicator(name);
        indicator.addParameter(getStartDateParameter());
        indicator.addParameter(getEndDateParameter());
        indicator.addParameter(getLocationParameter());
        indicator.setCohortDefinition(map(cd, mappings));
        return indicator;
    }

    protected ReportDesign xlsReportDesign(ReportDefinition reportDefinition, String templateName, String repeatingSections) throws IOException{

        InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream("org/openmrs/module/mirebalaisreports/reportTemplates/" + templateName + ".xls");
        byte[] excelTemplate = IOUtils.toByteArray(is);

        ReportDesign design = new ReportDesign();
        design.setName("mirebalaisreports.output.excel");
        design.setReportDefinition(reportDefinition);
        design.setRendererType(XlsReportRenderer.class);

        ReportDesignResource resource = new ReportDesignResource();
        resource.setName("template");
        resource.setExtension("xls");
        resource.setContentType("application/vnd.ms-excel");
        resource.setContents(excelTemplate);
        resource.setReportDesign(design);
        design.addResource(resource);

        Properties designProperties = new Properties();
        designProperties.put("repeatingSections", repeatingSections);
        design.setProperties(designProperties);

        return design;
    }


    protected ReportDesign csvReportDesign(ReportDefinition reportDefinition) {
        ReportDesign design = new ReportDesign();
        design.setName("mirebalaisreports.output.csv");
        design.setReportDefinition(reportDefinition);
        design.setRendererType(CsvReportRenderer.class);
        design.addPropertyValue("blacklistRegex", "[^\\p{InBasicLatin}\\p{L}]");
        design.addPropertyValue("characterEncoding", "ISO-8859-1");
        design.addPropertyValue("dateFormat", "dd-MMM-yyyy HH:mm:ss");
        return design;
    }

    protected ReportProcessorConfiguration constructSaveToDiskReportProcessorConfiguration() {
        Properties saveToDiskProperties = new Properties();
        saveToDiskProperties.put(DiskReportProcessor.SAVE_LOCATION, OpenmrsUtil.getApplicationDataDirectory() + "reports");
        saveToDiskProperties.put(DiskReportProcessor.COMPRESS_OUTPUT, "true");

        ReportProcessorConfiguration saveToDiskProcessorConfiguration
                = new ReportProcessorConfiguration("saveToDisk", DiskReportProcessor.class, saveToDiskProperties, true, false);
        saveToDiskProcessorConfiguration.setProcessorMode(ReportProcessorConfiguration.ProcessorMode.AUTOMATIC);

        return saveToDiskProcessorConfiguration;
    }

    public <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
        if (parameterizable == null) {
            throw new NullPointerException("Programming error: missing parameterizable");
        }
        if (mappings == null) {
            mappings = ""; // probably not necessary, just to be safe
        }
        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
    }

    protected byte[] getBytesForResource(String pathToResource) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(pathToResource);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        OpenmrsUtil.copyFile(inputStream, bytes);
        return bytes.toByteArray();
    }
}
