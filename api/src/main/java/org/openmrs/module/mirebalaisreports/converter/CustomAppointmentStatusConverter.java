package org.openmrs.module.mirebalaisreports.converter;

import org.openmrs.module.appointmentscheduling.Appointment;
import org.openmrs.module.reporting.data.converter.DataConverter;

public class CustomAppointmentStatusConverter implements DataConverter {

    @Override
    public Object convert(Object original) {
        Appointment.AppointmentStatus status = (Appointment.AppointmentStatus) original;
        return status.getType().toString();
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
