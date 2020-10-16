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
import org.openmrs.module.pihcore.config.Components;
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

    public final List<String> dataSetOptions = Arrays.asList(
            "patients", "registration", "visits", "checkins", "vitals", "consultations", "diagnoses",
            "hospitalizations", "postOpNote1", "postOpNote2",
            "radiologyOrders", "radiologyOrderEncounters", "radiologyStudyEncounters", "radiologyReportEncounters",
            "dispensing", "encounters"
    );

    private List<Configuration> configurations;

    /**
     * Note that we have moved all SQL-based reports this class genereates out of this module and into our config projects
     * However, we still have some Java-based reports that need to be configured here
     * We could hopefully clean this up if we decide to implement https://pihemr.atlassian.net/browse/UHM-4736
     */
    public List<Configuration> getConfigurations() {
        if (configurations == null) {
            configurations = new ArrayList<Configuration>();

            // mirebalais-specific
            if (config.getSite().equalsIgnoreCase("MIREBALAIS")) {
                // these reports use a conbination of SQL-based and Java data set definitions, so we can not yet configure the reports  via config
                configurations.add(new Configuration(MirebalaisReportsProperties.FULL_DATA_EXPORT_REPORT_DEFINITION_UUID, "fulldataexport", null));
                configurations.add(new Configuration(MirebalaisReportsProperties.DASHBOARD_DATA_EXPORT_REPORT_DEFINITION_UUID, "dashboarddataexport",
                        Arrays.asList("patients", "checkins", "vitals", "consultations", "diagnoses", "visits", "hospitalizations", "postOpNote1", "postOpNote2")));

                // Java-based dataset definition
                configurations.add(new Configuration(MirebalaisReportsProperties.PATIENTS_DATA_EXPORT_REPORT_DEFINITION_UUID, "patientsdataexport",
                        Arrays.asList("patients")));
            }

            // Haiti-specific
            if (config.getCountry().equals(ConfigDescriptor.Country.HAITI)) {
                // Java-based dataset definition
                configurations.add(new Configuration(MirebalaisReportsProperties.ENCOUNTERS_DATA_EXPORT_REPORT_DEFINITION_UUID, "encountersdataexport",
                        Arrays.asList("encounters")));
            }

            // others that depend on enabled components
            if (config.isComponentEnabled(Components.PATIENT_REGISTRATION) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                // Java-based dataset definition
                configurations.add(new Configuration(MirebalaisReportsProperties.REGISTRATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID, "registrationdataexport",
                        Arrays.asList("registration")));
            }

            if (config.isComponentEnabled(Components.DISPENSING) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                // Java-based dataset definition
                configurations.add(new Configuration(MirebalaisReportsProperties.DISPENSING_DATA_EXPORT_REPORT_DEFINITION_UUID, "dispensingdataexport",
                        Arrays.asList("dispensing")));
            }

            // Haiti Mirebalais uses a SQL data set definition, while the rest of Haiti, and other countries, use a Java DSD based report, so we don't define the vitals reports via config
            if ((config.isComponentEnabled(Components.VITALS) || config.isComponentEnabled(Components.UHM_VITALS))
                    || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.VITALS_DATA_EXPORT_REPORT_DEFINITION_UUID, "vitalsdataexport",
                        Arrays.asList("vitals")));
            }

            // diagnoses is a Java data set definition in Sierra Leone and Liberia, but a SQL report in Haiti, so we handle the config here
            if ((config.isComponentEnabled(Components.VISIT_NOTE) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) &&
                    (!config.getCountry().equals(ConfigDescriptor.Country.SIERRA_LEONE))) {
                configurations.add(new Configuration(MirebalaisReportsProperties.VISIT_NOTE_DATA_EXPORT_REPORT_DEFINITION_UUID, "visitnotedataexport",
                        Arrays.asList("chiefComplaint", "diagnoses", "exams", "feeding", "history", "primaryCarePlans", "supplements", "vaccinations")));
            }

            // Haiti used SQL data set definitions, so for Haiti we define these reports in config
            if (!config.getCountry().equals(ConfigDescriptor.Country.HAITI)) {

                if (config.isComponentEnabled(Components.CHECK_IN) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                    configurations.add(new Configuration(MirebalaisReportsProperties.CHECKINS_DATA_EXPORT_REPORT_DEFINITION_UUID, "checkinsdataexport",
                            Arrays.asList("checkins")));
                }
                if (config.isComponentEnabled(Components.CONSULT) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                    configurations.add(new Configuration(MirebalaisReportsProperties.CONSULTATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID, "consultationsdataexport",
                            Arrays.asList("consultations")));
                }
                if (config.isComponentEnabled(Components.CONSULT) || config.isComponentEnabled(Components.PRIMARY_CARE) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                    configurations.add(new Configuration(MirebalaisReportsProperties.DIAGNOSES_DATA_EXPORT_REPORT_DEFINITION_UUID, "diagnosesdataexport",
                            Arrays.asList("diagnoses")));
                }
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
