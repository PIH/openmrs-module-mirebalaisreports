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
import org.openmrs.module.mirebalaisreports.definitions.BaseMirebalaisReportTest;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.encounter.EvaluatedEncounterData;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.service.EncounterDataService;
import org.openmrs.module.reporting.evaluation.EvaluationException;
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

    private EncounterEvaluationContext context;

    @Before
    public void setUp() throws Exception {
        executeDataSet("org/openmrs/module/mirebalaisreports/EncounterDataLibrary-testData.xml");
        context = new EncounterEvaluationContext();
    }

    @Test
    public void testName() throws Exception {
        context.setBaseEncounters(new EncounterIdSet(10001, 10002, 10003));
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
}
