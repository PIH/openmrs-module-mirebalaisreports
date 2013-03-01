package org.openmrs.module.mirebalaisreports.page.controller;

import org.openmrs.module.mirebalaisreports.definitions.BasicStatisticsReportManager;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

import java.util.Date;

public class BasicStatisticsPageController {

    public void get(@SpringBean BasicStatisticsReportManager reportManager,
                      PageModel pageModel) throws EvaluationException {

        Date today = new Date();
        MapDataSet reportResult = reportManager.evaluate(today);

        addColumnValueToPageModel(pageModel, reportResult, "startedVisitOnDay");
        addColumnValueToPageModel(pageModel, reportResult, "startedVisitDayBefore");
        addColumnValueToPageModel(pageModel, reportResult, "activeVisits");
        addColumnValueToPageModel(pageModel, reportResult, "todayRegistrations");
    }

    private void addColumnValueToPageModel(PageModel pageModel, MapDataSet dataset, String columnName) {
        pageModel.addAttribute(columnName, dataset.getData().getColumnValue(columnName));
    }
}
