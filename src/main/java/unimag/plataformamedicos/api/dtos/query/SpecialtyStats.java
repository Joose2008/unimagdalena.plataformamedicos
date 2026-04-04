package unimag.plataformamedicos.api.dtos.query;

import unimag.plataformamedicos.domine.entities.Specialty;

public record SpecialtyStats(
        Specialty specialty,
        Long cancelled,
        Long noShow
) { }
