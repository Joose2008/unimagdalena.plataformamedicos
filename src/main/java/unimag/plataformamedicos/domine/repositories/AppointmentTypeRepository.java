package unimag.plataformamedicos.domine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import unimag.plataformamedicos.domine.entities.AppointmentType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    Optional<AppointmentType> findByNameIgnoreCase(String name);

    @Query("select a from AppointmentType a where a.active = true or a.active is null")
    List<AppointmentType> findAllActive();
}
