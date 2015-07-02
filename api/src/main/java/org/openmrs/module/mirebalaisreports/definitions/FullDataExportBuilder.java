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

import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.config.Config;
import org.openmrs.module.pihcore.config.ConfigDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper that <em>builds</em> FullDataExportReportManagers with specific combinations of datasets.
 */
@Component
public class FullDataExportBuilder {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    Config config;

    public static final String SQL_DIR = "org/openmrs/module/mirebalaisreports/sql/fullDataExport/";
    public static final String TEMPLATE_DIR = "org/openmrs/module/mirebalaisreports/reportTemplates/";

    public final List<String> dataSetOptions = Arrays.asList(
            "patients", "visits", "checkins", "vitals", "consultations", "diagnoses",
            "hospitalizations", "postOpNote1", "postOpNote2",
            "radiologyOrders", "radiologyOrderEncounters", "radiologyStudyEncounters", "radiologyReportEncounters",
            "dispensing", "encounters"
    );

    private List<Configuration> configurations;

    public List<Configuration> getConfigurations() {
        if (configurations == null) {
            configurations = new ArrayList<Configuration>();
            // For now, let's segment quickly based on country like this, but we should improve this overall configurability
            if (config.getCountry() == ConfigDescriptor.Country.LIBERIA) {
                configurations.add(new Configuration(MirebalaisReportsProperties.REGISTRATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID, "registrationdataexport",
                        Arrays.asList("registration")));
                configurations.add(new Configuration(MirebalaisReportsProperties.CHECKINS_DATA_EXPORT_REPORT_DEFINITION_UUID, "checkinsdataexport",
                        Arrays.asList("checkins")));
            }
            else {
                configurations.add(new Configuration(MirebalaisReportsProperties.FULL_DATA_EXPORT_REPORT_DEFINITION_UUID, "fulldataexport", null));
                configurations.add(new Configuration(MirebalaisReportsProperties.REGISTRATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID, "registrationdataexport",
                        Arrays.asList("registration")));
                configurations.add(new Configuration(MirebalaisReportsProperties.DASHBOARD_DATA_EXPORT_REPORT_DEFINITION_UUID, "dashboarddataexport",
                        Arrays.asList("patients", "checkins", "vitals", "consultations", "diagnoses", "visits", "hospitalizations", "postOpNote1", "postOpNote2")));
                configurations.add(new Configuration(MirebalaisReportsProperties.RADIOLOGY_DATA_EXPORT_REPORT_DEFINITION_UUID, "radiologydataexport",
                        Arrays.asList("radiologyOrders", "radiologyOrderEncounters", "radiologyStudyEncounters", "radiologyReportEncounters")));
                configurations.add(new Configuration(MirebalaisReportsProperties.SURGERY_DATA_EXPORT_REPORT_DEFINITION_UUID, "surgerydataexport",
                        Arrays.asList("postOpNote1", "postOpNote2")));
                configurations.add(new Configuration(MirebalaisReportsProperties.HOSPITALIZATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID, "hospitalizationsdataexport",
                        Arrays.asList("hospitalizations", "postOpNote1", "postOpNote2")));
                configurations.add(new Configuration(MirebalaisReportsProperties.CONSULTATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID, "consultationsdataexport",
                        Arrays.asList("consultations")));
                configurations.add(new Configuration(MirebalaisReportsProperties.PATIENTS_DATA_EXPORT_REPORT_DEFINITION_UUID, "patientsdataexport",
                        Arrays.asList("patients")));
                configurations.add(new Configuration(MirebalaisReportsProperties.ENCOUNTERS_DATA_EXPORT_REPORT_DEFINITION_UUID, "encountersdataexport",
                        Arrays.asList("encounters")));
                configurations.add(new Configuration(MirebalaisReportsProperties.DISPENSING_DATA_EXPORT_REPORT_DEFINITION_UUID, "dispensingdataexport",
                        Arrays.asList("dispensing")));
                configurations.add(new Configuration(MirebalaisReportsProperties.VISITS_DATA_EXPORT_REPORT_DEFINITION_UUID, "visitsdataexport",
                        Arrays.asList("visits")));
                configurations.add(new Configuration(MirebalaisReportsProperties.VITALS_DATA_EXPORT_REPORT_DEFINITION_UUID, "vitalsdataexport",
                        Arrays.asList("vitals")));
                configurations.add(new Configuration(MirebalaisReportsProperties.CHECKINS_DATA_EXPORT_REPORT_DEFINITION_UUID, "checkinsdataexport",
                        Arrays.asList("checkins")));
                configurations.add(new Configuration(MirebalaisReportsProperties.DIAGNOSES_DATA_EXPORT_REPORT_DEFINITION_UUID, "diagnosesdataexport",
                        Arrays.asList("diagnoses")));
            }
        }
        return configurations;
    }

    public List<Extension> getExtensions() {
        ArrayList<Extension> extensions = new ArrayList<Extension>();
        int i = 0;
        for (Configuration c : getConfigurations()) {
            Extension ext = new Extension("mirebalaisreports.dataExports." + (++i), // id
                    null, // appId
                    "org.openmrs.module.reportingui.reports.dataexport", // extensionPointId
                    "link", // type
                    c.getMessageCodePrefix() + "name", // label
                    "/reportingui/runReport.page?reportDefinition=" + c.getUuid(), // url
                    i, // order
                    "App: mirebalaisreports.dataexports", // required privilege
                    null); // extensionParams
            // ideally set extensionParams['linkId'] (but this is not a priority)
            extensions.add(ext);
        }

        return extensions;
    }

    public List<FullDataExportReportManager> getAllReportManagers() {
        ArrayList<FullDataExportReportManager> list = new ArrayList<FullDataExportReportManager>();
        for (Configuration configuration : getConfigurations()) {
            list.add(buildReportManager(configuration));
        }

        return list;
    }

    public FullDataExportReportManager buildReportManager(Configuration configuration) {
        List<String> dataSets = configuration.getDataSets();
        if (dataSets == null || dataSets.size() == 0) {
            dataSets = dataSetOptions;
        }

        FullDataExportReportManager manager = new FullDataExportReportManager(configuration.getUuid(), configuration.getCode(), dataSets);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(manager);
        return manager;
    }

    public static class Configuration {

        private String uuid;

        private String code;

        private List<String> dataSets;

        public Configuration(String uuid, String code, List<String> dataSets) {
            this.uuid = uuid;
            this.code = code;
            this.dataSets = dataSets;
        }

        public String getUuid() {
            return uuid;
        }

        public String getMessageCodePrefix() {
            return "mirebalaisreports." + code + ".";
        }

        public String getCode() {
            return code;
        }

        public List<String> getDataSets() {
            return dataSets;
        }

    }

}
