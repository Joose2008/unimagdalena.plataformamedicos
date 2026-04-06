package unimag.plataformamedicos.api.dtos;

import java.util.UUID;

public class DoctorDtos {

    public record CreateDoctorRequest(
            String name,
            String licenceNumber,
            String email
    ) {}

    public record UpdateDoctorRequest(
            String name,
            String email,
            Boolean active
    ) {}

    public record UpdateDoctorLicenceRequest(
            String licenceNumber
    ) {}

    public record DoctorResponse(
            UUID id,
            String name,
            String licenceNumber,
            String email,
            Boolean active,
            SpecialtyDtos.SpecialtyResponse specialty
    ) {}

    public record DoctorSummaryResponse(
            UUID id,
            String name,
            String nameSpecialty
    ){}
}
