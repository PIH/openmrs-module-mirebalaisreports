package org.openmrs.module.mirebalaisreports.page.controller;

import org.apache.http.impl.cookie.DateParseException;
import org.openmrs.module.mirebalaisreports.definitions.BasicStatisticsReportManager;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.Map;

public class BasicStatisticsPageController {

    public void get(@RequestParam(required = false, value = "date") Date date,
					@SpringBean BasicStatisticsReportManager reportManager,
                    PageModel pageModel) throws EvaluationException, DateParseException {

        Date today = ObjectUtil.nvl(date, new Date());
		MapDataSet reportResult = reportManager.evaluate(today);

        for (Map.Entry<String, Object> entry : reportResult.getData().getColumnValuesByKey().entrySet()) {
            pageModel.addAttribute(entry.getKey(), entry.getValue());
        }
    }

}
