package unimag.plataformamedicos.service.interfaces;


import unimag.plataformamedicos.api.dtos.OfficeDtos.CreateOfficeRequest;
import unimag.plataformamedicos.api.dtos.OfficeDtos.OfficeResponse;
import unimag.plataformamedicos.api.dtos.OfficeDtos.UpdateOfficeRequest;

import java.util.List;
import java.util.UUID;

public interface OfficeService {
    OfficeResponse create(CreateOfficeRequest request);
    OfficeResponse findById(UUID id);
    List<OfficeResponse> findAll();
    OfficeResponse update(UUID id, UpdateOfficeRequest request);
}
