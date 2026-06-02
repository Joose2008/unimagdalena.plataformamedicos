package unimag.plataformamedicos.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.ReportDtos;
import unimag.plataformamedicos.domine.entities.Appointment;
import unimag.plataformamedicos.domine.entities.AppointmentType;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.DoctorSchedule;
import unimag.plataformamedicos.domine.repositories.AppointmentRepository;
import unimag.plataformamedicos.domine.repositories.AppointmentTypeRepository;
import unimag.plataformamedicos.domine.repositories.DoctorRepository;
import unimag.plataformamedicos.domine.repositories.DoctorScheduleRepository;
import unimag.plataformamedicos.enums.AppointmentStatus;
import unimag.plataformamedicos.exception.ResourceNotFoundException;
import unimag.plataformamedicos.service.interfaces.AvailabilityService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AvailabilityServiceImpl implements AvailabilityService {

    private final DoctorRepository doctorRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    @Override
    @Transactional
    public List<ReportDtos.AvailabilitySlotResponse> getAvailableSlots(UUID doctorId, LocalDate date, UUID appointmentTypeId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor %s not found".formatted(doctorId)));

        AppointmentType appointmentType = appointmentTypeRepository.findById(appointmentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("AppointmentType %s not found".formatted(appointmentTypeId)));

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorSchedule> schedules = doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(doctor,dayOfWeek);

        if(schedules.isEmpty()){
            return List.of();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay   = date.plusDays(1).atStartOfDay();
        List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorAndDate(doctor, startOfDay, endOfDay)
                .stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED
                        || a.getStatus() == AppointmentStatus.CONFIRMED)
                .toList();

        // Generar slots para cada bloque del horario del doctor
        int duration = appointmentType.getDurationMinutes();
        List<ReportDtos.AvailabilitySlotResponse> slots = new ArrayList<>();

        for (DoctorSchedule schedule : schedules) {
            LocalTime cursor      = schedule.getStartTime();
            LocalTime scheduleEnd = schedule.getEndTime();

            // Avanzar slot por slot mientras quepa uno completo en el bloque
            while (!cursor.plusMinutes(duration).isAfter(scheduleEnd)) {
                LocalTime slotEnd = cursor.plusMinutes(duration);

                LocalDateTime slotStartDT = date.atTime(cursor);
                LocalDateTime slotEndDT   = date.atTime(slotEnd);

                // El slot está libre si ninguna cita activa se cruza con él
                boolean occupied = existingAppointments.stream().anyMatch(a ->
                        a.getStartAt().isBefore(slotEndDT) && a.getEndAt().isAfter(slotStartDT)
                );

                if (!occupied) {
                    slots.add(new ReportDtos.AvailabilitySlotResponse(slotStartDT, slotEndDT));
                }

                cursor = cursor.plusMinutes(duration);
            }
        }

        return slots;
    }
}