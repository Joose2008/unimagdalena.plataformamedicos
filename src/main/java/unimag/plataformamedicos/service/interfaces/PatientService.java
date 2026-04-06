package unimag.plataformamedicos.service.interfaces;

import unimag.plataformamedicos.api.dtos.PatientDtos.CreatePatientRequest;
import unimag.plataformamedicos.api.dtos.PatientDtos.PatientResponse;
import unimag.plataformamedicos.api.dtos.PatientDtos.UpdatePatientDocumentRequest;
import unimag.plataformamedicos.api.dtos.PatientDtos.UpdatePatientRequest;

import java.util.List;
import java.util.UUID;

public interface PatientService {
    PatientResponse create(CreatePatientRequest request);
    PatientResponse findById(UUID id);
    List<PatientResponse> findAll();
    PatientResponse update(UUID id, UpdatePatientRequest request);
    PatientResponse updateDocument(UUID id, UpdatePatientDocumentRequest request);
}
