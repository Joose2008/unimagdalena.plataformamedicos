package unimag.plataformamedicos.service.interfaces;

import unimag.plataformamedicos.api.dtos.DoctorScheduleDtos.CreateDoctorScheduleRequest;
import unimag.plataformamedicos.api.dtos.DoctorScheduleDtos.DoctorScheduleResponse;
import unimag.plataformamedicos.api.dtos.DoctorScheduleDtos.UpdateDoctorScheduleRequest;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface DoctorScheduleService {
    DoctorScheduleResponse create(UUID doctorId, CreateDoctorScheduleRequest request);
    List<DoctorScheduleResponse> findByDoctor(UUID doctorId);
    List<DoctorScheduleResponse> findDoctorScheduleByDoctorAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek);
    DoctorScheduleResponse update(UUID doctorId, UUID scheduleId, UpdateDoctorScheduleRequest request);
    void delete(UUID doctorId, UUID scheduleId);
}
