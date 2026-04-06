package unimag.plataformamedicos.api.dtos;

import java.io.Serializable;
import java.util.UUID;

public class SpecialtyDtos {

    public record CreateSpecialtyRequest(
            String name,
            String description
    ) implements Serializable  {}

    public record UpdateSpecialtyRequest(
            String name,
            String description
    ) implements Serializable {}

    public record SpecialtyResponse(
            UUID id,
            String name,
            String description
    ) implements Serializable {}
}
