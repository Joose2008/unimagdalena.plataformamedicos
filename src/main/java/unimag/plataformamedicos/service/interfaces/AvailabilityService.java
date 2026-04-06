package unimag.plataformamedicos.service.interfaces;

import unimag.plataformamedicos.api.dtos.ReportDtos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AvailabilityService {
    List<ReportDtos.AvailabilitySlotResponse> getAvailableSlots(UUID doctorId, LocalDate date, UUID appointmentTypeId);
}
