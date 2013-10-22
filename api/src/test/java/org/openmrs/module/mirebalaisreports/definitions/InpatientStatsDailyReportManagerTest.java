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
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.ExcelTemplateRenderer;
import org.openmrs.module.reporting.report.util.ReportUtil;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

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

        for (Map.Entry<String, DataSet> entry : evaluated.getDataSets().entrySet()) {
            DataSet dataSet = entry.getValue();
            System.out.println("Data Set: " + entry.getKey());
            MapDataSet mds = (MapDataSet) dataSet;
            for (DataSetColumn column : mds.getMetaData().getColumns()) {
                System.out.println(column.getLabel() + " = " + mds.getData(column));
            }
        }

        File outputFile = new File(System.getProperty("java.io.tmpdir"), "inpatientStatsDaily.xls");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ReportDesign design = manager.constructReportDesigns(reportDefinition).get(0);
        new ExcelTemplateRenderer() {
            @Override
            public ReportDesign getDesign(String argument) {
                return design;
            }
        }.render(evaluated, "", out);
        ReportUtil.writeByteArrayToFile(outputFile, out.toByteArray());
        System.out.println("Wrote to " + outputFile.getAbsolutePath());
    }

}
