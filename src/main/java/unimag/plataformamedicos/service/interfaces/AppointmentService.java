package unimag.plataformamedicos.service.interfaces;

import unimag.plataformamedicos.api.dtos.AppointmentDtos;
import unimag.plataformamedicos.api.dtos.AppointmentDtos.AppointmentResponse;
import unimag.plataformamedicos.api.dtos.AppointmentDtos.CancelAppointmentRequest;
import unimag.plataformamedicos.api.dtos.AppointmentDtos.CreateAppointmentRequest;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {
    AppointmentDtos.AppointmentResponse create(CreateAppointmentRequest request);
    AppointmentResponse findById(UUID id);
    List<AppointmentResponse> findAll();
    AppointmentResponse confirm(UUID id);
    AppointmentResponse cancel(UUID id, CancelAppointmentRequest request);
    AppointmentResponse complete(UUID id, AppointmentDtos.CompleteAppointmentRequest request);
    AppointmentResponse markAsNoShow(UUID id);
}
