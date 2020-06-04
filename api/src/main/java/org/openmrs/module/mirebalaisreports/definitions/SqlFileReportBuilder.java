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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.pihcore.config.Config;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Builds reports from files specified in the configuration folder
 */
@Component
public class SqlFileReportBuilder {

    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    Config config;

    public List<SqlFileReportManager> getSqlReportManagers() {
        List<SqlFileReportManager> ret = new ArrayList<SqlFileReportManager>();
        File configDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration");
        File sqlReportDir = FileUtils.getFile(configDir, "pih", "reports", "sql");
        if (sqlReportDir.exists()) {
            for (File sqlReportFile : sqlReportDir.listFiles()) {
                log.debug("Found SQL Report: " + sqlReportFile);
                ret.add(new SqlFileReportManager(config, sqlReportFile));
            }
        }
        else {
            log.info("No SQL Reports Configured in " + sqlReportDir);
        }
        return ret;
    }
}
