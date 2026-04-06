package unimag.plataformamedicos.api.dtos;

import unimag.plataformamedicos.enums.OfficeStatus;

import java.io.Serializable;
import java.util.UUID;

public class OfficeDtos {

    public record CreateOfficeRequest(
            String name,
            String location
    ) implements Serializable  {}

    public record UpdateOfficeRequest(
            String name,
            String location,
            OfficeStatus status
    ) implements Serializable {}

    public record OfficeResponse(
            UUID id,
            String name,
            String location,
            OfficeStatus status
    ) implements Serializable {}
}
