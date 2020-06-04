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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.pihcore.config.Components;
import org.openmrs.module.pihcore.config.Config;
import org.openmrs.module.reporting.dataset.definition.SqlFileDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;

/**
 * Builds reports from files specified in the configuration folder
 */
public class SqlFileReportManager extends BaseReportManager {

    protected final Log log = LogFactory.getLog(getClass());

    private Config config;
    private boolean enabled = true;
    private String code;
    private File sqlFile;
    private ReportDefinition rd;
    private List<ReportDesign> designs;
    private Category category = Category.DATA_EXPORT;
    private Integer order = 1000;

    public SqlFileReportManager(Config config, File sqlFile) {
        this.config = config;
        this.sqlFile = sqlFile;

        code = FilenameUtils.getBaseName(sqlFile.getName());

        rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");

        this.designs = new ArrayList<ReportDesign>();

        String reportBaseName = FilenameUtils.getBaseName(sqlFile.getName());
        try {
            List<String> lineByLineContents = FileUtils.readLines(sqlFile, "UTF-8");
            StringBuilder sql = new StringBuilder();

            for (String line : lineByLineContents) {
                if (line.startsWith("-- ##")) {
                    String[] keyValue = StringUtils.splitByWholeSeparator(line.substring(5, line.length()), "=");
                    String key = keyValue[0].trim().toLowerCase();
                    String value = keyValue[1].trim();
                    if (key.equals("report_uuid")) {
                        rd.setUuid(value);
                    }
                    else if (key.equals("parameter")) {
                        String[] paramElements = StringUtils.splitByWholeSeparator(value, "|");
                        Parameter p = new Parameter();
                        p.setName(paramElements[0]);
                        p.setLabel(paramElements[1]);
                        p.setType(Context.loadClass(paramElements[2]));
                        rd.addParameter(p);
                    }
                    else if (key.equals("design")) {
                        String[] designElements = StringUtils.splitByWholeSeparator(value, "|");
                        ReportDesign design = null;
                        String designUuid = designElements[0];
                        String designType = designElements[1];
                        if ("CSV".equalsIgnoreCase(designType)) {
                            design = ReportManagerUtil.createCsvReportDesign(designUuid, rd);
                        }
                        else if ("EXCEL".equalsIgnoreCase(designType)) {
                            design = ReportManagerUtil.createExcelDesign(designUuid, rd);
                        }
                        if (designUuid == null || designType == null || design == null) {
                            throw new IllegalArgumentException("Invalid report design specification: " + value);
                        }
                        designs.add(design);
                    }
                    else if (key.equals("deprecated")) {
                        enabled = false;
                    }
                    // If this key is found, then ensure at least one of the components is enabled
                    // If all data exports is enabled, then do not restrict by component
                    else if (key.equals("components")) {
                        if (StringUtils.isNotBlank(value)) {
                            enabled = config.isComponentEnabled(Components.ALL_DATA_EXPORTS);
                            String[] components = StringUtils.splitByWholeSeparator(value, "|");
                            for (String component : components) {
                                if (config.isComponentEnabled(component)) {
                                    enabled = true;
                                }
                            }
                        }
                    }
                    else if (key.equals("category")) {
                        try {
                            this.category = Category.valueOf(value);
                        }
                        catch (Exception e) {
                            log.warn("Unable to parse category as Category from " + value);
                        }
                    }
                    else if (key.equals("order")) {
                        try {
                            this.order = Integer.parseInt(value);
                        }
                        catch (Exception e) {
                            log.warn("Unable to parse order as Integer from " + value);
                        }
                    }
                }
                sql.append(line).append(System.getProperty("line.separator"));
            }

            if (rd.getUuid() == null || rd.getName() == null || designs.size() == 0) {
                throw new IllegalArgumentException("SQL report " + sqlFile + " must define a report_name, "
                        + "report_uuid and at least one report design at minimum");
            }

            SqlFileDataSetDefinition dsd = new SqlFileDataSetDefinition();
            dsd.setSql(sql.toString());
            dsd.setParameters(rd.getParameters());

            rd.addDataSetDefinition(reportBaseName, Mapped.mapStraightThrough(dsd));
        }
        catch (Exception e) {
            log.warn("Unable to load SQL Report from configuration at " + sqlFile, e);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getUuid() {
        return rd.getUuid();
    }

    @Override
    public String getName() {
        return getCode();
    }

    @Override
    public String getDescription() {
        return "mirebalaisreports." + getCode() + ".description";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        return rd;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) throws IOException {
        return designs;
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public Integer getOrder() {
        return order;
    }


}
