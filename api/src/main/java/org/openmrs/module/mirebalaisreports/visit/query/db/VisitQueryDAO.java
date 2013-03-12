package org.openmrs.module.mirebalaisreports.visit.query.db;


import org.openmrs.Concept;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface VisitQueryDAO {

    Map<Integer, Date> getMapOfVisitIdsAndDates(Concept concept, Date startDate, Date endDate);
}
