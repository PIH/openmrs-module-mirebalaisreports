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

import org.openmrs.module.reporting.dataset.definition.RowPerObjectDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class VisitDataSetDefinition extends RowPerObjectDataSetDefinition {

    public static final long serialVersionUID = 1L;

    //***** PROPERTIES *****

    @ConfigurationProperty
    private List<Mapped<? extends VisitQuery>> rowFilters;

    public void addRowFilter(Mapped<? extends VisitQuery> mappedQuery) {
        if (rowFilters == null) {
            rowFilters = new ArrayList<Mapped<? extends VisitQuery>>();
        }
        rowFilters.add(mappedQuery);
    }
}
