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

package org.openmrs.module.mirebalaisreports.page.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.mirebalaisreports.definitions.NonCodedDiagnosesReportManager;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class NonCodedDiagnosesPageController {
    private final Log log = LogFactory.getLog(getClass());

    public void get(@SpringBean NonCodedDiagnosesReportManager reportManager,
                    @SpringBean ProviderService providerService,
                    @RequestParam(required = false, value = "fromDate") Date fromDate,
                    @RequestParam(required = false, value = "toDate") Date toDate,
                    PageModel model) throws EvaluationException, IOException {

        if (fromDate == null) {
            fromDate = DateUtils.addDays(new Date(), -21);
        }
        if (toDate == null) {
            toDate = new Date();
        }
        fromDate = DateUtil.getStartOfDay(fromDate);
        toDate = DateUtil.getEndOfDay(toDate);

        model.addAttribute("nonCodedRows", null);
        model.addAttribute("providers", getAllProviders(providerService));
        model.addAttribute("reportManager", reportManager);
        model.addAttribute("fromDate", null);
        model.addAttribute("toDate", null);
        model.addAttribute("nonCoded", "");
        model.addAttribute("providerId", null);
    }

    public void post(@SpringBean NonCodedDiagnosesReportManager reportManager,
                     @SpringBean ProviderService providerService,
                     @SpringBean ReportDefinitionService reportDefinitionService,
                     @SpringBean CoreAppsProperties coreAppsProperties,
                     @RequestParam(required = false, value = "fromDate") Date fromDate,
                     @RequestParam(required = false, value = "toDate") Date toDate,
                     @RequestParam(required = false, value = "nonCoded") String nonCoded,
                     @RequestParam(required = false, value = "provider") Provider provider,
                     PageModel model) throws EvaluationException, IOException {


        if (fromDate == null) {
            fromDate = DateUtils.addDays(new Date(), -21);
        }
        if (toDate == null) {
            toDate = new Date();
        }
        fromDate = DateUtil.getStartOfDay(fromDate);
        toDate = DateUtil.getEndOfDay(toDate);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);
        if(StringUtils.isBlank(nonCoded)){
            nonCoded = "";
        }
        params.put("nonCoded", nonCoded);
        model.addAttribute("nonCoded", nonCoded);

        Integer providerId = null;
        if ( provider != null ){
            params.put("provider", provider);
            providerId =  provider.getId();
        }else {
            params.put("provider", null);
        }
        model.addAttribute("providerId", providerId);

        EvaluationContext context = new EvaluationContext();
        context.setParameterValues(params);

        ReportDefinition reportDefinition = reportManager.constructReportDefinition();
        ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);
        model.addAttribute("nonCodedRows", reportData.getDataSets().get(NonCodedDiagnosesReportManager.DATA_SET_NAME));

        model.addAttribute("reportManager", reportManager);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", DateUtil.getStartOfDay(toDate));
        model.addAttribute("providers", getAllProviders(providerService));
        model.addAttribute("dashboardUrl", coreAppsProperties.getDashboardUrl());

    }

    public List<Provider> getAllProviders(ProviderService providerService){
        List<Provider> providers = providerService.getAllProviders(true);
        if (providers != null && providers.size() > 0){
            Collections.sort(providers, new Comparator<Provider>() {
                @Override
                public int compare(Provider p1, Provider p2) {
                    return OpenmrsUtil.compareWithNullAsGreatest(p1.getName(), p2.getName());
                }
            });
        }
        return providers;
    }
}
