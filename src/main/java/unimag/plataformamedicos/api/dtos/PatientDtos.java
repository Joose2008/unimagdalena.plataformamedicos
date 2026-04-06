package unimag.plataformamedicos.api.dtos;

import unimag.plataformamedicos.enums.PatientStatus;

import java.util.UUID;

public class PatientDtos {

    public record CreatePatientRequest(
            String name,
            String documentNumber,
            String email,
            String phone
    ) {}

    public record UpdatePatientRequest(
            String name,
            String email,
            String phone
    ) {}

    public record UpdatePatientDocumentRequest(
            String documentNumber
    ){}

    public record PatientResponse(
            UUID id,
            String name,
            String documentNumber,
            String email,
            String phone,
            PatientStatus status
    ) {}

    public record PatientSummaryResponse (
            UUID id,
            String name,
            String documentNumber
    ){}
}
