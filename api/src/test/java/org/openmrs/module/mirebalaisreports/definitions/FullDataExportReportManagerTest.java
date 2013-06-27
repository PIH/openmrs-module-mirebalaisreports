package org.openmrs.module.mirebalaisreports.definitions;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Cohort;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.indicator.dimension.CohortIndicatorAndDimensionResult;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.util.ReportUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class FullDataExportReportManagerTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private FullDataExportReportManager reportManager;

    @Autowired
    private MirebalaisReportsProperties mirebalaisReportsProperties;

	@Autowired
	private ReportDefinitionService reportDefinitionService;

	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}

	@Before
    public void setup() throws Exception {
        //executeDataSet("fullDataExportReportTestDataset.xml");
        //fullDataExportReportManager.setMirebalaisReportsProperties(mirebalaisReportsProperties);
		authenticate();
    }

    @Test
    public void shouldSetupTheReportWithAllDataSets() throws Exception {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(reportManager.getStartDateParameter().getName(), new Date());
		params.put(reportManager.getEndDateParameter().getName(), new Date());
		params.put(reportManager.getWhichDataSetParameter().getName(), Arrays.asList("patients", "visits"));

		EvaluationContext context = reportManager.initializeContext(params);
		ReportDefinition reportDefinition = reportManager.constructReportDefinition(context);

		Assert.assertEquals(2, reportDefinition.getDataSetDefinitions().size());

		RenderingMode mode = reportManager.getRenderingModes().get(0);

		ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mode.getRenderer().render(reportData, mode.getArgument(), out);

		ReportUtil.writeByteArrayToFile(new File("/home/mseaton/Desktop/test.xls"), out.toByteArray());
	}
}
