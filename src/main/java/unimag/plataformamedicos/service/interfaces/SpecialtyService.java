package unimag.plataformamedicos.service.interfaces;

import unimag.plataformamedicos.api.dtos.SpecialtyDtos.CreateSpecialtyRequest;
import unimag.plataformamedicos.api.dtos.SpecialtyDtos.SpecialtyResponse;
import unimag.plataformamedicos.api.dtos.SpecialtyDtos.UpdateSpecialtyRequest;

import java.util.List;
import java.util.UUID;

public interface SpecialtyService {
    SpecialtyResponse create(CreateSpecialtyRequest request);
    SpecialtyResponse findById(UUID id);
    List<SpecialtyResponse> findAll();
    SpecialtyResponse update(UUID id, UpdateSpecialtyRequest request);
    void deactivate(UUID id);
}
