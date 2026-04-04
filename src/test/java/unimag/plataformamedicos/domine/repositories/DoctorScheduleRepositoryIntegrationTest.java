package unimag.plataformamedicos.domine.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.DoctorSchedule;
import unimag.plataformamedicos.domine.entities.Specialty;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para DoctorScheduleRepository.
 *
 * Método clave a probar:
 *   findDoctorScheduleByDoctorAndDayOfWeek(doctor, dayOfWeek)
 *
 * Importancia de negocio: este método es el que usa el servicio de disponibilidad
 * para saber si un doctor trabaja un día determinado y en qué horario.
 */
@DisplayName("DoctorScheduleRepository - Integration Tests")
class DoctorScheduleRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    private Doctor doctor;
    private Doctor otraDoctor;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        doctorRepository.deleteAll();
        specialtyRepository.deleteAll();

        Specialty esp = specialtyRepository.save(
                Specialty.builder().name("Medicina General").build()
        );

        doctor = doctorRepository.save(Doctor.builder()
                .name("Dr. Juan Suárez")
                .licenceNumber("LIC-100")
                .email("juan@hospital.com")
                .active(true)
                .specialty(esp)
                .build());

        otraDoctor = doctorRepository.save(Doctor.builder()
                .name("Dra. Paula Mora")
                .licenceNumber("LIC-200")
                .email("paula@hospital.com")
                .active(true)
                .specialty(esp)
                .build());

        // Doctor principal: trabaja lunes y martes
        scheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .build());

        scheduleRepository.save(DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(18, 0))
                .build());

        // Otro doctor que trabaja el mismo lunes → no debe aparecer en búsquedas del doctor principal
        scheduleRepository.save(DoctorSchedule.builder()
                .doctor(otraDoctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(13, 0))
                .build());
    }

    @Test
    @DisplayName("findByDoctorAndDayOfWeek → devuelve horario del día correcto")
    void findByDoctorAndDayOfWeek_shouldReturnScheduleForThatDay() {
        List<DoctorSchedule> result =
                scheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(doctor, DayOfWeek.MONDAY);

        assertThat(result).hasSize(1);
        DoctorSchedule schedule = result.get(0);
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    @DisplayName("findByDoctorAndDayOfWeek → no mezcla horarios de otros doctores")
    void findByDoctorAndDayOfWeek_shouldNotIncludeOtherDoctors() {
        List<DoctorSchedule> result =
                scheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(doctor, DayOfWeek.MONDAY);

        // Solo el horario del doctor principal, no el de otraDoctor
        assertThat(result).allMatch(s -> s.getDoctor().getId().equals(doctor.getId()));
    }

    @Test
    @DisplayName("findByDoctorAndDayOfWeek → lista vacía si el doctor no trabaja ese día")
    void findByDoctorAndDayOfWeek_whenNoDaySchedule_shouldReturnEmpty() {
        // El doctor no tiene horario los miércoles
        List<DoctorSchedule> result =
                scheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(doctor, DayOfWeek.WEDNESDAY);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByDoctorAndDayOfWeek → devuelve el horario correcto para martes")
    void findByDoctorAndDayOfWeek_shouldReturnTuesdaySchedule() {
        List<DoctorSchedule> result =
                scheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(doctor, DayOfWeek.TUESDAY);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(18, 0));
    }
}
