package org.openmrs.module.mirebalaisreports;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mirebalaisreports.fragment.controller.CohortFragmentController;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reporting.indicator.dimension.CohortIndicatorAndDimensionResult;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.TsvReportRenderer;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Ignore
public class IdSetBugTest extends BaseModuleWebContextSensitiveTest {

    public static final int TIMES = 5;

    ThreadLocal<StopWatch> stopWatchHolder = new ThreadLocal<StopWatch>();

    @Autowired
    ReportDefinitionService reportDefinitionService;

    @Autowired
    AllDefinitionLibraries libraries;

    @Autowired
    DataSetDefinitionService dataSetDefinitionService;

    @Autowired
    EvaluationService evaluationService;

    Set<Integer> running = Collections.synchronizedSet(new HashSet<Integer>());

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
    public void testMultipleAtATime() throws Exception {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < TIMES; ++i) {
            Thread thread = new Thread(new TestRun(i));
            thread.start();
            threads.add(thread);
            running.add(i);
        }

        for (Thread thread : threads) {
            thread.join();
        }

        Context.openSession();
        try {
            Context.authenticate("junit", "Test1234");
            evaluationService.resetAllIdSets();
        } finally {
            Context.closeSession();
        }

        stopWatch.stop();
        System.out.println("done with " + TIMES + " runs in: " + stopWatch.toString());
        System.out.println("Still running: " + running);
        assertThat(running.size(), is(0));
    }

    class TestRun implements Runnable {

        private Integer runNumber;

        public TestRun(Integer name) {
            this.runNumber = name;
        }

        @Override
        public void run() {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            stopWatchHolder.set(stopWatch);
            print("Starting: " + runNumber);
            try {
                Context.openSession();
                Context.authenticate("junit", "Test1234");
                try {
                    ReportDefinition def = reportDefinitionService.getDefinitionByUuid(MirebalaisReportsProperties.INPATIENT_STATS_DAILY_REPORT_DEFINITION_UUID);
                    ReportData data = reportDefinitionService.evaluate(def, buildEvalContext(runNumber));
                    print(data);

                    DataSet dataSet = data.getDataSets().values().iterator().next();
                    CohortIndicatorAndDimensionResult result = (CohortIndicatorAndDimensionResult) dataSet.iterator().next().getColumnValue("censusAtStart:8355ad15-c3c9-4471-a1e7-dc18b7983087");
                    CohortFragmentController controller = new CohortFragmentController();
                    SimpleObject simple = controller.getCohort(result.getCohortIndicatorAndDimensionCohort(), libraries, dataSetDefinitionService);
                    print("Simplified: " + simple);
                    running.remove(runNumber);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
//                    System.out.println("There are " + Cohorts.allPatients(null).size() + " patients");
            } finally {
                Context.closeSession();
            }
            print("Done with: " + runNumber);
        }
    }

    private EvaluationContext buildEvalContext(int dateOffset) {
        EvaluationContext context = new EvaluationContext();
        Date date = DateUtil.parseYmd("2014-03-02");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, dateOffset);
        date = cal.getTime();
        context.addParameterValue("day", date);
        return context;
    }

    private void print(ReportData data) throws IOException {
        StopWatch stopWatch = stopWatchHolder.get();
        stopWatch.split();
        System.out.println(stopWatch.toSplitString() + ": data set ready");
        new TsvReportRenderer().render(data, null, System.out);
    }

    private void print(String message) {
        StopWatch stopWatch = stopWatchHolder.get();
        stopWatch.split();
        System.out.println(stopWatch.toSplitString() + ": " + message);
    }

}
