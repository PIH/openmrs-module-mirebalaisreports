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

import org.apache.commons.io.IOUtils;
import org.openmrs.Location;
import org.openmrs.api.ConceptService;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.library.MirebalaisCohortDefinitionLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.TimePeriod;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.RepeatPerTimePeriodDataSetDefinition;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Monthly version of the inpatient stats report. This uses the underlying DSD from the daily version of this report
 */
@Component
public class InpatientStatsMonthlyReportManager extends BaseMirebalaisReportManager {

    @Autowired
    protected AllDefinitionLibraries libraries;

    @Autowired
    protected ConceptService conceptService;

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.INPATIENT_STATS_MONTHLY_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    protected String getMessageCodePrefix() {
        return "mirebalaisreports.inpatientStatsMonthlyReport.";
    }

    @Override
    public List<Parameter> getParameters() {
        Properties monthConfig = new Properties();
        monthConfig.put("uiframeworkFragmentProvider", "mirebalaisreports");
        monthConfig.put("uiframeworkFragment", "field/monthSinceMirebalaisOpening");

        Properties wardConfig = new Properties();
        wardConfig.put("uiframeworkFragmentProvider", "mirebalaisreports");
        wardConfig.put("uiframeworkFragment", "field/inpatientWard");

        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("month", "mirebalaisreports.parameter.month", Date.class, monthConfig));
        l.add(new Parameter("location", "mirebalaisreports.parameter.ward", Location.class, wardConfig));
        return l;
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        CohortCrossTabDataSetDefinition dsd = new CohortCrossTabDataSetDefinition();
        dsd.addParameter(getStartDateParameter());
        dsd.addParameter(getEndDateParameter());
        dsd.addParameter(getLocationParameter());

        CohortDefinition inpatientCensus = libraries.getDefinition(CohortDefinition.class, MirebalaisCohortDefinitionLibrary.PREFIX + "inpatientAtLocationOnDate");
        CohortDefinition admissionDuring = libraries.getDefinition(CohortDefinition.class, MirebalaisCohortDefinitionLibrary.PREFIX + "admissionAtLocationDuringPeriod");
        CohortDefinition transferInDuring = libraries.getDefinition(CohortDefinition.class, MirebalaisCohortDefinitionLibrary.PREFIX + "transferInToLocationDuringPeriod");
        CohortDefinition transferOutDuring = libraries.getDefinition(CohortDefinition.class, MirebalaisCohortDefinitionLibrary.PREFIX + "transferOutOfLocationDuringPeriod");
        CohortDefinition dischargedDuring = libraries.getDefinition(CohortDefinition.class, MirebalaisCohortDefinitionLibrary.PREFIX + "dischargeExitFromLocationDuringPeriod");
        CohortDefinition diedSoonAfterAdmissionDuring = libraries.getDefinition(CohortDefinition.class, MirebalaisCohortDefinitionLibrary.PREFIX + "diedExitFromLocationDuringPeriodSoonAfterAdmission");
        CohortDefinition diedLongAfterAdmissionDuring = libraries.getDefinition(CohortDefinition.class, MirebalaisCohortDefinitionLibrary.PREFIX + "diedExitFromLocationDuringPeriodNotSoonAfterAdmission");
        CohortDefinition transferOutOfHumDuring = libraries.getDefinition(CohortDefinition.class, MirebalaisCohortDefinitionLibrary.PREFIX + "transferOutOfHumExitFromLocationDuringPeriod");
        CohortDefinition leftWithoutCompletingTxDuring = libraries.getDefinition(CohortDefinition.class, MirebalaisCohortDefinitionLibrary.PREFIX + "leftWithoutCompletingTreatmentExitFromLocationDuringPeriod");
        CohortDefinition leftWithoutSeeingClinicianDuring = libraries.getDefinition(CohortDefinition.class, MirebalaisCohortDefinitionLibrary.PREFIX + "leftWithoutSeeingClinicianExitFromLocationDuringPeriod");

        dsd.addColumn("censusAtStart", Mapped.map(inpatientCensus, "date=${startDate},location=${location}"));
        dsd.addColumn("censusAtEnd", Mapped.map(inpatientCensus, "date=${endDate},location=${location}"));
        dsd.addColumn("admissionDuring", Mapped.mapStraightThrough(admissionDuring));
        dsd.addColumn("transferInDuring", Mapped.mapStraightThrough(transferInDuring));
        dsd.addColumn("transferOutDuring", Mapped.mapStraightThrough(transferOutDuring));
        dsd.addColumn("dischargedDuring", Mapped.mapStraightThrough(dischargedDuring));
        dsd.addColumn("diedSoonAfterAdmissionDuring", Mapped.mapStraightThrough(diedSoonAfterAdmissionDuring));
        dsd.addColumn("diedLongAfterAdmissionDuring", Mapped.mapStraightThrough(diedLongAfterAdmissionDuring));
        dsd.addColumn("transferOutOfHumDuring", Mapped.mapStraightThrough(transferOutOfHumDuring));
        dsd.addColumn("leftWithoutCompletingTxDuring", Mapped.mapStraightThrough(leftWithoutCompletingTxDuring));
        dsd.addColumn("leftWithoutSeeingClinicianDuring", Mapped.mapStraightThrough(leftWithoutSeeingClinicianDuring));

        RepeatPerTimePeriodDataSetDefinition repeatDsd = new RepeatPerTimePeriodDataSetDefinition();
        repeatDsd.addParameter(getStartDateParameter());
        repeatDsd.addParameter(getEndDateParameter());
        repeatDsd.addParameter(getLocationParameter());
        repeatDsd.setBaseDefinition(Mapped.mapStraightThrough(dsd));
        repeatDsd.setRepeatPerTimePeriod(TimePeriod.DAILY);

        ReportDefinition rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");
        rd.setUuid(getUuid());
        rd.setParameters(getParameters());

        rd.addDataSetDefinition("dsd", map(repeatDsd, "startDate=${month},endDate=${month+1m-1ms},location=${location}"));
        return rd;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) throws IOException {
        InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream("org/openmrs/module/mirebalaisreports/reportTemplates/InpatientStatsMonthly.xls");
        byte[] excelTemplate = IOUtils.toByteArray(is);

        Properties designProperties = new Properties();
        designProperties.put("repeatingSections", "sheet:1,column:3,dataset:dsd");

        return Arrays.asList(xlsReportDesign(reportDefinition, excelTemplate, designProperties));
    }

}
