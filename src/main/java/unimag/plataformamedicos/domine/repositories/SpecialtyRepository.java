package unimag.plataformamedicos.domine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import unimag.plataformamedicos.domine.entities.Specialty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    boolean existsSpecialtyByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    Optional<Specialty> findByNameIgnoreCase(String name);

    @Query("select s from Specialty s where s.active = true or s.active is null")
    List<Specialty> findAllActive();
}
