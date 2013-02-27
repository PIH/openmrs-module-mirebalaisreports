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

package org.openmrs.module.mirebalaisreports.page.controller.noncodeddiagnoses;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Obs;
import org.openmrs.module.emr.EmrProperties;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class ReportPageController {

    public void get(@SpringBean SessionFactory sessionFactory,
                    @SpringBean EmrProperties emrProperties,
                    @RequestParam(required = false, value = "fromDate") Date fromDate,
                    @RequestParam(required = false, value = "toDate") Date toDate,
                    PageModel model) {

        if (fromDate == null) {
            fromDate = DateUtils.addDays(new Date(), -7);
        }
        if (toDate == null) {
            toDate = new Date();
        }
        fromDate = DateUtil.getStartOfDay(fromDate);
        toDate = DateUtil.getEndOfDay(toDate);

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.ge("dateCreated", fromDate));
        criteria.add(Restrictions.le("dateCreated", toDate));
        criteria.add(Restrictions.eq("concept", emrProperties.getDiagnosisMetadata().getNonCodedDiagnosisConcept()));
        criteria.setProjection(Projections.projectionList().add(Projections.property("valueText")).add(Projections.property("creator")).add(Projections.property("dateCreated")));

        List<SimpleObject> list = new ArrayList<SimpleObject>();
        for (Object[] o : (List<Object[]>) criteria.list()) {
            list.add(SimpleObject.create("diagnosis", o[0], "creator", o[1], "dateCreated", o[2]));
        }

        model.addAttribute("list", list);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", DateUtil.getStartOfDay(toDate));
    }

}
