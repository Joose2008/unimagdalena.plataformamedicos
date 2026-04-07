package unimag.plataformamedicos.api.dtos.query;

import unimag.plataformamedicos.domine.entities.Doctor;

public record DoctorAppointment(
        Doctor doctor,
        Long countCompletedAppointment
) {
}
