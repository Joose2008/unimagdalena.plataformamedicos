package unimag.plataformamedicos.domine.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.plataformamedicos.domine.entities.Office;
import unimag.plataformamedicos.enums.OfficeStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para OfficeRepository.
 *
 * NOTA: En el proyecto actual OfficeRepository está vacío (solo @Repository),
 * lo que significa que hereda únicamente los métodos de JpaRepository.
 * Estos tests cubren esas operaciones básicas + el enum OfficeStatus.
 *
 * Cuando agregues queries personalizados a OfficeRepository,
 * añade los tests correspondientes aquí.
 */
@DisplayName("OfficeRepository - Integration Tests")
class OfficeRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OfficeRepository officeRepository;

    private Office availableOffice;
    private Office inactiveOffice;

    @BeforeEach
    void setUp() {
        officeRepository.deleteAll();

        availableOffice = officeRepository.save(Office.builder()
                .name("Consultorio A")
                .location("Piso 1")
                .status(OfficeStatus.AVAILABLE)
                .build());

        inactiveOffice = officeRepository.save(Office.builder()
                .name("Consultorio B")
                .location("Piso 2")
                .status(OfficeStatus.INACTIVE)
                .build());
    }

    @Test
    @DisplayName("Guardar consultorio → persiste con ID y estado por defecto AVAILABLE")
    void save_shouldPersistOfficeWithGeneratedId() {
        Office nuevo = officeRepository.save(Office.builder()
                .name("Consultorio C")
                .location("Piso 3")
                .build()); // status usa @Builder.Default = AVAILABLE

        assertThat(nuevo.getId()).isNotNull();
        assertThat(nuevo.getStatus()).isEqualTo(OfficeStatus.AVAILABLE);
    }

    @Test
    @DisplayName("findById existente → devuelve el consultorio correcto")
    void findById_whenExists_shouldReturnOffice() {
        Optional<Office> result = officeRepository.findById(availableOffice.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Consultorio A");
        assertThat(result.get().getStatus()).isEqualTo(OfficeStatus.AVAILABLE);
    }

    @Test
    @DisplayName("findById inexistente → Optional vacío")
    void findById_whenNotExists_shouldReturnEmpty() {
        Optional<Office> result = officeRepository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll → devuelve todos los consultorios")
    void findAll_shouldReturnAllOffices() {
        List<Office> offices = officeRepository.findAll();

        assertThat(offices).hasSize(2);
        assertThat(offices).extracting(Office::getName)
                .containsExactlyInAnyOrder("Consultorio A", "Consultorio B");
    }

    @Test
    @DisplayName("Actualizar status → el cambio persiste en la BD")
    void update_shouldPersistStatusChange() {
        // Desactivamos el consultorio disponible
        availableOffice.setStatus(OfficeStatus.INACTIVE);
        officeRepository.save(availableOffice);

        Office actualizado = officeRepository.findById(availableOffice.getId()).orElseThrow();
        assertThat(actualizado.getStatus()).isEqualTo(OfficeStatus.INACTIVE);
    }

    @Test
    @DisplayName("Eliminar consultorio → no aparece más en la BD")
    void delete_shouldRemoveOffice() {
        officeRepository.delete(inactiveOffice);

        assertThat(officeRepository.count()).isEqualTo(1);
        assertThat(officeRepository.findById(inactiveOffice.getId())).isEmpty();
    }
}
