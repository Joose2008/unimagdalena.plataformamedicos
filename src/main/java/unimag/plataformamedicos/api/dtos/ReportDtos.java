package unimag.plataformamedicos.api.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ReportDtos {

    public record AvailabilitySlotResponse(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) implements Serializable  {}

    public record OfficeOccupancyResponse(
            String officeName,
            Long occupiedMinutes,
            Long totalMinutes,
            Double occupancyPercentage
    ) implements Serializable {}

    public record DoctorProductivityResponse(
            String doctorName,
            String specialtyName,
            Long completedAppointments
    ) implements Serializable {}

    public record NoShowPatientResponse(
            String patientName,
            String documentNumber,
            Long noShowCount
    ) implements Serializable {}
}
