package org.openmrs.module.mirebalaisreports.definitions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.dispensing.DispensingProperties;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
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
import java.util.Date;

@SkipBaseSetup
public class FullDataExportReportManagerTest extends BaseMirebalaisReportTest {

    @Autowired
    FullDataExportBuilder builder;

    @Autowired
    DispensingProperties dispensingProperties;

    @Autowired
    ConceptService conceptService;

    @Autowired
    LocationService locationService;
    
    @Autowired
    TestDataManager data;

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

    @Test
    public void testDispensingExport() throws Exception {

        Patient patient = data.randomPatient()
                .identifier(emrApiProperties.getPrimaryIdentifierType(), "2AA00V", mirebalaisReportsProperties.getMirebalaisHospitalLocation())
                .save();
        Date date = new DateTime(2013, 8, 30, 10, 11, 12).toDate();

        Visit visit = data.visit().started(date).stopped(date).patient(patient).visitType(emrApiProperties.getAtFacilityVisitType()).save();

        Location mirebalaisHospital = locationService.getLocationByUuid("a084f714-a536-473b-94e6-ec317b152b43");
        Provider dispensedBy = data.randomProvider().save();
        Provider prescribedBy = data.randomProvider().save();

        // note that we are just using a consult encounter, since encounter type isn't relevant to the query
        Encounter enc = data.encounter().visit(visit).patient(patient)
                .encounterType(emrApiProperties.getConsultEncounterType())
                .provider(mirebalaisReportsProperties.getDispenserEncounterRole(), dispensedBy)
                .provider(mirebalaisReportsProperties.getPrescribedByEncounterRole(), prescribedBy)
                .encounterDatetime(date).location(mirebalaisHospital).save();

        Concept someMedication = conceptService.getConceptByUuid("3cccd35a-26fe-102b-80cb-0017a47871b2");
        Concept someFrequency = conceptService.getConceptByUuid("9b0068ac-4104-4bea-ba76-851e5faa9f2a");
        Concept someDurationUnits = conceptService.getConceptByUuid("e0d31892-690e-4063-9570-73d103c8efb0");
        Concept someTypeOfPrescription = conceptService.getConceptByUuid("eefba61c-17c5-40ee-bddc-08e64d39e9b1");

        Obs medication = data.obs().person(patient).concept(dispensingProperties.getMedicationConcept()).obsDatetime(date)
                .value(someMedication).save();
        Obs dosage = data.obs().person(patient).concept(dispensingProperties.getDosageConcept()).obsDatetime(date)
                .value(100).save();
        Obs dosageUnits = data.obs().person(patient).concept(dispensingProperties.getDosageUnitsConcept()).obsDatetime(date)
                .value("mg").save();
        Obs frequency = data.obs().person(patient).concept(dispensingProperties.getMedicationFrequencyConcept()).obsDatetime(date)
                .value(someFrequency).save();
        Obs duration = data.obs().person(patient).concept(dispensingProperties.getMedicationDurationConcept()).obsDatetime(date)
                .value(30).save();
        Obs durationUnits = data.obs().person(patient).concept(dispensingProperties.getMedicationDurationUnitsConcept()).obsDatetime(date)
                .value(someDurationUnits).save();
        Obs amount = data.obs().person(patient).concept(dispensingProperties.getDispensedAmountConcept()).obsDatetime(date)
                .value(60).save();
        Obs instructions = data.obs().person(patient).concept(dispensingProperties.getAdministrationInstructions()).obsDatetime(date)
                .value("some instructions").save();

        data.obs().person(patient).concept(dispensingProperties.getDispensingConstructConcept())
                .obsDatetime(date).member(medication).member(dosage).member(dosageUnits).member(frequency)
                .member(duration).member(durationUnits).member(amount).member(instructions)
                .encounter(enc).save();

        data.obs().person(patient).concept(mirebalaisReportsProperties.getTimingOfPrescriptionConcept())
                .obsDatetime(date).value(someTypeOfPrescription)
                .encounter(enc).save();

        data.obs().person(patient).concept(mirebalaisReportsProperties.getDischargeLocationConcept())
                .obsDatetime(date).value(mirebalaisHospital.getId().toString())
                .encounter(enc).save();

        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", Arrays.asList("dispensing"));
        FullDataExportReportManager reportManager = builder.buildReportManager(configuration);

        EvaluationContext context = new EvaluationContext();
        context.addParameterValue("startDate", DateUtil.parseDate("2013-07-01", "yyyy-MM-dd"));
        context.addParameterValue("endDate", DateUtil.parseDate("2013-09-30", "yyyy-MM-dd"));

        ReportDefinition reportDefinition = reportManager.constructReportDefinition();
        ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);

        DataSet dispensingDataSet = reportData.getDataSets().get("dispensing");
        DataSetRow row = dispensingDataSet.iterator().next();

        assertThat((String) row.getColumnValue("medication"), is("Aspirin"));
        assertThat(Double.valueOf((String) row.getColumnValue("dosage")), is(100.0));
        assertThat((String) row.getColumnValue("dosageUnits"), is("mg"));
        assertThat((String) row.getColumnValue("frequency"), is("Seven times a day"));
        assertThat(Double.valueOf((String) row.getColumnValue("duration")), is(30.0));
        assertThat((String) row.getColumnValue("durationUnits"), is("Hours"));
        assertThat(Double.valueOf((String) row.getColumnValue("amount")), is(60.0));
        assertThat((String) row.getColumnValue("instructions"), is("some instructions"));
        assertThat((String) row.getColumnValue("patientIdentifier"), is("2AA00V"));
        assertThat((String) row.getColumnValue("dispensedLocation"), is("Mirebalais Hospital"));
        assertThat((String) row.getColumnValue("dispensedDatetime"), is("30 Aug 2013 10:11 AM"));
        assertThat((String) row.getColumnValue("dispensedBy"), is(dispensedBy.getName()));
        assertThat((String) row.getColumnValue("prescribedBy"), is(prescribedBy.getName()));
        assertThat((String) row.getColumnValue("typeOfPrescription"), is("Discharge"));
        assertThat((String) row.getColumnValue("locationOfPrescription"), is("Mirebalais Hospital"));

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
