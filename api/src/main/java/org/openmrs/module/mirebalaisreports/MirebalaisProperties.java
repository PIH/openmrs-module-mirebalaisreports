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

package org.openmrs.module.mirebalaisreports;

import org.openmrs.ConceptSource;
import org.openmrs.EncounterType;
import org.openmrs.module.emr.EmrProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 */
@Component("mirebalaisProperties")
public class MirebalaisProperties extends EmrProperties {

    public static final String VITALS_ENCOUNTER_TYPE_UUID = "4fb47712-34a6-40d2-8ed3-e153abbd25b7";
    public static final String REGISTRATION_ENCOUNTER_TYPE_UUID = "873f968a-73a8-4f9c-ac78-9f4778b751b6";
    public static final String PAYMENT_ENCOUNTER_TYPE_UUID = "f1c286d0-b83f-4cd4-8348-7ea3c28ead13";
    public static final String ICD10_CONCEPT_SOURCE_UUID = "3f65bd34-26fe-102b-80cb-0017a47871b2";

    public EncounterType getRegistrationEncounterType() {
        return getRequiredEncounterTypeByUuid(REGISTRATION_ENCOUNTER_TYPE_UUID);
    }

    public EncounterType getPaymentEncounterType() {
        return getRequiredEncounterTypeByUuid(PAYMENT_ENCOUNTER_TYPE_UUID);
    }

    public EncounterType getVitalsEncounterType() {
        return getRequiredEncounterTypeByUuid(VITALS_ENCOUNTER_TYPE_UUID);
    }

    /**
     * @return all encounter types <em>except for</em> Registration, Payment, and Check-In
     */
    @Transactional(readOnly = true)
    public List<EncounterType> getClinicalEncounterTypes() {
        List<EncounterType> encounterTypes = encounterService.getAllEncounterTypes(false);
        encounterTypes.remove(getRegistrationEncounterType());
        encounterTypes.remove(getPaymentEncounterType());
        encounterTypes.remove(getCheckInEncounterType());
        return encounterTypes;
    }

    /**
     * @return all encounter types <em>except for</em> Registration
     */
    @Transactional(readOnly = true)
    public List<EncounterType> getVisitEncounterTypes() {
        List<EncounterType> encounterTypes = encounterService.getAllEncounterTypes(false);
        encounterTypes.remove(getRegistrationEncounterType());
        return encounterTypes;
    }

    private EncounterType getRequiredEncounterTypeByUuid(String uuid) {
        EncounterType encounterType = encounterService.getEncounterTypeByUuid(uuid);
        if (encounterType == null) {
            throw new IllegalStateException("Missing required encounter type with uuid: " + uuid);
        }
        return encounterType;
    }

    public ConceptSource getIcd10ConceptSource() {
        return conceptService.getConceptSourceByUuid(ICD10_CONCEPT_SOURCE_UUID);
    }

    public ConceptSource getPihConceptSource() {
        return conceptService.getConceptSourceByName("PIH");
    }

}
