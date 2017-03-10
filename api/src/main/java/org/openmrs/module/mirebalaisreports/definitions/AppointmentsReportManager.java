package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.PatientIdentifierType;
import org.openmrs.module.appointmentscheduling.AppointmentType;
import org.openmrs.module.appointmentscheduling.reporting.data.definition.AppointmentCancelReasonDataDefinition;
import org.openmrs.module.appointmentscheduling.reporting.data.definition.AppointmentEndDateDataDefinition;
import org.openmrs.module.appointmentscheduling.reporting.data.definition.AppointmentLocationDataDefinition;
import org.openmrs.module.appointmentscheduling.reporting.data.definition.AppointmentProviderDataDefinition;
import org.openmrs.module.appointmentscheduling.reporting.data.definition.AppointmentReasonDataDefinition;
import org.openmrs.module.appointmentscheduling.reporting.data.definition.AppointmentStartDateDataDefinition;
import org.openmrs.module.appointmentscheduling.reporting.data.definition.AppointmentStatusDataDefinition;
import org.openmrs.module.appointmentscheduling.reporting.data.definition.AppointmentTypeDataDefinition;
import org.openmrs.module.appointmentscheduling.reporting.dataset.definition.AppointmentDataSetDefinition;
import org.openmrs.module.appointmentscheduling.reporting.query.definition.BasicAppointmentQuery;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.mirebalaisreports.converter.AppointmentStatusToStatusTypeInFrenchConverter;
import org.openmrs.module.pihcore.config.ConfigDescriptor;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.ReportDesignRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openmrs.module.mirebalaisreports.definitions.BaseReportManager.Category.DATA_EXPORT;

@Component
public class AppointmentsReportManager extends BaseMirebalaisReportManager {

    @Autowired
    private AllDefinitionLibraries libraries;

    @Override
    public Category getCategory() {
        return DATA_EXPORT;
    }

    @Override
    public List<ConfigDescriptor.Site> getSites() {
        return Arrays.asList(ConfigDescriptor.Site.MIREBALAIS);
    }

    @Override
    public Integer getOrder() {
        return REPORTING_DATA_EXPORT_REPORTS_ORDER.indexOf(getUuid()) + 1000;
    }

    @Override
    public String getUuid() {
        return MirebalaisReportsProperties.APPOINTMENTS_REPORT_DEFINITION_UUID;
    }

    @Override
    public String getName() {
        return "appointments";
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();
        rd.setName(getMessageCodePrefix() + "name");
        rd.setDescription(getMessageCodePrefix() + "description");
        rd.setParameters(getParameters());
        rd.setUuid(getUuid());

        AppointmentDataSetDefinition dsd = new AppointmentDataSetDefinition();
        dsd.addParameter(getStartDateParameter());
        dsd.addParameter(getEndDateParameter());

        BasicAppointmentQuery query = new BasicAppointmentQuery();
        query.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
        query.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
        dsd.addRowFilter(query, "onOrAfter=${startDate},onOrBefore=${endDate}");

        dsd.addColumn("familyName", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.familyName"),
                "", new ObjectFormatter());
        dsd.addColumn("givenName", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.givenName"),
                "", new ObjectFormatter());
        dsd.addColumn("zlEmrId", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.preferredZlEmrId.identifier"), "");
        dsd.addColumn("dossierNumber", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.mostRecentDossierNumber.identifier"), "");
        dsd.addColumn("telephoneNumber", libraries.getDefinition(PersonDataDefinition.class, "mirebalais.personDataCalculation.telephoneNumber"), "");
        dsd.addColumn("date", new AppointmentStartDateDataDefinition(), "", new DateConverter(MirebalaisReportsProperties.DATE_FORMAT));
        dsd.addColumn("startTime", new AppointmentStartDateDataDefinition(), "", new DateConverter(MirebalaisReportsProperties.TIME_FORMAT));
        dsd.addColumn("endTime", new AppointmentEndDateDataDefinition(), "", new DateConverter(MirebalaisReportsProperties.TIME_FORMAT));
        dsd.addColumn("location", new AppointmentLocationDataDefinition(), "", new ObjectFormatter());
        dsd.addColumn("provider", new AppointmentProviderDataDefinition(), "", new ObjectFormatter());
        dsd.addColumn("serviceType", new AppointmentTypeDataDefinition(), "", new ObjectFormatter());
        dsd.addColumn("reason", new AppointmentReasonDataDefinition(), "", new ObjectFormatter());
        dsd.addColumn("cancelReason", new AppointmentCancelReasonDataDefinition(), "", new ObjectFormatter());
        dsd.addColumn("status", new AppointmentStatusDataDefinition(), "", new AppointmentStatusToStatusTypeInFrenchConverter());
        dsd.addColumn("confidential", new AppointmentTypeDataDefinition(), "", new PropertyConverter(AppointmentType.class, "confidential"));

        dsd.addSortCriteria("date", SortCriteria.SortDirection.ASC);
        dsd.addSortCriteria("startTime", SortCriteria.SortDirection.ASC);

        Map<String, Object> mappings =  new HashMap<String, Object>();
        mappings.put("startDate","${startDate}");
        mappings.put("endDate", "${endDate}");

        rd.addDataSetDefinition("appointments", dsd, mappings);

        return rd;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) throws IOException {

        ReportDesign reportDesign = csvReportDesign(reportDefinition);
        reportDesign.addPropertyValue(ReportDesignRenderer.FILENAME_BASE_PROPERTY,
                "appointments." +
                        "{{ formatDate request.evaluateStartDatetime \"yyyyMMdd\" }}." +
                        "{{ formatDate request.evaluateStartDatetime \"HHmm\" }}");

        // used to save this report to disk when running it as part of scheduled emergency backup
        reportDesign.addReportProcessor(constructSaveToDiskReportProcessorConfiguration());

        return Arrays.asList(reportDesign);
    }

    @Override
    public String getVersion() {
        return "1.10"; // last change: updated disk processor to save to /home/tomcat6 directory
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(getStartDateParameter());
        l.add(getEndDateParameter());
        return l;
    }

    private PatientIdentifierDataDefinition constructPatientIdentifierDataDefinition(PatientIdentifierType type) {
        PatientIdentifierDataDefinition patientIdentifierDataDefinition = new PatientIdentifierDataDefinition();
        patientIdentifierDataDefinition.addType(type);
        patientIdentifierDataDefinition.setIncludeFirstNonNullOnly(true);
        return patientIdentifierDataDefinition;
    }


}
