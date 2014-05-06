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

package org.openmrs.module.mirebalaisreports.visit.definition;

import java.util.Date;

import org.openmrs.Visit;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.query.BaseQuery;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;

/**
 *
 */
public class HasPrimaryDiagnosisVisitQuery extends BaseQuery<Visit> implements VisitQuery {

    @ConfigurationProperty
    private Date onOrAfter;

    @ConfigurationProperty
    private Date onOrBefore;

    public Date getOnOrAfter() {
        return onOrAfter;
    }

    public void setOnOrAfter(Date onOrAfter) {
        this.onOrAfter = onOrAfter;
    }

    public Date getOnOrBefore() {
        return onOrBefore;
    }

    public void setOnOrBefore(Date onOrBefore) {
        this.onOrBefore = onOrBefore;
    }

}
