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

import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper that <em>builds</em> FullDataExportReportManagers with specific combinations of datasets
 */
@Component
public class FullDataExportBuilder {

    @Autowired
    ApplicationContext applicationContext;

    public static final String SQL_DIR = "org/openmrs/module/mirebalaisreports/sql/fullDataExport/";
    public static final String TEMPLATE_DIR = "org/openmrs/module/mirebalaisreports/reportTemplates/";

    public final List<String> dataSetOptions = Arrays.asList(
            "patients", "visits", "checkins", "vitals", "consultations", "diagnoses",
            "hospitalizations", "postOpNote1", "postOpNote2",
            "radiologyOrders", "radiologyOrderEncounters", "radiologyStudyEncounters", "radiologyReportEncounters", "dispensing"
    );

    private List<Configuration> configurations = new ArrayList<Configuration>();

    public FullDataExportBuilder() {
        configurations.add(new Configuration(MirebalaisReportsProperties.FULL_DATA_EXPORT_REPORT_DEFINITION_UUID, "mirebalaisreports.fulldataexport.", null));
        configurations.add(new Configuration(MirebalaisReportsProperties.RADIOLOGY_DATA_EXPORT_REPORT_DEFINITION_UUID, "mirebalaisreports.radiologydataexport.",
                Arrays.asList("radiologyOrders", "radiologyOrderEncounters", "radiologyStudyEncounters", "radiologyReportEncounters")));
        configurations.add(new Configuration(MirebalaisReportsProperties.SURGERY_DATA_EXPORT_REPORT_DEFINITION_UUID, "mirebalaisreports.surgerydataexport.",
                Arrays.asList("postOpNote1", "postOpNote2")));
        configurations.add(new Configuration(MirebalaisReportsProperties.HOSPITALIZATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID, "mirebalaisreports.hospitalizationsdataexport.",
                Arrays.asList("hospitalizations", "postOpNote1", "postOpNote2")));
        configurations.add(new Configuration(MirebalaisReportsProperties.CONSULTATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID, "mirebalaisreports.consultationsdataexport.",
                Arrays.asList("consultations", "diagnoses")));
        configurations.add(new Configuration(MirebalaisReportsProperties.DASHBOARD_DATA_EXPORT_REPORT_DEFINITION_UUID, "mirebalaisreports.dashboarddataexport.",
                Arrays.asList("patients", "checkins", "vitals", "consultations", "diagnoses", "visits", "hospitalizations", "postOpNote1", "postOpNote2")));
        configurations.add(new Configuration(MirebalaisReportsProperties.PATIENTS_DATA_EXPORT_REPORT_DEFINITION_UUID, "mirebalaisreports.patientsdataexport.",
                Arrays.asList("patients")));
        configurations.add(new Configuration(MirebalaisReportsProperties.DISPENSING_DATA_EXPORT_REPORT_DEFINITION_UUID, "mirebalaisreports.dispensingdataexport.",
                Arrays.asList("dispensing")));
    }

    public List<FullDataExportReportManager> getAllReportManagers() {
        ArrayList<FullDataExportReportManager> list = new ArrayList<FullDataExportReportManager>();
        for (Configuration configuration : configurations) {
            list.add(buildReportManager(configuration));
        }

        return list;
    }

    public FullDataExportReportManager buildReportManager(Configuration configuration) {
        List<String> dataSets = configuration.getDataSets();
        if (dataSets == null || dataSets.size() == 0) {
            dataSets = dataSetOptions;
        }

        FullDataExportReportManager manager = new FullDataExportReportManager(configuration.getUuid(), configuration.getMessageCodePrefix(), dataSets);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(manager);
        return manager;
    }

    public static class Configuration {

        private String uuid;

        private String messageCodePrefix;

        private List<String> dataSets;

        public Configuration(String uuid, String messageCodePrefix, List<String> dataSets) {
            this.uuid = uuid;
            this.messageCodePrefix = messageCodePrefix;
            this.dataSets = dataSets;
        }

        public String getUuid() {
            return uuid;
        }

        public String getMessageCodePrefix() {
            return messageCodePrefix;
        }

        public List<String> getDataSets() {
            return dataSets;
        }

    }

}
