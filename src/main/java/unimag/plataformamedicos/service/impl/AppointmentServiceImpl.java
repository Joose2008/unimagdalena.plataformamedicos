package unimag.plataformamedicos.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.AppointmentDtos;
import unimag.plataformamedicos.domine.entities.*;
import unimag.plataformamedicos.domine.repositories.*;
import unimag.plataformamedicos.enums.AppointmentStatus;
import unimag.plataformamedicos.enums.OfficeStatus;
import unimag.plataformamedicos.enums.PatientStatus;
import unimag.plataformamedicos.exception.ResourceNotFoundException;
import unimag.plataformamedicos.service.interfaces.AppointmentService;
import unimag.plataformamedicos.service.mappers.AppointmentMapper;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    @Override
    @Transactional
    public AppointmentDtos.AppointmentResponse create(AppointmentDtos.CreateAppointmentRequest request) {

        if (!request.startAt().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Appointment must be in the future");
        }

        // Validar paciente
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient %s not found".formatted(request.patientId())));
        if (patient.getStatus() != PatientStatus.ACTIVE) {
            throw new RuntimeException("Inactive patient");
        }

        // Validar doctor
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor %s not found".formatted(request.doctorId())));
        if (!doctor.getActive()) {
            throw new RuntimeException("Inactive doctor");
        }

        // Validar consultorio
        Office office = officeRepository.findById(request.officeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Office %s not found".formatted(request.officeId())));
        if (office.getStatus() != OfficeStatus.AVAILABLE) {
            throw new RuntimeException("Inactive office");
        }

        // Validación apppointmentType
        AppointmentType appointmentType = appointmentTypeRepository.findById(request.appointmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AppointmentType %s not found".formatted(request.appointmentTypeId())));

        // Calcular endAt
        LocalDateTime endAt = request.startAt().plusMinutes(appointmentType.getDurationMinutes());

        DayOfWeek appointmentDay  = request.startAt().getDayOfWeek();
        LocalTime appointmentStart = request.startAt().toLocalTime();
        LocalTime appointmentEnd   = endAt.toLocalTime();

        // Buscar los horarios configurados para ese doctor en ese día de la semana
        List<DoctorSchedule> schedulesForDay = doctorScheduleRepository
                .findDoctorScheduleByDoctorAndDayOfWeek(doctor, appointmentDay);

        if (schedulesForDay.isEmpty()) {
            throw new RuntimeException(
                    "DoctorSchedule for that day is empty: " + appointmentDay);
        }

        // La cita debe caber dentro de alguno de los bloques del doctor
        boolean withinSchedule = schedulesForDay.stream().anyMatch(schedule ->
                !appointmentStart.isBefore(schedule.getStartTime()) &&   // startAt >= scheduleStart
                        !appointmentEnd.isAfter(schedule.getEndTime())            // endAt   <= scheduleEnd
        );

        if (!withinSchedule) {
            throw new RuntimeException(
                    "The appointment is outside the doctor's working hours for that day");
        }

        // Validar traslape: doctor
        if (appointmentRepository.existsOverLapForDoctor(doctor, request.startAt(), endAt)) {
            throw new RuntimeException(
                    "The doctor already has an appointment within that time frame");
        }

        // Validar traslape: consultorio
        if (appointmentRepository.existsOverLapForOffice(office, request.startAt(), endAt)) {
            throw new RuntimeException(
                    "The office is already booked during that time slot.");
        }

        // Validar traslape: paciente
        // NOTA: Debes agregar este método a AppointmentRepository (ver comentario al final).
        if (appointmentRepository.existsOverLapForPatient(patient,request.startAt(),endAt)) {
            throw new RuntimeException(
                    "The patient already has an active appointment within that time range.");
        }

        Appointment appointment = AppointmentMapper.toEntity(
                request, patient, doctor, office, appointmentType);
        appointment.setEndAt(endAt);
        // El estado SCHEDULED ya viene por @Builder.Default en la entidad

        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDtos.AppointmentResponse findById(UUID id) {
        return appointmentRepository.findById(id).map(AppointmentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment %s not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDtos.AppointmentResponse> findAll() {
        return appointmentRepository.findAll().stream().map(AppointmentMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public AppointmentDtos.AppointmentResponse confirm(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment %s not found".formatted(id)));

        if(appointment.getStatus()!=AppointmentStatus.SCHEDULED){
            throw  new RuntimeException("An appointment can only be confirmed if it is in SCHEDULED status");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(Instant.now());
        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentDtos.AppointmentResponse cancel(UUID id, AppointmentDtos.CancelAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment %s not found".formatted(id)));

        AppointmentStatus currentStatus = appointment.getStatus();

        if (currentStatus != AppointmentStatus.SCHEDULED && currentStatus != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException(
                    "Appointments can only be canceled if they are in SCHEDULED or CONFIRMED status. Current status: "
                            + currentStatus);
        }
        if (request.cancellationReason() == null || request.cancellationReason().isBlank()) {
            throw new RuntimeException("The reason for cancellation is mandatory.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(request.cancellationReason());
        appointment.setUpdatedAt(Instant.now());
        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentDtos.AppointmentResponse complete(UUID id, String requestObservation) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment %s not found".formatted(id)));

        if(appointment.getStatus() != AppointmentStatus.CONFIRMED){
            throw new RuntimeException("Only one CONFIRMED appointment can be completed");
        }
        if(LocalDateTime.now().isBefore(appointment.getStartAt())){
            throw new RuntimeException("An appointment cannot be completed before its start time");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setObservations(requestObservation);
        appointment.setUpdatedAt(Instant.now());
        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentDtos.AppointmentResponse markAsNoShow(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment %s not found".formatted(id)));

        if(appointment.getStatus() != AppointmentStatus.CONFIRMED){
            throw new RuntimeException("Only one CONFIRMED appointment can be marked NO_SHOW");
        }
        if(LocalDateTime.now().isBefore(appointment.getStartAt())){
            throw new RuntimeException("An appointment cannot be marked NO_SHOW before its start time");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointment.setUpdatedAt(Instant.now());
        return AppointmentMapper.toResponse(appointmentRepository.save(appointment));
    }
}