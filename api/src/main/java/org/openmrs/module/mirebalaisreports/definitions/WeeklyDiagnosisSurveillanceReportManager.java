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

import org.openmrs.Concept;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

        ConceptSource icd10 = mirebalaisProperties.getIcd10ConceptSource();
        ConceptSource pih = mirebalaisProperties.getPihConceptSource();
        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "hemorrhagicFever", "Syndrome de fièvre hémorragique aiguë", icd10, "A99");
        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "bacterialMeningitis", "Cas suspect de méningite bactérienne", icd10, "G00.9");
        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "diphtheria", "Cas suspect de diphtérie", icd10, "A36.9");
        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "acuteFlassicParalysis", "Cas suspect de paralysie flasque aiguë", pih, "Acute flassic paralysis");
        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "measles", "Cas suspect de rougeole", icd10, "B05.9");
        // TODO: need an appropriate concept for: Morsure par animal suspecté de rage
        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "suspectedMalaria", "Cas suspect de paludisme (malaria)", icd10, "B54");
        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "confirmedMalaria", "Cas confirmé de paludisme (malaria)", icd10, "B53.8");
        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "dengue", "Cas suspect de dengue (et la dengue hémorragique)", icd10, "A90", "A91");
        // TODO: Fièvre d’origine inconnue
        // TODO: Syndrome ictérique fébrile (maybe this is related to jaundice?)
        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "diarrhea", "Diarrhée aiguë non-sanglante", pih, "DIARRHEA"); // TODO maybe also "Gastroenteritis and colitis"
        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "bloodyDiarrhea", "Diarrhée aiguë sanglante", pih, "DIARRHEA, BLOODY");
        addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, "typhoid", "Cas suspect de typhoïde", icd10, "A01.0");
        //
        //Cas suspect de coqueluche
        //Infection respiratoire aiguë
        //Cas suspect de tuberculose
        //Cas suspect de tétanos
        //Cas suspect de charbon cutané
        //Troisième trimestre de grossesse sans suivi
        //Complications de grossesse
        return dsd;
    }

    private void addDiseaseColumnsForCode(CohortIndicatorDataSetDefinition dsd, CohortIndicator indicator, String name, String label, ConceptSource source, String... icdCodes) {
        Map<String, Object> mappings = ParameterizableUtil.createParameterMappings("startDate=${startOfWeek},endDate=${startOfWeek + 6d}");
        List<Concept> concepts = new ArrayList<Concept>();
        for (String icdCode : icdCodes) {
            concepts.addAll(emrConceptService.getConceptsSameOrNarrowerThan(conceptService.getConceptReferenceTermByCode(icdCode, source)));
        }
        mappings.put("codedDiagnoses", concepts);

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
