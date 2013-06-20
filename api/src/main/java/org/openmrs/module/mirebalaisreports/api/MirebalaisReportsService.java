/**
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
package org.openmrs.module.mirebalaisreports.api;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.indicator.Indicator;
import org.openmrs.module.reporting.indicator.dimension.Dimension;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Public API for Mirebalais Reports-related functionality.
 */
public interface MirebalaisReportsService extends OpenmrsService {

	/**
	 * @return the CohortDefinition with the passed uuid that is defined in a DefinitionLibrary
	 * if none is defined in a DefinitionLibrary it will query the Reporting definition service
	 */
	public CohortDefinition getCohortDefinition(String uuid);

	/**
	 * @return the Indicator with the passed uuid that is defined in a DefinitionLibrary
	 * if none is defined in a DefinitionLibrary it will query the Reporting definition service
	 */
	public Indicator getIndicator(String uuid);

	/**
	 * @return the Dimension with the passed uuid that is defined in a DefinitionLibrary
	 * if none is defined in a DefinitionLibrary it will query the Reporting definition service
	 */
	public Dimension getDimension(String uuid);

}
