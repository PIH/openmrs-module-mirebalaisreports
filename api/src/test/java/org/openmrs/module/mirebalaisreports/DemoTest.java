package org.openmrs.module.mirebalaisreports;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.encounter.library.BuiltInEncounterDataLibrary;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.query.encounter.definition.AuditEncounterQuery;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Adding to demonstrate how we can use an Encounter Data Set to get most recently registered patients
 */
@Ignore
public class DemoTest {

    @Autowired
    BuiltInEncounterDataLibrary encounterData;

    @Autowired
    DataSetDefinitionService dataSetDefinitionService;

    @Autowired
    BuiltInPatientDataLibrary patientData;

    @Test
    public void test() throws Exception {

        EncounterDataSetDefinition d = new EncounterDataSetDefinition();

        AuditEncounterQuery rowFilter = new AuditEncounterQuery();
        // rowFilter.setEncounterTypes(); // You'll need to set this to a list containing the registration encounter type
        rowFilter.setLatestCreatedNumber(10);

        d.addRowFilter(rowFilter, "");

        d.addColumn("patientId", patientData.getPatientId(), "");
        d.addColumn("identifier", patientData.getPreferredIdentifierIdentifier(), "");
        d.addColumn("givenName", patientData.getPreferredGivenName(), "");
        d.addColumn("familyName", patientData.getPreferredFamilyName(), "");
        d.addColumn("gender", patientData.getGender(), "");
        d.addColumn("dateEncounterCreated", encounterData.getDateCreated(), "");

        d.addSortCriteria("dateEncounterCreated", SortCriteria.SortDirection.DESC);

        SimpleDataSet dataSet = (SimpleDataSet)dataSetDefinitionService.evaluate(d, new EvaluationContext());
    }


}
