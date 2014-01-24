package org.openmrs.module.mirebalaisreports.definitions;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 *
 */
public class FullDataExportBuilderTest extends BaseMirebalaisReportTest {

    @Autowired
    FullDataExportBuilder builder;

    @Test
    public void shouldSetupTheReportWithAllDataSets() throws Exception {
        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", null);
        ReportDefinition reportDefinition = builder.buildReportManager(configuration).constructReportDefinition();
        Assert.assertEquals(15, reportDefinition.getDataSetDefinitions().size());
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

}
