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

import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MirebalaisReportsPropertiesTest {

    @Test
    public void getAllProviders_shouldNotFailIfAProviderIsForAVoidedPatient() throws Exception {
        Person normal = new Person();
        normal.addName(new PersonName("A", "Normal", "Person"));

        Person voided = new Person();
        voided.addName(new PersonName("Has", "Been", "Voided"));
        voided.setVoided(true);
        voided.getPersonName().setVoided(true);

        Provider alice = new Provider();
        alice.setPerson(normal);

        Provider bob = new Provider();
        bob.setPerson(voided);
        bob.setRetired(true);

        ProviderService providerService = mock(ProviderService.class);
        when(providerService.getAllProviders(true)).thenReturn(asList(bob, alice));

        MirebalaisReportsProperties mrp = new MirebalaisReportsProperties();
        mrp.setProviderService(providerService);

        List<Provider> allProviders = mrp.getAllProviders();
        assertThat(allProviders, contains(alice, bob));
    }
}
