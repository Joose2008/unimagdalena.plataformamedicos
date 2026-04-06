package unimag.plataformamedicos.service.mappers;

import unimag.plataformamedicos.api.dtos.DoctorScheduleDtos;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.DoctorSchedule;

public class DoctorScheduleMapper {

    public static DoctorScheduleDtos.DoctorScheduleResponse toResponse(DoctorSchedule schedule) {
        return new DoctorScheduleDtos.DoctorScheduleResponse(
                schedule.getId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime()
        );
    }

    public static DoctorSchedule toEntity(DoctorScheduleDtos.CreateDoctorScheduleRequest request, Doctor doctor) {
        return DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();
    }

    public static void patch(DoctorSchedule schedule, DoctorScheduleDtos.UpdateDoctorScheduleRequest request) {
        if (request.dayOfWeek() != null) {
            schedule.setDayOfWeek(request.dayOfWeek());
        }
        if (request.startTime() != null) {
            schedule.setStartTime(request.startTime());
        }
        if (request.endTime() != null) {
            schedule.setEndTime(request.endTime());
        }
    }
}
