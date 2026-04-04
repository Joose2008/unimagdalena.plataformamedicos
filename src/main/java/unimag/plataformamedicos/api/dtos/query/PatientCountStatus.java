package unimag.plataformamedicos.api.dtos.query;

import unimag.plataformamedicos.domine.entities.Patient;

public record PatientCountStatus(
        Patient patient,
        long countNoShow
) {

}
