package unimag.plataformamedicos.service.interfaces;

import unimag.plataformamedicos.api.dtos.AppointmentTypeDtos;
import unimag.plataformamedicos.api.dtos.AppointmentTypeDtos.AppointmentTypeResponse;
import unimag.plataformamedicos.api.dtos.AppointmentTypeDtos.CreateAppointmentTypeRequest;

import java.util.List;
import java.util.UUID;

public interface AppointmentTypeService {
    AppointmentTypeDtos.AppointmentTypeResponse create(CreateAppointmentTypeRequest request);
    AppointmentTypeResponse findById(UUID id);
    List<AppointmentTypeResponse> findAll();
}
