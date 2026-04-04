package unimag.plataformamedicos.domine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import unimag.plataformamedicos.domine.entities.Patient;

import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID>{

}
