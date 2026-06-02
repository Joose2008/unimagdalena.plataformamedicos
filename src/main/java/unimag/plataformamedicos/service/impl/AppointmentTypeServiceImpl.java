package unimag.plataformamedicos.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.AppointmentTypeDtos;
import unimag.plataformamedicos.domine.entities.AppointmentType;
import unimag.plataformamedicos.domine.repositories.AppointmentTypeRepository;
import unimag.plataformamedicos.exception.ConflictException;
import unimag.plataformamedicos.exception.ResourceNotFoundException;
import unimag.plataformamedicos.service.interfaces.AppointmentTypeService;
import unimag.plataformamedicos.service.mappers.AppointmentTypeMapper;

import jakarta.validation.ValidationException;
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
        validateName(request.name());
        String name = request.name().trim();
        var existingAppointmentType = appointmentTypeRepository.findByNameIgnoreCase(name);
        if (existingAppointmentType.isPresent()) {
            AppointmentType appointmentType = existingAppointmentType.get();
            if (!Boolean.FALSE.equals(appointmentType.getActive())) {
                throw new ConflictException("Ya existe un tipo de cita con ese nombre.");
            }

            appointmentType.setActive(true);
            appointmentType.setName(name);
            appointmentType.setDescription(request.description());
            appointmentType.setDurationMinutes(request.durationMinutes());
            return AppointmentTypeMapper.toResponse(appointmentTypeRepository.save(appointmentType));
        }

        AppointmentType appointmentTypeEntity = AppointmentTypeMapper.toEntity(request);
        appointmentTypeEntity.setName(name);
        AppointmentType appointmentTypeSave = appointmentTypeRepository.save(appointmentTypeEntity);
        return AppointmentTypeMapper.toResponse(appointmentTypeSave);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentTypeDtos.AppointmentTypeResponse findById(UUID id) {
        return appointmentTypeRepository.findById(id).map(AppointmentTypeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("AppointmentType %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentTypeDtos.AppointmentTypeResponse> findAll() {
        return appointmentTypeRepository.findAllActive().stream().map(AppointmentTypeMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public AppointmentTypeDtos.AppointmentTypeResponse update(UUID id, AppointmentTypeDtos.UpdateAppointmentTypeRequest request) {
        validateName(request.name());
        if (appointmentTypeRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("Ya existe un tipo de cita con ese nombre.");
        }

        AppointmentType appointmentType = appointmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppointmentType %s not found".formatted(id)));

        AppointmentTypeMapper.patch(appointmentType, request);
        appointmentType.setName(request.name().trim());
        return AppointmentTypeMapper.toResponse(appointmentTypeRepository.save(appointmentType));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        AppointmentType appointmentType = appointmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppointmentType %s not found".formatted(id)));

        appointmentType.setActive(false);
        appointmentTypeRepository.save(appointmentType);
    }

    private void validateName(String name) {
        if (name == null || name.trim().isBlank()) {
            throw new ValidationException("El nombre del tipo de cita no puede estar vacio.");
        }
    }
}
