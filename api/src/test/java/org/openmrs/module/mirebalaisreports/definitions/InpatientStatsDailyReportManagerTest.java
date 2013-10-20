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
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
@SkipBaseSetup
public class InpatientStatsDailyReportManagerTest extends BaseMirebalaisReportTest {

    @Autowired
    private InpatientStatsDailyReportManager manager;

    @Autowired
    private ReportDefinitionService reportDefinitionService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("org/openmrs/module/mirebalaisreports/inpatientDailyReportTestDataset.xml");
    }

    @Test
    public void testRunningReport() throws Exception {
        EvaluationContext context = new EvaluationContext();
        context.addParameterValue("day", DateUtil.parseDate("2013-10-03", "yyyy-MM-dd"));

        ReportDefinition reportDefinition = manager.constructReportDefinition();
        ReportData evaluated = reportDefinitionService.evaluate(reportDefinition, context);

        assertThat(evaluated.getDataSets().size(), is(1));

        for (DataSet dataSet : evaluated.getDataSets().values()) {
            MapDataSet mds = (MapDataSet) dataSet;
            for (DataSetColumn column : mds.getMetaData().getColumns()) {
                System.out.println(column.getLabel() + " = " + mds.getData(column));
            }
        }
    }

}
