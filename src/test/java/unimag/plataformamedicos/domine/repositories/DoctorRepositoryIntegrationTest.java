package unimag.plataformamedicos.domine.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.Specialty;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para DoctorRepository.
 *
 * El método más importante a cubrir aquí es:
 *   findDoctorBySpecialtyAndActiveTrue(specialty)
 * que es un Query Method derivado: Spring genera el SQL a partir del nombre del método.
 */
@DisplayName("DoctorRepository - Integration Tests")
class DoctorRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    private Specialty cardiologia;
    private Specialty psicologia;

    @BeforeEach
    void setUp() {
        doctorRepository.deleteAll();
        specialtyRepository.deleteAll();

        // Creamos dos especialidades distintas para probar el filtro
        cardiologia = specialtyRepository.save(
                Specialty.builder().name("Cardiología").description("Corazón").build()
        );
        psicologia = specialtyRepository.save(
                Specialty.builder().name("Psicología").description("Salud mental").build()
        );

        // Doctor activo en Cardiología
        doctorRepository.save(Doctor.builder()
                .name("Dr. Mario Rivas")
                .licenceNumber("LIC-001")
                .email("mario@hospital.com")
                .active(true)
                .specialty(cardiologia)
                .build());

        // Otro doctor activo en Cardiología
        doctorRepository.save(Doctor.builder()
                .name("Dra. Laura Nieto")
                .licenceNumber("LIC-002")
                .email("laura@hospital.com")
                .active(true)
                .specialty(cardiologia)
                .build());

        // Doctor INACTIVO en Cardiología → no debe aparecer en la búsqueda
        doctorRepository.save(Doctor.builder()
                .name("Dr. Inactivo Médico")
                .licenceNumber("LIC-003")
                .email("inactivo@hospital.com")
                .active(false)
                .specialty(cardiologia)
                .build());

        // Doctor activo en Psicología → no debe aparecer al buscar por Cardiología
        doctorRepository.save(Doctor.builder()
                .name("Dra. Psicóloga")
                .licenceNumber("LIC-004")
                .email("psi@hospital.com")
                .active(true)
                .specialty(psicologia)
                .build());
    }

    // -----------------------------------------------------------------------
    // Query Method: findDoctorBySpecialtyAndActiveTrue
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findDoctorBySpecialtyAndActiveTrue → solo devuelve activos de esa especialidad")
    void findBySpecialtyAndActiveTrue_shouldReturnOnlyActiveFromThatSpecialty() {
        List<Doctor> result = doctorRepository.findDoctorBySpecialtyAndActiveTrue(cardiologia);

        // Deben aparecer solo los 2 activos de Cardiología
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Doctor::getLicenceNumber)
                .containsExactlyInAnyOrder("LIC-001", "LIC-002");
    }

    @Test
    @DisplayName("findDoctorBySpecialtyAndActiveTrue → excluye doctores inactivos")
    void findBySpecialtyAndActiveTrue_shouldExcludeInactiveDoctors() {
        List<Doctor> result = doctorRepository.findDoctorBySpecialtyAndActiveTrue(cardiologia);

        // LIC-003 está inactive, no debe aparecer
        assertThat(result).extracting(Doctor::getLicenceNumber)
                .doesNotContain("LIC-003");
    }

    @Test
    @DisplayName("findDoctorBySpecialtyAndActiveTrue → filtra por especialidad correctamente")
    void findBySpecialtyAndActiveTrue_shouldFilterBySpecialty() {
        List<Doctor> resultPsi = doctorRepository.findDoctorBySpecialtyAndActiveTrue(psicologia);

        // Solo debe aparecer el doctor de Psicología
        assertThat(resultPsi).hasSize(1);
        assertThat(resultPsi.get(0).getLicenceNumber()).isEqualTo("LIC-004");
    }

    @Test
    @DisplayName("findDoctorBySpecialtyAndActiveTrue → lista vacía cuando no hay doctores activos")
    void findBySpecialtyAndActiveTrue_whenNoneActive_shouldReturnEmptyList() {
        // Especialidad nueva sin doctores
        Specialty sinDoctores = specialtyRepository.save(
                Specialty.builder().name("Nutrición").build()
        );

        List<Doctor> result = doctorRepository.findDoctorBySpecialtyAndActiveTrue(sinDoctores);

        assertThat(result).isEmpty();
    }
}
