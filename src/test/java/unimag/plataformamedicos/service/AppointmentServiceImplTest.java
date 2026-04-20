package unimag.plataformamedicos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.plataformamedicos.api.dtos.AppointmentDtos;
import unimag.plataformamedicos.domine.entities.*;
import unimag.plataformamedicos.domine.repositories.*;
import unimag.plataformamedicos.enums.AppointmentStatus;
import unimag.plataformamedicos.enums.OfficeStatus;
import unimag.plataformamedicos.enums.PatientStatus;
import unimag.plataformamedicos.exception.ResourceNotFoundException;
import unimag.plataformamedicos.service.impl.AppointmentServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock PatientRepository patientRepository;
    @Mock DoctorRepository doctorRepository;
    @Mock OfficeRepository officeRepository;
    @Mock AppointmentTypeRepository appointmentTypeRepository;
    @Mock DoctorScheduleRepository doctorScheduleRepository;
    @InjectMocks AppointmentServiceImpl service;

    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private UUID officeId;
    private UUID appointmentTypeId;

    private Patient patient;
    private Doctor doctor;
    private Office office;
    private AppointmentType appointmentType;
    private Specialty specialty;
    private DoctorSchedule schedule;
    private Appointment appointment;

    private final LocalDateTime FUTURE = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);

    @BeforeEach
    void setUp() {
        appointmentId     = UUID.randomUUID();
        patientId         = UUID.randomUUID();
        doctorId          = UUID.randomUUID();
        officeId          = UUID.randomUUID();
        appointmentTypeId = UUID.randomUUID();

        specialty = Specialty.builder().name("General").build();

        patient = Patient.builder()
                .id(patientId).name("Juan Perez")
                .documentNumber("123").email("juan@test.com")
                .status(PatientStatus.ACTIVE).build();

        doctor = Doctor.builder()
                .id(doctorId).name("Dr. García")
                .licenceNumber("LIC-1").email("garcia@test.com")
                .active(true).specialty(specialty).build();

        office = Office.builder()
                .id(officeId).name("Consultorio 1")
                .location("Piso 1").status(OfficeStatus.AVAILABLE).build();

        appointmentType = AppointmentType.builder()
                .id(appointmentTypeId).name("General")
                .durationMinutes(30).build();

        schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(FUTURE.getDayOfWeek())
                .startTime(FUTURE.toLocalTime().minusHours(1))
                .endTime(FUTURE.toLocalTime().plusHours(3))
                .build();

        appointment = Appointment.builder()
                .id(appointmentId)
                .patient(patient).doctor(doctor)
                .office(office).appointmentType(appointmentType)
                .startAt(FUTURE)
                .endAt(FUTURE.plusMinutes(30))
                .status(AppointmentStatus.SCHEDULED)
                .build();
    }

    // -----------------------------------------------------------------------
    // create
    // -----------------------------------------------------------------------

    @Test
    void shouldCreateAppointmentSuccessfully() {
        var request = new AppointmentDtos.CreateAppointmentRequest(
                patientId, doctorId, officeId, appointmentTypeId, FUTURE);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(any(), any()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.existsOverLapForDoctor(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsOverLapForOffice(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsOverLapForPatient(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any())).thenReturn(appointment);

        var response = service.create(request);

        assertNotNull(response);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowWhenAppointmentIsInThePast() {
        var request = new AppointmentDtos.CreateAppointmentRequest(
                patientId, doctorId, officeId, appointmentTypeId,
                LocalDateTime.now().minusDays(1));

        assertThrows(RuntimeException.class, () -> service.create(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPatientIsInactive() {
        patient.setStatus(PatientStatus.INACTIVE);
        var request = new AppointmentDtos.CreateAppointmentRequest(
                patientId, doctorId, officeId, appointmentTypeId, FUTURE);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        assertThrows(RuntimeException.class, () -> service.create(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDoctorIsInactive() {
        doctor.setActive(false);
        var request = new AppointmentDtos.CreateAppointmentRequest(
                patientId, doctorId, officeId, appointmentTypeId, FUTURE);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThrows(RuntimeException.class, () -> service.create(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOfficeIsInactive() {
        office.setStatus(OfficeStatus.INACTIVE);
        var request = new AppointmentDtos.CreateAppointmentRequest(
                patientId, doctorId, officeId, appointmentTypeId, FUTURE);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));

        assertThrows(RuntimeException.class, () -> service.create(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDoctorHasNoScheduleForThatDay() {
        var request = new AppointmentDtos.CreateAppointmentRequest(
                patientId, doctorId, officeId, appointmentTypeId, FUTURE);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(any(), any()))
                .thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> service.create(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAppointmentIsOutsideDoctorSchedule() {
        DoctorSchedule tightSchedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(FUTURE.getDayOfWeek())
                .startTime(FUTURE.toLocalTime().plusHours(2))
                .endTime(FUTURE.toLocalTime().plusHours(4))
                .build();

        var request = new AppointmentDtos.CreateAppointmentRequest(
                patientId, doctorId, officeId, appointmentTypeId, FUTURE);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(any(), any()))
                .thenReturn(List.of(tightSchedule));

        assertThrows(RuntimeException.class, () -> service.create(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDoctorOverlapExists() {
        var request = new AppointmentDtos.CreateAppointmentRequest(
                patientId, doctorId, officeId, appointmentTypeId, FUTURE);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(any(), any()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.existsOverLapForDoctor(any(), any(), any())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.create(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOfficeOverlapExists() {
        var request = new AppointmentDtos.CreateAppointmentRequest(
                patientId, doctorId, officeId, appointmentTypeId, FUTURE);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(appointmentTypeId)).thenReturn(Optional.of(appointmentType));
        when(doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(any(), any()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.existsOverLapForDoctor(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsOverLapForOffice(any(), any(), any())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.create(request));
        verify(appointmentRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // confirm
    // -----------------------------------------------------------------------

    @Test
    void shouldConfirmScheduledAppointment() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);

        var response = service.confirm(appointmentId);

        assertNotNull(response);
        verify(appointmentRepository).save(any());
    }

    @Test
    void shouldThrowWhenConfirmingNonScheduledAppointment() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(RuntimeException.class, () -> service.confirm(appointmentId));
        verify(appointmentRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // cancel
    // -----------------------------------------------------------------------

    @Test
    void shouldCancelScheduledAppointment() {
        var request = new AppointmentDtos.CancelAppointmentRequest("Paciente solicitó cancelación");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);

        var response = service.cancel(appointmentId, request);

        assertNotNull(response);
        verify(appointmentRepository).save(any());
    }

    @Test
    void shouldCancelConfirmedAppointment() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        var request = new AppointmentDtos.CancelAppointmentRequest("Emergencia");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);

        var response = service.cancel(appointmentId, request);

        assertNotNull(response);
        verify(appointmentRepository).save(any());
    }

    @Test
    void shouldThrowWhenCancellingCompletedAppointment() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        var request = new AppointmentDtos.CancelAppointmentRequest("Intento");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(RuntimeException.class, () -> service.cancel(appointmentId, request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCancellationReasonIsBlank() {
        var request = new AppointmentDtos.CancelAppointmentRequest("");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(RuntimeException.class, () -> service.cancel(appointmentId, request));
        verify(appointmentRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // complete
    // -----------------------------------------------------------------------

    @Test
    void shouldCompleteConfirmedAppointment() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().minusMinutes(10));
        var request = new AppointmentDtos.CompleteAppointmentRequest("Todo bien");
        String observation = request.observations();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);

        var response = service.complete(appointmentId, observation);

        assertNotNull(response);
        verify(appointmentRepository).save(any());
    }

    @Test
    void shouldThrowWhenCompletingNonConfirmedAppointment() {
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        var request = new AppointmentDtos.CompleteAppointmentRequest("obs");
        String observation = request.observations();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(RuntimeException.class, () -> service.complete(appointmentId, observation));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCompletingBeforeStartTime() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().plusHours(1));
        var request = new AppointmentDtos.CompleteAppointmentRequest("obs");
        String observation = request.observations();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(RuntimeException.class, () -> service.complete(appointmentId, observation));
        verify(appointmentRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // markAsNoShow
    // -----------------------------------------------------------------------

    @Test
    void shouldMarkAsNoShowConfirmedAppointment() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().minusMinutes(10));

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);

        var response = service.markAsNoShow(appointmentId);

        assertNotNull(response);
        verify(appointmentRepository).save(any());
    }

    @Test
    void shouldThrowWhenMarkingNoShowBeforeStartTime() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartAt(LocalDateTime.now().plusHours(1));

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(RuntimeException.class, () -> service.markAsNoShow(appointmentId));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenMarkingNoShowNonConfirmedAppointment() {
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(RuntimeException.class, () -> service.markAsNoShow(appointmentId));
        verify(appointmentRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // findById
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnAppointmentById() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        var response = service.findById(appointmentId);

        assertNotNull(response);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void shouldThrowWhenAppointmentNotFound() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(appointmentId));
    }
}
