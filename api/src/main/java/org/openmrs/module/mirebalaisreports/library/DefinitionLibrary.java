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

import org.openmrs.module.reporting.evaluation.Definition;

import java.util.List;

/**
 * Implementations of this class can conveniently implement on-the-fly-created reporting definitions, with inline
 * documentation on how they'll behave.
 * <br/>
 * Remember to annotate implementations as @Handler(supports = T.class)
 */
public interface DefinitionLibrary<T extends Definition> {

    public String getKeyPrefix();

    public T getDefinition(String uuid);

    public List<T> getAllDefinitions(boolean includeRetired);

    public int getNumberOfDefinitions(boolean includeRetired);
}
