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
import org.openmrs.module.reporting.evaluation.context.EncounterEvaluationContext;
import org.openmrs.module.reporting.query.encounter.EncounterIdSet;
import org.springframework.beans.factory.annotation.Autowired;

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

}
