package unimag.plataformamedicos.domine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unimag.plataformamedicos.api.dtos.query.DoctorAppointment;
import unimag.plataformamedicos.api.dtos.query.OfficeOccupancy;
import unimag.plataformamedicos.api.dtos.query.PatientCountStatus;
import unimag.plataformamedicos.api.dtos.query.SpecialtyStats;
import unimag.plataformamedicos.domine.entities.Appointment;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.Office;
import unimag.plataformamedicos.domine.entities.Patient;
import unimag.plataformamedicos.enums.AppointmentStatus;
import unimag.plataformamedicos.enums.PatientStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface    AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findAppointmentByPatientAndStatus(Patient patient, AppointmentStatus appointmentStatus);

    List<Appointment> findAppointmentByStartAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
        select count(a) > 0
        from Appointment a
        where a.doctor = :doctor
        And a.status NOT IN ('CANCELLED', 'COMPLETED', 'NO_SHOW')
        And a.startAt < :endAt
        And a.endAt > :startAt
""")
    boolean existsOverLapForDoctor(
            @Param("doctor") Doctor doctor,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Query("""
        select count(a) > 0
        from Appointment a
        where a.office = :office
        And a.status NOT IN ('CANCELLED', 'COMPLETED', 'NO_SHOW')
        And a.startAt < :endAt
        And a.endAt > :startAt
""")
    boolean existsOverLapForOffice(
            @Param("office") Office office,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Query("""
        select count(a) > 0
        from Appointment a
        where a.patient = :patient
        And a.status NOT IN ('CANCELLED', 'COMPLETED', 'NO_SHOW')
        And a.startAt < :endAt
        And a.endAt > :startAt
""")
    boolean existsOverLapForPatient(
            @Param("patient") Patient patient,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );


    @Query("""
select a from Appointment a
where a.doctor = :doctor
and a.startAt >= :startOfDay
and a.startAt < :endOfDay
order by a.startAt ASC
""")
    List<Appointment> findByDoctorAndDate(
            @Param("doctor") Doctor doctor,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
select new unimag.plataformamedicos.api.dtos.query.OfficeOccupancy(
a.office,
SUM(a.appointmentType.durationMinutes)
)
from Appointment a
where a.startAt between :start And :end
And a.status NOT IN ('CANCELLED')
group by a.office
""")
    List<OfficeOccupancy> sumOccupiedMinutesByOffice(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
Select new unimag.plataformamedicos.api.dtos.query.SpecialtyStats(
    a.doctor.specialty,
    SUM(case when a.status = 'CANCELLED' then 1 else 0 end),
    SUM(case when a.status = 'NO_SHOW' then 1 else 0 end)
    )
from Appointment a
where a.status IN ('CANCELLED','NO_SHOW')
group by a.doctor.specialty
""")
    List<SpecialtyStats> countCancelledAndNoShowAppointmentsBySpecialty();

    @Query("""
select new unimag.plataformamedicos.api.dtos.query.DoctorAppointment (
    a.doctor,
    count(a)
    )
from Appointment a
where a.status = 'COMPLETED'
group by a.doctor
order by count(a) DESC
""")
    List<DoctorAppointment> rankDoctorByAppointment();

    @Query("""
select new unimag.plataformamedicos.api.dtos.query.PatientCountStatus(
a.patient,
count(a)
)
from Appointment a
where a.status = 'NO_SHOW'
and a.startAt between :start and :end
group by a.patient
order by count(a) DESC
""")
    List<PatientCountStatus> rankPatientByStatusNoShow(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
