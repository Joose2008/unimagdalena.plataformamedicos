package unimag.plataformamedicos.service.mappers;

import unimag.plataformamedicos.api.dtos.AppointmentDtos;
import unimag.plataformamedicos.domine.entities.Appointment;
import unimag.plataformamedicos.domine.entities.*;

public class AppointmentMapper {

    public static Appointment toEntity(
            AppointmentDtos.CreateAppointmentRequest request,
            Patient patient,
            Doctor doctor,
            Office office,
            AppointmentType appointmentType) {
        return Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .office(office)
                .appointmentType(appointmentType)
                .startAt(request.startAt())
                .build();
    }

    public static AppointmentDtos.AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentDtos.AppointmentResponse(
                appointment.getId(),
                PatientMapper.toSummaryResponse(appointment.getPatient()),
                DoctorMapper.toSummaryResponse(appointment.getDoctor()),
                OfficeMapper.toResponse(appointment.getOffice()),
                AppointmentTypeMapper.toSummaryResponse(appointment.getAppointmentType()),
                appointment.getStartAt(),
                appointment.getEndAt(),
                appointment.getStatus(),
                appointment.getCancellationReason(),
                appointment.getObservations()
        );
    }
}
