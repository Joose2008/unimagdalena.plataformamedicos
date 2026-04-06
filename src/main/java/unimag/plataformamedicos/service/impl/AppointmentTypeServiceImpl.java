package unimag.plataformamedicos.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.AppointmentTypeDtos;
import unimag.plataformamedicos.domine.entities.AppointmentType;
import unimag.plataformamedicos.domine.repositories.AppointmentTypeRepository;
import unimag.plataformamedicos.exception.ResourceNoFoundException;
import unimag.plataformamedicos.service.interfaces.AppointmentTypeService;
import unimag.plataformamedicos.service.mappers.AppointmentTypeMapper;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AppointmentTypeServiceImpl implements AppointmentTypeService {

    private final AppointmentTypeRepository appointmentTypeRepository;

    @Override
    @Transactional
    public AppointmentTypeDtos.AppointmentTypeResponse create(AppointmentTypeDtos.CreateAppointmentTypeRequest request) {
        AppointmentType appointmentTypeEntity = AppointmentTypeMapper.toEntity(request);
        AppointmentType appointmentTypeSave = appointmentTypeRepository.save(appointmentTypeEntity);
        return AppointmentTypeMapper.toResponse(appointmentTypeSave);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentTypeDtos.AppointmentTypeResponse findById(UUID id) {
        return appointmentTypeRepository.findById(id).map(AppointmentTypeMapper::toResponse)
                .orElseThrow(() -> new ResourceNoFoundException("AppointmentType %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentTypeDtos.AppointmentTypeResponse> findAll() {
        return appointmentTypeRepository.findAll().stream().map(AppointmentTypeMapper::toResponse).toList();
    }
}
