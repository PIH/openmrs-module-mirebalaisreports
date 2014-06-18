package org.openmrs.module.mirebalaisreports;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mirebalaismetadata.MirebalaisMetadataProperties;
import org.openmrs.module.mirebalaisreports.definitions.FullDataExportBuilder;
import org.openmrs.module.mirebalaisreports.definitions.FullDataExportReportManager;
import org.openmrs.module.mirebalaisreports.definitions.InpatientStatsDailyReportManager;
import org.openmrs.module.mirebalaisreports.definitions.ReportManager;
import org.openmrs.module.mirebalaisreports.fragment.controller.CohortFragmentController;
import org.openmrs.module.mirebalaisreports.library.MirebalaisCohortDefinitionLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.Arrays.asList;

@Ignore
public class IdSetConcurrencyTest extends BaseModuleWebContextSensitiveTest {

	@Autowired
	FullDataExportBuilder fullDataExportBuilder;

	@Autowired
	InpatientStatsDailyReportManager inpatientStatsDailyReportManager;

	@Autowired
	ReportService reportService;

    @Autowired
    ReportDefinitionService reportDefinitionService;

    @Autowired
    AllDefinitionLibraries libraries;

    @Autowired
    DataSetDefinitionService dataSetDefinitionService;

    @Autowired
    EvaluationService evaluationService;

	@Autowired
	LocationService locationService;

    @Override
    public Boolean useInMemoryDatabase() {
        return false;
    }

    @Override
    public Properties getRuntimeProperties() {
        Properties rp = super.getRuntimeProperties();
        rp.setProperty("connection.username", "openmrs");
        rp.setProperty("connection.password", "openmrs");
        rp.setProperty("connection.url", "jdbc:mysql://localhost:3306/openmrs_mirebalais?autoReconnect=true&sessionVariables=storage_engine=InnoDB&useUnicode=true&characterEncoding=UTF-8");
        return rp;
    }

    @Test
    public void testReport() throws Exception {

        Context.openSession();
        try {
            Context.authenticate("junit", "Test1234");
            evaluationService.resetAllIdSets();

			System.out.println("Preparing encounter export");

			FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", asList("encounters"));
			FullDataExportReportManager encounterExportReportManager = fullDataExportBuilder.buildReportManager(configuration);

			Map<String, Object> encounterExportParameters = new HashMap<String, Object>();
			encounterExportParameters.put("startDate", DateUtil.getDateTime(2014, 1, 1));
			encounterExportParameters.put("endDate", DateUtil.getDateTime(2014, 1, 31));

			TestRun encounterExportRun = new TestRun(encounterExportReportManager, encounterExportParameters);
			encounterExportRun.start();

			long time = System.currentTimeMillis();

			System.out.println("Waiting 5 seconds.");
			while (System.currentTimeMillis() < (time + 5000)) {
			}

			System.out.println("Getting a Cohort of Patients");
			EvaluationContext context = new EvaluationContext();
			Date evaluationDate = DateUtil.getDateTime(2014, 3, 12);
			context.addParameterValue("date", evaluationDate);
			Location mensWard = locationService.getLocationByUuid(MirebalaisMetadataProperties.MENS_INTERNAL_MEDICINE_LOCATION_UUID);
			CohortDefinition censusCohortDef = libraries.cohortDefinition(MirebalaisCohortDefinitionLibrary.PREFIX + "inpatientAtLocationOnDate", "location", mensWard);
			Cohort c = Context.getService(CohortDefinitionService.class).evaluate(censusCohortDef, context);
			System.out.println("In patients at men's ward on " + ObjectUtil.format(evaluationDate) + ": " + c.size());

			System.out.println("Now getting Encounter data for this Cohort");
			CohortFragmentController controller = new CohortFragmentController();
			SimpleObject data = controller.getCohort(c, libraries, dataSetDefinitionService);
			System.out.println("Got data: " + data);

			Assert.assertFalse(encounterExportRun.isHasError());
		}
		finally {
            Context.closeSession();
        }
    }

	class TestRun extends Thread {
		private ReportManager reportManager;
		private Map<String, Object> parameters;
		private ReportData data;
		private boolean hasError = false;

		public TestRun(ReportManager reportManager, Map<String, Object> parameters) {
			this.reportManager = reportManager;
			this.parameters = parameters;
		}
		@Override
		public void run() {
			try {
				Context.openSession();
				Context.authenticate("junit", "Test1234");
				try {
					EvaluationContext context = new EvaluationContext();
					context.setParameterValues(parameters);
					System.out.println("Running: " + reportManager.getClass().getSimpleName());
					data = reportDefinitionService.evaluate(reportManager.constructReportDefinition(), context);
					System.out.println("Completed: " + reportManager.getClass().getSimpleName());
				}
				catch (Exception ex) {
					hasError = true;
					throw new RuntimeException(ex);
				}
			}
			finally {
				Context.closeSession();
			}
		}
		public ReportData getData() {
			return data;
		}
		public boolean isHasError() {
			return hasError;
		}
	}
}
