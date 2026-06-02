package unimag.plataformamedicos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.SpecialtyDtos;
import unimag.plataformamedicos.domine.entities.Specialty;
import unimag.plataformamedicos.domine.repositories.SpecialtyRepository;
import unimag.plataformamedicos.exception.ConflictException;
import unimag.plataformamedicos.exception.ResourceNotFoundException;
import unimag.plataformamedicos.service.interfaces.SpecialtyService;
import unimag.plataformamedicos.service.mappers.SpecialtyMapper;

import jakarta.validation.ValidationException;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    @Override
    @Transactional
    public SpecialtyDtos.SpecialtyResponse create(SpecialtyDtos.CreateSpecialtyRequest request) {
        validateName(request.name());
        String name = request.name().trim();
        var existingSpecialty = specialtyRepository.findByNameIgnoreCase(name);
        if (existingSpecialty.isPresent()) {
            Specialty specialty = existingSpecialty.get();
            if (!Boolean.FALSE.equals(specialty.getActive())) {
                throw new ConflictException("Ya existe una especialidad con ese nombre.");
            }

            specialty.setActive(true);
            specialty.setName(name);
            specialty.setDescription(request.description());
            return SpecialtyMapper.toResponse(specialtyRepository.save(specialty));
        }

        Specialty specialty = SpecialtyMapper.toEntity(request);
        specialty.setName(name);
        Specialty specialtySaved = specialtyRepository.save(specialty);
        return SpecialtyMapper.toResponse(specialtySaved);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialtyDtos.SpecialtyResponse findById(UUID id) {
        return specialtyRepository.findById(id).map(SpecialtyMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Member %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyDtos.SpecialtyResponse> findAll() {
        return specialtyRepository.findAllActive().stream().map(SpecialtyMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public SpecialtyDtos.SpecialtyResponse update(UUID id, SpecialtyDtos.UpdateSpecialtyRequest request) {
        validateName(request.name());
        if (specialtyRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("Ya existe una especialidad con ese nombre.");
        }

        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty %s not found".formatted(id)));

        SpecialtyMapper.patch(specialty, request);
        specialty.setName(request.name().trim());
        return SpecialtyMapper.toResponse(specialtyRepository.save(specialty));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty %s not found".formatted(id)));

        specialty.setActive(false);
        specialtyRepository.save(specialty);
    }

    private void validateName(String name) {
        if (name == null || name.trim().isBlank()) {
            throw new ValidationException("El nombre de la especialidad no puede estar vacio.");
        }
    }
}
