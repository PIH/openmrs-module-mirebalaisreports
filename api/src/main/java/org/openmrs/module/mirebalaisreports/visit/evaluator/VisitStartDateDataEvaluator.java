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

import org.openmrs.annotation.Handler;
import org.openmrs.module.mirebalaisreports.visit.definition.VisitDataDefinition;
import org.openmrs.module.mirebalaisreports.visit.definition.VisitStartDateDataDefinition;
import org.openmrs.module.mirebalaisreports.visit.query.VisitQueryService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 *
 */
@Handler(supports = VisitStartDateDataDefinition.class)
public class VisitStartDateDataEvaluator implements VisitDataEvaluator {

    @Autowired
    public VisitQueryService visitQueryService;

    @Override
    public EvaluatedVisitData evaluate(VisitDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EvaluatedVisitData c = new EvaluatedVisitData(definition, context);

        VisitStartDateDataDefinition visitStartDateDataDefinition = (VisitStartDateDataDefinition) definition;

        Date startDate = visitStartDateDataDefinition.getOnOrAfter();
        Date endDate = visitStartDateDataDefinition.getOnOrBefore();

        Map<Integer, Date> visitIdsFromVisitsThatHaveDiagnoses =
                visitQueryService.getMapOfVisitIdsAndStartDatesFromVisitsThatHaveDiagnoses(startDate, endDate);


//        c.setData((Map<Integer, Object>) visitIdsFromVisitsThatHaveDiagnoses);
        return c;
    }

}
