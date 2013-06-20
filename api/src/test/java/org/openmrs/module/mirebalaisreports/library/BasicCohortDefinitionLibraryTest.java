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
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.openmrs.module.emr.test.ReportingMatchers.parameterNamed;

/**
 *
 */
public class BasicCohortDefinitionLibraryTest {

    private BasicCohortDefinitionLibrary library;

    @Before
    public void setUp() throws Exception {
        library = new BasicCohortDefinitionLibrary();
    }

    @Test
    public void testNonExistent() throws Exception {
        assertNull(library.getDefinitionByUuid("something random"));
        assertNull(library.getDefinitionByUuid(library.getUuidPrefix() + "something random"));
    }

    @Test
    public void testMales() throws Exception {
        CohortDefinition actual = library.getDefinitionByUuid(library.getUuidPrefix() + "males");
        assertThat(actual, instanceOf(GenderCohortDefinition.class));
        assertThat(actual.getParameters().size(), is(0));
        assertThat(actual, hasProperty("maleIncluded", is(true)));
        assertThat(actual, hasProperty("femaleIncluded", is(false)));
        assertThat(actual, hasProperty("unknownGenderIncluded", is(false)));
    }

    @Test
    public void testFemales() throws Exception {
        CohortDefinition actual = library.getDefinitionByUuid(library.getUuidPrefix() + "females");
        assertThat(actual, instanceOf(GenderCohortDefinition.class));
        assertThat(actual.getParameters().size(), is(0));
        assertThat(actual, hasProperty("maleIncluded", is(false)));
        assertThat(actual, hasProperty("femaleIncluded", is(true)));
        assertThat(actual, hasProperty("unknownGenderIncluded", is(false)));
    }

    @Test
    public void testUnknownGender() throws Exception {
        CohortDefinition actual = library.getDefinitionByUuid(library.getUuidPrefix() + "unknown gender");
        assertThat(actual, instanceOf(GenderCohortDefinition.class));
        assertThat(actual.getParameters().size(), is(0));
        assertThat(actual, hasProperty("maleIncluded", is(false)));
        assertThat(actual, hasProperty("femaleIncluded", is(false)));
        assertThat(actual, hasProperty("unknownGenderIncluded", is(true)));
    }

    @Test
    public void testAgeUpTo() throws Exception {
        CohortDefinition actual = library.getDefinitionByUuid(library.getUuidPrefix() + "up to age on date");
        assertThat(actual, instanceOf(AgeCohortDefinition.class));
        assertThat(actual.getParameters(), containsInAnyOrder(parameterNamed("effectiveDate"), parameterNamed("maxAge")));
        assertThat(actual, hasProperty("maxAgeUnit", is(DurationUnit.YEARS)));
    }

    @Test
    public void testAgeAtLeast() throws Exception {
        CohortDefinition actual = library.getDefinitionByUuid(library.getUuidPrefix() + "at least age on date");
        assertThat(actual, instanceOf(AgeCohortDefinition.class));
        assertThat(actual.getParameters(), containsInAnyOrder(parameterNamed("effectiveDate"), parameterNamed("minAge")));
        assertThat(actual, hasProperty("minAgeUnit", is(DurationUnit.YEARS)));
    }

}
