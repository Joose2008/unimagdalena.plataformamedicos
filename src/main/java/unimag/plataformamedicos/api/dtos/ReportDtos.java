package unimag.plataformamedicos.api.dtos;

import java.time.Instant;

public class ReportDtos {

    public record AvailabilitySlotResponse(
            Instant startAt,
            Instant endAt
    ) {}

    public record OfficeOccupancyResponse(
            String officeName,
            Long occupiedMinutes,
            Long totalMinutes,
            Double occupancyPercentage
    ) {}

    public record DoctorProductivityResponse(
            String doctorName,
            String specialtyName,
            Long completedAppointments
    ) {}

    public record NoShowPatientResponse(
            String patientName,
            String documentNumber,
            Long noShowCount
    ) {}
}
