package unimag.plataformamedicos.api.dtos;

import unimag.plataformamedicos.enums.OfficeStatus;

import java.util.UUID;

public class OfficeDtos {

    public record CreateOfficeRequest(
            String name,
            String location
    ) {}

    public record UpdateOfficeRequest(
            String name,
            String location,
            OfficeStatus status
    ) {}

    public record OfficeResponse(
            UUID id,
            String name,
            String location,
            OfficeStatus status
    ) {}
}
