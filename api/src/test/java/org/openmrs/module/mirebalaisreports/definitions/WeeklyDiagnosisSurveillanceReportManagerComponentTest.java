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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emr.EmrProperties;
import org.openmrs.module.emr.TestUtils;
import org.openmrs.module.emr.test.TestTimer;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class WeeklyDiagnosisSurveillanceReportManagerComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private WeeklyDiagnosisSurveillanceReportManager manager;

    @Autowired
    private DataSetDefinitionService service;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EmrProperties emrProperties;

    @Before
    public void setUp() throws Exception {
        TestUtils.setupDiagnosisMetadata(conceptService, emrProperties);
    }

    @Test
    public void testReport() throws Exception {
        TestTimer timer = new TestTimer();

        timer.println("Started");
        CohortIndicatorDataSetDefinition dsd = manager.buildDataSetDefinition();

        timer.println("Build DSD");

        EvaluationContext evaluationContext = new EvaluationContext();
        evaluationContext.addParameterValue("startOfWeek", DateUtil.parseDate("2005-01-01", "yyyy-MM-dd"));
        DataSet evaluated = service.evaluate(dsd, evaluationContext);

        timer.println("Evaluated");

        for (DataSetRow row : evaluated) {
            for (DataSetColumn column : evaluated.getMetaData().getColumns()) {
                System.out.println(column.getLabel() + " = " + row.getColumnValue(column) + "\t");
            }
            System.out.println();
        }

    }

}
