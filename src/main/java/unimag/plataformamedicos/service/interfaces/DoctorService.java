package unimag.plataformamedicos.service.interfaces;


import unimag.plataformamedicos.api.dtos.DoctorDtos;
import unimag.plataformamedicos.api.dtos.DoctorDtos.CreateDoctorRequest;
import unimag.plataformamedicos.api.dtos.DoctorDtos.DoctorResponse;
import unimag.plataformamedicos.api.dtos.DoctorDtos.UpdateDoctorRequest;

import java.util.List;
import java.util.UUID;

public interface DoctorService {
    DoctorResponse create(CreateDoctorRequest request);
    DoctorResponse findById(UUID id);
    List<DoctorDtos.DoctorResponse> findAll();
    DoctorResponse update(UUID id, UpdateDoctorRequest request);
    DoctorResponse update(UUID id,DoctorDtos.UpdateDoctorLicenceRequest request);
}
