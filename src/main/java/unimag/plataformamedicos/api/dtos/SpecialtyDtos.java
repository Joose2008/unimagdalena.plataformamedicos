package unimag.plataformamedicos.api.dtos;

import java.util.UUID;

public class SpecialtyDtos {

    public record CreateSpecialtyRequest(
            String name,
            String description
    ) {}

    public record UpdateSpecialtyRequest(
            String name,
            String description
    ){}

    public record SpecialtyResponse(
            UUID id,
            String name,
            String description
    ) {}
}
