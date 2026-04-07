package unimag.plataformamedicos.api.dtos;

import java.io.Serializable;
import java.util.UUID;

public class AppointmentTypeDtos {

    public record CreateAppointmentTypeRequest(
            String name,
            String description,
            Integer durationMinutes
    ) implements Serializable {}

    public record AppointmentTypeResponse(
            UUID id,
            String name,
            String description,
            Integer durationMinutes
    ) implements Serializable{}

    public record AppointmentTypeSummaryResponse(
            UUID id,
            String name,
            Integer durationMinutes
    ) implements Serializable{}
}
