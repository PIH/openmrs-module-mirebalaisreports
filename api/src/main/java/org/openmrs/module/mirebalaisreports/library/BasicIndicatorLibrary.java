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
import org.openmrs.module.mirebalaisreports.api.MirebalaisReportsService;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.Indicator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 *
 */
@Handler(supports = Indicator.class)
public class BasicIndicatorLibrary extends BaseDefinitionLibrary<CohortIndicator> {

    public static final String PREFIX = "emr.indicator.";

    @Autowired
	MirebalaisReportsService reportsService;

    public BasicIndicatorLibrary() {
    }

    public BasicIndicatorLibrary(MirebalaisReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @Override
    public String getKeyPrefix() {
        return PREFIX;
    }

    @DocumentedDefinition(value = "specific coded diagnoses during period", definition = "Patients with any diagnosis of $codedDiagnoses between $startDate and $endDate")
    public CohortIndicator getSpecificCodedDiagnosesBetweenDates() {
        CohortDefinition query = reportsService.getCohortDefinition(BasicCohortDefinitionLibrary.PREFIX + "specific coded diagnoses between dates");

        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(new Parameter("startDate", "Start of period", Date.class));
        ci.addParameter(new Parameter("endDate", "End of period", Date.class));
        ci.addParameter(new Parameter("codedDiagnoses", "Which coded diagnoses", Concept.class, List.class, null));
        ci.setCohortDefinition(map(query, "onOrAfter=${startDate},onOrBefore=${endDate},codedDiagnoses=${codedDiagnoses}"));
        return ci;
    }

}
