package unimag.plataformamedicos.api.dtos;

import java.util.UUID;

public class AppointmentTypeDtos {

    public record CreateAppointmentTypeRequest(
            String name,
            String description,
            Integer durationMinutes
    ) {}

    public record AppointmentTypeResponse(
            UUID id,
            String name,
            String description,
            Integer durationMinutes
    ) {}

    public record AppointmentTypeSummaryResponse(
            UUID id,
            String name,
            Integer durationMinutes
    ) {}
}
