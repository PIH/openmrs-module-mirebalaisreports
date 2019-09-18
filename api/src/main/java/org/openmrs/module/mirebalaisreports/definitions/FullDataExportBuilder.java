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

    public static final String SQL_DIR = "org/openmrs/module/mirebalaisreports/sql/fullDataExport/";
    public static final String TEMPLATE_DIR = "org/openmrs/module/mirebalaisreports/reportTemplates/";

    public final List<String> dataSetOptions = Arrays.asList(
            "patients", "registration", "visits", "checkins", "vitals", "consultations", "diagnoses",
            "hospitalizations", "postOpNote1", "postOpNote2",
            "radiologyOrders", "radiologyOrderEncounters", "radiologyStudyEncounters", "radiologyReportEncounters",
            "dispensing", "encounters"
    );

    private List<Configuration> configurations;

    public List<Configuration> getConfigurations() {
        if (configurations == null) {
            configurations = new ArrayList<Configuration>();

            // mirebalais-specific
            if (config.getSite().equals(ConfigDescriptor.Site.MIREBALAIS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.FULL_DATA_EXPORT_REPORT_DEFINITION_UUID, "fulldataexport", null));
                configurations.add(new Configuration(MirebalaisReportsProperties.DASHBOARD_DATA_EXPORT_REPORT_DEFINITION_UUID, "dashboarddataexport",
                        Arrays.asList("patients", "checkins", "vitals", "consultations", "diagnoses", "visits", "hospitalizations", "postOpNote1", "postOpNote2")));

                // TODO should/can these be more generic?
                configurations.add(new Configuration(MirebalaisReportsProperties.VISITS_DATA_EXPORT_REPORT_DEFINITION_UUID, "visitsdataexport",
                        Arrays.asList("visits")));

                configurations.add(new Configuration(MirebalaisReportsProperties.PATIENTS_DATA_EXPORT_REPORT_DEFINITION_UUID, "patientsdataexport",
                        Arrays.asList("patients")));

                configurations.add(new Configuration(MirebalaisReportsProperties.BILLABLE_EXPORT_REPORT_DEFINITION_UUID, "billabledataexport",
                        Arrays.asList("billable")));

            }

            // Haiti-specific
            if (config.getCountry().equals(ConfigDescriptor.Country.HAITI)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.ENCOUNTERS_DATA_EXPORT_REPORT_DEFINITION_UUID, "encountersdataexport",
                        Arrays.asList("encounters")));
                configurations.add(new Configuration(MirebalaisReportsProperties.SOCIAL_ECONOMICS_DATA_EXPORT_REPORT_DEFINITION_UUID, "socialeconomicsdataexport",
                        Arrays.asList("socialEconomics")));

            }

            // others that depend on enabled components
            if (config.isComponentEnabled(Components.PATIENT_REGISTRATION) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.REGISTRATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID, "registrationdataexport",
                        Arrays.asList("registration")));
            }
            if (config.isComponentEnabled(Components.CHECK_IN) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.CHECKINS_DATA_EXPORT_REPORT_DEFINITION_UUID, "checkinsdataexport",
                        Arrays.asList("checkins")));
            }
            if (config.isComponentEnabled(Components.RADIOLOGY) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.RADIOLOGY_DATA_EXPORT_REPORT_DEFINITION_UUID, "radiologydataexport",
                        Arrays.asList("radiologyOrders", "radiologyOrderEncounters", "radiologyStudyEncounters", "radiologyReportEncounters")));
                configurations.add(new Configuration(MirebalaisReportsProperties.RADIOLOGY_CONSOIDATED_DATA_EXPORT_REPORT_DEFINITION_UUID, "radiologyconsolidateddataexport",
                        Arrays.asList("radiologyConsolidated")));

            }
            if (config.isComponentEnabled(Components.SURGERY) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.SURGERY_DATA_EXPORT_REPORT_DEFINITION_UUID, "surgerydataexport",
                        Arrays.asList("postOpNote1", "postOpNote2")));
            }
            if ((config.isComponentEnabled(Components.ADT) && config.isComponentEnabled(Components.SURGERY))
                    || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.HOSPITALIZATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID, "hospitalizationsdataexport",
                        Arrays.asList("hospitalizations", "postOpNote1", "postOpNote2")));
            }
            if (config.isComponentEnabled(Components.CONSULT) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.CONSULTATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID, "consultationsdataexport",
                        Arrays.asList("consultations")));
            }
            if (config.isComponentEnabled(Components.CONSULT) || config.isComponentEnabled(Components.PRIMARY_CARE) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.DIAGNOSES_DATA_EXPORT_REPORT_DEFINITION_UUID, "diagnosesdataexport",
                        Arrays.asList("diagnoses")));
            }
            if (config.isComponentEnabled(Components.DISPENSING) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.DISPENSING_DATA_EXPORT_REPORT_DEFINITION_UUID, "dispensingdataexport",
                        Arrays.asList("dispensing")));
            }
            if ((config.isComponentEnabled(Components.VITALS) || config.isComponentEnabled(Components.UHM_VITALS))
                    || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.VITALS_DATA_EXPORT_REPORT_DEFINITION_UUID, "vitalsdataexport",
                        Arrays.asList("vitals")));
            }
            if (config.isComponentEnabled(Components.LAB_RESULTS) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.LAB_RESULTS_DATA_EXPORT_REPORT_DEFINITION_UUID, "labresultsdataexport",
                        Arrays.asList("labReports")));
            }
            if (config.isComponentEnabled(Components.LAB_RESULTS) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.LAB_RESULTS_EXPORT_DEFINITION_UUID, "labresultsexport",
                        Arrays.asList("labResultsExport")));
            }
            if (config.isComponentEnabled(Components.LAB_RESULTS) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.LAB_ORDERS_DATA_EXPORT_REPORT_DEFINITION_UUID, "labordersdataexport",
                        Arrays.asList("labOrdersReport")));
            }
            if (config.isComponentEnabled(Components.ONCOLOGY) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.ONCOLOGY_DATA_EXPORT_REPORT_DEFINITION_UUID, "oncologydataexport",
                        Arrays.asList("oncology")));
            }
            if (config.isComponentEnabled(Components.NCD) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.NCD_DATA_EXPORT_REPORT_DEFINITION_UUID, "ncddataexport",
                        Arrays.asList("ncd")));
                configurations.add(new Configuration(MirebalaisReportsProperties.NCD_DATA_PROGRAM_EXPORT_REPORT_DEFINITION_UUID, "ncdprogramdataexport",
                        Arrays.asList("ncdProgram")));
            }
            if (config.isComponentEnabled(Components.MENTAL_HEALTH) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.MENTAL_HEALTH_DATA_EXPORT_REPORT_DEFINITION_UUID, "mentalhealthdataexport",
                        Arrays.asList("mentalHealth")));
            }
            if (config.isComponentEnabled(Components.ED_TRIAGE) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.ED_TRIAGE_DATA_EXPORT_REPORT_DEFINITION_UUID, "edtriagedataexport",
                        Arrays.asList("edTriage")));
            }

            if ((config.isComponentEnabled(Components.VISIT_NOTE) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) &&
                    (!config.getCountry().equals(ConfigDescriptor.Country.SIERRA_LEONE))) {
                configurations.add(new Configuration(MirebalaisReportsProperties.VISIT_NOTE_DATA_EXPORT_REPORT_DEFINITION_UUID, "visitnotedataexport",
                        Arrays.asList("chiefComplaint", "diagnoses", "exams", "feeding", "history", "primaryCarePlans", "supplements", "vaccinations")));
            }
            if (config.isComponentEnabled(Components.ALLERGIES) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.ALLERGIES_EXPORT_REPORT_DEFINITION_UUID, "allergiesdataexport",
                        Arrays.asList("allergies")));
            }
            if (config.isComponentEnabled(Components.PATHOLOGY_TRACKING) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.PATHOLOGY_EXPORT_REPORT_DEFINITION_UUID, "pathologydataexport",
                        Arrays.asList("pathology")));
            }
            if (config.isComponentEnabled(Components.BIOMETRICS_FINGERPRINTS) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.PATIENTS_WITH_FINGERPRINTS_DEFINITION_UUID, "fingerprintsdataexport",
                        Arrays.asList("patientsWithFingerprints")));
            }
            if (config.isComponentEnabled(Components.VCT) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.VCT_REPORT_DEFINITION_UUID, "vctdataexport",
                        Arrays.asList("hivCounselingTest")));
            }
            if (config.isComponentEnabled(Components.CHW_APP) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.MCH_PROVIDER_REPORT_DEFINITION_UUID, "mchproviderdataexport",
                        Arrays.asList("mchProvider")));
            }
            if (config.isComponentEnabled(Components.MCH) || config.isComponentEnabled(Components.ALL_DATA_EXPORTS)) {
                configurations.add(new Configuration(MirebalaisReportsProperties.MCH_REPORT_DEFINITION_UUID, "mchdataexport",
                        Arrays.asList("prenatalAndDelivery", "vaccinationsANC")));
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
