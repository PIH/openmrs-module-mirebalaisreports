package org.openmrs.module.mirebalaisreports.definitions;

import org.apache.poi.ss.usermodel.Workbook;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.dispensing.DispensingProperties;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.haiticore.metadata.HaitiPersonAttributeTypes;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.metadata.Metadata;
import org.openmrs.module.pihcore.reporting.dataset.manager.EncounterDataSetManagerTest;
import org.openmrs.module.reporting.common.Birthdate;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ExcelUtil;
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
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@SkipBaseSetup
public class FullDataExportReportManagerTest extends EncounterDataSetManagerTest {

    @Autowired
    FullDataExportBuilder builder;

    @Autowired
    DispensingProperties dispensingProperties;

    @Autowired
    ConceptService conceptService;

    @Autowired
    LocationService locationService;

    @Autowired
    ProviderService providerService;

    @Autowired
    DispositionService dispositionService;

    @Autowired
    MirebalaisReportsProperties mirebalaisReportsProperties;

    @Autowired @Qualifier("adminService")
    AdministrationService administrationService;

    @Autowired
    TestDataManager data;

    private Encounter e1;

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        dispositionService.setDispositionConfig("testDispositionConfig.json");
        Context.setLocale(Locale.ENGLISH);
    }

    @Test
	public void shouldSuccessfullyRenderToExcel() throws Exception {

        setUpPatientsBasedOnCoreMetadata();

        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", asList("patients"));
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
        Workbook wb = ExcelUtil.loadWorkbookFromInputStream(is);

        Assert.assertEquals(1, wb.getNumberOfSheets());
        Assert.assertEquals("patients", wb.getSheetName(0));
    }

    @Test
    public void testEncountersExport() throws Exception {
        administrationService.setGlobalProperty("reporting.defaultLocale", "fr");
        Patient patient = data.randomPatient()
                .birthdate("2001-02-03")
                .gender("F")
                .identifier(emrApiProperties.getPrimaryIdentifierType(), "2AA00V", locationService.getLocation("Hôpital Universitaire de Mirebalais - Prensipal"))
                .save();

        User checkinUser = data.user().personName("Checkin", null, "Clerk").gender("M").username("checkin").save();
        Provider checkInProvider = data.provider().person(checkinUser.getPerson()).save();

        User nurseUser = data.user().personName("Nurse", null, "Nursing").gender("F").username("nurse").save();
        Provider nurseProvider = data.provider().person(nurseUser.getPerson()).save();

        Visit visit = data.visit().patient(patient).started(DateUtil.parseYmdhms("2013-08-30 09:00:00")).visitType(emrApiProperties.getAtFacilityVisitType()).save();

        Encounter e1 = data.encounter().visit(visit)
                .encounterType(emrApiProperties.getCheckInEncounterType())
                .location(locationService.getLocation("Biwo Resepsyon"))
                .encounterDatetime("2013-08-30 09:00:00")
                .provider(mirebalaisReportsProperties.getAdministrativeClerkEncounterRole(), checkInProvider)
                .provider(mirebalaisReportsProperties.getNurseEncounterRole(), nurseProvider)
                .obs(mirebalaisReportsProperties.getTypeOfPatientConcept(), conceptService.getConceptByUuid("3cda41f2-26fe-102b-80cb-0017a47871b2"))
                .creator(checkinUser).save();

        Encounter e2 = data.encounter().visit(visit)
                .encounterType(getVitalsEncounterType())
                .location(locationService.getLocation("Klinik Ekstèn"))
                .encounterDatetime("2013-08-30 09:15:00")
                .provider(mirebalaisReportsProperties.getNurseEncounterRole(), nurseProvider)
                .obs(dispositionService.getDispositionDescriptor().getDispositionConcept(), conceptService.getConcept("Admit to hospital")) // unrealistic data
                .obs(dispositionService.getDispositionDescriptor().getAdmissionLocationConcept(), locationService.getLocation("Sal Fanm").getId().toString())
                .obs(dispositionService.getDispositionDescriptor().getInternalTransferLocationConcept(), locationService.getLocation("Sal Gason").getId().toString()) // would never really have this and admission location
                .creator(nurseUser).save();


        Visit oldVisit = data.visit().patient(patient).started(DateUtil.parseYmdhms("2012-01-01 09:00:00")).stopped(DateUtil.parseYmdhms("2012-01-01 12:00:00")).visitType(emrApiProperties.getAtFacilityVisitType()).save();
        Encounter oldEncounter = data.encounter().visit(oldVisit)
                .encounterType(emrApiProperties.getCheckInEncounterType())
                .location(locationService.getLocation("Biwo Resepsyon"))
                .encounterDatetime(oldVisit.getStartDatetime())
                .provider(mirebalaisReportsProperties.getAdministrativeClerkEncounterRole(), checkInProvider)
                .creator(checkinUser).save();

        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", asList("encounters"));
        FullDataExportReportManager reportManager = builder.buildReportManager(configuration);

        EvaluationContext context = new EvaluationContext();
        context.addParameterValue("startDate", DateUtil.parseYmd("2013-08-01"));
        context.addParameterValue("endDate", DateUtil.parseYmd("2013-08-31"));

        ReportDefinition reportDefinition = reportManager.constructReportDefinition();
        ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);

        new TsvReportRenderer().render(reportData, null, System.out);

        DataSet dataSet = reportData.getDataSets().get("encounters");
        assertThat(sizeOf(dataSet), is(2));

        for (DataSetRow row : dataSet) {
            assertThat((String) row.getColumnValue("zlEmrId"), is("2AA00V"));
            assertThat((Double) row.getColumnValue("age"), is(12.6));
            assertThat((String) row.getColumnValue("gender"), is("F"));
            assertThat((Integer) row.getColumnValue("visitId"), is(visit.getId()));
            assertThat((Timestamp) row.getColumnValue("visitStart"), is(Timestamp.valueOf("2013-08-30 09:00:00")));
            assertNull((Timestamp) row.getColumnValue("visitStop"));

            Integer encounterId = (Integer) row.getColumnValue("encounterId");
            if (encounterId.equals(e1.getEncounterId())) {
                assertThat((Integer) row.getColumnValue("encounterId"), is(e1.getEncounterId()));
                assertThat((String) row.getColumnValue("encounterType"), is(getCheckInEncounterType().getName()));
                assertThat((String) row.getColumnValue("encounterLocation"), is("Biwo Resepsyon"));
                assertThat((Timestamp) row.getColumnValue("encounterDatetime"), is(Timestamp.valueOf("2013-08-30 09:00:00")));
                assertThat(row.getColumnValue("disposition"), nullValue());
                assertThat((String) row.getColumnValue("enteredBy"), is("Checkin Clerk"));
                assertThat((String) row.getColumnValue("allProviders"), anyOf(is("Checkin Clerk, Nurse Nursing"), is("Nurse Nursing, Checkin Clerk")));
                assertThat((Integer) row.getColumnValue("numberOfProviders"), is(2));
                assertThat((String) row.getColumnValue("administrativeClerk"), is("Checkin Clerk"));
                assertThat((String) row.getColumnValue("nurse"), is("Nurse Nursing"));
                assertThat((String) row.getColumnValue("consultingClinician"), is(nullValue()));
                assertThat(((Birthdate) row.getColumnValue("birthdate")).getBirthdate(), sameDateTime(DateUtil.parseYmd("2001-02-03")));
                assertThat((Boolean) row.getColumnValue("birthdate_estimated"), is(true));
                assertThat((String) row.getColumnValue("admissionStatus"), is("Hospitalisé"));
                assertThat((String) row.getColumnValue("requestedAdmissionLocation"), is(nullValue()));
                assertThat((String) row.getColumnValue("requestedTransferLocation"), is(nullValue()));
            }
            else if (encounterId.equals(e2.getEncounterId())) {
                assertThat((Integer) row.getColumnValue("encounterId"), is(e2.getEncounterId()));
                assertThat((String) row.getColumnValue("encounterType"), is(getVitalsEncounterType().getName()));
                assertThat((String) row.getColumnValue("encounterLocation"), is("Klinik Ekstèn"));
                assertThat((Timestamp) row.getColumnValue("encounterDatetime"), is(Timestamp.valueOf("2013-08-30 09:15:00")));
                assertThat((String) row.getColumnValue("disposition"), is("Admettre à l'hôpital"));
                assertThat((String) row.getColumnValue("enteredBy"), is("Nurse Nursing"));
                assertThat((String) row.getColumnValue("allProviders"), is("Nurse Nursing"));
                assertThat((Integer) row.getColumnValue("numberOfProviders"), is(1));
                assertThat((String) row.getColumnValue("administrativeClerk"), is(nullValue()));
                assertThat((String) row.getColumnValue("nurse"), is("Nurse Nursing"));
                assertThat((String) row.getColumnValue("consultingClinician"), is(nullValue()));
                assertThat(((Birthdate) row.getColumnValue("birthdate")).getBirthdate(), sameDateTime(DateUtil.parseYmd("2001-02-03")));
                assertThat((Boolean) row.getColumnValue("birthdate_estimated"), is(true));
                assertThat((String) row.getColumnValue("admissionStatus"), is(nullValue()));
                assertThat((String) row.getColumnValue("requestedAdmissionLocation"), is(locationService.getLocation("Sal Fanm").getName()));
                assertThat((String) row.getColumnValue("requestedTransferLocation"), is(locationService.getLocation("Sal Gason").getName()));
            }
            else {
                Assert.fail("Only encounters should be e1 and e2");
            }
        }
    }

    private Matcher<? super Date> sameDateTime(final Date date) {
        return new BaseMatcher<Date>() {
            @Override
            public boolean matches(Object o) {
                return ((Date) o).getTime() == date.getTime();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("same date/time as " + date + " ignoring timezone");
            }
        };
    }

    private int sizeOf(DataSet dataSet) {
        int i = 0;
        for (DataSetRow row : dataSet) {
            ++i;
        }
        return i;
    }

    @Test
    public void testDispensingExport() throws Exception {

        // simple export that adds drugs to the sample data
        executeDataSet("org/openmrs/module/mirebalaisreports/dispensingExportTestData.xml");

        Patient patient = data.randomPatient()
                .age(40)
                .identifier(emrApiProperties.getPrimaryIdentifierType(), "2AA00V", locationService.getLocation("Hôpital Universitaire de Mirebalais - Prensipal"))
                .save();
        Date date = new DateTime(2013, 8, 30, 10, 11, 12).toDate();

        Visit visit = data.visit().started(date).stopped(date).patient(patient).visitType(emrApiProperties.getAtFacilityVisitType()).save();

        Location mirebalaisCdiParent = locationService.getLocation("Mirebalais");
        Provider dispensedBy = data.randomProvider().save();
        Provider prescribedBy = data.randomProvider().save();

        // note that we are just using a check-in encounter, since encounter type isn't relevant to the query
        Encounter enc = data.encounter().visit(visit).patient(patient)
                .encounterType(emrApiProperties.getCheckInEncounterType())
                .provider(mirebalaisReportsProperties.getDispenserEncounterRole(), dispensedBy)
                .provider(mirebalaisReportsProperties.getPrescribedByEncounterRole(), prescribedBy)
                .encounterDatetime(date).location(mirebalaisCdiParent).save();

        Concept someMedication = conceptService.getConceptByUuid("3cccd35a-26fe-102b-80cb-0017a47871b2");
        Concept someFrequency = conceptService.getConceptByUuid("9b0068ac-4104-4bea-ba76-851e5faa9f2a");
        Concept someDurationUnits = conceptService.getConceptByUuid("e0d31892-690e-4063-9570-73d103c8efb0");
        Concept someTypeOfPrescription = conceptService.getConceptByUuid("eefba61c-17c5-40ee-bddc-08e64d39e9b1");
        Drug someDrug = conceptService.getDrugByUuid("05ec820a-d297-44e3-be6e-698531d9dd3f");

        Obs medication = data.obs().person(patient).concept(dispensingProperties.getMedicationConcept()).obsDatetime(date)
                .value(someMedication).value(someDrug).get();
        Obs dosage = data.obs().person(patient).concept(dispensingProperties.getDosageConcept()).obsDatetime(date)
                .value(100).get();
        Obs dosageUnits = data.obs().person(patient).concept(dispensingProperties.getDosageUnitsConcept()).obsDatetime(date)
                .value("mg").get();
        Obs frequency = data.obs().person(patient).concept(dispensingProperties.getMedicationFrequencyConcept()).obsDatetime(date)
                .value(someFrequency).get();
        Obs duration = data.obs().person(patient).concept(dispensingProperties.getMedicationDurationConcept()).obsDatetime(date)
                .value(30).get();
        Obs durationUnits = data.obs().person(patient).concept(dispensingProperties.getMedicationDurationUnitsConcept()).obsDatetime(date)
                .value(someDurationUnits).get();
        Obs amount = data.obs().person(patient).concept(dispensingProperties.getDispensedAmountConcept()).obsDatetime(date)
                .value(60).get();
        Obs instructions = data.obs().person(patient).concept(dispensingProperties.getAdministrationInstructions()).obsDatetime(date)
                .value("some instructions").get();

        data.obs().person(patient).concept(dispensingProperties.getDispensingConstructConcept())
                .obsDatetime(date).member(medication).member(dosage).member(dosageUnits).member(frequency)
                .member(duration).member(durationUnits).member(amount).member(instructions)
                .encounter(enc).save();

        data.obs().person(patient).concept(mirebalaisReportsProperties.getTimingOfPrescriptionConcept())
                .obsDatetime(date).value(someTypeOfPrescription)
                .encounter(enc).save();

        data.obs().person(patient).concept(mirebalaisReportsProperties.getDischargeLocationConcept())
                .obsDatetime(date).value(mirebalaisCdiParent.getId().toString())
                .encounter(enc).save();

        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", asList("dispensing"));
        FullDataExportReportManager reportManager = builder.buildReportManager(configuration);

        EvaluationContext context = new EvaluationContext();
        context.addParameterValue("startDate", DateUtil.parseDate("2013-07-01", "yyyy-MM-dd"));
        context.addParameterValue("endDate", DateUtil.parseDate("2013-09-30", "yyyy-MM-dd"));

        ReportDefinition reportDefinition = reportManager.constructReportDefinition();
        ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);

        DataSet dispensingDataSet = reportData.getDataSets().get("dispensing");
        DataSetRow row = dispensingDataSet.iterator().next();

        assertThat((String) row.getColumnValue("medication"), is("Aspirin Full Drug Name"));
        assertThat(Double.valueOf((String) row.getColumnValue("dosage")), is(100.0));
        assertThat((String) row.getColumnValue("dosageUnits"), is("mg"));
        assertThat((String) row.getColumnValue("frequency"), is("Seven times a day"));
        assertThat(Double.valueOf((String) row.getColumnValue("duration")), is(30.0));
        assertThat((String) row.getColumnValue("durationUnits"), is("Hours"));
        assertThat(Double.valueOf((String) row.getColumnValue("amount")), is(60.0));
        assertThat((String) row.getColumnValue("instructions"), is("some instructions"));
        assertThat((String) row.getColumnValue("patientIdentifier"), is("2AA00V"));
        assertThat((String) row.getColumnValue("dispensedLocation"), is("Mirebalais"));
        assertThat((String) row.getColumnValue("dispensedDatetime"), is("30 Aug 2013 10:11 AM"));
        assertThat((String) row.getColumnValue("dispensedBy"), is(dispensedBy.getName()));
        assertThat((String) row.getColumnValue("prescribedBy"), is(prescribedBy.getName()));
        assertThat((String) row.getColumnValue("typeOfPrescription"), is("Discharge"));
        assertThat((String) row.getColumnValue("locationOfPrescription"), is("Mirebalais"));
    }

    private void setUpPatientsBasedOnCoreMetadata() {
        Patient patient = data.patient().name("Christy","Lee").gender("F")
                .identifier(mirebalaisReportsProperties.getZlEmrIdentifierType(), "TT200E", locationService.getLocation("Klinik Ekstèn"))
                .address("1050 Wishard Blvd", "RG5", "Indianapolis", "IN").save();
        data.patient().name("Bobby", "Joe").gender("M")
                .identifier(mirebalaisReportsProperties.getZlEmrIdentifierType(), "TT201C", locationService.getLocation("Klinik Ekstèn"))
                .address("", "", "Kapina").save();
        data.encounter().patient(patient).encounterType(getRegistrationEncounterType())
                .encounterDatetime("2013-09-08").location(locationService.getLocation("Klinik Ekstèn")).save();
    }

    @Test @Ignore("H2 cannot handle DATE() function to cast a timestamp to a date")
    public void shouldSuccessfullyRenderConsultationsToExcel() throws Exception {
        PatientIdentifierType zlEmrId = mirebalaisReportsProperties.getZlEmrIdentifierType();
        Location mirebalaisHospital = locationService.getLocation("Hôpital Universitaire de Mirebalais - Prensipal");
        Location clinicRegistration = locationService.getLocation("Biwo Resepsyon");
        Location womensWard = locationService.getLocation("Sal Fanm");
        PersonAttributeType unknownPatient = Metadata.lookup(HaitiPersonAttributeTypes.UNKNOWN_PATIENT);
        VisitType visitType = emrApiProperties.getAtFacilityVisitType();
        EncounterType checkIn = getCheckInEncounterType();
        EncounterType admission = getAdmissionEncounterType();
        EncounterType consult = getConsultationEncounterType();

        EncounterRole consultingClinician = mirebalaisReportsProperties.getConsultingClinicianEncounterRole();

        User paulaMorris = data.user().personName("Paula", null, "Morris").username("pmorris").gender("F").save();
        Provider unknownProvider = providerService.getProvider(1);

        PersonAddress addr = new PersonAddress();
        addr.setAddress1("1050 Wishard Blvd.");
        addr.setAddress2("RG5");
        addr.setAddress3("RBI");
        addr.setCityVillage("Indianapolis");
        addr.setStateProvince("IN");

        Patient p1 = data.randomPatient().clearIdentifiers().identifier(zlEmrId, "Y2C4VA", mirebalaisHospital).personAttribute(unknownPatient, "false")
                .female().birthdate("1946-05-26", false).dateCreated("2013-10-01").uuid("be7890be-36a4-11e3-b90a-a351ac6b1528")
                .address(addr)
                .dead(true).deathDate("2013-12-01 00:00:00.0").causeOfDeath("unknown", "PIH").save();

        Visit v1 = data.visit().patient(p1).started("2013-10-02 09:15:00").stopped("2013-10-14 04:30:00").location(mirebalaisHospital).visitType(visitType).save();
        Encounter e1 = data.encounter().visit(v1).encounterType(checkIn).location(clinicRegistration).encounterDatetime("2013-10-02 09:15:00").dateCreated("2013-10-01 00:00:00.0").creator(paulaMorris).save();
        Encounter e2 = data.encounter().visit(v1).encounterType(admission).location(womensWard).encounterDatetime("2013-10-02 12:30:00").dateCreated("2013-10-03 00:00:00.0").creator(paulaMorris).save();
        Encounter e3 = data.encounter().visit(v1).encounterType(consult).location(womensWard).encounterDatetime("2013-10-02 12:45:00")
                .dateCreated("2013-10-02 00:00:00.0").creator(paulaMorris).provider(consultingClinician, unknownProvider).save();

        FullDataExportBuilder.Configuration configuration = new FullDataExportBuilder.Configuration("uuid", "prefix", asList("consultations-new"));
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
        Workbook wb = ExcelUtil.loadWorkbookFromInputStream(is);

        Assert.assertEquals(1, wb.getNumberOfSheets());
        Assert.assertEquals("consultations", wb.getSheetName(0));
    }

}
