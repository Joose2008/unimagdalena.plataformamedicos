package unimag.plataformamedicos.api.dtos;

import unimag.plataformamedicos.enums.PatientStatus;

import java.io.Serializable;
import java.util.UUID;

public class PatientDtos {

    public record CreatePatientRequest(
            String name,
            String documentNumber,
            String email,
            String phone
    ) implements Serializable  {}

    public record UpdatePatientRequest(
            String name,
            String email,
            String phone,
            PatientStatus patientStatus
    ) implements Serializable {}

    public record UpdatePatientDocumentRequest(
            String documentNumber
    ) implements Serializable {}

    public record PatientResponse(
            UUID id,
            String name,
            String documentNumber,
            String email,
            String phone,
            PatientStatus status
    ) implements Serializable {}

    public record PatientSummaryResponse (
            UUID id,
            String name,
            String documentNumber
    )implements Serializable {}
}
