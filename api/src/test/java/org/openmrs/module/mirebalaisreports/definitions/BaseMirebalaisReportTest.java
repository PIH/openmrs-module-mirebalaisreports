package org.openmrs.module.mirebalaisreports.definitions;

import org.junit.Before;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Sets up basic Mirebalais metadata (instead of the standardTestDataset.xml from openmrs-core)
 */
@SkipBaseSetup // because of TRUNK-4051, this annotation will not be picked up, and you need to declare this on your concrete subclass
public abstract class BaseMirebalaisReportTest extends BaseModuleContextSensitiveTest {

    @Autowired
    protected ReportDefinitionService reportDefinitionService;

    @Before
    public void setup() throws Exception {
        executeDataSet("org/openmrs/module/mirebalaisreports/coreMetadata.xml");
        authenticate();
    }

}
