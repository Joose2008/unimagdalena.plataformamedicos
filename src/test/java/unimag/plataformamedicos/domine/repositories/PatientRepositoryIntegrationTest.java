package unimag.plataformamedicos.domine.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.plataformamedicos.domine.entities.Patient;
import unimag.plataformamedicos.enums.PatientStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para PatientRepository.
 *
 * Hereda de AbstractIntegrationTest para obtener el contenedor PostgreSQL
 * y la configuración de @DataJpaTest automáticamente.
 *
 * Usamos AssertJ (assertThat) en lugar de JUnit assertions porque
 * produce mensajes de error más descriptivos al fallar.
 */
@DisplayName("PatientRepository - Integration Tests")
class PatientRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;

    // Pacientes de prueba reutilizados en varios tests
    private Patient activePatient;
    private Patient inactivePatient;

    /**
     * @BeforeEach se ejecuta ANTES de cada test.
     * Limpiamos la tabla y creamos datos frescos para garantizar
     * que los tests sean independientes entre sí.
     */
    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();

        activePatient = patientRepository.save(
                Patient.builder()
                        .name("Carlos Pérez")
                        .documentNumber("1000123456")
                        .email("carlos@test.com")
                        .phone("3001234567")
                        .status(PatientStatus.ACTIVE)
                        .build()
        );

        inactivePatient = patientRepository.save(
                Patient.builder()
                        .name("Ana Gómez")
                        .documentNumber("1000987654")
                        .email("ana@test.com")
                        .phone("3009876543")
                        .status(PatientStatus.INACTIVE)
                        .build()
        );
    }

    // -----------------------------------------------------------------------
    // Tests de operaciones CRUD básicas (heredadas de JpaRepository)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Guardar paciente → persiste con ID generado")
    void save_shouldPersistPatientWithGeneratedId() {
        Patient nuevo = patientRepository.save(
                Patient.builder()
                        .name("Luis Torres")
                        .documentNumber("2000000001")
                        .email("luis@test.com")
                        .status(PatientStatus.ACTIVE)
                        .build()
        );

        // El id lo genera la BD, no debe ser null
        assertThat(nuevo.getId()).isNotNull();
        // La BD debe contener ahora 3 registros (2 del setUp + 1 nuevo)
        assertThat(patientRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Buscar por ID existente → devuelve el paciente")
    void findById_whenExists_shouldReturnPatient() {
        Optional<Patient> result = patientRepository.findById(activePatient.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Carlos Pérez");
        assertThat(result.get().getStatus()).isEqualTo(PatientStatus.ACTIVE);
    }

    @Test
    @DisplayName("Buscar por ID inexistente → devuelve Optional vacío")
    void findById_whenNotExists_shouldReturnEmpty() {
        Optional<Patient> result = patientRepository.findById(UUID.randomUUID());

        // Optional.empty() cuando el registro no existe
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll → devuelve todos los pacientes guardados")
    void findAll_shouldReturnAllPatients() {
        List<Patient> all = patientRepository.findAll();

        assertThat(all).hasSize(2);
        // Verificamos que están los dos documentos que insertamos
        assertThat(all).extracting(Patient::getDocumentNumber)
                .containsExactlyInAnyOrder("1000123456", "1000987654");
    }

    @Test
    @DisplayName("Eliminar paciente → ya no aparece en la BD")
    void delete_shouldRemovePatient() {
        patientRepository.delete(activePatient);

        assertThat(patientRepository.count()).isEqualTo(1);
        assertThat(patientRepository.findById(activePatient.getId())).isEmpty();
    }

    @Test
    @DisplayName("Actualizar estado → el cambio se refleja en la BD")
    void update_shouldPersistStatusChange() {
        // Cambiamos ACTIVE → INACTIVE
        activePatient.setStatus(PatientStatus.INACTIVE);
        patientRepository.save(activePatient);

        Patient actualizado = patientRepository.findById(activePatient.getId()).orElseThrow();
        assertThat(actualizado.getStatus()).isEqualTo(PatientStatus.INACTIVE);
    }
}
