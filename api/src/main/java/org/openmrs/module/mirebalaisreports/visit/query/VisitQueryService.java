package org.openmrs.module.mirebalaisreports.visit.query;


import java.util.Date;
import java.util.Map;

import org.openmrs.module.reporting.definition.service.DefinitionService;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;

public interface VisitQueryService extends DefinitionService<VisitQuery> {
    Map<Integer, Date> getMapOfVisitIdsAndStartDatesFromVisitsThatHaveDiagnoses(Date startDate, Date endDate);
}
