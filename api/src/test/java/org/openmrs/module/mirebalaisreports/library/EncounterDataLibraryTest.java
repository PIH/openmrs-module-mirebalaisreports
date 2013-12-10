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

    private EncounterEvaluationContext context;

    @Before
    public void setUp() throws Exception {
        executeDataSet("org/openmrs/module/mirebalaisreports/EncounterDataLibrary-testData.xml");
        context = new EncounterEvaluationContext();
    }

    @Test
    public void testReturnVisitDate() throws Exception {
        context.setBaseEncounters(new EncounterIdSet(10002, 10003));
        EncounterDataDefinition definition = library.getReturnVisitDate();
        EvaluatedEncounterData data = encounterDataService.evaluate(definition, context);
        assertThat(data.getData().get(10001), nullValue());
        assertThat(data.getData().get(10002), nullValue());
        assertThat((Timestamp) data.getData().get(10003), is(new Timestamp(DateUtil.parseDate("2013-11-02", "yyyy-MM-dd").getTime())));
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
