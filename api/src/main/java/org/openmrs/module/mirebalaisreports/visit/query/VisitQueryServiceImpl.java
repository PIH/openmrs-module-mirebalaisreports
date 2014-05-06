package org.openmrs.module.mirebalaisreports.visit.query;


import java.util.Date;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.mirebalaisreports.visit.query.db.VisitQueryDAO;
import org.openmrs.module.reporting.definition.service.BaseDefinitionService;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;

public class VisitQueryServiceImpl extends BaseDefinitionService<VisitQuery> implements VisitQueryService{

    private VisitQueryDAO visitQueryDAO;

    private EmrApiProperties emrApiProperties;


    public void setVisitQueryDAO(VisitQueryDAO visitQueryDAO) {
        this.visitQueryDAO = visitQueryDAO;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    @Override
    public Map<Integer, Date> getMapOfVisitIdsAndStartDatesFromVisitsThatHaveDiagnoses(Date startDate, Date endDate){
        DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
        Concept diagnosisSetConcept = diagnosisMetadata.getDiagnosisSetConcept();

        return visitQueryDAO.getMapOfVisitIdsAndDates(diagnosisSetConcept, startDate, endDate);
    }

    @Override
    public Class<VisitQuery> getDefinitionType() {
        return VisitQuery.class;
    }
}
