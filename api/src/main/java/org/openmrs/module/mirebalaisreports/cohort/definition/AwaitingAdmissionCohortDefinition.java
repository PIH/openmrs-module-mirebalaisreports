package org.openmrs.module.mirebalaisreports.cohort.definition;

import org.openmrs.Location;
import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

/**
 * Query for patients based on their assigned inpatient location
 */
@Caching(strategy=ConfigurationPropertyCachingStrategy.class)
public class AwaitingAdmissionCohortDefinition extends BaseCohortDefinition{

    @ConfigurationProperty
    private Location location;

    public Location getLocation() { return location; }

    public void setLocation(Location location) { this.location = location; }

}
