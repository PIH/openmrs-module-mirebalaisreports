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

package org.openmrs.module.mirebalaisreports.library;

import org.openmrs.Encounter;
import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.reporting.common.AuditInfo;
import org.openmrs.module.reporting.data.converter.AgeConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.encounter.definition.AgeAtEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.AuditInfoEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.ConvertedEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDatetimeDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.EncounterIdDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.PatientToEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.SimultaneousEncountersDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.SqlEncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Component
public class EncounterDataLibrary extends BaseDefinitionLibrary<EncounterDataDefinition> {

    @Autowired
    MirebalaisReportsProperties props;

    @Autowired
    DispositionService dispositionService;

    @Autowired
    PatientDataLibrary patientDataLibrary;

    @Autowired
    BuiltInPatientDataLibrary builtInPatientDataLibrary;

    @Override
    public Class<? super EncounterDataDefinition> getDefinitionType() {
        return EncounterDataDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return "mirebalais.encounterDataCalculation.";
    }

    @DocumentedDefinition("encounterId")
    public EncounterDataDefinition getEncounterId() {
        return new EncounterIdDataDefinition();
    }

    @DocumentedDefinition("encounterDatetime")
    public EncounterDataDefinition getEncounterDatetimeYmd() {
        return new EncounterDatetimeDataDefinition();
    }

    @DocumentedDefinition("creator")
    public EncounterDataDefinition getCreator() {
        return auditInfo(new PropertyConverter(AuditInfo.class, "creator"),
                new PropertyConverter(User.class, "personName"),
                new ObjectFormatter("{givenName} {familyName}"));
    }

    @DocumentedDefinition("dateCreated")
    public EncounterDataDefinition getDateCreated() {
        return auditInfo(new PropertyConverter(AuditInfo.class, "dateCreated"));
    }

    @DocumentedDefinition("returnVisitDate")
    public EncounterDataDefinition getReturnVisitDate() {
        return sqlEncounterDataDefinition("returnVisitDate.sql", new Replacements().add("returnVisitDate", props.getReturnVisitDate().getId()));
    }

    @DocumentedDefinition("consultationComments")
    public EncounterDataDefinition getComments() {
        return sqlEncounterDataDefinition("comments.sql", new Replacements().add("comments", props.getClinicalImpressionsConcept().getId()));
    }

    @DocumentedDefinition("disposition")
    public EncounterDataDefinition getDisposition() {
        return sqlEncounterDataDefinition("disposition.sql", new Replacements().add("disposition", dispositionService.getDispositionDescriptor().getDispositionConcept()));
    }

    @DocumentedDefinition("preferredZlEmrId")
    public EncounterDataDefinition getPreferredZLEmrId() {
        return new PatientToEncounterDataDefinition(patientDataLibrary.getPreferredZlEmrIdIdentifier());
    }

    @DocumentedDefinition("mostRecentZlEmrId")
    public EncounterDataDefinition getMostRecentZLEmrId() {
        return new PatientToEncounterDataDefinition(patientDataLibrary.getMostRecentZlEmrIdIdentifier());
    }

    @DocumentedDefinition("mostRecentZlEmrIdLocation")
    public EncounterDataDefinition getMostRecentZLEmrIdLocation() {
        return new PatientToEncounterDataDefinition(patientDataLibrary.getMostRecentZlEmrIdLocation());
    }

    @DocumentedDefinition("unknownPatient")
    public EncounterDataDefinition getUnknownPatient() {
        return new PatientToEncounterDataDefinition(patientDataLibrary.getUnknownPatient());

    }

    @DocumentedDefinition("location.name")
    public EncounterDataDefinition getLocationName() {
        return sqlEncounterDataDefinition("locationName.sql",null);
    }

    @DocumentedDefinition("patientId")
    public EncounterDataDefinition getPatientId() {
        return new PatientToEncounterDataDefinition(builtInPatientDataLibrary.getPatientId());
    }

    @DocumentedDefinition("gender")
    public EncounterDataDefinition getGender() {
        return new PatientToEncounterDataDefinition(builtInPatientDataLibrary.getGender());
    }

    @DocumentedDefinition("birthdate.YMD")
    public EncounterDataDefinition getBirthDateYMD() {
        return new PatientToEncounterDataDefinition(builtInPatientDataLibrary.getBirthdateYmd());
    }

    @DocumentedDefinition("ageAtEncounter")
    public EncounterDataDefinition getAgeAtEncounter() {
        return new ConvertedEncounterDataDefinition(new AgeAtEncounterDataDefinition(), new AgeConverter(AgeConverter.YEARS_TO_ONE_DECIMAL_PLACE));
    }

    @DocumentedDefinition("vitalStatus.deathDate")
    public EncounterDataDefinition getVitalStatusDeathDate() {
        return new PatientToEncounterDataDefinition(builtInPatientDataLibrary.getVitalStatusDeathDate());
    }

    @DocumentedDefinition("preferredAddress.department")
    public EncounterDataDefinition getPreferredAddressDepartment() {
        return new PatientToEncounterDataDefinition(patientDataLibrary.getPreferredAddressDepartment());
    }

    @DocumentedDefinition("preferredAddress.commune")
    public EncounterDataDefinition getPreferredAddressCommune() {
        return new PatientToEncounterDataDefinition(patientDataLibrary.getPreferredAddressCommune());
    }

    @DocumentedDefinition("preferredAddress.section")
    public EncounterDataDefinition getPreferredAddressSection() {
        return new PatientToEncounterDataDefinition(patientDataLibrary.getPreferredAddressSection());
    }

    @DocumentedDefinition("preferredAddress.locality")
    public EncounterDataDefinition getPreferredAddressLocality() {
        return new PatientToEncounterDataDefinition(patientDataLibrary.getPreferredAddressLocality());
    }

    @DocumentedDefinition("preferredAddress.streetLandmark")
    public EncounterDataDefinition getPreferredAddressStreetLandmark() {
        return new PatientToEncounterDataDefinition(patientDataLibrary.getPreferredAddressStreetLandmark());
    }

    @DocumentedDefinition("transferOutLocation")
    public EncounterDataDefinition getTransferOutLocation() {
        return sqlEncounterDataDefinition("transferOutLocation.sql", new Replacements().add("transfOut", props.getTransferOutLocationConcept()));
    }

    @DocumentedDefinition("traumaOccurrence")
    public EncounterDataDefinition getTraumaOccurrence() {
        return sqlEncounterDataDefinition("traumaOccurrence.sql", new Replacements().add("trauma", props.getOccurrenceOfTraumaConcept()));
    }

    @DocumentedDefinition("traumaType")
    public EncounterDataDefinition getTraumaType() {
        return sqlEncounterDataDefinition("traumaType.sql", new Replacements().add("traumaType", props.getTraumaTypeConcept()));
    }

    @DocumentedDefinition("codedDiagnosis")
    public EncounterDataDefinition getCodedDiagnosis() {
        return sqlEncounterDataDefinition("countOfObs.sql", new Replacements().add("conceptId", props.getCodedDiagnosis()));
    }

    @DocumentedDefinition("nonCodedDiagnosis")
    public EncounterDataDefinition getNonCodedDiagnosis() {
        return sqlEncounterDataDefinition("countOfObs.sql", new Replacements().add("conceptId", props.getNonCodedDiagnosis()));
    }

    @DocumentedDefinition("encounterType.name")
    public EncounterDataDefinition getEncounterTypeName() {
        return sqlEncounterDataDefinition("encounterTypeName.sql", null);
    }

    @DocumentedDefinition("surgicalService")
    public EncounterDataDefinition getSurgicalService() {
        return sqlEncounterDataDefinition("surgicalService.sql", new Replacements().add("surgicalService", props.getSurgicalService()));
    }

    @DocumentedDefinition("attendingSurgeon.name")
    public EncounterDataDefinition getAttendingSurgeonName() {
        return sqlEncounterDataDefinition("attending.sql", new Replacements().add("attending", props.getAttendingSurgeonEncounterRole()));
    }

//    @DocumentedDefinition("assistantOne")
//    public EncounterDataDefinition getAssistantOne() {
//        return sqlEncounterDataDefinition("assistantOne.sql", new Replacements().add("assistantOne", props.getAssistantOne()));
//    }

    @DocumentedDefinition("otherAssistant")
    public EncounterDataDefinition getOtherAssistant() {
        return sqlEncounterDataDefinition("otherAssistant.sql", new Replacements().add("otherAssistant", props.getOtherAssistant()));
    }

    @DocumentedDefinition("associatedAdtEncounter.encounterType")
    public EncounterDataDefinition getAssociatedAdtEncounterType() {
        return associatedAdtEncounter(new PropertyConverter(Encounter.class, "encounterType"), new ObjectFormatter());
    }

    @DocumentedDefinition("associatedAdtEncounter.location")
    public EncounterDataDefinition getAssociatedAdtEncounterLocation() {
        return associatedAdtEncounter(new PropertyConverter(Encounter.class, "location"), new ObjectFormatter());
    }

    @DocumentedDefinition("retrospective")
    public EncounterDataDefinition getRetrospective() {
        return sqlEncounterDataDefinition("retrospective.sql", null);
    }

    private ConvertedEncounterDataDefinition associatedAdtEncounter(DataConverter... converters) {
        SimultaneousEncountersDataDefinition associated = new SimultaneousEncountersDataDefinition();
        associated.addEncounterType(props.getAdmissionEncounterType());
        associated.addEncounterType(props.getTransferEncounterType());
        associated.addEncounterType(props.getExitFromInpatientEncounterType());

        return new ConvertedEncounterDataDefinition(associated, converters);
    }

    private ConvertedEncounterDataDefinition auditInfo(DataConverter... converters) {
        return new ConvertedEncounterDataDefinition(new AuditInfoEncounterDataDefinition(), converters);
    }

    private EncounterDataDefinition sqlEncounterDataDefinition(String resourceName, Replacements replacements) {
        String sql = MirebalaisReportsUtil.getStringFromResource("org/openmrs/module/mirebalaisreports/sql/encounterData/" + resourceName);
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                sql = sql.replaceAll(":" + entry.getKey(), entry.getValue());
             }
        }

        SqlEncounterDataDefinition definition = new SqlEncounterDataDefinition();
        definition.setSql(sql);
        return definition;
    }

    private class Replacements extends HashMap<String, String> {
        public Replacements add(String key, Object replacement) {
            String asString = replacement instanceof OpenmrsObject ? ((OpenmrsObject) replacement).getId().toString() : replacement.toString();
            super.put(key, asString);
            return this;
        }
    }

}
