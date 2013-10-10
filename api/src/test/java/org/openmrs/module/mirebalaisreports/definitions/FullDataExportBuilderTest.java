package org.openmrs.module.mirebalaisreports.definitions;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mirebalaisreports.MirebalaisHospitalReportingModuleActivator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 *
 */
public class FullDataExportBuilderTest extends BaseMirebalaisReportTest {

    @Autowired
    FullDataExportBuilder builder;
    
    @Autowired
    ReportService reportService;

    @Autowired
    ReportDefinitionService reportDefinitionService;

    @Test
    public void shouldSetupTheReportWithAllDataSets() throws Exception {
        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", null);
        ReportDefinition reportDefinition = builder.buildReportManager(configuration).constructReportDefinition();
        Assert.assertEquals(13, reportDefinition.getDataSetDefinitions().size());
    }

    @Test
    public void shouldSetupTheReportWithSelectedDataSets() throws Exception {
        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", Arrays.asList("patients", "visits"));
        ReportDefinition reportDefinition = builder.buildReportManager(configuration).constructReportDefinition();
        Assert.assertEquals(2, reportDefinition.getDataSetDefinitions().size());
    }

    @Test
    public void shouldOrderTheDataSetsInTheReportCorrectly() throws Exception {
        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", null);
        ReportDefinition reportDefinition = builder.buildReportManager(configuration).constructReportDefinition();
        int i=0;
        for (String dsName : reportDefinition.getDataSetDefinitions().keySet()) {
			Assert.assertEquals(builder.dataSetOptions.get(i), dsName);
            i++;
        }
    }
    
    @Test
    public void shouldRunAReportAfterPersistingIt() throws Exception {
        executeDataSet("org/openmrs/module/mirebalaisreports/patientsBasedOnCoreMetadata.xml");
        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", Arrays.asList("patients"));
        FullDataExportReportManager reportManager = builder.buildReportManager(configuration);
        MirebalaisHospitalReportingModuleActivator activator = new MirebalaisHospitalReportingModuleActivator();
        activator.setReportService(reportService);
        activator.setReportDefinitionService(reportDefinitionService);
        activator.setupReport(reportManager);

        ReportDefinition rd = reportDefinitionService.getDefinitionByUuid(reportManager.getUuid());
        EvaluationContext context = new EvaluationContext();
        context.addParameterValue("startDate", DateUtil.parseDate("2013-09-01", "yyyy-MM-dd"));
        context.addParameterValue("endDate", DateUtil.parseDate("2013-09-30", "yyyy-MM-dd"));
        reportDefinitionService.evaluate(rd, context);
    }

}
