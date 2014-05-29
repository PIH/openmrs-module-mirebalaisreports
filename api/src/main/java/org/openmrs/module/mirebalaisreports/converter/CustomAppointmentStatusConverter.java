package org.openmrs.module.mirebalaisreports.converter;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointmentscheduling.Appointment;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.util.Locale;

/**
 * Custom rendering of Appointment status--we always want to show the French translation of the appointment
 * status type, regardless of the locale we are in
 */
public class CustomAppointmentStatusConverter implements DataConverter {

    @Override
    public Object convert(Object original) {
        Appointment.AppointmentStatus status = (Appointment.AppointmentStatus) original;
        return Context.getMessageSourceService()
                .getMessage("appointmentschedulingui.scheduleAppointment.status.type." + status.getType().toString().toLowerCase(),
                        null, new Locale("fr"));
    }

    @Override
    public Class<?> getInputDataType() {
        return Appointment.AppointmentStatus.class;
    }

    @Override
    public Class<?> getDataType() {
        return String.class;
    }
}
