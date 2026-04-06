package unimag.plataformamedicos.api.dtos;

import unimag.plataformamedicos.enums.AppointmentStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentDtos {

    public record CreateAppointmentRequest(
            UUID patientId,
            UUID doctorId,
            UUID officeId,
            UUID appointmentTypeId,
            LocalDateTime startAt
    ) implements Serializable {}

    public record CompleteAppointmentRequest(
            String observations
    ) implements Serializable {}

    public record CancelAppointmentRequest(
            String cancellationReason
    ) implements Serializable{}

    public record AppointmentResponse(
            UUID id,
            PatientDtos.PatientSummaryResponse patientId,
            DoctorDtos.DoctorSummaryResponse doctorId,
            OfficeDtos.OfficeResponse officeId,
            AppointmentTypeDtos.AppointmentTypeSummaryResponse appointmentTypeId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            AppointmentStatus status,
            String cancellationReason,
            String observations
    )implements Serializable {}
}
