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

package org.openmrs.module.mirebalaisreports.dataset.definition.evaluator;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.mirebalaisreports.dataset.definition.NonCodedDiagnosisDataSetDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

@Handler(supports = NonCodedDiagnosisDataSetDefinition.class)
public class NonCodedDiagnosisDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private PatientService patientService;

	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		NonCodedDiagnosisDataSetDefinition dsd = (NonCodedDiagnosisDataSetDefinition) dataSetDefinition;

		Date fromDate = ObjectUtil.nvl(dsd.getFromDate(), DateUtils.addDays(new Date(), -7));
		Date toDate = ObjectUtil.nvl(dsd.getToDate(), new Date());
		fromDate = DateUtil.getStartOfDay(fromDate);
		toDate = DateUtil.getEndOfDay(toDate);

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		criteria.add(Restrictions.eq("voided", false));
		criteria.add(Restrictions.ge("dateCreated", fromDate));
		criteria.add(Restrictions.le("dateCreated", toDate));
		criteria.add(Restrictions.eq("concept", emrApiProperties.getDiagnosisMetadata().getNonCodedDiagnosisConcept()));
		criteria.setProjection(Projections.projectionList()
                .add(Projections.property("valueText"))
                .add(Projections.property("creator"))
                .add(Projections.property("dateCreated"))
                .add(Projections.property("personId"))
                .add(Projections.property("obsId")));

		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
		for (Object[] o : (List<Object[]>) criteria.list()) {
			DataSetRow row = new DataSetRow();
			row.addColumnValue(new DataSetColumn("diagnosis", "diagnosis", Concept.class), o[0]);
			row.addColumnValue(new DataSetColumn("creator", "creator", User.class), o[1]);
			row.addColumnValue(new DataSetColumn("dateCreated", "dateCreated", Date.class), o[2]);
            Integer patientId = (Integer) o[3];
            row.addColumnValue(new DataSetColumn("patientId", "patientId", Patient.class), patientId);
            Patient patient = patientService.getPatient( patientId ) ;
            if ( patient !=null ){
                row.addColumnValue(new DataSetColumn("patientIdentifier", "patientIdentifier", PatientIdentifier.class), patient.getPatientIdentifier());
                row.addColumnValue(new DataSetColumn("personName", "personName", PersonName.class), patient.getPersonName());
            }
            row.addColumnValue(new DataSetColumn("obsId", "obsId", Obs.class), o[4]);
			dataSet.addRow(row);
		}
		return dataSet;
	}
}
