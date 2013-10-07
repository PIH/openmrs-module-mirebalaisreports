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
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.mirebalaisreports.cohort.definition.DiagnosisCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PersonAttributeCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Handler(supports = CohortDefinition.class)
public class BasicCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {

    public static final String PREFIX = "emr.cohortDefinition.";

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Override
    public String getKeyPrefix() {
        return PREFIX;
    }

    @DocumentedDefinition(value = "males", definition = "Patients whose gender is M")
    public GenderCohortDefinition getMales() {
        GenderCohortDefinition males = new GenderCohortDefinition();
        males.setMaleIncluded(true);
        return males;
    }

    @DocumentedDefinition(value = "females", definition = "Patients whose gender is F")
    public GenderCohortDefinition getFemales() {
        GenderCohortDefinition females = new GenderCohortDefinition();
        females.setFemaleIncluded(true);
        return females;
    }

    @DocumentedDefinition(value = "unknown gender", definition = "Patients whose gender is neither M or F")
    public GenderCohortDefinition getUnknownGender() {
        GenderCohortDefinition unknownGender = new GenderCohortDefinition();
        unknownGender.setUnknownGenderIncluded(true);
        return unknownGender;
    }

    @DocumentedDefinition(value = "up to age on date", definition = "Patients whose age is <= $maxAge years on $effectiveDate")
    public AgeCohortDefinition getUpToAgeOnDate() {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.addParameter(new Parameter("maxAge", "Max Age (years)", Integer.class));
        return cd;
    }

    @DocumentedDefinition(value = "at least age on date", definition = "Patients whose age is >= $minAge years on $effectiveDate")
    public AgeCohortDefinition getAtLeastAgeOnDate() {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.addParameter(new Parameter("minAge", "Min Age (years)", Integer.class));
        return cd;
    }

    @DocumentedDefinition(value = "specific coded diagnoses between dates", definition = "Patients with any diagnosis of $codedDiagnoses between $onOrAfter and $onOrBefore")
    public DiagnosisCohortDefinition getSpecificCodedDiagnosesBetweenDates() {
        DiagnosisCohortDefinition cd = new DiagnosisCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "On or after date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or before date", Date.class));
        cd.addParameter(new Parameter("codedDiagnoses", "Which coded diagnoses", Concept.class, List.class, null));
        return cd;
    }

    @DocumentedDefinition(value = "exclude test patients")
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
}
