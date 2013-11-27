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

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.cohort.definition.DiagnosisCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterWithCodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.MappedParametersCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PersonAttributeCohortDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Component
public class MirebalaisCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {

    public static final String PREFIX = "mirebalais.cohortDefinition.";

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private MirebalaisReportsProperties mirebalaisReportsProperties;

    @Autowired
    private ConceptService conceptService;

    @Override
    public Class<? super CohortDefinition> getDefinitionType() {
        return CohortDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return PREFIX;
    }

    @DocumentedDefinition(value = "specificCodedDiagnosesBetweenDates", definition = "Patients with any diagnosis of $codedDiagnoses between $onOrAfter and $onOrBefore")
    public DiagnosisCohortDefinition getSpecificCodedDiagnosesBetweenDates() {
        DiagnosisCohortDefinition cd = new DiagnosisCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "On or after date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or before date", Date.class));
        cd.addParameter(new Parameter("codedDiagnoses", "Which coded diagnoses", Concept.class, List.class, null));
        return cd;
    }

    @DocumentedDefinition(value = "excludeTestPatients")
    public CohortDefinition getExcludeTestPatients() {
        PersonAttributeCohortDefinition personAttributeCohortDefinition = new PersonAttributeCohortDefinition();
        personAttributeCohortDefinition.setAttributeType(emrApiProperties.getTestPatientPersonAttributeType());
        //the method add value has a bug, using set values for now
        personAttributeCohortDefinition.setValues(Arrays.asList("true"));

        CompositionCohortDefinition excludeTestPatientsCohortDefinition = new CompositionCohortDefinition();
        excludeTestPatientsCohortDefinition.addSearch("test", map((CohortDefinition) personAttributeCohortDefinition, ""));
        excludeTestPatientsCohortDefinition.setCompositionString("NOT test");
        return excludeTestPatientsCohortDefinition;
    }

    @DocumentedDefinition(value = "clinicalCheckInAtLocation")
    public CohortDefinition getClinicalCheckInAtLocation() {
        EncounterWithCodedObsCohortDefinition cd = new EncounterWithCodedObsCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
        cd.addParameter(new Parameter("locationList", "Locations", Location.class));
        cd.addEncounterType(mirebalaisReportsProperties.getCheckInEncounterType());
        cd.setConcept(conceptService.getConceptByMapping("Type of HUM visit", "PIH"));
        cd.addIncludeCodedValue(conceptService.getConceptByMapping("CLINICAL", "PIH"));

        return new MappedParametersCohortDefinition(cd, "onOrAfter", "startDate", "onOrBefore", "endDate", "locationList", "location");
    }

}
