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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.module.pihcore.config.ConfigDescriptor;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.BaseReportManager;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
import org.openmrs.util.OpenmrsClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Base implementation of ReportManager that provides some common method implementations
 */
public abstract class BasePihReportManager extends BaseReportManager {

    protected final Log log = LogFactory.getLog(getClass());

    public enum Category { OVERVIEW, DAILY, DATA_EXPORT, DATA_QUALITY, MONITORING };

    public Category getCategory() { return null; }

    public Integer getOrder() { return 9999; }

    public List<ConfigDescriptor.Country> getCountries() {
        return Collections.emptyList();
    }

    public List<String> getSites() {
        return Collections.emptyList();
    }

    public String getComponent() { return null; }  // null==don't restrict by component

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

    public Parameter getStartDateParameter() {
        return new Parameter("startDate", "mirebalaisreports.parameter.startDate", Date.class);
    }

    public Parameter getEndDateParameter() {
        return new Parameter("endDate", "mirebalaisreports.parameter.endDate", Date.class);
    }

    public Parameter getLocationParameter() {
        return new Parameter("location", "mirebalaisreports.parameter.location", Location.class);
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

        String templatePath = "org/openmrs/module/mirebalaisreports/reportTemplates/" + templateName + ".xls";
        InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream(templatePath);

        ReportDesign design = new ReportDesign();
        design.setName("mirebalaisreports.output.excel");
        design.setReportDefinition(reportDefinition);
        design.setRendererType(XlsReportRenderer.class);

        ReportDesignResource resource = new ReportDesignResource();
        resource.setName("template");
        resource.setExtension("xls");
        resource.setContentType("application/vnd.ms-excel");
        try {
            byte[] excelTemplate = IOUtils.toByteArray(is);
            resource.setContents(excelTemplate);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load XLS template from " + templatePath
                    + "\n\tVery likely the file doesn't exist.", e);
        }
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

    public <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
        if (parameterizable == null) {
            throw new NullPointerException("Programming error: missing parameterizable");
        }
        if (mappings == null) {
            mappings = ""; // probably not necessary, just to be safe
        }
        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
    }
}
