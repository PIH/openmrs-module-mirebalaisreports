package org.openmrs.module.mirebalaisreports.definitions;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.util.ReportUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SkipBaseSetup
public class FullDataExportReportManagerTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private FullDataExportReportManager fullDataExport;

	@Autowired
	private ReportDefinitionService reportDefinitionService;

    @Before
    public void setup() throws Exception {
        executeDataSet("org/openmrs/module/mirebalaisreports/coreMetadata.xml");
		authenticate();
    }

	public EvaluationContext createContext(Date startDate, Date endDate, String... dataSets) {
		Map<String, Object> params = new HashMap<String, Object>();
		if (startDate != null) {
			params.put(fullDataExport.getStartDateParameter().getName(), startDate);
		}
		if (endDate != null) {
			params.put(fullDataExport.getEndDateParameter().getName(), endDate);
		}
		if (dataSets != null && dataSets.length > 0) {
			params.put(fullDataExport.getWhichDataSetParameter().getName(), Arrays.asList(dataSets));
		}
		return fullDataExport.initializeContext(params);
	}

    @Test
    public void shouldSetupTheReportWithAllDataSets() throws Exception {
		EvaluationContext context = createContext(new Date(), new Date());
		ReportDefinition reportDefinition = fullDataExport.constructReportDefinition(context);
		Assert.assertEquals(8, reportDefinition.getDataSetDefinitions().size());
	}

	@Test
	public void shouldSetupTheReportWithSelectedDataSets() throws Exception {
		EvaluationContext context = createContext(new Date(), new Date(), "patients", "visits");
		ReportDefinition reportDefinition = fullDataExport.constructReportDefinition(context);
		Assert.assertEquals(2, reportDefinition.getDataSetDefinitions().size());
	}

	@Test
	@Ignore // Ignoring this for now since H2 doesnt seem to like our sql queries
	public void shouldSuccessfullyRenderToExcel() throws Exception {

		EvaluationContext context = createContext(new Date(), new Date(), "patients", "visits");
		ReportDefinition reportDefinition = fullDataExport.constructReportDefinition(context);
		RenderingMode mode = fullDataExport.getRenderingModes().get(0);
		ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mode.getRenderer().render(reportData, mode.getArgument(), out);
		File outputFile = new File(System.getProperty("java.io.tmpdir"), "test.xls");
		ReportUtil.writeByteArrayToFile(outputFile, out.toByteArray());

		InputStream is = new FileInputStream(outputFile);
		POIFSFileSystem fs = new POIFSFileSystem(is);
		HSSFWorkbook wb = new HSSFWorkbook(fs);

		Assert.assertEquals(2, wb.getNumberOfSheets());
		Assert.assertEquals("patients", wb.getSheetName(0));
		Assert.assertEquals("visits", wb.getSheetName(1));
	}
}
