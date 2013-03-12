package org.openmrs.module.mirebalaisreports.visit.query;


import org.openmrs.api.OpenmrsService;
import org.openmrs.module.mirebalaisreports.visit.definition.VisitQuery;
import org.openmrs.module.reporting.definition.service.DefinitionService;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface VisitQueryService extends DefinitionService<VisitQuery> {
    Map<Integer, Date> getMapOfVisitIdsAndStartDatesFromVisitsThatHaveDiagnoses(Date startDate, Date endDate);
}
