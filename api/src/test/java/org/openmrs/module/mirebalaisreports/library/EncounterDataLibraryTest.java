/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.mirebalaisreports.library;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.mirebalaisreports.definitions.BaseMirebalaisReportTest;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.encounter.EvaluatedEncounterData;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.service.EncounterDataService;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.context.EncounterEvaluationContext;
import org.openmrs.module.reporting.query.encounter.EncounterIdSet;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.sql.Timestamp;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class EncounterDataLibraryTest extends BaseMirebalaisReportTest {

    @Autowired
    private EncounterDataLibrary library;

    @Autowired
    private EncounterDataService encounterDataService;

    @Autowired
    TestDataManager data;

    private EncounterEvaluationContext context;
    private Encounter e1;
    private Encounter e2;
    private Encounter e3;

    @Before
    public void setUp() throws Exception {
        VisitType visitType = emrApiProperties.getAtFacilityVisitType();
        Location visitLocation = mirebalaisReportsProperties.getMirebalaisHospitalLocation();
        Location outpatient = mirebalaisReportsProperties.getOutpatientLocation();
        EncounterType checkIn = mirebalaisReportsProperties.getCheckInEncounterType();
        EncounterType admission = mirebalaisReportsProperties.getAdmissionEncounterType();
        Location womensWard = mirebalaisReportsProperties.getWomensInternalMedicineLocation();

        Patient p1 = data.randomPatient().female().birthdate("1946-05-26", false).dateCreated("2013-10-01").uuid("be7890be-36a4-11e3-b90a-a351ac6b1528").save();
        Visit v1 = data.visit().patient(p1).started("2013-10-02 09:15:00").stopped("2013-10-14 04:30:00").location(visitLocation).visitType(visitType).save();
        e1 = data.encounter().visit(v1).encounterType(checkIn).location(outpatient).encounterDatetime("2013-10-02 09:15:00").save();
        e2 = data.encounter().visit(v1).encounterType(admission).location(womensWard).encounterDatetime("2013-10-02 12:30:00").save();
        e3 = data.encounter().visit(v1).encounterType(mirebalaisReportsProperties.getConsultEncounterType()).location(womensWard).encounterDatetime("2013-10-02 12:45:00").save();
        Obs rvd = data.obs().encounter(e3).concept("RETURN VISIT DATE", "PIH").value(DateUtil.parseDate("2013-11-02", "yyyy-MM-dd")).save();

        Patient p2 = data.randomPatient().female().birthdate("1975-01-02", false).dateCreated("2013-10-01").uuid("d2c28390-36a4-11e3-b90a-a351ac6b1528").save();
        Visit v2 = data.visit().patient(p2).visitType(visitType).started("2013-10-01 17:30:00").stopped("2013-10-03 12:45:00").location(visitLocation).save();
        data.encounter().visit(v2).encounterType(checkIn).location(outpatient).encounterDatetime("2013-10-01 17:30:00").save();
        data.encounter().visit(v2).encounterType(admission).location(womensWard).encounterDatetime("2013-10-01 18:30:00").save();
        data.encounter().visit(v1).encounterType(mirebalaisReportsProperties.getExitFromInpatientEncounterType()).location(womensWard).encounterDatetime("2013-10-02 23:45:00").save();

        Context.flushSession();

        context = new EncounterEvaluationContext();
    }

    @Test
    public void testName() throws Exception {
        int encId1 = e1.getId();
        int encId2 = e2.getId();
        int encId3 = e3.getId();
        context.setBaseEncounters(new EncounterIdSet(encId1, encId2, encId3));
        EncounterDataDefinition definition = library.getReturnVisitDate();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(encId1), nullValue());
        assertThat(data.getData().get(encId2), nullValue());
        assertThat((Timestamp) data.getData().get(encId3), is(new Timestamp(DateUtil.parseDate("2013-11-02", "yyyy-MM-dd").getTime())));
    }

    @Test
    public void testComments() throws Exception {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getComments();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((String) data.getData().get(10003), is("comment"));
    }

    @Test
    public void testDisposition() throws Exception {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getDisposition();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((String) data.getData().get(10003), is("Transfer within hospital"));
    }

    @Test
    public void testMostRecentZlEmrId() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getMostRecentZLEmrId();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String)data.getData().get(10001), is("Y2C4VA"));
        assertThat((String)data.getData().get(10002), is("Y2C4VA"));
        assertThat((String) data.getData().get(10003), is("Y2C4VA"));
    }

    @Test
    public void testMostRecentZlEmrIdLocation() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getMostRecentZLEmrIdLocation();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String)data.getData().get(10001), is("Antepartum Ward"));
        assertThat((String)data.getData().get(10002), is("Antepartum Ward"));
        assertThat((String) data.getData().get(10003), is("Antepartum Ward"));
    }

    @Test
    public void testUnknownPatient() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getUnknownPatient();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String)data.getData().get(10001), is("Unknown Patient"));
        assertThat((String)data.getData().get(10002), is("Unknown Patient"));
        assertThat((String) data.getData().get(10003), is("Unknown Patient"));
    }

    @Test
    public void testLocationName() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getLocationName();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String)data.getData().get(10001), is("Clinic Registration"));
        assertThat((String)data.getData().get(10002), is("Outpatient Clinic"));
        assertThat((String)data.getData().get(10003), is("Unknown Location"));
    }

    @Test
    public void testGender() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getGender();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String)data.getData().get(10001), is("F"));
        assertThat((String)data.getData().get(10002), is("F"));
        assertThat((String)data.getData().get(10003), is("F"));
    }

    @Test
    public void testBirthDateYMD() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getBirthDateYMD();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String)data.getData().get(10001), is("1946-05-26"));
        assertThat((String)data.getData().get(10002), is("1946-05-26"));
        assertThat((String)data.getData().get(10003), is("1946-05-26"));
    }

    @Test
    public void testVitalStatusDeathDate() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getVitalStatusDeathDate();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((Timestamp) data.getData().get(10001), is(new Timestamp(DateUtil.parseDate("1996-05-26", "yyyy-MM-dd").getTime())));
        assertThat((Timestamp) data.getData().get(10002), is(new Timestamp(DateUtil.parseDate("1996-05-26", "yyyy-MM-dd").getTime())));
        assertThat((Timestamp) data.getData().get(10003), is(new Timestamp(DateUtil.parseDate("1996-05-26", "yyyy-MM-dd").getTime())));
    }

    @Test
    public void testPreferredAddressDepartment() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getPreferredAddressDepartment();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String)data.getData().get(10001), is("IN"));
        assertThat((String)data.getData().get(10002), is("IN"));
        assertThat((String)data.getData().get(10003), is("IN"));
    }

    @Test
    public void testPreferredAddressCommune() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getPreferredAddressCommune();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String)data.getData().get(10001), is("Indianapolis"));
        assertThat((String)data.getData().get(10002), is("Indianapolis"));
        assertThat((String)data.getData().get(10003), is("Indianapolis"));
    }

    @Test
    public void testPreferredAddressSection() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getPreferredAddressSection();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String)data.getData().get(10001), is("RBI"));
        assertThat((String)data.getData().get(10002), is("RBI"));
        assertThat((String)data.getData().get(10003), is("RBI"));
    }

    @Test
    public void testPreferredAddressLocality() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getPreferredAddressLocality();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String)data.getData().get(10001), is("1050 Wishard Blvd."));
        assertThat((String)data.getData().get(10002), is("1050 Wishard Blvd."));
        assertThat((String)data.getData().get(10003), is("1050 Wishard Blvd."));
    }

    @Test
    public void testPreferredAddressStreetLandmark() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getPreferredAddressStreetLandmark();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String)data.getData().get(10001), is("RG5"));
        assertThat((String)data.getData().get(10002), is("RG5"));
        assertThat((String)data.getData().get(10003), is("RG5"));
    }

    @Test
    public void testTransferOutLocation() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getTransferOutLocation();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((String)data.getData().get(10003), is("Site not supported by Zanmi Lasante"));
    }

    @Test
    public void testTraumaType() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getTraumaType();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((String)data.getData().get(10003), is("Type of trauma"));
    }

    @Test
    public void testCodedDiagnosis() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getCodedDiagnosis();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((BigInteger) data.getData().get(10003), is(new BigInteger("2")));
    }

    @Test
    public void testNonCodedDiagnosis() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getNonCodedDiagnosis();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((BigInteger) data.getData().get(10003), is(new BigInteger("2")));
    }

    @Test
    public void testEncounterID() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getEncounterID();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((Integer) data.getData().get(10001), is(10001));
        assertThat((Integer) data.getData().get(10002), is(10002));
        assertThat((Integer) data.getData().get(10003), is(10003));
    }

    @Test
    public void testEncounterName() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getEncounterName();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String) data.getData().get(10001), is("Exit from Inpatient"));
        assertThat((String) data.getData().get(10002), is("Admission"));
        assertThat((String) data.getData().get(10003), is("Transfer"));
    }

    @Test
    public void testEncounterLocation() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getEncounterLocation();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((String) data.getData().get(10001), is("Clinic Registration"));
        assertThat((String) data.getData().get(10002), is("Outpatient Clinic"));
        assertThat((String) data.getData().get(10003), is("Unknown Location"));
    }

    @Test
    public void testEncounterDateCreated() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getDateCreated();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat((Timestamp) data.getData().get(10001), is(new Timestamp(DateUtil.parseDate("2013-10-01", "yyyy-MM-dd").getTime())));
        assertThat((Timestamp) data.getData().get(10002), is(new Timestamp(DateUtil.parseDate("2013-10-03", "yyyy-MM-dd").getTime())));
        assertThat((Timestamp) data.getData().get(10003), is(new Timestamp(DateUtil.parseDate("2013-10-02", "yyyy-MM-dd").getTime())));
    }

    @Test
    public void testSurgicalService() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getSurgicalService();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((String) data.getData().get(10003), is("Surgical service"));
    }

    @Test
    @Ignore
    public void testOtherAssistant() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getOtherAssistant();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((String) data.getData().get(10003), is(""));
    }

    @Test
    public void testAttending() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getAttending();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((String) data.getData().get(10003), is("Paula Morris"));
    }

    @Test
    @Ignore
    public void testAssistantOne() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getAssistantOne();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((String) data.getData().get(10003), is(""));
    }

    @Test
    public void testCreator() throws EvaluationException {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
        EncounterDataDefinition definition = library.getCreator();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((String) data.getData().get(10003), is("Paula Morris"));
    }

}
