package unimag.plataformamedicos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.plataformamedicos.api.dtos.ReportDtos;
import unimag.plataformamedicos.domine.entities.*;
import unimag.plataformamedicos.domine.repositories.AppointmentRepository;
import unimag.plataformamedicos.domine.repositories.AppointmentTypeRepository;
import unimag.plataformamedicos.domine.repositories.DoctorRepository;
import unimag.plataformamedicos.domine.repositories.DoctorScheduleRepository;
import unimag.plataformamedicos.enums.AppointmentStatus;
import unimag.plataformamedicos.enums.OfficeStatus;
import unimag.plataformamedicos.enums.PatientStatus;
import unimag.plataformamedicos.exception.ResourceNotFoundException;
import unimag.plataformamedicos.service.impl.AvailabilityServiceImpl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock DoctorRepository doctorRepository;
    @Mock AppointmentTypeRepository appointmentTypeRepository;
    @Mock AppointmentRepository appointmentRepository;
    @Mock DoctorScheduleRepository doctorScheduleRepository;
    @InjectMocks AvailabilityServiceImpl service;

    private UUID doctorId;
    private UUID appointmentTypeId;
    private Doctor doctor;
    private AppointmentType appointmentType;
    private DoctorSchedule schedule;

    // Usamos un lunes fijo para que el DayOfWeek sea predecible
    private final LocalDate DATE = LocalDate.now().plusDays(1);

    @BeforeEach
    void setUp() {
        doctorId          = UUID.randomUUID();
        appointmentTypeId = UUID.randomUUID();

        doctor = Doctor.builder()
                .id(doctorId).name("Dr. Martínez")
                .licenceNumber("LIC-3").email("martinez@test.com")
                .active(true)
                .specialty(Specialty.builder().name("Nutrición").build())
                .build();

        appointmentType = AppointmentType.builder()
                .id(appointmentTypeId).name("Consulta")
                .durationMinutes(30).build();

        schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DATE.getDayOfWeek())
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .build();
    }

    // -----------------------------------------------------------------------
    // getAvailableSlots
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnAllSlotsWhenNoCitasExist() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(any(), any()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorAndDate(any(), any(), any()))
                .thenReturn(List.of());

        List<ReportDtos.AvailabilitySlotResponse> slots = service.getAvailableSlots(
                doctorId, DATE, appointmentTypeId);

        // 8:00-10:00 con slots de 30 min = 4 slots
        assertNotNull(slots);
        assertEquals(4, slots.size());
        assertEquals(DATE.atTime(8, 0), slots.get(0).startAt());
        assertEquals(DATE.atTime(8, 30), slots.get(0).endAt());
    }

    @Test
    void shouldReturnEmptyWhenDoctorHasNoScheduleForThatDay() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(any(), any()))
                .thenReturn(List.of());

        List<ReportDtos.AvailabilitySlotResponse> slots = service.getAvailableSlots(
                doctorId, DATE, appointmentTypeId);

        assertTrue(slots.isEmpty());
        verify(appointmentRepository, never()).findByDoctorAndDate(any(), any(), any());
    }

    @Test
    void shouldExcludeOccupiedSlots() {
        // Cita que ocupa el slot de 8:00-8:30
        Appointment existingAppointment = Appointment.builder()
                .startAt(DATE.atTime(8, 0))
                .endAt(DATE.atTime(8, 30))
                .status(AppointmentStatus.SCHEDULED)
                .patient(Patient.builder().status(PatientStatus.ACTIVE).build())
                .doctor(doctor)
                .office(Office.builder().status(OfficeStatus.AVAILABLE).build())
                .appointmentType(appointmentType)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(any(), any()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorAndDate(any(), any(), any()))
                .thenReturn(List.of(existingAppointment));

        List<ReportDtos.AvailabilitySlotResponse> slots = service.getAvailableSlots(
                doctorId, DATE, appointmentTypeId);

        // 4 slots totales - 1 ocupado = 3 libres
        assertEquals(3, slots.size());
        // El primero disponible debe ser 8:30
        assertEquals(DATE.atTime(8, 30), slots.get(0).startAt());
    }

    @Test
    void shouldReturnOnlyCompleteSlots() {
        // Horario de 8:00 a 9:50 — no cabe un slot completo de 30 min después de 9:30
        DoctorSchedule shortSchedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DATE.getDayOfWeek())
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 50))
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(any(), any()))
                .thenReturn(List.of(shortSchedule));
        when(appointmentRepository.findByDoctorAndDate(any(), any(), any()))
                .thenReturn(List.of());

        List<ReportDtos.AvailabilitySlotResponse> slots = service.getAvailableSlots(
                doctorId, DATE, appointmentTypeId);

        // 8:00, 8:30, 9:00, 9:30 — el de 9:50 no cabe completo
        // 8:00, 8:30, 9:00 — el de 9:30 tampoco cabe porque terminaría a las 10:00 > 9:50
        assertEquals(3, slots.size());
        assertEquals(DATE.atTime(9, 0), slots.get(2).startAt());
    }

    @Test
    void shouldThrowWhenDoctorNotFound() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getAvailableSlots(doctorId, DATE, appointmentTypeId));
    }

    @Test
    void shouldThrowWhenAppointmentTypeNotFound() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getAvailableSlots(doctorId, DATE, appointmentTypeId));
    }
}
