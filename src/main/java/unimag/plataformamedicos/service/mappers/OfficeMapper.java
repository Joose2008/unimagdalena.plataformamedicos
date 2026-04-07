package unimag.plataformamedicos.service.mappers;

import unimag.plataformamedicos.api.dtos.OfficeDtos;
import unimag.plataformamedicos.domine.entities.Office;
import unimag.plataformamedicos.enums.OfficeStatus;

public class OfficeMapper {

    public static OfficeDtos.OfficeResponse toResponse(Office office) {
        return new OfficeDtos.OfficeResponse(
                office.getId(),
                office.getName(),
                office.getLocation(),
                office.getStatus()
        );
    }

    public static Office toEntity(OfficeDtos.CreateOfficeRequest request) {
        return Office.builder()
                .name(request.name())
                .location(request.location())
                .status(OfficeStatus.AVAILABLE)
                .build();
    }

    public static void patch(Office office, OfficeDtos.UpdateOfficeRequest request) {
        if (request.name() != null) {
            office.setName(request.name());
        }
        if (request.location() != null) {
            office.setLocation(request.location());
        }
        if (request.status() != null) {
            office.setStatus(request.status());
        }
    }
}
