package unimag.plataformamedicos.api.dtos;

import unimag.plataformamedicos.domine.entities.AppointmentType;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.enums.AppointmentStatus;

import java.time.Instant;
import java.util.UUID;

public class AppointmentDtos {

    public record CreateAppointmentRequest(
            Instant startAt
    ) {}

    public record CompleteAppointmentRequest(
            String observations
    ) {}

    public record CancelAppointmentRequest(
            String cancellationReason
    ) {}

    public record AppointmentResponse(
            UUID id,
            PatientDtos.PatientSummaryResponse patientId,
            DoctorDtos.DoctorSummaryResponse doctorId,
            OfficeDtos.OfficeResponse officeId,
            AppointmentTypeDtos.AppointmentTypeSummaryResponse appointmentTypeId,
            Instant startAt,
            Instant endAt,
            AppointmentStatus status,
            String cancellationReason,
            String observations
    ) {}
}
