package unimag.plataformamedicos.service.mappers;

import unimag.plataformamedicos.api.dtos.AppointmentTypeDtos;
import unimag.plataformamedicos.domine.entities.AppointmentType;

public class AppointmentTypeMapper {

    public static AppointmentTypeDtos.AppointmentTypeResponse toResponse(AppointmentType appointmentType) {
        return new AppointmentTypeDtos.AppointmentTypeResponse(
                appointmentType.getId(),
                appointmentType.getName(),
                appointmentType.getDescription(),
                appointmentType.getDurationMinutes()
        );
    }

    public static AppointmentTypeDtos.AppointmentTypeSummaryResponse toSummaryResponse(AppointmentType appointmentType) {
        return new AppointmentTypeDtos.AppointmentTypeSummaryResponse(
                appointmentType.getId(),
                appointmentType.getName(),
                appointmentType.getDurationMinutes()
        );
    }

    public static AppointmentType toEntity(AppointmentTypeDtos.CreateAppointmentTypeRequest request) {
        return AppointmentType.builder()
                .name(request.name())
                .description(request.description())
                .durationMinutes(request.durationMinutes())
                .build();
    }
}
