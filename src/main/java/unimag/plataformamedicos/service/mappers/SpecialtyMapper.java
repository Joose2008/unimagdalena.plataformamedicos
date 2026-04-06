package unimag.plataformamedicos.service.mappers;

import unimag.plataformamedicos.api.dtos.SpecialtyDtos;
import unimag.plataformamedicos.domine.entities.Specialty;

public class SpecialtyMapper {

    public static SpecialtyDtos.SpecialtyResponse toResponse(Specialty specialty) {
        return new SpecialtyDtos.SpecialtyResponse(
                specialty.getId(),
                specialty.getName(),
                specialty.getDescription()
        );
    }

    public static Specialty toEntity(SpecialtyDtos.CreateSpecialtyRequest request) {
        return Specialty.builder()
                .name(request.name())
                .description(request.description())
                .build();
    }

    public static void patch(Specialty specialty, SpecialtyDtos.UpdateSpecialtyRequest request) {
        if (request.name() != null) {
            specialty.setName(request.name());
        }
        if (request.description() != null) {
            specialty.setDescription(request.description());
        }
    }
}
