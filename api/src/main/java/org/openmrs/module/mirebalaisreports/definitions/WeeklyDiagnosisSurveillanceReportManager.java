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

package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emr.concept.EmrConceptService;
import org.openmrs.module.emr.reporting.library.BasicDimensionLibrary;
import org.openmrs.module.emr.reporting.library.BasicIndicatorLibrary;
import org.openmrs.module.mirebalaisreports.MirebalaisProperties;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.indicator.dimension.service.DimensionService;
import org.openmrs.module.reporting.indicator.service.IndicatorService;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Component
public class WeeklyDiagnosisSurveillanceReportManager {

    @Autowired
    CohortDefinitionService cohortDefinitionService;

    @Autowired
    DimensionService dimensionService;

    @Autowired
    IndicatorService indicatorService;

    @Autowired
    ConceptService conceptService;

    @Autowired
    EmrConceptService emrConceptService;

    @Autowired
    MirebalaisProperties mirebalaisProperties;

    public ReportDefinition buildReportDefinition() {
        CohortIndicatorDataSetDefinition dsd = buildDataSetDefinition();

        ReportDefinition reportDefinition = new ReportDefinition();
        reportDefinition.addParameter(new Parameter("startOfWeek", "Start of Week", Date.class));
        reportDefinition.addDataSetDefinition("indicators", map(dsd, "startOfWeek=${startOfWeek}"));

        return reportDefinition;
    }

    public CohortIndicatorDataSetDefinition buildDataSetDefinition() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

        dsd.addParameter(new Parameter("startOfWeek", "Start of Week", Date.class));

        dsd.addDimension("age",
                map((CohortDefinitionDimension) dimensionService.getDefinitionByUuid(BasicDimensionLibrary.PREFIX + "age two levels (cutoff in years)"),
                        "date", "${startOfWeek}", "cutoff", 5));
        dsd.addDimension("gender",
                map((CohortDefinitionDimension) dimensionService.getDefinitionByUuid(BasicDimensionLibrary.PREFIX + "gender"), ""));

        CohortIndicator specificCodedDiagnosesIndicator = (CohortIndicator) indicatorService.getDefinitionByUuid(BasicIndicatorLibrary.PREFIX + "specific coded diagnoses during period");
        if (specificCodedDiagnosesIndicator == null) {
            throw new IllegalStateException("Cannot find indicator for specific coded diagnoses");
        }

        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "hemorrhagicFever", "Syndrome de fièvre hémorragique aiguë", mirebalaisProperties.getIcd10ConceptSource(), "A99");
        return dsd;
    }

    private void addDiseaseColumnsForCode(CohortIndicatorDataSetDefinition dsd, CohortIndicator indicator, String name, String label, ConceptSource source, String icdCode) {
        Map<String, Object> mappings = ParameterizableUtil.createParameterMappings("startDate=${startOfWeek},endDate=${startOfWeek + 6d}");
        mappings.put("codedDiagnoses", emrConceptService.getConceptsSameOrNarrowerThan(conceptService.getConceptReferenceTermByCode(icdCode, source)));

        Mapped<CohortIndicator> mappedIndicator = new Mapped<CohortIndicator>(indicator, mappings);

        dsd.addColumn(name + ".male.young", label + " (<5 ans H)", mappedIndicator, "age=young|gender=male");
        dsd.addColumn(name + ".female.young", label + " (<5 ans F)", mappedIndicator, "age=young|gender=female");
        dsd.addColumn(name + ".male.old", label + " (>=5 ans H)", mappedIndicator, "age=old|gender=male");
        dsd.addColumn(name + ".female.old", label + " (>=5 ans F)", mappedIndicator, "age=old|gender=female");
    }

    private <T extends Parameterizable> Mapped<T> map(T parameterizable, Object... mappings) {
        if (parameterizable == null) {
            throw new NullPointerException("Programming error: missing parameterizable");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < mappings.length; i += 2) {
            map.put((String) mappings[i], mappings[i + 1]);
        }
        return new Mapped<T>(parameterizable, map);
    }

    private <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
        if (parameterizable == null) {
            throw new NullPointerException("Programming error: missing parameterizable");
        }
        if (mappings == null) {
            mappings = ""; // probably not necessary, just to be safe
        }
        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
    }
}
