package unimag.plataformamedicos.domine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unimag.plataformamedicos.domine.entities.Patient;

import java.util.List;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID>{

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id);

    @Query("""
select p from Patient p
where p.status = 'ACTIVE'
and (
    lower(p.name) like lower(concat('%', :query, '%'))
    or lower(p.documentNumber) like lower(concat('%', :query, '%'))
)
order by p.name asc
""")
    List<Patient> searchActiveByNameOrDocument(@Param("query") String query);
}
