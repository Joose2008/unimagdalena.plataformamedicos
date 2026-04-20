package unimag.plataformamedicos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.DoctorDtos;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.Specialty;
import unimag.plataformamedicos.domine.repositories.DoctorRepository;
import unimag.plataformamedicos.domine.repositories.SpecialtyRepository;
import unimag.plataformamedicos.exception.ResourceNotFoundException;
import unimag.plataformamedicos.service.interfaces.DoctorService;
import unimag.plataformamedicos.service.mappers.DoctorMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;

    @Override
    @Transactional
    public DoctorDtos.DoctorResponse create(DoctorDtos.CreateDoctorRequest request) {
        var specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty with id %d".formatted(request.specialtyId())));

        var doctorEntity = DoctorMapper.toEntity(request);
        doctorEntity.setSpecialty(specialty);

        var doctorSave = doctorRepository.save(doctorEntity);
        return DoctorMapper.toResponse(doctorSave);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDtos.DoctorResponse findById(UUID id) {
        return doctorRepository.findById(id).map(DoctorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDtos.DoctorResponse> findAll() {
        return doctorRepository.findAll().stream().map(DoctorMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public DoctorDtos.DoctorResponse update(UUID id, DoctorDtos.UpdateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor %d not found".formatted(id)));

        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty %d not found".formatted(request.specialtyId())));

        doctor.setSpecialty(specialty);
        DoctorMapper.patch(doctor, request);
        doctorRepository.save(doctor);
        return DoctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional
    public DoctorDtos.DoctorResponse update(UUID id, DoctorDtos.UpdateDoctorLicenceRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor %d not found".formatted(id)));

        doctor.setLicenceNumber(request.licenceNumber());
        doctorRepository.save(doctor);
        return DoctorMapper.toResponse(doctor);
    }
}
