/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mirebalaisreports.etl.config;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FileUtils;
import org.openmrs.util.OpenmrsUtil;

/**
 * Loads configuration files into useful formats
 */
public class ConfigLoader {

    private static File configDirectory;

    public static File getConfigDirectory() {
        if (configDirectory == null) {
            File configDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration");
            File pihConfigDir = new File(configDir, "pih");
            File etlDir = new File(pihConfigDir, "etl");
            return etlDir;
        }
        return configDirectory;
    }

    public static void setConfigDirectory(File configDirectory) {
        ConfigLoader.configDirectory = configDirectory;
    }

    /**
     * @return the configuration file at the path relative to the base configuration directory
     */
    public static File getConfigFile(String path) {
        return new File(getConfigDirectory(), path);
    }

    /**
     * @return file contents as String represented by the file at the given path
     */
    public static String getFileContents(String path) {
        File f = getConfigFile(path);
        if (f == null) {
            throw new IllegalStateException("Unable to find configuration file at " + path);
        }
        try {
            return FileUtils.readFileToString(f, "UTF-8");
        }
        catch (Exception e) {
            throw new IllegalStateException("Error parsing " + path + ", please check that the file is valid", e);
        }
    }

    public static <T> T getConfigurationFromFile(String path, Class<T> type) {
        File f = getConfigFile(path);
        if (f == null) {
            throw new IllegalStateException("Unable to find " + type.getSimpleName() + " file at " + path);
        }
        try {
            JsonNode jsonNode = getYamlMapper().readTree(f);
            return getYamlMapper().treeToValue(jsonNode, type);
        }
        catch (Exception e) {
            throw new IllegalStateException("Error parsing " + path + ", please check that the YML is valid", e);
        }
    }

    /**
     * @return an EtlDataSource represented by the file at the given path
     */
    public static EtlJobConfig getEtlJobConfigFromFile(String path) {
        File f = getConfigFile(path);
        if (f == null) {
            throw new IllegalStateException("Unable to find job config file at " + path);
        }
        try {
            JsonNode jsonNode = getYamlMapper().readTree(f);
            return new EtlJobConfig(jsonNode);
        }
        catch (Exception e) {
            throw new IllegalStateException("Error parsing " + path + ", please check that the YML is valid", e);
        }
    }

    /**
     * @return a standard Yaml mapper that can be used for processing YML files
     */
    public static ObjectMapper getYamlMapper() {
        return new ObjectMapper(new YAMLFactory());
    }
}
