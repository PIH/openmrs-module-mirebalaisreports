package org.openmrs.module.mirebalaisreports.visit.query;


import org.openmrs.Concept;
import org.openmrs.module.emr.EmrProperties;
import org.openmrs.module.emr.consult.DiagnosisMetadata;
import org.openmrs.module.mirebalaisreports.visit.definition.VisitQuery;
import org.openmrs.module.mirebalaisreports.visit.query.db.VisitQueryDAO;
import org.openmrs.module.reporting.definition.service.BaseDefinitionService;

import java.util.Date;
import java.util.Map;

public class VisitQueryServiceImpl extends BaseDefinitionService<VisitQuery> implements VisitQueryService{

    private VisitQueryDAO visitQueryDAO;

    private EmrProperties emrProperties;


    public void setVisitQueryDAO(VisitQueryDAO visitQueryDAO) {
        this.visitQueryDAO = visitQueryDAO;
    }

    public void setEmrProperties(EmrProperties emrProperties) {
        this.emrProperties = emrProperties;
    }

    @Override
    public Map<Integer, Date> getMapOfVisitIdsAndStartDatesFromVisitsThatHaveDiagnoses(Date startDate, Date endDate){
        DiagnosisMetadata diagnosisMetadata = emrProperties.getDiagnosisMetadata();
        Concept diagnosisSetConcept = diagnosisMetadata.getDiagnosisSetConcept();

        return visitQueryDAO.getMapOfVisitIdsAndDates(diagnosisSetConcept, startDate, endDate);
    }

    @Override
    public Class<VisitQuery> getDefinitionType() {
        return VisitQuery.class;
    }
}
