package unimag.plataformamedicos.api.dtos;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public class DoctorScheduleDtos {

    public record CreateDoctorScheduleRequest(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) implements Serializable  {}

    public record UpdateDoctorScheduleRequest(
            UUID id,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) implements Serializable  {}

    public record DoctorScheduleResponse(
            UUID id,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) implements Serializable {}
}
