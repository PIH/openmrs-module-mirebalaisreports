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

import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.PatientToEncounterDataDefinition;
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

    @DocumentedDefinition("returnVisitDate")
    public EncounterDataDefinition getReturnVisitDate() {
        return sqlEncounterDataDefinition("returnVisitDate.sql", new Replacements().add("returnVisitDate", props.getReturnVisitDate().getId()));
    }

    @DocumentedDefinition("comments")
    public EncounterDataDefinition getComments() {
        return sqlEncounterDataDefinition("comments.sql", new Replacements().add("comments", props.getComments().getId()));
    }

    @DocumentedDefinition("disposition")
    public EncounterDataDefinition getDisposition() {
        return sqlEncounterDataDefinition("disposition.sql", new Replacements().add("disposition", dispositionService.getDispositionDescriptor().getDispositionConcept()));
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

    @DocumentedDefinition("gender")
    public EncounterDataDefinition getGender() {
        return new PatientToEncounterDataDefinition(builtInPatientDataLibrary.getGender());
    }

    @DocumentedDefinition("birthDate.YMD")
    public EncounterDataDefinition getBirthDateYMD() {
        return new PatientToEncounterDataDefinition(builtInPatientDataLibrary.getBirthdateYmd());
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

    @DocumentedDefinition("registration.creator.name")
    public EncounterDataDefinition getRegistrationCreatorName() {
        return new PatientToEncounterDataDefinition(patientDataLibrary.getRegistrationCreatorName());
    }

    @DocumentedDefinition("transferOutLocation")
    public EncounterDataDefinition getTransferOutLocation() {
        return sqlEncounterDataDefinition("transferOutLocation.sql", new Replacements().add("transfOut", props.getTransferOutLocation()));
    }

    @DocumentedDefinition("traumaType")
    public EncounterDataDefinition getTraumaType() {
        return sqlEncounterDataDefinition("traumaType.sql", new Replacements().add("traumaType", props.getTraumaType()));
    }

    @DocumentedDefinition("transferOutLocationTraumaName")
    public EncounterDataDefinition getTraumaName() {
        return sqlEncounterDataDefinition("transferOutLocationTraumaName.sql", new Replacements().add("traumaName", props.getTransferOutLocationTraumaName()));
    }

    @DocumentedDefinition("codedDiagnosis")
    public EncounterDataDefinition getCodedDiagnosis() {
        return sqlEncounterDataDefinition("diagnosis.sql", new Replacements().add("diagnosis", props.getCodedDiagnosis()));
    }

    @DocumentedDefinition("nonCodedDiagnosis")
    public EncounterDataDefinition getNonCodedDiagnosis() {
        return sqlEncounterDataDefinition("diagnosis.sql", new Replacements().add("diagnosis", props.getNonCodedDiagnosis()));
    }

    @DocumentedDefinition("admissionEncounterID")
    public EncounterDataDefinition getAdmissionEncounterID() {
        return sqlEncounterDataDefinition("encounterID.sql", new Replacements().add("encounterType", props.getAdmissionEncounter()));
    }

    @DocumentedDefinition("dischargeEncounterID")
    public EncounterDataDefinition getDischargeEncounterID() {
        return sqlEncounterDataDefinition("encounterID.sql", new Replacements().add("encounterType", props.getDischargeEncounter()));
    }

    @DocumentedDefinition("transferEncounterID")
    public EncounterDataDefinition getTransferEncounterID() {
        return sqlEncounterDataDefinition("encounterID.sql", new Replacements().add("encounterType", props.getTransferEncounter()));
    }

    @DocumentedDefinition("admissionEncounterLocation")
    public EncounterDataDefinition getAdmissionEncounterLocation() {
        return sqlEncounterDataDefinition("encounterLocation.sql", new Replacements().add("encounterType", props.getAdmissionEncounter()));
    }

    @DocumentedDefinition("dischargeEncounterLocation")
    public EncounterDataDefinition getDischargeEncounterLocation() {
        return sqlEncounterDataDefinition("encounterLocation.sql", new Replacements().add("encounterType", props.getDischargeEncounter()));
    }

    @DocumentedDefinition("transferEncounterLocation")
    public EncounterDataDefinition getTransferEncounterLocation() {
        return sqlEncounterDataDefinition("encounterLocation.sql", new Replacements().add("encounterType", props.getTransferEncounter()));
    }

    @DocumentedDefinition("admissionEncounterDateCreated")
    public EncounterDataDefinition getAdmissionEncounterDateCreated() {
        return sqlEncounterDataDefinition("encounterDateCreated.sql", new Replacements().add("encounterType", props.getAdmissionEncounter()));
    }

    @DocumentedDefinition("dischargeEncounterDateCreated")
    public EncounterDataDefinition getDischargeEncounterDateCreated() {
        return sqlEncounterDataDefinition("encounterDateCreated.sql", new Replacements().add("encounterType", props.getDischargeEncounter()));
    }

    @DocumentedDefinition("transferEncounterDateCreated")
    public EncounterDataDefinition getTransferEncounterDateCreated() {
        return sqlEncounterDataDefinition("encounterDateCreated.sql", new Replacements().add("encounterType", props.getTransferEncounter()));
    }


    private EncounterDataDefinition sqlEncounterDataDefinition(String resourceName, Replacements replacements) {
        String sql = MirebalaisReportsUtil.getStringFromResource("org/openmrs/module/mirebalaisreports/sql/encounterData/" + resourceName);
        if(replacements != null){
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
            super.put(key, replacement.toString());
            return this;
        }
    }
}
