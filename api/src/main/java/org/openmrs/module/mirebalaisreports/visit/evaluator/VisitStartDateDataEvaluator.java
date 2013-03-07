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

package org.openmrs.module.mirebalaisreports.visit.evaluator;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.mirebalaisreports.visit.definition.VisitDataDefinition;
import org.openmrs.module.mirebalaisreports.visit.definition.VisitStartDateDataDefinition;
import org.openmrs.module.reporting.data.encounter.EncounterDataUtil;
import org.openmrs.module.reporting.data.encounter.EvaluatedEncounterData;
import org.openmrs.module.reporting.dataset.query.service.DataSetQueryService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;

import java.util.Map;
import java.util.Set;

/**
 *
 */
@Handler(supports = VisitStartDateDataDefinition.class)
public class VisitStartDateDataEvaluator implements VisitDataEvaluator {

    @Override
    public EvaluatedVisitData evaluate(VisitDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EvaluatedVisitData c = new EvaluatedVisitData(definition, context);
        DataSetQueryService qs = Context.getService(DataSetQueryService.class);
        Map<Integer, Object> data = qs.getPropertyValues(Visit.class, "startDatetime", context);
        // note that we are _not_ filtering based on any base patient cohort from context
        c.setData(data);
        return c;
    }

}
