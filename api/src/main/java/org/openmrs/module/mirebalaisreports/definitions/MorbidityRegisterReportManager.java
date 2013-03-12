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

import org.openmrs.module.mirebalaisreports.visit.definition.HasPrimaryDiagnosisVisitQuery;
import org.openmrs.module.mirebalaisreports.visit.definition.VisitDataSetDefinition;
import org.openmrs.module.mirebalaisreports.visit.definition.VisitStartDateDataDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 *
 */
public class MorbidityRegisterReportManager {

    @Autowired
    private DataSetDefinitionService dataSetDefinitionService;

    public DataSet evaluate(Date startOfPeriod, Date endOfPeriod) throws EvaluationException {
        VisitDataSetDefinition visitDataSetDefinition = buildDataSetDefinition(startOfPeriod, endOfPeriod);

        DataSet evaluated = dataSetDefinitionService.evaluate(visitDataSetDefinition, new EvaluationContext());
        return evaluated;
    }

    public VisitDataSetDefinition buildDataSetDefinition(Date startOfPeriod, Date endOfPeriod) {
        VisitDataSetDefinition visitDataSetDefinition = new VisitDataSetDefinition();

        // define what will be the rows
        HasPrimaryDiagnosisVisitQuery query = new HasPrimaryDiagnosisVisitQuery();
        query.setOnOrAfter(startOfPeriod);
        query.setOnOrBefore(endOfPeriod);
        visitDataSetDefinition.addRowFilter(query, null);

        // definitions for each column
        visitDataSetDefinition.addColumn("date", new VisitStartDateDataDefinition(), "");
        return visitDataSetDefinition;
    }

    private <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
        if (parameterizable == null) {
            throw new NullPointerException("Programming error: missing parameterizable");
        }
        if (mappings == null) {
            mappings = ""; // probably not necessary, just to be safe
        }
        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
    }
}
