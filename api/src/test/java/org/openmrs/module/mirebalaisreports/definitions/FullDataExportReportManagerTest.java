package org.openmrs.module.mirebalaisreports.definitions;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.TsvReportRenderer;
import org.openmrs.module.reporting.report.util.ReportUtil;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

@SkipBaseSetup
public class FullDataExportReportManagerTest extends BaseMirebalaisReportTest {

    @Autowired
    FullDataExportBuilder builder;

	@Test
	public void shouldSuccessfullyRenderToExcel() throws Exception {

        setUpPatientsBasedOnCoreMetadata();

        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", Arrays.asList("patients"));
        FullDataExportReportManager reportManager = builder.buildReportManager(configuration);

        EvaluationContext context = new EvaluationContext();
        context.addParameterValue("startDate", DateUtil.parseDate("2013-07-01", "yyyy-MM-dd"));
        context.addParameterValue("endDate", DateUtil.parseDate("2013-09-30", "yyyy-MM-dd"));

        ReportDefinition reportDefinition = reportManager.constructReportDefinition();
        RenderingMode mode = reportManager.getRenderingModes().get(0);
        ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);

        new TsvReportRenderer().render(reportData, null, System.out);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mode.getRenderer().render(reportData, mode.getArgument(), out);
        File outputFile = new File(System.getProperty("java.io.tmpdir"), "test.xls");
        ReportUtil.writeByteArrayToFile(outputFile, out.toByteArray());

        System.out.println("Wrote to " + outputFile.getAbsolutePath());

        InputStream is = new FileInputStream(outputFile);
        POIFSFileSystem fs = new POIFSFileSystem(is);
        HSSFWorkbook wb = new HSSFWorkbook(fs);

        Assert.assertEquals(1, wb.getNumberOfSheets());
        Assert.assertEquals("patients", wb.getSheetName(0));
    }

    @Ignore
    @Test
    public void testDispensingExport() throws Exception {
        Patient patient = data.patient().name("Darius", "Mark").gender("M").age(30).identifier(mirebalaisReportsProperties.getHivEmrIdentifierType(), "123").save();
        data.obs().person(patient).concept(mirebalaisReportsProperties.getCodedDiagnosisConcept()).save();

        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", Arrays.asList("dispensing"));
        FullDataExportReportManager reportManager = builder.buildReportManager(configuration);

        EvaluationContext context = new EvaluationContext();
        context.addParameterValue("startDate", DateUtil.parseDate("2013-07-01", "yyyy-MM-dd"));
        context.addParameterValue("endDate", DateUtil.parseDate("2013-09-30", "yyyy-MM-dd"));

        ReportDefinition reportDefinition = reportManager.constructReportDefinition();
        RenderingMode mode = reportManager.getRenderingModes().get(0);
        ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);

        new TsvReportRenderer().render(reportData, null, System.out);
    }

    private void setUpPatientsBasedOnCoreMetadata() {
        Patient patient = data.patient().name("Christy","Lee").gender("F")
                .identifier(mirebalaisReportsProperties.getZlEmrIdentifierType(), "TT200E", mirebalaisReportsProperties.getOutpatientLocation())
                .address("1050 Wishard Blvd", "RG5", "Indianapolis", "IN").save();
        data.patient().name("Bobby", "Joe").gender("M")
                .identifier(mirebalaisReportsProperties.getZlEmrIdentifierType(), "TT201C", mirebalaisReportsProperties.getOutpatientLocation())
                .address("", "", "Kapina").save();
        data.encounter().patient(patient).encounterType(mirebalaisReportsProperties.getRegistrationEncounterType())
                .encounterDatetime("2013-09-08").location(mirebalaisReportsProperties.getOutpatientLocation()).save();
    }

    @Test
    public void shouldSuccessfullyRenderConsultationsToExcel() throws Exception {
        executeDataSet("org/openmrs/module/mirebalaisreports/consultationsExportTestData.xml");

        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", Arrays.asList("consultations-new"));
        FullDataExportReportManager reportManager = builder.buildReportManager(configuration);

        EvaluationContext context = new EvaluationContext();
        context.addParameterValue("startDate", DateUtil.parseDate("2013-10-01", "yyyy-MM-dd"));
        context.addParameterValue("endDate", DateUtil.parseDate("2013-10-31", "yyyy-MM-dd"));

        ReportDefinition reportDefinition = reportManager.constructReportDefinition();
        RenderingMode mode = reportManager.getRenderingModes().get(0);
        ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);

        new TsvReportRenderer().render(reportData, null, System.out);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mode.getRenderer().render(reportData, mode.getArgument(), out);
        File outputFile = new File(System.getProperty("java.io.tmpdir"), "test.xls");
        ReportUtil.writeByteArrayToFile(outputFile, out.toByteArray());

        System.out.println("Wrote to " + outputFile.getAbsolutePath());

        InputStream is = new FileInputStream(outputFile);
        POIFSFileSystem fs = new POIFSFileSystem(is);
        HSSFWorkbook wb = new HSSFWorkbook(fs);

        Assert.assertEquals(1, wb.getNumberOfSheets());
        Assert.assertEquals("consultations", wb.getSheetName(0));
    }

}
