package unimag.plataformamedicos.api.dtos;

import java.io.Serializable;
import java.util.UUID;

public class DoctorDtos {

    public record CreateDoctorRequest(
            String name,
            String licenceNumber,
            String email,
            UUID specialtyId
    ) implements Serializable {}

    public record UpdateDoctorRequest(
            String name,
            String email,
            Boolean active,
            UUID specialtyId
    ) implements Serializable {}

    public record UpdateDoctorLicenceRequest(
            String licenceNumber
    ) implements Serializable {}

    public record DoctorResponse(
            UUID id,
            String name,
            String licenceNumber,
            String email,
            Boolean active,
            SpecialtyDtos.SpecialtyResponse specialty
    ) implements Serializable {}

    public record DoctorSummaryResponse(
            UUID id,
            String name,
            String nameSpecialty
    ) implements Serializable {}
}
