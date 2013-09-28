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
import org.hibernate.SessionFactory;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.dataset.definition.NonCodedDiagnosisDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
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
public class NonCodedDiagnosesReportManager extends BaseReportManager {

    private final Log log = LogFactory.getLog(getClass());

	public final static String DATA_SET_NAME = "data";

	//***** PROPERTIES *****

    @Autowired
	EmrApiProperties emrApiProperties;

	@Autowired
	SessionFactory sessionFactory;

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.NON_CODED_DIAGNOSES_REPORT_DEFINITION_UUID;
    }

	public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
		this.emrApiProperties = emrApiProperties;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

//***** INSTANCE METHODS

	@Override
	protected String getMessageCodePrefix() {
		return "mirebalaisreports.noncodeddiagnoses.";
	}

	@Override
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("fromDate", "From Date", Date.class));
		l.add(new Parameter("toDate", "To Date", Date.class));
        l.add(new Parameter("nonCoded", "Non-Coded", String.class));
		return l;
	}

	@Override
	public ReportDefinition constructReportDefinition() {

		log.info("Constructing " + getName());
        ReportDefinition rd = new ReportDefinition();
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());

		NonCodedDiagnosisDataSetDefinition dsd = new NonCodedDiagnosisDataSetDefinition();
		dsd.addParameters(getParameters());
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("fromDate", "${fromDate}");
		mappings.put("toDate", "${toDate}");
        mappings.put("nonCoded", "${nonCoded}");

		rd.addDataSetDefinition(DATA_SET_NAME, dsd, mappings);

		return rd;
	}

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        return Arrays.asList(xlsReportDesign(reportDefinition));
    }

}
