package unimag.plataformamedicos.domine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.DoctorSchedule;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {

    List<DoctorSchedule> findDoctorScheduleByDoctorAndDayOfWeek(Doctor doctor, DayOfWeek dayOfWeek);

    List<DoctorSchedule> findByDoctor(Doctor doctor);
}
