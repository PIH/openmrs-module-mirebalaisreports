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

import org.openmrs.module.mirebalaisreports.api.MirebalaisReportsService;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.indicator.dimension.Dimension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 *
 */
public class BasicDimensionLibrary extends BaseDefinitionLibrary<Dimension> {

    public static final String PREFIX = "emr.dimension.";

    @Autowired
	MirebalaisReportsService reportsService;

    public BasicDimensionLibrary() {
    }

    /**
     * Used for tests
     * @param reportsService
     */
    public BasicDimensionLibrary(MirebalaisReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @Override
    public Class<? super Dimension> getDefinitionType() {
        return Dimension.class;
    }

    @Override
    public String getKeyPrefix() {
        return PREFIX;
    }

    @DocumentedDefinition(value = "gender", definition = "males | females")
    public CohortDefinitionDimension getGenderDimension() {
        CohortDefinitionDimension gender = new CohortDefinitionDimension();
        gender.addCohortDefinition("female", noMappings(reportsService.getCohortDefinition(MirebalaisCohortDefinitionLibrary.PREFIX + "females")));
        gender.addCohortDefinition("male", noMappings(reportsService.getCohortDefinition(MirebalaisCohortDefinitionLibrary.PREFIX + "males")));
        return gender;
    }

    @DocumentedDefinition(value = "age two levels (cutoff in years)", definition = "young = < $cutoff years , old = >= $cutoff years, age taken on $date")
    public CohortDefinitionDimension getTwoLevelAgeDimensionInYears() {
        CohortDefinitionDimension age = new CohortDefinitionDimension();
        age.addParameter(new Parameter("date", "Date", Date.class));
        age.addParameter(new Parameter("cutoff", "Cutoff (< $cutoff years , >= $cutoff years)", Integer.class));
        age.addCohortDefinition("young",
                map(reportsService.getCohortDefinition(MirebalaisCohortDefinitionLibrary.PREFIX + "up to age on date"),
                        "maxAge=${cutoff-1},effectiveDate=${date}"));
        age.addCohortDefinition("old",
                map(reportsService.getCohortDefinition(MirebalaisCohortDefinitionLibrary.PREFIX + "at least age on date"),
                        "minAge=${cutoff},effectiveDate=${date}"));
        return age;
    }

}
