package unimag.plataformamedicos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.SpecialtyDtos;
import unimag.plataformamedicos.domine.entities.Specialty;
import unimag.plataformamedicos.domine.repositories.SpecialtyRepository;
import unimag.plataformamedicos.exception.ResourceNoFoundException;
import unimag.plataformamedicos.service.interfaces.SpecialtyService;
import unimag.plataformamedicos.service.mappers.SpecialtyMapper;

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
            Specialty specialtySaved = specialtyRepository.save(SpecialtyMapper.toEntity(request));
            return SpecialtyMapper.toResponse(specialtySaved);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialtyDtos.SpecialtyResponse findById(UUID id) {
        return specialtyRepository.findById(id).map(SpecialtyMapper::toResponse)
                .orElseThrow(() -> new ResourceNoFoundException("Member %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyDtos.SpecialtyResponse> findAll() {
        return specialtyRepository.findAll().stream().map(SpecialtyMapper::toResponse).toList();
    }
}
