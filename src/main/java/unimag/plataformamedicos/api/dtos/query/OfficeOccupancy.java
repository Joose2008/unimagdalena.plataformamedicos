package unimag.plataformamedicos.api.dtos.query;

import unimag.plataformamedicos.domine.entities.Office;

public record OfficeOccupancy(
        Office office,
        long sumOccupiedMinutes
) {
}
