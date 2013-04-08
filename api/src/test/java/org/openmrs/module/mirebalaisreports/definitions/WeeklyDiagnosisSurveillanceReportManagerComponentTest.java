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

package org.openmrs.module.mirebalaisreports.definitions;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emr.EmrConstants;
import org.openmrs.module.emr.TestUtils;
import org.openmrs.module.emr.test.TestTimer;
import org.openmrs.module.emr.test.builder.ConceptBuilder;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.mirebalaisreports.MirebalaisProperties;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.indicator.dimension.CohortIndicatorAndDimensionResult;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertThat;
import static org.openmrs.module.emr.test.ReportingMatchers.isCohortWithExactlyIds;

/**
 *
 */
@Ignore
public class WeeklyDiagnosisSurveillanceReportManagerComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private WeeklyDiagnosisSurveillanceReportManager manager;

    @Autowired
    private DataSetDefinitionService service;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private ObsService obsService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Before
    public void setUp() throws Exception {
        TestTimer timer = new TestTimer();

        ConceptSource icd10 = conceptService.getConceptSourceByName("ICD-10");
        icd10.setUuid(MirebalaisProperties.ICD10_CONCEPT_SOURCE_UUID);
        conceptService.saveConceptSource(icd10);

        ConceptSource pih = new ConceptSource();
        pih.setUuid("fb9aaaf1-65e2-4c18-b53c-16b575f2f385");
        pih.setName("PIH");
        conceptService.saveConceptSource(pih);

        createTerms(icd10, "G00.9", "A36.9", "B05.9", "B54", "B53.8", "A90", "A91", "A01.0");
        createTerms(pih, "Acute flassic paralysis", "DIARRHEA", "DIARRHEA, BLOODY");

        ConceptMapType narrowerThan = conceptService.getConceptMapTypeByName("is-parent-to");
        narrowerThan.setUuid(EmrConstants.NARROWER_THAN_CONCEPT_MAP_TYPE_UUID);
        conceptService.saveConceptMapType(narrowerThan);

        DiagnosisMetadata diagnosisMetadata = TestUtils.setupDiagnosisMetadata(conceptService, emrApiProperties);

        ConceptDatatype naDatatype = conceptService.getConceptDatatypeByName("N/A");
        ConceptClass diagnosisClass = conceptService.getConceptClassByName("Diagnosis");

        Concept diag = new ConceptBuilder(conceptService, naDatatype, diagnosisClass)
                .addName("Viral Haemmorhagic Fever")
                .addMapping(narrowerThan, icd10, "A99").saveAndGet();

        Date obsDatetime = DateUtil.parseDate("2013-01-04", "yyyy-MM-dd");

        Patient femalePatient = patientService.getPatient(7);
        Obs obs = diagnosisMetadata.buildDiagnosisObsGroup(new Diagnosis(new CodedOrFreeTextAnswer(diag), Diagnosis.Order.PRIMARY));
        setAndCascade(obs, femalePatient, obsDatetime);
        obsService.saveObs(obs, null);

        Patient malePatient = patientService.getPatient(6);
        obs = diagnosisMetadata.buildDiagnosisObsGroup(new Diagnosis(new CodedOrFreeTextAnswer(diag), Diagnosis.Order.PRIMARY));
        setAndCascade(obs, malePatient, obsDatetime);
        obsService.saveObs(obs, null);

        timer.println("Finished setup");
    }

    private void createTerms(ConceptSource source, String... terms) {
        for (String term : terms) {
            conceptService.saveConceptReferenceTerm(new ConceptReferenceTerm(source, term, null));
        }
    }

    private void setAndCascade(Obs obs, Person person, Date obsDatetime) {
        obs.setPerson(person);
        obs.setObsDatetime(obsDatetime);
        if (obs.isObsGrouping()) {
            for (Obs child : obs.getGroupMembers()) {
                setAndCascade(child, person, obsDatetime);
            }
        }
    }

    @Test
    public void testReport() throws Exception {
        TestTimer timer = new TestTimer();

        timer.println("Started");
        CohortIndicatorDataSetDefinition dsd = manager.buildDataSetDefinition();

        timer.println("Built DSD");

        EvaluationContext evaluationContext = new EvaluationContext();
        evaluationContext.addParameterValue("startOfWeek", DateUtil.parseDate("2013-01-01", "yyyy-MM-dd"));
        MapDataSet evaluated = (MapDataSet) service.evaluate(dsd, evaluationContext);

        timer.println("Evaluated");

        DataSetRow data = evaluated.getData();
        assertThat(((CohortIndicatorAndDimensionResult) data.getColumnValue("hemorrhagicFever.male.young")).getCohortIndicatorAndDimensionCohort(), isCohortWithExactlyIds());
        assertThat(((CohortIndicatorAndDimensionResult) data.getColumnValue("hemorrhagicFever.female.young")).getCohortIndicatorAndDimensionCohort(), isCohortWithExactlyIds());
        assertThat(((CohortIndicatorAndDimensionResult) data.getColumnValue("hemorrhagicFever.male.old")).getCohortIndicatorAndDimensionCohort(), isCohortWithExactlyIds(6));
        assertThat(((CohortIndicatorAndDimensionResult) data.getColumnValue("hemorrhagicFever.female.old")).getCohortIndicatorAndDimensionCohort(), isCohortWithExactlyIds(7));
    }

}
