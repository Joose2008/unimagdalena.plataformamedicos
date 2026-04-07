package unimag.plataformamedicos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.plataformamedicos.api.dtos.DoctorScheduleDtos;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.DoctorSchedule;
import unimag.plataformamedicos.domine.entities.Specialty;
import unimag.plataformamedicos.domine.repositories.DoctorRepository;
import unimag.plataformamedicos.domine.repositories.DoctorScheduleRepository;
import unimag.plataformamedicos.exception.ResourceNoFoundException;
import unimag.plataformamedicos.service.impl.DoctorScheduleServiceImpl;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceImplTest {

    @Mock DoctorScheduleRepository doctorScheduleRepository;
    @Mock DoctorRepository doctorRepository;
    @InjectMocks DoctorScheduleServiceImpl service;

    private UUID doctorId;
    private Doctor doctor;
    private DoctorSchedule schedule;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();

        doctor = Doctor.builder()
                .id(doctorId).name("Dr. López")
                .licenceNumber("LIC-2").email("lopez@test.com")
                .active(true)
                .specialty(Specialty.builder().name("Psicología").build())
                .build();

        schedule = DoctorSchedule.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .build();
    }

    // -----------------------------------------------------------------------
    // create
    // -----------------------------------------------------------------------

    @Test
    void shouldCreateScheduleSuccessfully() {
        var request = new DoctorScheduleDtos.CreateDoctorScheduleRequest(
                DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(16, 0));

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.save(any())).thenReturn(schedule);

        var response = service.create(doctorId, request);

        assertNotNull(response);
        assertEquals(DayOfWeek.MONDAY, response.dayOfWeek());
        verify(doctorScheduleRepository).save(any(DoctorSchedule.class));
    }

    @Test
    void shouldThrowWhenDoctorNotFoundOnCreate() {
        var request = new DoctorScheduleDtos.CreateDoctorScheduleRequest(
                DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(16, 0));

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNoFoundException.class, () -> service.create(doctorId, request));
        verify(doctorScheduleRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // findByDoctor
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnSchedulesByDoctor() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctor(doctor)).thenReturn(List.of(schedule));

        var result = service.findByDoctor(doctorId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(doctorScheduleRepository).findByDoctor(doctor);
    }

    @Test
    void shouldReturnEmptyListWhenDoctorHasNoSchedules() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctor(doctor)).thenReturn(List.of());

        var result = service.findByDoctor(doctorId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowWhenDoctorNotFoundOnFindByDoctor() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNoFoundException.class, () -> service.findByDoctor(doctorId));
    }

    // -----------------------------------------------------------------------
    // findDoctorScheduleByDoctorAndDayOfWeek
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnScheduleByDoctorAndDayOfWeek() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(doctor, DayOfWeek.MONDAY))
                .thenReturn(List.of(schedule));

        var result = service.findDoctorScheduleByDoctorAndDayOfWeek(doctorId, DayOfWeek.MONDAY);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DayOfWeek.MONDAY, result.get(0).dayOfWeek());
    }

    @Test
    void shouldReturnEmptyWhenNoDoctorScheduleForThatDay() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(doctor, DayOfWeek.SUNDAY))
                .thenReturn(List.of());

        var result = service.findDoctorScheduleByDoctorAndDayOfWeek(doctorId, DayOfWeek.SUNDAY);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
