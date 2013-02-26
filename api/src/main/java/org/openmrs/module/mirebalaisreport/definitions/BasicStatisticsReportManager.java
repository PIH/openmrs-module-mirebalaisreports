package org.openmrs.module.mirebalaisreport.definitions;

import org.openmrs.EncounterType;
import org.openmrs.module.emr.EmrProperties;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.dataset.MapDataSet;

import java.util.Collections;
import java.util.Date;

public class BasicStatisticsReportManager {

    private EmrProperties emrProperties;

    public BasicStatisticsReportManager() {

    }

    public MapDataSet evaluate(Date fromDate, Date toDate) {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setTimeQualifier(TimeQualifier.ANY);
        cd.setEncounterTypeList(Collections.singletonList(emrProperties.getCheckInEncounterType()));

        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
