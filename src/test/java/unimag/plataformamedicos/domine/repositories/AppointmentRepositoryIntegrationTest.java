package unimag.plataformamedicos.domine.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import unimag.plataformamedicos.api.dtos.query.DoctorAppointment;
import unimag.plataformamedicos.api.dtos.query.OfficeOccupancy;
import unimag.plataformamedicos.api.dtos.query.PatientCountStatus;
import unimag.plataformamedicos.api.dtos.query.SpecialtyStats;
import unimag.plataformamedicos.domine.entities.*;
import unimag.plataformamedicos.enums.AppointmentStatus;
import unimag.plataformamedicos.enums.OfficeStatus;
import unimag.plataformamedicos.enums.PatientStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para AppointmentRepository.
 *
 * Este es el repositorio más complejo del proyecto: tiene queries JPQL
 * que validan traslapes, calculan ocupación y generan rankings.
 *
 * Estrategia de tiempos:
 *   - BASE_TIME = ahora truncado a horas (ej: 2025-01-01T10:00:00Z)
 *   - Todas las citas se crean relativamente a BASE_TIME para que los
 *     tests sean reproducibles sin importar cuándo se ejecuten.
 */
@DisplayName("AppointmentRepository - Integration Tests")
class AppointmentRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private OfficeRepository officeRepository;
    @Autowired private AppointmentTypeRepository appointmentTypeRepository;
    @Autowired private SpecialtyRepository specialtyRepository;

    // Punto de referencia temporal para todos los tests
    private final LocalDateTime BASE_TIME = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

    // Entidades compartidas entre tests
    private Patient patient1;
    private Patient patient2;
    private Doctor doctor;
    private Office office;
    private AppointmentType appointmentType; // duración: 30 minutos

    @BeforeEach
    @org.springframework.transaction.annotation.Transactional
    void setUp() {
        limpiarTablas();

        Specialty esp = specialtyRepository.save(
                Specialty.builder().name("Medicina General").build()
        );

        patient1 = patientRepository.save(Patient.builder()
                .name("Paciente Uno")
                .documentNumber("111")
                .email("p1@test.com")
                .status(PatientStatus.ACTIVE)
                .build());

        patient2 = patientRepository.save(Patient.builder()
                .name("Paciente Dos")
                .documentNumber("222")
                .email("p2@test.com")
                .status(PatientStatus.ACTIVE)
                .build());

        doctor = doctorRepository.save(Doctor.builder()
                .name("Dr. Pérez")
                .licenceNumber("LIC-999")
                .email("perez@hospital.com")
                .active(true)
                .specialty(esp)
                .build());

        office = officeRepository.save(Office.builder()
                .name("Consultorio 1")
                .location("Piso 1")
                .status(OfficeStatus.AVAILABLE)
                .build());

        appointmentType = appointmentTypeRepository.save(AppointmentType.builder()
                .name("Consulta General")
                .durationMinutes(30)
                .build());
    }

    // Método helper para crear y guardar citas fácilmente
    private Appointment saveAppointment(Patient p, Doctor d, Office o,
                                        LocalDateTime start, LocalDateTime end,
                                        AppointmentStatus status) {
        return appointmentRepository.save(Appointment.builder()
                .patient(p)
                .doctor(d)
                .office(o)
                .appointmentType(appointmentType)
                .startAt(start)
                .endAt(end)
                .status(status)
                .build());
    }

    // -----------------------------------------------------------------------
    // findAppointmentByPatientAndStatus
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findByPatientAndStatus → filtra por paciente y estado correctamente")
    void findByPatientAndStatus_shouldFilterByPatientAndStatus() {
        // Cita SCHEDULED del patient1
        saveAppointment(patient1, doctor, office,
                BASE_TIME, BASE_TIME.plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.SCHEDULED);

        // Cita CANCELLED del patient1 → no debe aparecer
        saveAppointment(patient1, doctor, office,
                BASE_TIME.plus(1, ChronoUnit.HOURS),
                BASE_TIME.plus(90, ChronoUnit.MINUTES),
                AppointmentStatus.CANCELLED);

        // Cita SCHEDULED del patient2 → no debe aparecer
        saveAppointment(patient2, doctor, office,
                BASE_TIME.plus(2, ChronoUnit.HOURS),
                BASE_TIME.plus(150, ChronoUnit.MINUTES),
                AppointmentStatus.SCHEDULED);

        List<Appointment> result = appointmentRepository
                .findAppointmentByPatientAndStatus(patient1, AppointmentStatus.SCHEDULED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatient().getId()).isEqualTo(patient1.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    // -----------------------------------------------------------------------
    // findAppointmentByStartAtBetween
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findByStartAtBetween → devuelve citas dentro del rango")
    void findByStartAtBetween_shouldReturnOnlyAppointmentsInRange() {
        LocalDateTime rangeStart = BASE_TIME;
        LocalDateTime rangeEnd   = BASE_TIME.plus(4, ChronoUnit.HOURS);

        // Cita DENTRO del rango
        saveAppointment(patient1, doctor, office,
                BASE_TIME.plus(1, ChronoUnit.HOURS),
                BASE_TIME.plus(90, ChronoUnit.MINUTES),
                AppointmentStatus.SCHEDULED);

        // Cita FUERA del rango
        saveAppointment(patient1, doctor, office,
                BASE_TIME.plus(6, ChronoUnit.HOURS),
                BASE_TIME.plus(390, ChronoUnit.MINUTES),
                AppointmentStatus.SCHEDULED);

        List<Appointment> result = appointmentRepository
                .findAppointmentByStartAtBetween(rangeStart, rangeEnd);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartAt()).isAfterOrEqualTo(rangeStart);
        assertThat(result.get(0).getStartAt()).isBeforeOrEqualTo(rangeEnd);
    }

    // -----------------------------------------------------------------------
    // existsOverLapForDoctor  (JPQL crítica de negocio)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("existsOverLapForDoctor → detecta traslape real")
    void existsOverLapForDoctor_whenOverlap_shouldReturnTrue() {
        // Cita existente: 10:00 - 10:30
        saveAppointment(patient1, doctor, office,
                BASE_TIME, BASE_TIME.plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.SCHEDULED);

        // Intento de nueva cita: 10:15 - 10:45 → traslape con la anterior
        boolean overlap = appointmentRepository.existsOverLapForDoctor(
                doctor,
                BASE_TIME.plus(15, ChronoUnit.MINUTES),
                BASE_TIME.plus(45, ChronoUnit.MINUTES)
        );

        assertThat(overlap).isTrue();
    }

    @Test
    @DisplayName("existsOverLapForDoctor → no detecta traslape cuando hay espacio")
    void existsOverLapForDoctor_whenNoOverlap_shouldReturnFalse() {
        // Cita existente: 10:00 - 10:30
        saveAppointment(patient1, doctor, office,
                BASE_TIME, BASE_TIME.plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.SCHEDULED);

        // Nueva cita: 10:30 - 11:00 → empieza justo donde termina la anterior, sin traslape
        boolean overlap = appointmentRepository.existsOverLapForDoctor(
                doctor,
                BASE_TIME.plus(30, ChronoUnit.MINUTES),
                BASE_TIME.plus(60, ChronoUnit.MINUTES)
        );

        assertThat(overlap).isFalse();
    }

    @Test
    @DisplayName("existsOverLapForDoctor → ignora citas CANCELLED/COMPLETED/NO_SHOW")
    void existsOverLapForDoctor_shouldIgnoreCancelledAndCompleted() {
        // Cita CANCELADA en el mismo horario → no debe bloquear
        saveAppointment(patient1, doctor, office,
                BASE_TIME, BASE_TIME.plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.CANCELLED);

        boolean overlap = appointmentRepository.existsOverLapForDoctor(
                doctor,
                BASE_TIME,
                BASE_TIME.plus(30, ChronoUnit.MINUTES)
        );

        // CANCELLED no bloquea la agenda
        assertThat(overlap).isFalse();
    }

    // -----------------------------------------------------------------------
    // existsOverLapForOffice  (JPQL crítica de negocio)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("existsOverLapForOffice → detecta traslape en consultorio")
    void existsOverLapForOffice_whenOverlap_shouldReturnTrue() {
        saveAppointment(patient1, doctor, office,
                BASE_TIME, BASE_TIME.plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.CONFIRMED);

        boolean overlap = appointmentRepository.existsOverLapForOffice(
                office,
                BASE_TIME.plus(10, ChronoUnit.MINUTES),
                BASE_TIME.plus(40, ChronoUnit.MINUTES)
        );

        assertThat(overlap).isTrue();
    }

    @Test
    @DisplayName("existsOverLapForOffice → no detecta traslape cuando hay espacio")
    void existsOverLapForOffice_whenNoOverlap_shouldReturnFalse() {
        saveAppointment(patient1, doctor, office,
                BASE_TIME, BASE_TIME.plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.CONFIRMED);

        boolean overlap = appointmentRepository.existsOverLapForOffice(
                office,
                BASE_TIME.plus(30, ChronoUnit.MINUTES),
                BASE_TIME.plus(60, ChronoUnit.MINUTES)
        );

        assertThat(overlap).isFalse();
    }

    // -----------------------------------------------------------------------
    // sumOccupiedMinutesByOffice  (reporte de ocupación)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("sumOccupiedMinutesByOffice → suma minutos ocupados correctamente")
    void sumOccupiedMinutesByOffice_shouldSumDurationMinutes() {
        // 2 citas de 30 min cada una = 60 minutos totales
        saveAppointment(patient1, doctor, office,
                BASE_TIME, BASE_TIME.plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.COMPLETED);

        saveAppointment(patient2, doctor, office,
                BASE_TIME.plus(1, ChronoUnit.HOURS),
                BASE_TIME.plus(90, ChronoUnit.MINUTES),
                AppointmentStatus.CONFIRMED);

        LocalDateTime rangeStart = BASE_TIME.minus(1, ChronoUnit.HOURS);
        LocalDateTime rangeEnd   = BASE_TIME.plus(3, ChronoUnit.HOURS);

        List<OfficeOccupancy> result = appointmentRepository
                .sumOccupiedMinutesByOffice(rangeStart, rangeEnd);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).office().getId()).isEqualTo(office.getId());
        assertThat(result.get(0).sumOccupiedMinutes()).isEqualTo(60L);
    }

    @Test
    @DisplayName("sumOccupiedMinutesByOffice → excluye citas CANCELADAS del cálculo")
    void sumOccupiedMinutesByOffice_shouldExcludeCancelledAppointments() {
        // Cita COMPLETED = cuenta
        saveAppointment(patient1, doctor, office,
                BASE_TIME, BASE_TIME.plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.COMPLETED);

        // Cita CANCELLED = NO cuenta
        saveAppointment(patient2, doctor, office,
                BASE_TIME.plus(1, ChronoUnit.HOURS),
                BASE_TIME.plus(90, ChronoUnit.MINUTES),
                AppointmentStatus.CANCELLED);

        List<OfficeOccupancy> result = appointmentRepository
                .sumOccupiedMinutesByOffice(
                        BASE_TIME.minus(1, ChronoUnit.HOURS),
                        BASE_TIME.plus(3, ChronoUnit.HOURS)
                );

        assertThat(result).hasSize(1);
        // Solo los 30 minutos de la cita completada
        assertThat(result.get(0).sumOccupiedMinutes()).isEqualTo(30L);
    }

    // -----------------------------------------------------------------------
    // rankDoctorByAppointment  (ranking de productividad)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("rankDoctorByAppointment → devuelve doctor con más citas COMPLETED primero")
    void rankDoctorByAppointment_shouldReturnDoctorWithMostCompletedFirst() {
        // Guardamos un segundo doctor para comparar
        Specialty esp = specialtyRepository.findAll().get(0);
        Doctor doctor2 = doctorRepository.save(Doctor.builder()
                .name("Dra. Vargas")
                .licenceNumber("LIC-888")
                .email("vargas@hospital.com")
                .active(true)
                .specialty(esp)
                .build());

        // doctor tiene 2 citas completadas
        saveAppointment(patient1, doctor, office,
                BASE_TIME, BASE_TIME.plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.COMPLETED);
        saveAppointment(patient2, doctor, office,
                BASE_TIME.plus(1, ChronoUnit.HOURS),
                BASE_TIME.plus(90, ChronoUnit.MINUTES),
                AppointmentStatus.COMPLETED);

        // doctor2 tiene 1 cita completada
        saveAppointment(patient1, doctor2, office,
                BASE_TIME.plus(2, ChronoUnit.HOURS),
                BASE_TIME.plus(150, ChronoUnit.MINUTES),
                AppointmentStatus.COMPLETED);

        List<DoctorAppointment> ranking = appointmentRepository.rankDoctorByAppointment();

        assertThat(ranking).hasSize(2);
        // El primero debe ser el doctor con 2 citas
        assertThat(ranking.get(0).countCompletedAppointment()).isEqualTo(2L);
        assertThat(ranking.get(0).doctor().getId()).isEqualTo(doctor.getId());
    }

    // -----------------------------------------------------------------------
    // rankPatientByStatusNoShow  (ranking de inasistencias)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("rankPatientByStatusNoShow → identifica paciente con más NO_SHOW")
    void rankPatientByStatusNoShow_shouldRankPatientWithMostNoShows() {
        LocalDateTime periodStart = BASE_TIME.minus(30, ChronoUnit.DAYS);
        LocalDateTime periodEnd   = BASE_TIME.plus(1, ChronoUnit.DAYS);

        // patient1: 2 NO_SHOW
        saveAppointment(patient1, doctor, office,
                BASE_TIME.minus(10, ChronoUnit.DAYS),
                BASE_TIME.minus(10, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.NO_SHOW);
        saveAppointment(patient1, doctor, office,
                BASE_TIME.minus(5, ChronoUnit.DAYS),
                BASE_TIME.minus(5, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.NO_SHOW);

        // patient2: 1 NO_SHOW
        saveAppointment(patient2, doctor, office,
                BASE_TIME.minus(3, ChronoUnit.DAYS),
                BASE_TIME.minus(3, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.NO_SHOW);

        List<PatientCountStatus> ranking = appointmentRepository
                .rankPatientByStatusNoShow(periodStart, periodEnd);

        assertThat(ranking).hasSize(2);
        // patient1 debe estar primero con 2 no-shows
        assertThat(ranking.get(0).patient().getId()).isEqualTo(patient1.getId());
        assertThat(ranking.get(0).countNoShow()).isEqualTo(2L);
    }

    // -----------------------------------------------------------------------
    // countCancelledAndNoShowAppointmentsBySpecialty
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("countCancelledAndNoShowBySpecialty → agrupa y cuenta correctamente")
    void countCancelledAndNoShowBySpecialty_shouldGroupBySpecialty() {
        saveAppointment(patient1, doctor, office,
                BASE_TIME, BASE_TIME.plus(30, ChronoUnit.MINUTES),
                AppointmentStatus.CANCELLED);
        saveAppointment(patient2, doctor, office,
                BASE_TIME.plus(1, ChronoUnit.HOURS),
                BASE_TIME.plus(90, ChronoUnit.MINUTES),
                AppointmentStatus.NO_SHOW);

        List<SpecialtyStats> stats = appointmentRepository
                .countCancelledAndNoShowAppointmentsBySpecialty();

        assertThat(stats).hasSize(1);
        SpecialtyStats stat = stats.get(0);
        assertThat(stat.cancelled()).isEqualTo(1L);
        assertThat(stat.noShow()).isEqualTo(1L);
    }
}
