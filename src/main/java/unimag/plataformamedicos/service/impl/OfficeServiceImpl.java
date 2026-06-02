package unimag.plataformamedicos.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.OfficeDtos;
import unimag.plataformamedicos.domine.entities.Office;
import unimag.plataformamedicos.domine.repositories.OfficeRepository;
import unimag.plataformamedicos.exception.ResourceNotFoundException;
import unimag.plataformamedicos.service.interfaces.OfficeService;
import unimag.plataformamedicos.service.mappers.OfficeMapper;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class OfficeServiceImpl implements OfficeService {

    private final OfficeRepository officeRepository;

    @Override
    @Transactional
    public OfficeDtos.OfficeResponse create(OfficeDtos.CreateOfficeRequest request) {
        var officeEntity = OfficeMapper.toEntity(request);
        var officeSave = officeRepository.save(officeEntity);
        return OfficeMapper.toResponse(officeSave);
    }

    @Override
    public OfficeDtos.OfficeResponse findById(UUID id) {
        return officeRepository.findById(id).map(OfficeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Office %d not found".formatted(id)));
    }

    @Override
    public List<OfficeDtos.OfficeResponse> findAll() {
        return officeRepository.findAll().stream().map(OfficeMapper::toResponse).toList();
    }

    @Override
    public OfficeDtos.OfficeResponse update(UUID id, OfficeDtos.UpdateOfficeRequest request) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office %d not found".formatted(id)));
        OfficeMapper.patch(office,request);
        officeRepository.save(office);
        return OfficeMapper.toResponse(office);
    }
}
