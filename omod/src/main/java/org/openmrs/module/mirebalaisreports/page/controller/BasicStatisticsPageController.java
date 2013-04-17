package org.openmrs.module.mirebalaisreports.page.controller;

import org.apache.http.impl.cookie.DateParseException;
import org.openmrs.module.mirebalaisreports.definitions.BasicStatisticsReportManager;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

import java.util.Date;
import java.util.Map;

public class BasicStatisticsPageController {

    public void get(@SpringBean BasicStatisticsReportManager reportManager,
                      PageModel pageModel) throws EvaluationException, DateParseException {

        Date today = new Date();
        MapDataSet reportResult = reportManager.evaluate(today);

        for (Map.Entry<String, Object> entry : reportResult.getData().getColumnValuesByKey().entrySet()) {
            pageModel.addAttribute(entry.getKey(), entry.getValue());
        }
    }

}
