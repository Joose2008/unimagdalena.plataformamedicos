package unimag.plataformamedicos.domine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.Specialty;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    List<Doctor> findDoctorBySpecialtyAndActiveTrue(Specialty specialty);

    @Query("""
select d from Doctor d
where d.active = true
and (
    lower(d.name) like lower(concat('%', :query, '%'))
    or lower(d.licenceNumber) like lower(concat('%', :query, '%'))
    or lower(d.email) like lower(concat('%', :query, '%'))
)
order by d.name asc
""")
    List<Doctor> searchActiveByNameLicenceOrEmail(@Param("query") String query);
}
