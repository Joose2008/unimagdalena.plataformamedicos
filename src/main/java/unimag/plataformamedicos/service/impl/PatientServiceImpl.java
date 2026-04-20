package unimag.plataformamedicos.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.PatientDtos;
import unimag.plataformamedicos.domine.entities.Patient;
import unimag.plataformamedicos.domine.repositories.PatientRepository;
import unimag.plataformamedicos.exception.ResourceNotFoundException;
import unimag.plataformamedicos.service.interfaces.PatientService;
import unimag.plataformamedicos.service.mappers.PatientMapper;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public PatientDtos.PatientResponse create(PatientDtos.CreatePatientRequest request) {
        var patientEntity = PatientMapper.toEntity(request);
        var patientSave = patientRepository.save(patientEntity);
        return PatientMapper.toResponse(patientSave);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDtos.PatientResponse findById(UUID id) {
        return patientRepository.findById(id).map(PatientMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Patient %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDtos.PatientResponse> findAll() {
        return patientRepository.findAll().stream().map(PatientMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public PatientDtos.PatientResponse update(UUID id, PatientDtos.UpdatePatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient %d not found".formatted(id)));
        PatientMapper.patch(patient,request);
        patientRepository.save(patient);
        return PatientMapper.toResponse(patient);
    }

    @Override
    @Transactional
    public PatientDtos.PatientResponse updateDocument(UUID id, PatientDtos.UpdatePatientDocumentRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient %d not found".formatted(id)));
        patient.setDocumentNumber(request.documentNumber());
        return PatientMapper.toResponse(patient);
    }
}
