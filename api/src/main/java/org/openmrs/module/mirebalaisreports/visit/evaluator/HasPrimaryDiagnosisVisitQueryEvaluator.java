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
import org.openmrs.module.mirebalaisreports.visit.definition.HasPrimaryDiagnosisVisitQuery;
import org.openmrs.module.mirebalaisreports.visit.definition.VisitQuery;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;

/**
 *
 */
@Handler(supports=HasPrimaryDiagnosisVisitQuery.class)
public class HasPrimaryDiagnosisVisitQueryEvaluator implements VisitQueryEvaluator {

    @Override
    public Evaluated<VisitQuery> evaluate(VisitQuery definition, EvaluationContext context) throws EvaluationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
