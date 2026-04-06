package unimag.plataformamedicos.service.mappers;

import unimag.plataformamedicos.api.dtos.PatientDtos;
import unimag.plataformamedicos.domine.entities.Patient;
import unimag.plataformamedicos.enums.PatientStatus;

public class PatientMapper {

    public static PatientDtos.PatientResponse toResponse(Patient patient) {
        return new PatientDtos.PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getDocumentNumber(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getStatus()
        );
    }

    public static PatientDtos.PatientSummaryResponse toSummaryResponse(Patient patient) {
        return new PatientDtos.PatientSummaryResponse(
                patient.getId(),
                patient.getName(),
                patient.getDocumentNumber()
        );
    }

    public static Patient toEntity(PatientDtos.CreatePatientRequest request) {
        return Patient.builder()
                .name(request.name())
                .documentNumber(request.documentNumber())
                .email(request.email())
                .phone(request.phone())
                .build();
    }

    public static void patch(Patient patient, PatientDtos.UpdatePatientRequest request) {
        if (request.name() != null) {
            patient.setName(request.name());
        }
        if (request.email() != null) {
            patient.setEmail(request.email());
        }
        if (request.phone() != null) {
            patient.setPhone(request.phone());
        }
    }
}
