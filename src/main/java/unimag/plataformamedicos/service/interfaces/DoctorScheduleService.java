package unimag.plataformamedicos.service.interfaces;

import unimag.plataformamedicos.api.dtos.DoctorScheduleDtos.CreateDoctorScheduleRequest;
import unimag.plataformamedicos.api.dtos.DoctorScheduleDtos.DoctorScheduleResponse;
import unimag.plataformamedicos.domine.entities.Doctor;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface DoctorScheduleService {
    DoctorScheduleResponse create(UUID doctorId, CreateDoctorScheduleRequest request);
    List<DoctorScheduleResponse> findByDoctor(UUID doctorId);
    List<DoctorScheduleResponse> findDoctorScheduleByDoctorAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek);

}
