package unimag.plataformamedicos.domine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unimag.plataformamedicos.domine.entities.AppointmentType;

import java.util.UUID;

@Repository
public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, UUID> {

}