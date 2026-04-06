package unimag.plataformamedicos.api.dtos;

import org.springframework.cglib.core.Local;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public class DoctorScheduleDtos {

    public record CreateDoctorScheduleRequest(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {}

    public record UpdateDoctorScheduleRequest(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {}

    public record DoctorScheduleResponse(
            UUID id,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {}
}
