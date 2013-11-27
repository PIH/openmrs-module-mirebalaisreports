package org.openmrs.module.mirebalaisreports.definitions.helper;

import org.openmrs.Location;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.module.mirebalaismetadata.MirebalaisMetadataProperties;
import org.openmrs.module.mirebalaisreports.definitions.BaseMirebalaisReportManager;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class DailyIndicatorByLocationReportDefinition extends BaseMirebalaisReportManager {

    public static final String MAP_DAY_TO_START_AND_END_DATE = "startDate=${day},endDate=${day+1d-1s}";

    public abstract String getNameOfLocationParameterOnCohortDefinition();

    public abstract void addDataSetDefinitions(ReportDefinition reportDefinition);

    @Autowired
    protected AllDefinitionLibraries definitionLibraries;

    @Autowired
    protected LocationService locationService;

    @Autowired
    protected ConceptService conceptService;

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("day", "mirebalaisreports.parameter.day", Date.class));
        return l;
    }

    @Override
    public String getName() {
        return getMessageCodePrefix() + "name";
    }

    @Override
    public String getDescription() {
        return getMessageCodePrefix() + "description";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        log.info("Constructing " + getName());

        ReportDefinition rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");
        rd.setUuid(getUuid());
        rd.setParameters(getParameters());
        addDataSetDefinitions(rd);

        return rd;
    }

    /**
     * Override if you want to execute this on a different set of locations
     * @return
     */
    public List<Location> getLocations() {
        List<String> skip = Arrays.asList(MirebalaisMetadataProperties.UNKNOWN_LOCATION_UUID,
                MirebalaisMetadataProperties.MIREBALAIS_HOSPITAL_LOCATION_UUID);
        List<Location> locations = locationService.getAllLocations(false);
        for (Iterator<Location> i = locations.iterator(); i.hasNext(); ) {
            Location candidate = i.next();
            if (skip.contains(candidate.getUuid())) {
                i.remove();
            }
        }
        return locations;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        // TODO what should we return for things that should be web-rendered?
        return new ArrayList<ReportDesign>();
    }

    protected List<Map<String, Object>> getParameterOptions() {
        List<Map<String, Object>> options = new ArrayList<Map<String, Object>>();
        List<Location> locations = getLocations();
        for (Location location : locations) {
            Map<String, Object> option = new HashMap<String, Object>();
            option.put(getNameOfLocationParameterOnCohortDefinition(), location);
            options.add(option);
        }
        return options;
    }
}
