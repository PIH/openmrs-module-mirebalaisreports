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

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.mirebalaisreports.library.DefinitionLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.indicator.Indicator;
import org.openmrs.module.reporting.indicator.dimension.Dimension;
import org.openmrs.util.HandlerUtil;

public class MirebalaisReportsServiceImpl extends BaseOpenmrsService implements MirebalaisReportsService {

	@Override
	public CohortDefinition getCohortDefinition(String uuid) {
		return getDefinition(uuid, CohortDefinition.class);
	}

	@Override
	public Indicator getIndicator(String uuid) {
		return getDefinition(uuid, Indicator.class);
	}

	@Override
	public Dimension getDimension(String uuid) {
		return getDefinition(uuid, Dimension.class);
	}

	/**
	 * @return The definition of the passed uuid and type.  This will first check any
	 * DefinitionLibrary that is defined, and if none found, will check the appropriate
	 * reporting definition service
	 */
	protected <T extends Definition> T getDefinition(String uuid, Class<T> type) {
		DefinitionLibrary<T> l = HandlerUtil.getPreferredHandler(DefinitionLibrary.class, type);
		if (l != null) {
			return l.getDefinitionByUuid(uuid);
		}
		return DefinitionContext.getDefinitionByUuid(type, uuid);
	}
}
