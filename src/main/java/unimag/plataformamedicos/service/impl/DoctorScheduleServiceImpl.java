package unimag.plataformamedicos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.DoctorScheduleDtos;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.DoctorSchedule;
import unimag.plataformamedicos.domine.repositories.DoctorRepository;
import unimag.plataformamedicos.domine.repositories.DoctorScheduleRepository;
import unimag.plataformamedicos.exception.ConflictException;
import unimag.plataformamedicos.exception.ResourceNotFoundException;
import unimag.plataformamedicos.service.interfaces.DoctorScheduleService;
import unimag.plataformamedicos.service.mappers.DoctorScheduleMapper;

import jakarta.validation.ValidationException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public DoctorScheduleDtos.DoctorScheduleResponse create(UUID doctorId, DoctorScheduleDtos.CreateDoctorScheduleRequest request) {

        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id %s not found".formatted(doctorId)));

        validateScheduleTime(request.dayOfWeek(), request.startTime(), request.endTime());
        validateNoOverlap(doctor, request.dayOfWeek(), request.startTime(), request.endTime(), null);

        var doctorSchedule = DoctorScheduleMapper.toEntity(request, doctor);
        var  doctorScheduleSaved =  doctorScheduleRepository.save(doctorSchedule);
        return DoctorScheduleMapper.toResponse(doctorScheduleSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleDtos.DoctorScheduleResponse> findByDoctor(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id %s not found".formatted(doctorId)));

        return doctorScheduleRepository.findByDoctor(doctor).stream().map(DoctorScheduleMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public List<DoctorScheduleDtos.DoctorScheduleResponse> findDoctorScheduleByDoctorAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek){
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor %s not found".formatted(doctorId)));

        return doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(doctor,dayOfWeek).stream().map(DoctorScheduleMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public DoctorScheduleDtos.DoctorScheduleResponse update(UUID doctorId, UUID scheduleId, DoctorScheduleDtos.UpdateDoctorScheduleRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id %s not found".formatted(doctorId)));

        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule %s not found".formatted(scheduleId)));

        if (!schedule.getDoctor().getId().equals(doctorId)) {
            throw new ResourceNotFoundException("Schedule %s not found for doctor %s".formatted(scheduleId, doctorId));
        }

        DayOfWeek dayOfWeek = request.dayOfWeek() != null ? request.dayOfWeek() : schedule.getDayOfWeek();
        LocalTime startTime = request.startTime() != null ? request.startTime() : schedule.getStartTime();
        LocalTime endTime = request.endTime() != null ? request.endTime() : schedule.getEndTime();

        validateScheduleTime(dayOfWeek, startTime, endTime);
        validateNoOverlap(doctor, dayOfWeek, startTime, endTime, scheduleId);

        DoctorScheduleMapper.patch(schedule, request);
        return DoctorScheduleMapper.toResponse(doctorScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public void delete(UUID doctorId, UUID scheduleId) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule %s not found".formatted(scheduleId)));

        if (!schedule.getDoctor().getId().equals(doctorId)) {
            throw new ResourceNotFoundException("Schedule %s not found for doctor %s".formatted(scheduleId, doctorId));
        }

        doctorScheduleRepository.delete(schedule);
    }

    private void validateScheduleTime(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        if (dayOfWeek == null || startTime == null || endTime == null) {
            throw new ValidationException("Dia, hora de inicio y hora de fin son obligatorios.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("La hora de inicio debe ser anterior a la hora de fin.");
        }
    }

    private void validateNoOverlap(Doctor doctor, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, UUID ignoredScheduleId) {
        List<DoctorSchedule> schedules = doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(doctor, dayOfWeek);
        if (schedules == null) {
            schedules = List.of();
        }

        boolean hasOverlap = schedules.stream()
                .filter(schedule -> ignoredScheduleId == null || !schedule.getId().equals(ignoredScheduleId))
                .anyMatch(schedule -> startTime.isBefore(schedule.getEndTime()) && endTime.isAfter(schedule.getStartTime()));

        if (hasOverlap) {
            throw new ConflictException("El doctor ya tiene un horario registrado que se cruza con el horario seleccionado.");
        }
    }
}
