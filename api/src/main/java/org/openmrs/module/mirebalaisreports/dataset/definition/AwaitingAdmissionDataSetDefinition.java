package org.openmrs.module.mirebalaisreports.dataset.definition;

import org.openmrs.Location;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

/**
 *Searches for patients who are waiting to be admitted at a given location
 */
public class AwaitingAdmissionDataSetDefinition extends BaseDataSetDefinition {

    @ConfigurationProperty
    private Location location;

    public Location getLocation() { return location; }

    public void setLocation(Location location) { this.location = location; }


}
