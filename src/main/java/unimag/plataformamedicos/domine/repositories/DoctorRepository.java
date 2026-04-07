package unimag.plataformamedicos.domine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.Specialty;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    List<Doctor> findDoctorBySpecialtyAndActiveTrue(Specialty specialty);
}
