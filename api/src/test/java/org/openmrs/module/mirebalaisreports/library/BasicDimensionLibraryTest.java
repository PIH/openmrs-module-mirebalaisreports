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
import org.openmrs.module.mirebalaisreports.api.MirebalaisReportsService;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.indicator.dimension.Dimension;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openmrs.module.emr.test.ReportingMatchers.parameterNamed;

/**
 *
 */
public class BasicDimensionLibraryTest {

    private BasicDimensionLibrary library;

    @Before
    public void setUp() throws Exception {
        MirebalaisReportsService reportsService = mock(MirebalaisReportsService.class);
        when(reportsService.getCohortDefinition(anyString())).thenReturn(new GenderCohortDefinition());

        library = new BasicDimensionLibrary(reportsService);
    }

    @Test
    public void testNonExistent() throws Exception {
        assertNull(library.getDefinitionByUuid("none like this"));
        assertNull(library.getDefinitionByUuid(library.getUuidPrefix() + "none like this"));
    }

    @Test
    public void testGender() throws Exception {
        Dimension dimension = library.getDefinitionByUuid(library.getUuidPrefix() + "gender");
        assertThat(dimension, instanceOf(CohortDefinitionDimension.class));
        assertThat(dimension.getParameters().size(), is(0));
        assertThat(dimension.getOptionKeys(), containsInAnyOrder("male", "female"));
    }

    @Test
    public void testAgeTwoLevelInYears() throws Exception {
        Dimension dimension = library.getDefinitionByUuid(library.getUuidPrefix() + "age two levels (cutoff in years)");
        assertThat(dimension, instanceOf(CohortDefinitionDimension.class));
        assertThat(dimension.getParameters(), containsInAnyOrder(parameterNamed("date"), parameterNamed("cutoff")));
        assertThat(dimension.getOptionKeys(), containsInAnyOrder("young", "old"));
    }

}
